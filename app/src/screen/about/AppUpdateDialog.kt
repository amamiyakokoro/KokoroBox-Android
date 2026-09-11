package com.amamiyakokoro.box.screen.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amamiyakokoro.box.BuildConfig
import com.amamiyakokoro.box.common.util.openUrl
import com.amamiyakokoro.box.data.integration.update.ReleaseCheck
import com.amamiyakokoro.box.data.integration.update.ReleaseVersion
import com.amamiyakokoro.box.data.integration.update.isNewerThan
import com.amamiyakokoro.box.integration.update.AppUpdateInstallState
import dev.oom_wg.purejoy.mlang.MLang

@Composable
fun AppUpdateDialog(
    result: ReleaseCheck,
    installState: AppUpdateInstallState,
    onDownloadAndInstall: (ReleaseCheck.Published) -> Unit,
    onContinueInstall: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val release = result as? ReleaseCheck.Published
    val currentVersion = ReleaseVersion.parse(BuildConfig.VERSION_NAME)
    val newer = release?.isNewerThan(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE) == true
    val canInstallInApp = release?.hasVerifiedInAppAsset() == true
    val busy = installState.isBusy()
    val message = installState.messageOrNull() ?: when (result) {
        is ReleaseCheck.Published -> when {
            currentVersion == null -> MLang.About.Update.UnknownVersion
            newer -> "${MLang.About.Update.Available}: ${result.tag}"
            else -> MLang.About.Update.UpToDate
        }
        ReleaseCheck.Failure.NoRelease -> MLang.About.Update.NoRelease
        ReleaseCheck.Failure.RateLimited -> MLang.About.Update.RateLimited
        ReleaseCheck.Failure.Network -> MLang.About.Update.NetworkError
        ReleaseCheck.Failure.InvalidResponse -> MLang.About.Update.InvalidResponse
    }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(MLang.About.License.CheckUpdate) },
        text = {
            Column {
                Text(message)
                if (installState is AppUpdateInstallState.Downloading) {
                    Spacer(Modifier.height(16.dp))
                    val progress = if (installState.totalBytes > 0L) {
                        installState.downloadedBytes.toFloat() / installState.totalBytes.toFloat()
                    } else {
                        null
                    }
                    if (progress != null) {
                        LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) })
                    } else {
                        LinearProgressIndicator()
                    }
                }
                if (installState is AppUpdateInstallState.Failed) {
                    Spacer(Modifier.height(8.dp))
                    Text(installState.message)
                }
                if (installState is AppUpdateInstallState.Idle && newer) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (canInstallInApp) MLang.About.Update.InAppDownloadSummary else MLang.About.Update.NoApk,
                    )
                }
            }
        },
        confirmButton = {
            when {
                installState is AppUpdateInstallState.InstallPermissionRequired -> {
                    TextButton(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:${context.packageName}"),
                                ),
                            )
                        },
                    ) { Text(MLang.About.Update.OpenInstallSettings) }
                }

                installState is AppUpdateInstallState.ReadyToInstall -> {
                    TextButton(onClick = onContinueInstall) { Text(MLang.About.Update.ContinueInstall) }
                }

                installState is AppUpdateInstallState.Failed && release != null && canInstallInApp -> {
                    TextButton(onClick = { onDownloadAndInstall(release) }) { Text(MLang.About.Update.Retry) }
                }

                newer && !busy -> {
                    TextButton(
                        onClick = {
                            if (canInstallInApp) {
                                onDownloadAndInstall(release)
                                return@TextButton
                            }
                            try {
                                openUrl(context, release.apkUrl ?: release.releaseUrl)
                                onDismiss()
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, MLang.About.Update.NoBrowser, Toast.LENGTH_LONG).show()
                            }
                        },
                    ) {
                        Text(
                            if (canInstallInApp) MLang.About.Update.InAppDownload
                            else if (release.apkUrl != null) MLang.About.Update.Download
                            else MLang.About.Update.OpenRelease,
                        )
                    }
                }

                !busy -> TextButton(onClick = onDismiss) { Text(MLang.About.Update.Ok) }
            }
        },
        dismissButton = {
            when (installState) {
                is AppUpdateInstallState.InstallPermissionRequired -> {
                    TextButton(onClick = onContinueInstall) { Text(MLang.About.Update.ContinueInstall) }
                }
                is AppUpdateInstallState.ReadyToInstall,
                is AppUpdateInstallState.Failed, -> {
                    if (!busy) TextButton(onClick = onDismiss) { Text(MLang.About.Update.Ok) }
                }
                AppUpdateInstallState.Installed,
                AppUpdateInstallState.Idle, -> Unit
                else -> Unit
            }
        },
    )
}

private fun ReleaseCheck.Published.hasVerifiedInAppAsset(): Boolean =
    apkName != null && apkUrl != null && apkSizeBytes != null && checksumUrl != null

private fun AppUpdateInstallState.isBusy(): Boolean = when (this) {
    is AppUpdateInstallState.Downloading,
    is AppUpdateInstallState.Verifying,
    is AppUpdateInstallState.Installing,
    is AppUpdateInstallState.WaitingForUserConfirmation, -> true
    else -> false
}

private fun AppUpdateInstallState.messageOrNull(): String? = when (this) {
    is AppUpdateInstallState.Downloading -> MLang.About.Update.Downloading
    is AppUpdateInstallState.Verifying -> MLang.About.Update.Verifying
    is AppUpdateInstallState.ReadyToInstall,
    is AppUpdateInstallState.Installing, -> MLang.About.Update.PreparingInstall
    is AppUpdateInstallState.InstallPermissionRequired -> MLang.About.Update.InstallPermissionRequired
    is AppUpdateInstallState.WaitingForUserConfirmation -> MLang.About.Update.WaitingForInstallConfirmation
    AppUpdateInstallState.Installed -> MLang.About.Update.Installed
    is AppUpdateInstallState.Failed -> MLang.About.Update.UpdateFailed
    AppUpdateInstallState.Idle -> null
}
