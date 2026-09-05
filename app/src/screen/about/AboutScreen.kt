/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  YumeLira 2025 - Present
 *
 */

package com.github.yumelira.yumebox.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.github.yumelira.yumebox.data.integration.update.ReleaseCheck
import com.github.yumelira.yumebox.data.integration.update.ReleaseVersion
import com.github.yumelira.yumebox.BuildConfig
import com.github.yumelira.yumebox.common.util.openUrl
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3PreferenceItem
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.OpenSourceLicensesScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>
fun AboutScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val updateViewModel = koinViewModel<AppUpdateViewModel>()
    val updateState by updateViewModel.state.collectAsStateWithLifecycle()
    updateState.result?.let { result ->
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
            onDismissRequest = updateViewModel::dismiss,
            title = { Text(MLang.About.License.CheckUpdate) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(message)
                    if (newer) {
                        Spacer(Modifier.height(UiDp.dp12))
                        Text(if (release.apkUrl != null) MLang.About.Update.BrowserDownload
                            else MLang.About.Update.NoApk)
                        if (release.notes.isNotBlank()) {
                            Spacer(Modifier.height(UiDp.dp12))
                            Text(release.notes)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = updateViewModel::dismiss) { Text(MLang.About.Update.Ok) }
            },
            dismissButton = {
                if (newer) {
                    TextButton(onClick = {
                        // Use the system browser; no package installation permission is needed.
                        try {
                            openUrl(context, release.apkUrl ?: release.releaseUrl)
                            updateViewModel.dismiss()
                        } catch (_: android.content.ActivityNotFoundException) {
                            android.widget.Toast.makeText(context, MLang.About.Update.NoBrowser,
                                android.widget.Toast.LENGTH_LONG).show()
                        }
                    }) {
                        Text(if (release.apkUrl != null) MLang.About.Update.Download
                            else MLang.About.Update.OpenRelease)
                    }
                }
            },
        )
    }
    val appIcon = remember(context) {
        runCatching {
            context.packageManager
                .getApplicationIcon(context.packageName)
                .toBitmap(width = 256, height = 256)
                .asImageBitmap()
        }.getOrNull()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = MLang.About.Title,
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = combinePaddingValues(innerPadding, mainLikePadding),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(UiDp.dp24))

                    appIcon?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "KokoroBox app icon",
                            modifier = Modifier
                                .size(UiDp.dp120)
                                .clip(RoundedCornerShape(UiDp.dp24)),
                        )
                    }

                    Spacer(modifier = Modifier.height(UiDp.dp24))

                    Text(
                        text = "KokoroBox",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(UiDp.dp8))

                    Text(
                        text = "v${BuildConfig.VERSION_NAME} (Mihomo ${BuildConfig.MIHOMO_VERSION})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(UiDp.dp32))
                }

                Card {
                    YumeMd3PreferenceItem(
                        title = "KokoroBox",
                        summary = "A Material Design 3 / Material You fork of YumeBox, an open-source Android client based on Mihomo",
                        showDivider = false,
                    )
                }

                Card {
                    YumeMd3PreferenceItem(
                        title = MLang.About.License.CheckUpdate,
                        summary = if (updateState.checking) MLang.About.Update.Checking
                            else MLang.About.License.CheckUpdateSummary,
                        enabled = !updateState.checking,
                        onClick = updateViewModel::check,
                        trailingContent = { ChevronText() },
                    )
                }

                Title(MLang.About.Section.ProjectLinks)
                Card {
                    AboutLinkItem(
                        title = "KokoroBox",
                        url = "https://github.com/amamiyakokoro/KokoroBox-Android",
                        onOpenUrl = { url -> openUrl(context, url) },
                        showArrow = false,
                    )
                    AboutLinkItem(
                        title = "Mihomo",
                        url = "https://github.com/MetaCubeX/mihomo",
                        onOpenUrl = { url -> openUrl(context, url) },
                        showArrow = false,
                    )
                }

                Title(MLang.About.Section.License)
                Card {
                    YumeMd3PreferenceItem(
                        title = MLang.About.License.Libraries,
                        summary = MLang.About.License.LibrariesSummary,
                        onClick = { navigator.navigate(OpenSourceLicensesScreenDestination) },
                        trailingContent = { ChevronText() },
                    )
                    YumeMd3PreferenceItem(
                        title = MLang.About.License.AgplName,
                        summary = MLang.About.License.AgplDescription,
                        showDivider = false,
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = UiDp.dp32),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = MLang.About.Copyright,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(UiDp.dp32))
            }
        }
    }
}

@Composable
private fun AboutLinkItem(
    title: String,
    url: String,
    onOpenUrl: (String) -> Unit,
    showArrow: Boolean,
) {
    YumeMd3PreferenceItem(
        title = title,
        summary = url,
        onClick = { onOpenUrl(url) },
        trailingContent = if (showArrow) {
            { ChevronText() }
        } else {
            null
        },
    )
}

@Composable
private fun ChevronText() {
    Icon(
        imageVector = AppMd3Icons.Navigation.Forward,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
