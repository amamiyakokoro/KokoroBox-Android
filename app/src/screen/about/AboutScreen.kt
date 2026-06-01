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
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.github.yumelira.yumebox.BuildConfig
import com.github.yumelira.yumebox.R
import com.github.yumelira.yumebox.common.util.openUrl
import com.github.yumelira.yumebox.core.bridge.Bridge
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
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination<RootGraph>
fun AboutScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val coreVersion by produceState(initialValue = MLang.About.App.VersionLoading) {
        value = try {
            Bridge.nativeCoreVersion()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            MLang.About.App.VersionFailed
        }
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

                    Icon(
                        painter = painterResource(id = R.drawable.yume),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .size(UiDp.dp120)
                            .clip(RoundedCornerShape(UiDp.dp24)),
                        tint = Color.Unspecified,
                    )

                    Spacer(modifier = Modifier.height(UiDp.dp24))

                    Text(
                        text = "YumeBox MD3",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(UiDp.dp8))

                    Text(
                        text = "v${BuildConfig.VERSION_NAME} ($coreVersion)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(UiDp.dp32))
                }

                Card {
                    YumeMd3PreferenceItem(
                        title = "YumeBox MD3",
                        summary = "A Material Design 3 / Material You fork of YumeBox, an open-source Android client based on Mihomo",
                        showDivider = false,
                    )
                }

                Title(MLang.About.Section.ProjectLinks)
                Card {
                    AboutLinkItem(
                        title = "YumeBox-MaterialDesign",
                        url = "https://github.com/Yizuka17/YumeBox-MaterialDesign",
                        onOpenUrl = { url -> openUrl(context, url) },
                        showArrow = false,
                    )
                    AboutLinkItem(
                        title = "YumeBox",
                        url = "https://github.com/YumeLira/YumeBox",
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

                Title(MLang.About.Section.More)
                Card {
                    AboutLinkItem(
                        title = MLang.About.Link.TelegramGroup,
                        url = "https://t.me/OOM_Group",
                        onOpenUrl = { url -> openUrl(context, url) },
                        showArrow = true,
                    )
                    AboutLinkItem(
                        title = MLang.About.Link.TelegramChannel,
                        url = "https://t.me/YumeLira",
                        onOpenUrl = { url -> openUrl(context, url) },
                        showArrow = true,
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
