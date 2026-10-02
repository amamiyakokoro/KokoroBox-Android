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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amamiyakokoro.box.BuildConfig
import com.amamiyakokoro.box.common.util.openUrl
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.data.integration.update.ReleaseCheck
import com.amamiyakokoro.box.data.integration.update.ReleaseVersion
import com.amamiyakokoro.box.data.integration.update.isNewerThan
import com.amamiyakokoro.box.integration.update.AppUpdateInstallState
import com.amamiyakokoro.box.integration.update.versionLabel

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
    val noBrowserMessage = stringResource(LocaleR.string.about_update_no_browser)
    val message = installState.messageOrNull() ?: when (result) {
        is ReleaseCheck.Published -> when {
            currentVersion == null -> stringResource(LocaleR.string.about_update_unknown_version)
            newer -> stringResource(LocaleR.string.about_update_version_available, result.versionLabel(context))
            else -> stringResource(LocaleR.string.about_update_up_to_date)
        }
        ReleaseCheck.Failure.NoRelease -> stringResource(LocaleR.string.about_update_no_release)
        ReleaseCheck.Failure.RateLimited -> stringResource(LocaleR.string.about_update_rate_limited)
        ReleaseCheck.Failure.Network -> stringResource(LocaleR.string.about_update_network_error)
        ReleaseCheck.Failure.InvalidResponse -> stringResource(LocaleR.string.about_update_invalid_response)
    }
    val title = when (installState) {
        is AppUpdateInstallState.Failed -> LocaleR.string.about_update_update_failed
        AppUpdateInstallState.Installed -> LocaleR.string.about_update_install_complete
        is AppUpdateInstallState.InstallPermissionRequired -> LocaleR.string.about_update_open_install_settings
        AppUpdateInstallState.Idle -> when {
            result == ReleaseCheck.Failure.NoRelease -> LocaleR.string.about_license_check_update
            result is ReleaseCheck.Failure -> LocaleR.string.about_update_check_failed
            newer -> LocaleR.string.about_update_available
            else -> LocaleR.string.about_license_check_update
        }
        else -> LocaleR.string.about_update_title
    }
    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(title)) },
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
                if (installState is AppUpdateInstallState.Idle && newer) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when {
                            canInstallInApp -> stringResource(LocaleR.string.about_update_in_app_download_summary)
                            release.apkUrl != null -> stringResource(LocaleR.string.about_update_browser_download)
                            else -> stringResource(LocaleR.string.about_update_no_apk)
                        },
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
                    ) { Text(stringResource(LocaleR.string.about_update_open_install_settings)) }
                }

                installState is AppUpdateInstallState.ReadyToInstall -> {
                    TextButton(onClick = onContinueInstall) { Text(stringResource(LocaleR.string.about_update_continue_install)) }
                }

                installState is AppUpdateInstallState.Failed && release != null && canInstallInApp -> {
                    TextButton(onClick = { onDownloadAndInstall(release) }) { Text(stringResource(LocaleR.string.about_update_retry)) }
                }

                installState is AppUpdateInstallState.Idle && release != null && (newer || currentVersion == null) -> {
                    TextButton(
                        onClick = {
                            if (newer && canInstallInApp) {
                                onDownloadAndInstall(release)
                                return@TextButton
                            }
                            try {
                                openUrl(context, if (newer) release.apkUrl ?: release.releaseUrl else release.releaseUrl)
                                onDismiss()
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, noBrowserMessage, Toast.LENGTH_LONG).show()
                            }
                        },
                    ) {
                        Text(
                            when {
                                newer && canInstallInApp -> stringResource(LocaleR.string.about_update_in_app_download)
                                newer && release.apkUrl != null -> stringResource(LocaleR.string.about_update_download)
                                else -> stringResource(LocaleR.string.about_update_open_release)
                            },
                        )
                    }
                }

                !busy -> TextButton(onClick = onDismiss) { Text(stringResource(LocaleR.string.about_update_ok)) }
            }
        },
        dismissButton = {
            when (installState) {
                is AppUpdateInstallState.InstallPermissionRequired -> {
                    TextButton(onClick = onContinueInstall) { Text(stringResource(LocaleR.string.about_update_continue_install)) }
                }
                is AppUpdateInstallState.ReadyToInstall,
                is AppUpdateInstallState.Failed, -> {
                    if (!busy) TextButton(onClick = onDismiss) { Text(stringResource(LocaleR.string.about_update_ok)) }
                }
                AppUpdateInstallState.Idle -> {
                    if (release != null && (newer || currentVersion == null)) {
                        TextButton(onClick = onDismiss) { Text(stringResource(LocaleR.string.about_update_later)) }
                    }
                }
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

@Composable
private fun AppUpdateInstallState.messageOrNull(): String? = when (this) {
    is AppUpdateInstallState.Downloading -> stringResource(LocaleR.string.about_update_downloading)
    is AppUpdateInstallState.Verifying -> stringResource(LocaleR.string.about_update_verifying)
    is AppUpdateInstallState.ReadyToInstall -> stringResource(LocaleR.string.about_update_ready_to_install)
    is AppUpdateInstallState.Installing -> stringResource(LocaleR.string.about_update_preparing_install)
    is AppUpdateInstallState.InstallPermissionRequired -> stringResource(LocaleR.string.about_update_install_permission_required)
    is AppUpdateInstallState.WaitingForUserConfirmation -> stringResource(LocaleR.string.about_update_waiting_for_install_confirmation)
    AppUpdateInstallState.Installed -> stringResource(LocaleR.string.about_update_installed)
    is AppUpdateInstallState.Failed -> stringResource(messageResId)
    AppUpdateInstallState.Idle -> null
}
