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



package com.amamiyakokoro.box.screen.profiles

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.amamiyakokoro.box.core.model.FetchStatus
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.locale.UiText
import com.amamiyakokoro.box.core.presentation.AndroidContractStateViewModel
import com.amamiyakokoro.box.core.presentation.LoadableState
import com.amamiyakokoro.box.data.store.LinkOpenMode
import com.amamiyakokoro.box.data.integration.kokoro.KokoroRepository
import com.amamiyakokoro.box.data.store.Preference
import com.amamiyakokoro.box.data.store.ProfileLink
import com.amamiyakokoro.box.data.store.ProfileLinksStore
import com.amamiyakokoro.box.runtime.client.ProfilesRepository
import com.amamiyakokoro.box.service.remote.IFetchObserver
import com.amamiyakokoro.box.service.runtime.entity.Profile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.*

class ProfilesViewModel(
    application: Application,
    private val profilesRepository: ProfilesRepository,
    profileLinksStorage: ProfileLinksStore,
    private val kokoroRepository: KokoroRepository,
) : AndroidContractStateViewModel<ProfilesUiState, ProfilesViewModel.ProfilesUiEffect>(
    application,
    ProfilesUiState(),
) {

    val linkOpenMode: Preference<LinkOpenMode> = profileLinksStorage.linkOpenMode
    val links: Preference<List<ProfileLink>> = profileLinksStorage.links
    val defaultLinkId: Preference<String> = profileLinksStorage.defaultLinkId

    fun setOpenMode(mode: LinkOpenMode) = linkOpenMode.set(mode)

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _activeProfile = MutableStateFlow<Profile?>(null)
    val activeProfile: StateFlow<Profile?> = _activeProfile.asStateFlow()

    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()

    private val _updatingProfileIds = MutableStateFlow<Set<UUID>>(emptySet())
    val updatingProfileIds: StateFlow<Set<UUID>> = _updatingProfileIds.asStateFlow()

    private val _kokoroAuthState = MutableStateFlow<KokoroAuthState>(KokoroAuthState.Checking)
    internal val kokoroAuthState: StateFlow<KokoroAuthState> = _kokoroAuthState.asStateFlow()

    private val _kokoroSubscriptionOptions = MutableStateFlow(KokoroSubscriptionOptions.fallback())
    internal val kokoroSubscriptionOptions: StateFlow<KokoroSubscriptionOptions> =
        _kokoroSubscriptionOptions.asStateFlow()

    private val updateJobs = mutableMapOf<UUID, Job>()
    private val canceledProfileUpdateIds = mutableSetOf<UUID>()

    init {
        refreshProfiles()
        refreshKokoroAccount()
    }

    internal fun refreshKokoroAccount(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _kokoroAuthState.value = KokoroAuthState.Checking
            _kokoroAuthState.value = try {
                val account = kokoroRepository.getAccount(forceRefresh)
                if (account == null) {
                    _kokoroSubscriptionOptions.value = KokoroSubscriptionOptions.fallback()
                    KokoroAuthState.LoggedOut
                } else {
                    _kokoroSubscriptionOptions.value = runCatching {
                        kokoroRepository.getSubscriptionOptions(account, forceRefresh)
                    }.getOrElse { error ->
                        Timber.w(
                            "Unable to load Kokoro subscription options (%s); using account fallback",
                            error::class.java.simpleName,
                        )
                        KokoroSubscriptionOptions.fallback(account)
                    }
                    KokoroAuthState.Authenticated(account)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.w("Failed to refresh amamiyakoko.ro account (%s)", e::class.java.simpleName)
                KokoroAuthState.Error(
                    UiText.Resource(LocaleR.string.profiles_page_kokoro_check_failed_detail),
                )
            }
        }
    }

    internal suspend fun beginKokoroLogin(): String = kokoroRepository.beginLogin()

    internal suspend fun cancelKokoroLogin(loginUrl: String) = kokoroRepository.cancelLogin(loginUrl)

    internal suspend fun resolveKokoroSubscription(
        settings: MihomoSubscriptionSettings,
    ): ResolvedSubscription = kokoroRepository.resolveSubscription(settings)

    internal fun reportKokoroLoginFailure() {
        _kokoroAuthState.value = KokoroAuthState.Error(
            UiText.Resource(LocaleR.string.profiles_page_kokoro_login_failed),
        )
    }

    internal fun logoutKokoroAccount() {
        viewModelScope.launch {
            _kokoroAuthState.value = KokoroAuthState.Checking
            try {
                kokoroRepository.revoke()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.w("Failed to revoke amamiyakoko.ro session (%s)", e::class.java.simpleName)
            } finally {
                _kokoroSubscriptionOptions.value = KokoroSubscriptionOptions.fallback()
                _kokoroAuthState.value = KokoroAuthState.LoggedOut
            }
        }
    }

    fun refreshProfiles() {
        viewModelScope.launch {
            try {
                applyLoading(true)
                val allProfiles = normalizeKokoroSubscriptionUserAgents(
                    profilesRepository.queryAllProfiles(),
                )
                val active = profilesRepository.queryActiveProfile()

                _profiles.value = allProfiles
                _activeProfile.value = active
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to refresh profiles")
                showError(UiText.Resource(LocaleR.string.profiles_vm_message_update_failed, listOf(e.message.orEmpty())))
            } finally {
                applyLoading(false)
            }
        }
    }

    fun createProfile(
        type: Profile.Type,
        name: String,
        source: String = "",
        interval: Long = 0L,
        fileUri: Uri? = null,
        userAgent: String = "",
    ) {
        viewModelScope.launch {
            var createdUuid: UUID? = null
            try {
                applyLoading(true)
                val effectiveUserAgent = resolveProfileUserAgent(type, source, userAgent)
                val uuid = profilesRepository.createProfile(type, name, source, effectiveUserAgent)
                createdUuid = uuid

                _downloadProgress.value = DownloadProgress(
                    percent = 0,
                    message = UiText.Resource(LocaleR.string.profiles_vm_progress_preparing),
                )

                val observer = IFetchObserver { status ->
                    _downloadProgress.value = status.toDownloadProgress()
                }

                if (type == Profile.Type.File && fileUri != null) {
                    copyFileToImportedDir(fileUri, uuid)
                }

                profilesRepository.updateProfile(uuid, observer)
                val activateNewKokoroSubscription =
                    type == Profile.Type.Url && KokoroApi.isManagedConfigUrl(source)
                if (activateNewKokoroSubscription) {
                    profilesRepository.setActiveProfile(uuid)
                    Timber.i("New Kokoro subscription activated: $uuid")
                }
                _downloadProgress.value = DownloadProgress(
                    percent = 100,
                    message = UiText.Resource(LocaleR.string.profiles_vm_progress_import_complete),
                    isCompleted = true,
                )

                showMessage(
                    if (activateNewKokoroSubscription) {
                        UiText.Resource(LocaleR.string.profiles_vm_message_profile_added_and_activated, listOf(name))
                    } else {
                        UiText.Resource(LocaleR.string.profiles_vm_message_profile_added, listOf(name))
                    },
                )
                refreshProfiles()
                Timber.i("Profile created: $uuid")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to create profile")
                createdUuid?.let { uuid ->
                    runCatching { profilesRepository.deleteProfile(uuid) }
                        .onFailure { deleteError ->
                            Timber.w(deleteError, "Failed to rollback profile creation: $uuid")
                        }
                }
                refreshProfiles()
                showError(UiText.Resource(LocaleR.string.profiles_vm_message_add_failed, listOf(e.message.orEmpty())))
                _downloadProgress.value = null
            } finally {
                applyLoading(false)
            }
        }
    }

    private suspend fun copyFileToImportedDir(uri: Uri, uuid: UUID) {
        withContext(Dispatchers.IO) {
            val context = getApplication<Application>()
            val importedDir = File(context.filesDir, "imported/${uuid}")
            importedDir.mkdirs()

            val outputFile = File(importedDir, "config.yaml")
            context.contentResolver.openInputStream(uri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
                ?: throw IllegalArgumentException("Failed to open file: $uri")
            Timber.d("File copied: ${outputFile.absolutePath}")
        }
    }

    fun cloneProfile(uuid: UUID) {
        viewModelScope.launch {
            try {
                applyLoading(true)
                val newUuid = profilesRepository.cloneProfile(uuid)
                showMessage(UiText.Resource(LocaleR.string.profiles_vm_message_profile_added, listOf("Clone")))
                refreshProfiles()
                Timber.i("Profile cloned: from=$uuid to=$newUuid")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to clone profile")
                showError(UiText.Resource(LocaleR.string.profiles_vm_message_add_failed, listOf(e.message.orEmpty())))
            } finally {
                applyLoading(false)
            }
        }
    }

    fun deleteProfile(uuid: UUID) {
        viewModelScope.launch {
            try {
                applyLoading(true)
                profilesRepository.deleteProfile(uuid)
                showMessage(UiText.Resource(LocaleR.string.profiles_vm_message_profile_deleted))
                refreshProfiles()
                Timber.i("Profile deleted: $uuid")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to delete profile")
                showError(UiText.Resource(LocaleR.string.profiles_vm_message_delete_failed, listOf(e.message.orEmpty())))
            } finally {
                applyLoading(false)
            }
        }
    }

    fun activateProfile(uuid: UUID) {
        viewModelScope.launch {
            try {
                applyLoading(true)
                profilesRepository.setActiveProfile(uuid)
                showMessage(UiText.Resource(LocaleR.string.profiles_vm_message_profile_updated, listOf("Active")))
                refreshProfiles()
                Timber.i("Profile activated: $uuid")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to activate profile")
                showError(UiText.Resource(LocaleR.string.profiles_vm_message_toggle_failed, listOf(e.message.orEmpty())))
            } finally {
                applyLoading(false)
            }
        }
    }

    fun updateProfile(uuid: UUID) {
        if (uuid in _updatingProfileIds.value) return
        val updateJob = viewModelScope.launch(start = CoroutineStart.LAZY) {
            try {
                applyLoading(true)
                _downloadProgress.value = DownloadProgress(
                    percent = 0,
                    message = UiText.Resource(LocaleR.string.profiles_vm_progress_preparing),
                )

                val observer = IFetchObserver { status ->
                    _downloadProgress.value = status.toDownloadProgress()
                }

                profilesRepository.updateProfile(uuid, observer)
                if (!profileConfigExists(uuid)) {
                    error("Updated configuration file is missing")
                }

                if (uuid !in canceledProfileUpdateIds) {
                    _downloadProgress.value = DownloadProgress(
                        percent = 100,
                        message = UiText.Resource(LocaleR.string.profiles_vm_progress_import_complete),
                        isCompleted = true,
                    )
                    showMessage(UiText.Resource(LocaleR.string.profiles_vm_message_profile_updated, listOf(uuid.toString())))
                    refreshProfiles()
                    Timber.i("Profile updated: $uuid")
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Timber.d("Profile update cancelled: $uuid")
                    throw e
                } else {
                    Timber.e(e, "Failed to update profile")
                    showError(UiText.Resource(LocaleR.string.profiles_vm_message_update_failed, listOf(e.message.orEmpty())))
                    _downloadProgress.value = null
                }
            } finally {
                if (canceledProfileUpdateIds.remove(uuid)) {
                    _downloadProgress.value = null
                }
                updateJobs.remove(uuid)
                _updatingProfileIds.update { it - uuid }
                applyLoading(false)
            }
        }
        updateJobs[uuid] = updateJob
        _updatingProfileIds.update { it + uuid }
        updateJob.start()
    }

    fun cancelProfileUpdateAndRestore(uuid: UUID) {
        if (uuid !in _updatingProfileIds.value) return
        canceledProfileUpdateIds.add(uuid)
        updateJobs[uuid]?.cancel()
        // Keep the update marked busy until service-side staging has been cleaned up.
    }

    fun patchProfile(uuid: UUID, name: String, source: String, interval: Long, userAgent: String) {
        viewModelScope.launch {
            try {
                applyLoading(true)
                profilesRepository.patchProfile(
                    uuid,
                    name,
                    source,
                    interval,
                    resolveProfileUserAgent(Profile.Type.Url, source, userAgent),
                )
                showMessage(UiText.Resource(LocaleR.string.profiles_vm_message_profile_updated, listOf(name)))
                refreshProfiles()
                Timber.i("Profile patched: $uuid")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to patch profile")
                showError(UiText.Resource(LocaleR.string.profiles_vm_message_update_failed, listOf(e.message.orEmpty())))
            } finally {
                applyLoading(false)
            }
        }
    }

    fun patchAndUpdateProfile(
        uuid: UUID,
        name: String,
        source: String,
        interval: Long,
        userAgent: String,
    ) {
        if (uuid in _updatingProfileIds.value) return
        viewModelScope.launch {
            var patched = false
            try {
                applyLoading(true)
                profilesRepository.patchProfile(
                    uuid,
                    name,
                    source,
                    interval,
                    resolveProfileUserAgent(Profile.Type.Url, source, userAgent),
                )
                refreshProfiles()
                patched = true
                Timber.i("Kokoro profile settings patched: $uuid")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to patch Kokoro profile settings")
                showError(UiText.Resource(LocaleR.string.profiles_vm_message_update_failed, listOf(e.message.orEmpty())))
            } finally {
                applyLoading(false)
            }
            if (patched) updateProfile(uuid)
        }
    }

    fun importProfileFromFile(uri: Uri, name: String) {
        createProfile(
            type = Profile.Type.File,
            name = name,
            fileUri = uri
        )
    }

    private suspend fun normalizeKokoroSubscriptionUserAgents(
        profiles: List<Profile>,
    ): List<Profile> = profiles.map { profile ->
        val expectedUserAgent = resolveProfileUserAgent(profile.type, profile.source, profile.userAgent)
        if (expectedUserAgent == profile.userAgent) {
            profile
        } else {
            try {
                profilesRepository.patchProfile(
                    profile.uuid,
                    profile.name,
                    profile.source,
                    profile.interval,
                    expectedUserAgent,
                )
                profile.copy(userAgent = expectedUserAgent)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.w(e, "Unable to normalize Kokoro subscription User-Agent")
                profile
            }
        }
    }

    private fun resolveProfileUserAgent(
        type: Profile.Type,
        source: String,
        requestedUserAgent: String,
    ): String = if (type == Profile.Type.Url && KokoroApi.isManagedConfigUrl(source)) {
        KokoroApi.subscriptionUserAgent
    } else {
        requestedUserAgent
    }

    fun reorderProfiles(from: Int, to: Int) {
        viewModelScope.launch {
            try {
                val current = _profiles.value
                if (from !in current.indices || to !in current.indices || from == to) return@launch

                val reordered = current.toMutableList()
                val moved = reordered.removeAt(from)
                reordered.add(to, moved)

                _profiles.value = reordered
                profilesRepository.reorderProfiles(reordered.map { it.uuid })
                Timber.d("Profiles reordered: $from->$to")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to reorder profiles")
                refreshProfiles()
            }
        }
    }

    fun toggleProfileEnabled(uuid: UUID) {
        viewModelScope.launch {
            try {
                val profile = profilesRepository.queryProfileByUUID(uuid)
                    ?: error("Profile not found: $uuid")

                if (profile.active) {
                    cancelProfileUpdateAndRestore(uuid)
                    profilesRepository.clearActiveProfile(profile)
                    showMessage(UiText.Resource(LocaleR.string.profiles_vm_message_profile_updated, listOf(profile.name)))
                } else {
                    profilesRepository.setActiveProfile(uuid)
                    showMessage(UiText.Resource(LocaleR.string.profiles_vm_message_profile_updated, listOf(profile.name)))
                }
                refreshProfiles()
                Timber.d("Profile toggled: $uuid, active=${!profile.active}")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to toggle profile")
                showError(UiText.Resource(LocaleR.string.profiles_vm_message_toggle_failed, listOf(e.message.orEmpty())))
            }
        }
    }

    fun clearDownloadProgress() {
        _downloadProgress.value = null
    }

    fun clearError() {
        updateState { it.copy(error = null, errorText = null) }
    }

    fun clearMessage() {
        updateState { it.copy(message = null, messageText = null) }
    }

    private fun applyLoading(loading: Boolean) {
        super.setLoading(loading)
    }

    private suspend fun profileConfigExists(uuid: UUID): Boolean = withContext(Dispatchers.IO) {
        profileConfigFile(uuid).exists()
    }

    private fun profileConfigFile(uuid: UUID): File {
        return File(getApplication<Application>().filesDir, "imported/${uuid}/config.yaml")
    }

    private fun showError(message: UiText) {
        updateState { it.copy(errorText = message, error = null, isLoading = false) }
        tryEmitEffect(ProfilesUiEffect.ShowError(message))
    }

    private fun showMessage(message: UiText) {
        updateState { it.copy(messageText = message, message = null) }
        tryEmitEffect(ProfilesUiEffect.ShowMessage(message))
    }

    sealed interface ProfilesUiEffect {
        data class ShowMessage(val message: UiText) : ProfilesUiEffect
        data class ShowError(val message: UiText) : ProfilesUiEffect
    }
}

data class ProfilesUiState(
    override val isLoading: Boolean = false,
    override val error: String? = null,
    override val message: String? = null,
    val errorText: UiText? = null,
    val messageText: UiText? = null,
) : LoadableState<ProfilesUiState> {
    override fun withLoading(loading: Boolean): ProfilesUiState = copy(isLoading = loading)
    override fun withError(error: String?): ProfilesUiState = copy(error = error)
    override fun withMessage(message: String?): ProfilesUiState = copy(message = message)
}

data class DownloadProgress(
    val percent: Int?,
    val message: UiText,
    val isCompleted: Boolean = false,
)

private fun FetchStatus.toDownloadProgress(): DownloadProgress {
    val percent = if (max > 0) ((progress * 100) / max).coerceIn(0, 100) else null
    val detail = args.firstOrNull().orEmpty().trim()

    val message: UiText = when (action) {
        FetchStatus.Action.FetchConfiguration -> {
            if (percent == null || percent <= 5) {
                UiText.Resource(LocaleR.string.profiles_vm_progress_preparing)
            } else {
                if (detail.isBlank()) {
                    UiText.Resource(LocaleR.string.profiles_page_progress_downloading)
                } else {
                    UiText.Dynamic(detail)
                }
            }
        }

        FetchStatus.Action.FetchProviders -> {
            UiText.Dynamic(detail)
        }

        FetchStatus.Action.Verifying -> {
            if (detail.isBlank()) {
                UiText.Resource(LocaleR.string.profiles_vm_progress_verifying)
            } else {
                UiText.Dynamic(detail)
            }
        }
    }

    return DownloadProgress(
        percent = percent,
        message = message,
    )
}
