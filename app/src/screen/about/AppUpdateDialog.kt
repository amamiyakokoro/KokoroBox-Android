package com.github.yumelira.yumebox.screen.about

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.yumelira.yumebox.BuildConfig
import com.github.yumelira.yumebox.common.util.openUrl
import com.github.yumelira.yumebox.data.integration.update.ReleaseCheck
import com.github.yumelira.yumebox.data.integration.update.ReleaseVersion
import com.github.yumelira.yumebox.presentation.theme.UiDp
import dev.oom_wg.purejoy.mlang.MLang

@Composable
fun AppUpdateDialog(
    result: ReleaseCheck,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val release = result as? ReleaseCheck.Published
    val currentVersion = ReleaseVersion.parse(BuildConfig.VERSION_NAME)
    val newer = release != null && currentVersion != null && release.version > currentVersion
    val message = when (result) {
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
        onDismissRequest = onDismiss,
        title = { Text(MLang.About.License.CheckUpdate) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(message)
                if (newer) {
                    Spacer(Modifier.height(UiDp.dp12))
                    Text(
                        if (release.apkUrl != null) MLang.About.Update.BrowserDownload
                        else MLang.About.Update.NoApk,
                    )
                    if (release.notes.isNotBlank()) {
                        Spacer(Modifier.height(UiDp.dp12))
                        Text(release.notes)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(MLang.About.Update.Ok) }
        },
        dismissButton = {
            if (newer) {
                TextButton(
                    onClick = {
                        try {
                            openUrl(context, release.apkUrl ?: release.releaseUrl)
                            onDismiss()
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                MLang.About.Update.NoBrowser,
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    },
                ) {
                    Text(
                        if (release.apkUrl != null) MLang.About.Update.Download
                        else MLang.About.Update.OpenRelease,
                    )
                }
            }
        },
    )
}
