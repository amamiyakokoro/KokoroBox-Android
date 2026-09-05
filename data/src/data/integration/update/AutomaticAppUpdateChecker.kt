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
    currentVersionName: String,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val currentVersion = ReleaseVersion.parse(currentVersionName)
    private val checkMutex = Mutex()
    private val mutableAvailableUpdate = MutableStateFlow<ReleaseCheck.Published?>(null)
    val availableUpdate = mutableAvailableUpdate.asStateFlow()

    fun checkIfDue() {
        if (!settings.automaticUpdateCheckEnabled.value) {
            dismiss()
            return
        }
        applicationScope.launch {
            checkMutex.withLock {
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
                    client.check() as? ReleaseCheck.Published
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    return@withLock
                }
                if (settings.automaticUpdateCheckEnabled.value &&
                    currentVersion != null &&
                    release != null &&
                    release.version > currentVersion
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
