package com.github.yumelira.yumebox.data.integration.update

import com.github.yumelira.yumebox.data.store.AppSettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AutomaticAppUpdateChecker(
    private val client: GitHubReleaseClient,
    private val settings: AppSettingsStore,
    private val applicationScope: CoroutineScope,
    private val currentVersionName: String,
    private val currentVersionCode: Int,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val checkMutex = Mutex()
    private val mutableAvailableUpdate = MutableStateFlow<ReleaseCheck.Published?>(null)
    private var checkedChannel = settings.appUpdateChannel.value
    val availableUpdate = mutableAvailableUpdate.asStateFlow()

    fun checkIfDue() {
        if (!settings.automaticUpdateCheckEnabled.value) {
            dismiss()
            return
        }
        applicationScope.launch {
            checkMutex.withLock {
                val channel = settings.appUpdateChannel.value
                if (channel != checkedChannel) {
                    checkedChannel = channel
                    mutableAvailableUpdate.value = null
                }
                if (!settings.initialSetupCompleted.value ||
                    !settings.automaticUpdateCheckEnabled.value
                ) return@withLock

                val now = nowMillis()
                if (!AutomaticUpdateCheckPolicy.isDue(
                        lastCheckAtMillis = settings.lastAutomaticUpdateCheckAtMillis,
                        nowMillis = now,
                    )
                ) return@withLock

                // Record the attempt before starting I/O so rapid foreground transitions and
                // process restarts cannot repeatedly hit the unauthenticated GitHub API.
                settings.lastAutomaticUpdateCheckAtMillis = now
                val release = try {
                    client.check(channel) as? ReleaseCheck.Published
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    return@withLock
                }
                if (settings.automaticUpdateCheckEnabled.value &&
                    settings.appUpdateChannel.value == channel &&
                    release != null &&
                    release.isNewerThan(currentVersionName, currentVersionCode)
                ) {
                    mutableAvailableUpdate.value = release
                }
            }
        }
    }

    fun onEnabledChanged(enabled: Boolean) {
        if (enabled) checkIfDue() else dismiss()
    }

    fun dismiss() {
        mutableAvailableUpdate.value = null
    }
}
