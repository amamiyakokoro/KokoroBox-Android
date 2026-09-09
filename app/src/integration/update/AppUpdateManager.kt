package com.github.yumelira.yumebox.integration.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import com.github.yumelira.yumebox.common.update.ApkUpdateVerifier
import com.github.yumelira.yumebox.common.update.PackageUpdateInstaller
import com.github.yumelira.yumebox.common.update.VerifiedUpdateApk
import com.github.yumelira.yumebox.data.integration.update.AppUpdateDownloader
import com.github.yumelira.yumebox.data.integration.update.ReleaseCheck
import com.github.yumelira.yumebox.data.model.AppUpdateInstallMethod
import com.github.yumelira.yumebox.data.store.AppSettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppUpdateInstallState {
    data object Idle : AppUpdateInstallState

    data class Downloading(
        val release: ReleaseCheck.Published,
        val downloadedBytes: Long,
        val totalBytes: Long,
    ) : AppUpdateInstallState

    data class Verifying(val release: ReleaseCheck.Published) : AppUpdateInstallState

    data class ReadyToInstall(val release: ReleaseCheck.Published) : AppUpdateInstallState

    data class InstallPermissionRequired(val release: ReleaseCheck.Published) : AppUpdateInstallState

    data class Installing(val release: ReleaseCheck.Published) : AppUpdateInstallState
    data class WaitingForUserConfirmation(val release: ReleaseCheck.Published) : AppUpdateInstallState
    data object Installed : AppUpdateInstallState
    data class Failed(val message: String) : AppUpdateInstallState
}

/** Coordinates the update transport, verification, and Android's system installation confirmation. */
class AppUpdateManager(
    private val context: Context,
    private val downloader: AppUpdateDownloader,
    private val verifier: ApkUpdateVerifier,
    private val installer: PackageUpdateInstaller,
    private val shizukuInstaller: ShizukuUpdateInstaller,
    private val rootInstaller: RootUpdateInstaller,
    private val settings: AppSettingsStore,
    private val foregroundTracker: AppForegroundTracker,
    private val installNotifier: AppUpdateInstallNotifier,
    private val applicationScope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow<AppUpdateInstallState>(AppUpdateInstallState.Idle)
    val state = mutableState.asStateFlow()

    private var verifiedUpdate: VerifiedUpdateApk? = null

    fun downloadAndPrepare(release: ReleaseCheck.Published) {
        if (state.value.isBusy()) return
        verifiedUpdate = null
        mutableState.value = AppUpdateInstallState.Downloading(release, 0, release.apkSizeBytes ?: 0)
        applicationScope.launch {
            try {
                val downloaded = downloader.download(release) { bytes, total ->
                    mutableState.value = AppUpdateInstallState.Downloading(release, bytes, total)
                }
                mutableState.value = AppUpdateInstallState.Verifying(release)
                verifiedUpdate = verifier.verify(
                    apk = downloaded.file,
                    expectedVersionName = release.expectedApkVersionName(),
                    expectedVersionCode = release.versionCode?.toLong(),
                )
                mutableState.value = AppUpdateInstallState.ReadyToInstall(release)
                installPreparedUpdate()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                verifiedUpdate = null
                mutableState.value = AppUpdateInstallState.Failed(
                    error.message ?: "Unable to prepare app update",
                )
            }
        }
    }

    fun installPreparedUpdate() {
        val update = verifiedUpdate ?: return
        val release = state.value.releaseOrNull() ?: return
        when (settings.appUpdateInstallMethod.value) {
            AppUpdateInstallMethod.System -> installWithSystemInstaller(update, release)
            AppUpdateInstallMethod.Shizuku -> installPrivileged(release) {
                shizukuInstaller.install(update.file)
            }
            AppUpdateInstallMethod.Root -> installPrivileged(release) {
                rootInstaller.install(update.file)
            }
        }
    }

    private fun installWithSystemInstaller(
        update: VerifiedUpdateApk,
        release: ReleaseCheck.Published,
    ) {
        if (!installer.canRequestInstallPackages()) {
            mutableState.value = AppUpdateInstallState.InstallPermissionRequired(release)
            return
        }
        try {
            mutableState.value = AppUpdateInstallState.Installing(release)
            installer.install(
                apk = update.file,
                resultIntent = Intent(context, AppUpdateInstallReceiver::class.java),
            )
        } catch (error: Exception) {
            mutableState.value = AppUpdateInstallState.Failed(
                error.message ?: "Unable to start app update installation",
            )
        }
    }

    private fun installPrivileged(
        release: ReleaseCheck.Published,
        install: suspend () -> String,
    ) {
        mutableState.value = AppUpdateInstallState.Installing(release)
        applicationScope.launch {
            try {
                install()
                verifiedUpdate = null
                mutableState.value = AppUpdateInstallState.Installed
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                mutableState.value = AppUpdateInstallState.Failed(
                    error.message ?: "Privileged app update installation failed",
                )
            }
        }
    }

    fun handleInstallResult(intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = intent.intentExtra(Intent.EXTRA_INTENT)
                if (confirmationIntent == null) {
                    mutableState.value = AppUpdateInstallState.Failed("System installation confirmation is unavailable")
                    return
                }
                val release = state.value.releaseOrNull()
                if (release != null) {
                    mutableState.value = AppUpdateInstallState.WaitingForUserConfirmation(release)
                }
                if (foregroundTracker.isForeground) {
                    confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmationIntent)
                } else if (!installNotifier.showConfirmation(confirmationIntent)) {
                    mutableState.value = AppUpdateInstallState.Failed(
                        "Open KokoroBox to continue the Android installation confirmation",
                    )
                }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                verifiedUpdate = null
                mutableState.value = AppUpdateInstallState.Installed
            }

            else -> {
                verifiedUpdate = null
                mutableState.value = AppUpdateInstallState.Failed(
                    intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                        ?: "System installation failed",
                )
            }
        }
    }

    fun dismiss() {
        if (!state.value.isBusy()) {
            mutableState.value = AppUpdateInstallState.Idle
        }
    }

    private fun ReleaseCheck.Published.expectedApkVersionName(): String = buildString {
        append(version.major)
        append('.')
        append(version.minor)
        append('.')
        append(version.patch)
        if (versionCode != null) append("-nightly")
    }

    private fun AppUpdateInstallState.isBusy(): Boolean = when (this) {
        is AppUpdateInstallState.Downloading,
        is AppUpdateInstallState.Verifying,
        is AppUpdateInstallState.Installing,
        is AppUpdateInstallState.WaitingForUserConfirmation, -> true
        else -> false
    }

    private fun AppUpdateInstallState.releaseOrNull(): ReleaseCheck.Published? = when (this) {
        is AppUpdateInstallState.Downloading -> release
        is AppUpdateInstallState.Verifying -> release
        is AppUpdateInstallState.ReadyToInstall -> release
        is AppUpdateInstallState.InstallPermissionRequired -> release
        is AppUpdateInstallState.Installing -> release
        is AppUpdateInstallState.WaitingForUserConfirmation -> release
        else -> null
    }

    @Suppress("DEPRECATION")
    private fun Intent.intentExtra(key: String): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, Intent::class.java)
        } else {
            getParcelableExtra(key)
        }
}
