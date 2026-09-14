/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */



package com.amamiyakokoro.box.screen.home

import android.app.Application
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.amamiyakokoro.box.core.presentation.AndroidContractStateViewModel
import com.amamiyakokoro.box.core.presentation.LoadableState
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.locale.UiText
import com.amamiyakokoro.box.core.util.AutoStartSessionGate
import com.amamiyakokoro.box.core.util.PollingTimerSpecs
import com.amamiyakokoro.box.core.util.PollingTimers
import com.amamiyakokoro.box.data.model.ProxyMode
import com.amamiyakokoro.box.data.gateway.IpMonitoringState
import com.amamiyakokoro.box.data.gateway.NetworkInfoService
import com.amamiyakokoro.box.data.store.NetworkSettingsStore
import com.amamiyakokoro.box.data.store.ProxyDisplaySettingsStore
import com.amamiyakokoro.box.domain.model.TrafficData
import com.amamiyakokoro.box.runtime.client.ProfilesRepository
import com.amamiyakokoro.box.runtime.client.ProxyFacade
import com.amamiyakokoro.box.runtime.client.ProxyGroupSyncPriority
import com.amamiyakokoro.box.runtime.client.TrafficPollingPriority
import com.amamiyakokoro.box.runtime.client.RuntimeStateMapper
import com.amamiyakokoro.box.service.root.RootAccessSupport
import com.amamiyakokoro.box.service.runtime.entity.Profile
import com.amamiyakokoro.box.service.runtime.state.RuntimePhase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

enum class HomeProxyControlState {
    Idle,
    Connecting,
    Running,
    Disconnecting;

    val canInteract: Boolean
        get() = this == Idle || this == Running
}

private enum class PendingTransition {
    None,
    AwaitingPermission,
    Starting,
    Stopping,
}

class HomeViewModel(
    application: Application,
    private val proxyFacade: ProxyFacade,
    private val profilesRepository: ProfilesRepository,
    private val networkInfoService: NetworkInfoService,
    private val networkSettingsStore: NetworkSettingsStore,
    private val proxyDisplaySettingsStore: ProxyDisplaySettingsStore,
) : AndroidContractStateViewModel<HomeViewModel.HomeUiState, HomeViewModel.HomeUiEffect>(
    application,
    HomeUiState(),
) {
    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _recommendedProfile = MutableStateFlow<Profile?>(null)
    val recommendedProfile: StateFlow<Profile?> = _recommendedProfile.asStateFlow()

    private val _profilesLoaded = MutableStateFlow(false)
    val profilesLoaded: StateFlow<Boolean> = _profilesLoaded.asStateFlow()

    val hasEnabledProfile: Flow<Boolean> = profiles.map { list ->
        list.any { it.active }
    }

    val runtimeSnapshot = proxyFacade.runtimeSnapshot
    val isRunning = runtimeSnapshot
        .map(RuntimeStateMapper::isActuallyRunning)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RuntimeStateMapper.isActuallyRunning(runtimeSnapshot.value))
    val currentProfile = proxyFacade.currentProfile
    val trafficNow = proxyFacade.trafficNow
    val tunnelMode: StateFlow<com.amamiyakokoro.box.core.model.TunnelState.Mode> = proxyFacade.preferredTunnelMode

    private val _proxyMode = MutableStateFlow(ProxyMode.Tun)
    val proxyMode: StateFlow<ProxyMode> = _proxyMode.asStateFlow()

    private val _pendingTransition = MutableStateFlow(PendingTransition.None)
    private var pendingStartRequest: PendingStartRequest? = null

    private val _vpnPrepareIntent = MutableStateFlow<Intent?>(null)
    val vpnPrepareIntent: StateFlow<Intent?> = _vpnPrepareIntent.asStateFlow()

    val controlState: StateFlow<HomeProxyControlState> = combine(
        runtimeSnapshot,
        _pendingTransition,
    ) { snapshot, pendingTransition ->
        resolveControlState(snapshot.phase, pendingTransition)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        resolveControlState(runtimeSnapshot.value.phase, _pendingTransition.value),
    )

    private val _speedHistory = MutableStateFlow<List<Long>>(emptyList())
    val speedHistory: StateFlow<List<Long>> = _speedHistory.asStateFlow()

    private var reconcileJob: Job? = null
    private var speedSamplingJob: Job? = null
    private val homeScreenActive = MutableStateFlow(false)

    private val mainProxyNode: StateFlow<com.amamiyakokoro.box.core.model.Proxy?> =
        proxyFacade.resolvedPrimaryNode

    val selectedServerName: StateFlow<String?> =
        mainProxyNode.map { it?.name }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedServerPing: StateFlow<Int?> = mainProxyNode.map { node ->
        node?.delay?.takeIf { d -> d > 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val ipMonitoringState: StateFlow<IpMonitoringState> = combine(
        isRunning,
        tunnelMode,
        homeScreenActive,
    ) { running, _, isHomeActive ->
        running to isHomeActive
    }.flatMapLatest { (running, isHomeActive) ->
        when {
            running && isHomeActive -> {
                networkInfoService.startIpMonitoring(
                    isProxyActiveFlow = isRunning,
                    externalRefreshFlow = PollingTimers.ticks(PollingTimerSpecs.HomeIpFallbackRefresh).map { Unit },
                )
            }

            running -> emptyFlow()
            else -> flowOf(IpMonitoringState.Loading)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IpMonitoringState.Loading)

    init {
        refreshProfiles()
        reconcileRuntimeState()
        observeControlState()
        observeRuntimeState()
        observeRuntimeFailures()
        syncProxyModeState()
        observeProfileChanges()
    }

    private fun refreshProfiles() {
        viewModelScope.launch {
            try {
                val allProfiles = profilesRepository.queryAllProfiles()
                val active = profilesRepository.queryActiveProfile()
                _profiles.value = allProfiles
                _recommendedProfile.value = active
                _profilesLoaded.value = true
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to refresh profiles")
                _profilesLoaded.value = true
            }
        }
    }

    private fun observeProfileChanges() {
        viewModelScope.launch {
            proxyFacade.currentProfile
                .map { it?.uuid }
                .distinctUntilChanged()
                .collect {
                    refreshProfiles()
                }
        }
    }

    private fun observeControlState() {
        viewModelScope.launch {
            controlState
                .collect { state ->
                    if (state != HomeProxyControlState.Running) {
                        _speedHistory.value = List(24) { 0L }
                    }
                    _uiState.update {
                        it.copy(
                            isStartingProxy = state == HomeProxyControlState.Connecting,
                            loadingProgressText = if (state == HomeProxyControlState.Connecting) {
                                UiText.Resource(LocaleR.string.home_message_preparing)
                            } else {
                                null
                            },
                        )
                    }
                }
        }
    }

    private fun observeRuntimeState() {
        viewModelScope.launch {
            runtimeSnapshot
                .map { it.phase }
                .distinctUntilChanged()
                .collect { phase ->
                    when (phase) {
                        RuntimePhase.Starting -> {
                            clearPendingStart()
                            if (_pendingTransition.value == PendingTransition.AwaitingPermission ||
                                _pendingTransition.value == PendingTransition.Starting
                            ) {
                                _pendingTransition.value = PendingTransition.None
                            }
                        }

                        RuntimePhase.Running -> {
                            clearPendingStart()
                            if (_pendingTransition.value == PendingTransition.Starting ||
                                _pendingTransition.value == PendingTransition.AwaitingPermission
                            ) {
                                _pendingTransition.value = PendingTransition.None
                            }
                        }

                        RuntimePhase.Stopping -> {
                            clearPendingStart()
                            if (_pendingTransition.value == PendingTransition.Stopping) {
                                _pendingTransition.value = PendingTransition.None
                            }
                        }

                        RuntimePhase.Idle,
                        RuntimePhase.Failed -> {
                            clearPendingStart()
                            _pendingTransition.value = PendingTransition.None
                        }
                    }
                }
        }
    }

    private fun syncProxyModeState() {
        viewModelScope.launch {
            runtimeSnapshot
                .map { RuntimeStateMapper.resolveDisplayMode(it, networkSettingsStore.proxyMode.value) }
                .distinctUntilChanged()
                .collect {
                refreshProxyMode()
                }
        }
    }

    private fun observeRuntimeFailures() {
        viewModelScope.launch {
            runtimeSnapshot
                .drop(1)
                .map { snapshot -> Triple(snapshot.phase, snapshot.lastError, snapshot.generation) }
                .distinctUntilChanged()
                .collect { (phase, lastError, _) ->
                    if (phase == RuntimePhase.Failed && !lastError.isNullOrBlank()) {
                        showError(UiText.Dynamic(lastError))
                    }
                }
        }
    }

    fun refreshProxyMode() {
        val configuredMode = networkSettingsStore.proxyMode.value
        _proxyMode.value = RuntimeStateMapper.resolveDisplayMode(runtimeSnapshot.value, configuredMode)
    }

    fun setHomeScreenActive(isActive: Boolean) {
        homeScreenActive.value = isActive
        proxyFacade.setProxyGroupSyncPriority(
            priority = if (isActive) ProxyGroupSyncPriority.FAST else ProxyGroupSyncPriority.OFF,
            source = "home",
        )
        proxyFacade.setTrafficPollingPriority(
            priority = if (isActive) TrafficPollingPriority.FAST else TrafficPollingPriority.OFF,
            source = "home",
        )
        if (isActive) {
            startSpeedSampling()
        } else {
            stopSpeedSampling()
        }
    }

    fun reconcileRuntimeState() {
        if (reconcileJob?.isActive == true) return
        reconcileJob = viewModelScope.launch {
            runCatching {
                proxyFacade.reconcileRuntimeState()
                refreshProfiles()
                refreshProxyMode()
            }.onFailure { error ->
                if (error is CancellationException) throw error
                Timber.w(error, "Failed to reconcile runtime state for home")
            }
        }
    }

    suspend fun reloadProfile() {
        try {
            applyLoading(true)

            val activeProfile = profilesRepository.queryActiveProfile()
            if (activeProfile == null) {
                showError(
                    UiText.Resource(
                        LocaleR.string.home_message_config_switch_failed,
                        listOf(UiText.Resource(LocaleR.string.profiles_vm_error_profile_not_exist)),
                    ),
                )
                return
            }

            profilesRepository.updateProfile(activeProfile.uuid)

            profilesRepository.setActiveProfile(activeProfile.uuid)
            showMessage(UiText.Resource(LocaleR.string.home_message_config_switched))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to reload profile")
            showError(UiText.Resource(LocaleR.string.home_message_config_switch_failed, listOf(e.message.orEmpty())))
        } finally {
            applyLoading(false)
        }
    }

    fun isCurrentProfile(profileId: java.util.UUID): Boolean {
        return currentProfile.value?.uuid == profileId
    }

    fun startProxy(profileId: String, mode: ProxyMode? = null) {
        val currentControlState = controlState.value
        if (currentControlState != HomeProxyControlState.Idle) {
            showMessage(
                if (_pendingTransition.value == PendingTransition.AwaitingPermission) {
                    UiText.Resource(LocaleR.string.home_message_waiting_for_vpn_permission)
                } else {
                    UiText.Resource(LocaleR.string.home_message_control_busy, listOf(currentControlState.label))
                },
            )
            return
        }

        val request = PendingStartRequest(
            profileId = profileId,
            mode = mode ?: networkSettingsStore.proxyMode.value,
        )
        pendingStartRequest = request
        _pendingTransition.value = PendingTransition.Starting

        viewModelScope.launch {
            startProxyInternal(request)
        }
    }

    fun startCurrentOrRecommendedProxy() {
        if (!profilesLoaded.value) {
            showMessage(UiText.Resource(LocaleR.string.home_control_hint_profiles_loading))
            return
        }

        val targetProfile = recommendedProfile.value
        when {
            profiles.value.isEmpty() -> {
                showMessage(UiText.Resource(LocaleR.string.home_control_hint_add_profile))
                return
            }

            targetProfile == null || profiles.value.none { profile ->
                profile.uuid == targetProfile.uuid && profile.active
            } -> {
                showMessage(UiText.Resource(LocaleR.string.home_control_hint_enable_profile))
                return
            }
        }

        startProxy(profileId = targetProfile.uuid.toString())
    }

    fun onVpnPermissionResult(granted: Boolean) {
        val request = pendingStartRequest ?: return
        if (_pendingTransition.value != PendingTransition.AwaitingPermission) return
        _vpnPrepareIntent.value = null

        if (!granted) {
            clearPendingStart()
            _pendingTransition.value = PendingTransition.None
            refreshProxyMode()
            return
        }

        _pendingTransition.value = PendingTransition.Starting
        viewModelScope.launch {
            startProxyInternal(request)
        }
    }

    fun onVpnPermissionLaunchStarted(intent: Intent) {
        if (_vpnPrepareIntent.value == intent) {
            _vpnPrepareIntent.value = null
        }
    }

    fun onVpnPermissionLaunchFailed(error: Throwable) {
        _vpnPrepareIntent.value = null
        clearPendingStart()
        _pendingTransition.value = PendingTransition.None
        Timber.e(error, "Failed to launch VPN permission request")
        showError(
            UiText.Resource(
                LocaleR.string.home_message_start_failed,
                listOf(error.message ?: "VPN permission request failed"),
            ),
        )
    }

    suspend fun stopProxy() {
        if (!controlState.value.canInteract || controlState.value != HomeProxyControlState.Running) return

        _pendingTransition.value = PendingTransition.Stopping

        try {
            withContext(Dispatchers.IO) {
                AutoStartSessionGate.markManualPaused()
                proxyFacade.stopProxy()
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _pendingTransition.value = PendingTransition.None
            Timber.e(e, "Failed to stop proxy")
            showError(UiText.Resource(LocaleR.string.home_message_stop_failed, listOf(e.message.orEmpty())))
        }
    }

    private fun startSpeedSampling(sampleLimit: Int = 24) {
        if (speedSamplingJob?.isActive == true) return
        speedSamplingJob = viewModelScope.launch {
            PollingTimers.ticks(PollingTimerSpecs.HomeSpeedSampling).collect {
                val snapshot = runtimeSnapshot.value
                val sample = when {
                    snapshot.phase == RuntimePhase.Idle || snapshot.phase == RuntimePhase.Failed -> 0L
                    snapshot.phase.running -> {
                        val t = proxyFacade.trafficNow.value
                        val d = TrafficData.from(t)
                        (d.upload + d.download).coerceAtLeast(0L)
                    }

                    else -> 0L
                }
                _speedHistory.update { old ->
                    buildList(sampleLimit) {
                        repeat((sampleLimit - old.size - 1).coerceAtLeast(0)) { add(0L) }
                        addAll(old.takeLast(sampleLimit - 1))
                        add(sample)
                    }
                }
            }
        }
    }

    private fun stopSpeedSampling() {
        speedSamplingJob?.cancel()
        speedSamplingJob = null
    }

    private fun applyLoading(loading: Boolean) = super.setLoading(loading)
    private fun showMessage(message: UiText) {
        updateState { it.copy(messageText = message, message = null) }
        tryEmitEffect(HomeUiEffect.ShowMessage(message))
    }

    private fun showError(error: UiText) {
        updateState { it.copy(errorText = error, error = null, isLoading = false) }
        tryEmitEffect(HomeUiEffect.ShowError(error))
    }

    fun consumeMessage() = updateState { it.copy(message = null, messageText = null) }
    fun consumeError() = updateState { it.copy(error = null, errorText = null) }

    private suspend fun startProxyInternal(request: PendingStartRequest) {
        val startedAt = System.currentTimeMillis()
        try {
            _proxyMode.value = request.mode
            Timber.d("Home startProxy kickoff: mode=${request.mode} profileId=${request.profileId}")

            if (request.mode == ProxyMode.RootTun) {
                val rootStatus = RootAccessSupport.evaluateAsync(getApplication())
                if (!rootStatus.canStartRootTun) {
                    clearPendingStart()
                    _pendingTransition.value = PendingTransition.None
                    showError(UiText.Dynamic(rootStatus.rootTunBlockedMessage(getApplication())))
                    return
                }
            }

            withContext(Dispatchers.IO) {
                if (request.profileId.isNotBlank()) {
                    profilesRepository.setActiveProfile(java.util.UUID.fromString(request.profileId))
                }

                AutoStartSessionGate.clearManualPaused()
                proxyFacade.startProxy(request.mode)
            }

            Timber.i("Home startProxy completed in ${System.currentTimeMillis() - startedAt}ms, mode=${request.mode}")
        } catch (e: com.amamiyakokoro.box.remote.VpnPermissionRequired) {
            _pendingTransition.value = PendingTransition.AwaitingPermission
            _vpnPrepareIntent.value = e.intent
            Timber.i("VPN permission required")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            clearPendingStart()
            _pendingTransition.value = PendingTransition.None
            Timber.e(e, "Failed to start proxy")
            showError(UiText.Resource(LocaleR.string.home_message_start_failed, listOf(e.message.orEmpty())))
        }
    }

    private fun clearPendingStart() {
        pendingStartRequest = null
    }

    private val HomeProxyControlState.label: UiText
        get() = when (this) {
            HomeProxyControlState.Idle -> UiText.Resource(LocaleR.string.home_status_tap_to_start)
            HomeProxyControlState.Connecting -> UiText.Resource(LocaleR.string.home_status_connecting)
            HomeProxyControlState.Running -> UiText.Resource(LocaleR.string.home_status_running)
            HomeProxyControlState.Disconnecting -> UiText.Resource(LocaleR.string.home_status_disconnecting)
        }

    private fun resolveControlState(
        phase: RuntimePhase,
        pendingTransition: PendingTransition,
    ): HomeProxyControlState {
        if (pendingTransition == PendingTransition.Stopping &&
            phase != RuntimePhase.Stopping &&
            phase != RuntimePhase.Idle &&
            phase != RuntimePhase.Failed
        ) {
            return HomeProxyControlState.Disconnecting
        }
        return when (phase) {
            RuntimePhase.Running -> HomeProxyControlState.Running
            RuntimePhase.Starting -> HomeProxyControlState.Connecting
            RuntimePhase.Stopping -> HomeProxyControlState.Disconnecting
            RuntimePhase.Idle,
            RuntimePhase.Failed -> when (pendingTransition) {
                PendingTransition.AwaitingPermission,
                PendingTransition.Starting -> HomeProxyControlState.Connecting
                PendingTransition.Stopping -> HomeProxyControlState.Idle
                PendingTransition.None -> HomeProxyControlState.Idle
            }
        }
    }

    private data class PendingStartRequest(
        val profileId: String,
        val mode: ProxyMode,
    )

    data class HomeUiState(
        override val isLoading: Boolean = false,
        val isStartingProxy: Boolean = false,
        val loadingProgressText: UiText? = null,
        override val message: String? = null,
        override val error: String? = null,
        val messageText: UiText? = null,
        val errorText: UiText? = null,
    ) : LoadableState<HomeUiState> {
        override fun withLoading(loading: Boolean): HomeUiState = copy(isLoading = loading)
        override fun withError(error: String?): HomeUiState = copy(error = error)
        override fun withMessage(message: String?): HomeUiState = copy(message = message)
    }

    sealed interface HomeUiEffect {
        data class ShowMessage(val message: UiText) : HomeUiEffect
        data class ShowError(val message: UiText) : HomeUiEffect
    }
}
