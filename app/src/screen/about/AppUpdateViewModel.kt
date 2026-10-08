package com.amamiyakokoro.box.screen.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amamiyakokoro.box.data.integration.update.GitHubReleaseClient
import com.amamiyakokoro.box.data.integration.update.ReleaseCheck
import com.amamiyakokoro.box.data.store.AppSettingsStore
import com.amamiyakokoro.box.data.model.AppUpdateChannel
import com.amamiyakokoro.box.integration.update.AppUpdateManager
import com.amamiyakokoro.box.integration.update.AppUpdateInstallState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppUpdateState(
    val checking: Boolean = false,
    val result: ReleaseCheck? = null,
    val installationRequested: Boolean = false,
) {
    // A check must not display completion or failure from an earlier installation.
    fun installStateForDialog(state: AppUpdateInstallState): AppUpdateInstallState =
        if (installationRequested) state else AppUpdateInstallState.Idle
}

class AppUpdateViewModel(
    private val client: GitHubReleaseClient,
    private val settings: AppSettingsStore,
    private val updateManager: AppUpdateManager,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AppUpdateState())
    val state = mutableState.asStateFlow()
    val installState = updateManager.state
    private var checkJob: Job? = null
    private var checkGeneration = 0

    fun check() {
        if (mutableState.value.checking) return
        check(settings.appUpdateChannel.value)
    }

    fun check(channel: AppUpdateChannel) {
        val generation = ++checkGeneration
        checkJob?.cancel()
        updateManager.dismiss()
        mutableState.value = AppUpdateState(checking = true)
        checkJob = viewModelScope.launch {
            try {
                val result = client.check(channel, forceRefresh = true)
                if (generation == checkGeneration) mutableState.value = AppUpdateState(result = result)
            } catch (error: CancellationException) {
                throw error
            } finally {
                if (generation == checkGeneration) {
                    mutableState.value = mutableState.value.copy(checking = false)
                }
            }
        }
    }

    fun dismiss() {
        checkGeneration++
        checkJob?.cancel()
        checkJob = null
        updateManager.dismiss()
        mutableState.value = AppUpdateState()
    }

    fun downloadAndInstall(release: ReleaseCheck.Published) {
        if (mutableState.value.checking || mutableState.value.result != release) return
        if (updateManager.downloadAndPrepare(release)) {
            mutableState.value = mutableState.value.copy(installationRequested = true)
        }
    }

    fun continueInstall() = updateManager.installPreparedUpdate()
}
