package com.amamiyakokoro.box.screen.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amamiyakokoro.box.data.integration.update.GitHubReleaseClient
import com.amamiyakokoro.box.data.integration.update.ReleaseCheck
import com.amamiyakokoro.box.data.store.AppSettingsStore
import com.amamiyakokoro.box.integration.update.AppUpdateManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppUpdateState(val checking: Boolean = false, val result: ReleaseCheck? = null)

class AppUpdateViewModel(
    private val client: GitHubReleaseClient,
    private val settings: AppSettingsStore,
    private val updateManager: AppUpdateManager,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AppUpdateState())
    val state = mutableState.asStateFlow()
    val installState = updateManager.state

    fun check() {
        if (mutableState.value.checking) return
        mutableState.value = AppUpdateState(checking = true)
        viewModelScope.launch {
            try {
                mutableState.value = AppUpdateState(result = client.check(settings.appUpdateChannel.value))
            } catch (error: CancellationException) {
                throw error
            } finally {
                mutableState.value = mutableState.value.copy(checking = false)
            }
        }
    }

    fun dismiss() { mutableState.value = AppUpdateState() }

    fun downloadAndInstall(release: ReleaseCheck.Published) = updateManager.downloadAndPrepare(release)

    fun continueInstall() = updateManager.installPreparedUpdate()
}
