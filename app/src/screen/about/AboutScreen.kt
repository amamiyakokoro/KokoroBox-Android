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

package com.amamiyakokoro.box.screen.about

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.amamiyakokoro.box.BuildConfig
import com.amamiyakokoro.box.common.util.openUrl
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.Title
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3PreferenceItem
import com.amamiyakokoro.box.presentation.component.rememberStandalonePageMainPadding
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.UiDp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.OpenSourceLicensesScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>
fun AboutScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val updateViewModel = koinViewModel<AppUpdateViewModel>()
    val updateState by updateViewModel.state.collectAsStateWithLifecycle()
    val updateInstallState by updateViewModel.installState.collectAsStateWithLifecycle()
    updateState.result?.let { result ->
        AppUpdateDialog(
            result = result,
            installState = updateInstallState,
            onDownloadAndInstall = updateViewModel::downloadAndInstall,
            onContinueInstall = updateViewModel::continueInstall,
            onDismiss = updateViewModel::dismiss,
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
                        text = stringResource(LocaleR.string.about_title),
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
                        summary = stringResource(LocaleR.string.about_app_description),
                    )
                    YumeMd3PreferenceItem(
                        title = stringResource(LocaleR.string.about_license_check_update),
                        summary = if (updateState.checking) stringResource(LocaleR.string.about_update_checking)
                            else stringResource(LocaleR.string.about_license_check_update_summary),
                        enabled = !updateState.checking,
                        onClick = updateViewModel::check,
                        trailingContent = { ChevronText() },
                    )
                }

                Title(stringResource(LocaleR.string.about_section_project_links))
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

                Title(stringResource(LocaleR.string.about_section_license))
                Card {
                    YumeMd3PreferenceItem(
                        title = stringResource(LocaleR.string.about_license_libraries),
                        summary = stringResource(LocaleR.string.about_license_libraries_summary),
                        onClick = { navigator.navigate(OpenSourceLicensesScreenDestination) },
                        trailingContent = { ChevronText() },
                    )
                    YumeMd3PreferenceItem(
                        title = stringResource(LocaleR.string.about_license_agpl_name),
                        summary = stringResource(LocaleR.string.about_license_agpl_description),
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
                        text = stringResource(LocaleR.string.about_copyright),
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
