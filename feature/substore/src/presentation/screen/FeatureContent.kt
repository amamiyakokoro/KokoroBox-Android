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



package com.github.yumelira.yumebox.presentation.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.yumelira.yumebox.common.util.DeviceUtil
import com.github.yumelira.yumebox.data.store.LinkOpenMode
import com.github.yumelira.yumebox.presentation.component.*
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DropdownPreference
import com.github.yumelira.yumebox.presentation.viewmodel.FeatureViewModel
import com.github.yumelira.yumebox.substore.model.AutoCloseMode
import dev.oom_wg.purejoy.mlang.MLang
import org.koin.androidx.compose.koinViewModel

@Composable
fun FeatureContent(
    onOpenExternalUrl: (String) -> Unit,
    onOpenInAppUrl: (String) -> Unit,
) {
    val viewModel = koinViewModel<FeatureViewModel>()
    val isServiceRunning by viewModel.serviceRunningState.collectAsState()
    val allowLanAccess by viewModel.allowLanAccess.state.collectAsState()
    val frontendPort by viewModel.frontendPort.state.collectAsState()
    val backendPort by viewModel.backendPort.state.collectAsState()
    val autoCloseMode by viewModel.autoCloseMode.collectAsState()

    val host = "127.0.0.1"
    val frontendUrl = "http://${host}:${frontendPort}"
    val backendUrl = "http://${host}:${backendPort}"
    val subStoreUrl = "${frontendUrl}/subs?api=${backendUrl}"

    val isDownloadingSubStoreFrontend by viewModel.isDownloadingSubStoreFrontend.collectAsState()
    val isDownloadingSubStoreBackend by viewModel.isDownloadingSubStoreBackend.collectAsState()
    val isExtensionInstalled by viewModel.isExtensionInstalled.collectAsState()
    val isJavetLoaded by viewModel.isJavetLoaded.collectAsState()
    val isSubStoreInitialized by viewModel.isSubStoreInitialized.collectAsState()
    val selectedPanelType by viewModel.selectedPanelType.state.collectAsState()
    val panelOpenMode by viewModel.panelOpenMode.state.collectAsState()

    val panelDisplayNames = listOf("Zashboard", "MetaCubeXD", "Yacd")

    LaunchedEffect(Unit) {
        viewModel.initializeSubStoreStatus()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(title = MLang.Feature.Title)
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
        ) {
            item {
                val canStartService = isExtensionInstalled && isSubStoreInitialized
                when {
                    isServiceRunning -> MLang.Feature.ServiceStatus.Running.format(frontendUrl)
                    !isExtensionInstalled -> MLang.Feature.ServiceStatus.NeedExtension
                    !isSubStoreInitialized -> MLang.Feature.ServiceStatus.NeedSubStore
                    else -> MLang.Feature.ServiceStatus.NotRunning
                }
                Title(MLang.Feature.ServiceStatus.Section)
                Card {
                    val autoCloseItems = AutoCloseMode.entries.map { it.getDisplayName() }
                    val autoCloseValues = AutoCloseMode.entries

                    EnumSelector(
                        title = MLang.Feature.ServiceStatus.SwitchStartSubStore,
                        summary = MLang.Feature.ServiceStatus.AutoCloseModeSummary,
                        currentValue = autoCloseMode,
                        items = autoCloseItems,
                        values = autoCloseValues,
                        onValueChange = { mode ->
                            viewModel.setAutoCloseMode(mode)
                            if (mode != AutoCloseMode.DISABLED && !isServiceRunning && canStartService) {
                                viewModel.startService()
                            } else if (mode == AutoCloseMode.DISABLED && isServiceRunning) {
                                viewModel.stopService()
                            }
                        },
                    )
                    PreferenceSwitchItem(
                        title = MLang.Feature.ServiceStatus.AllowLan,
                        summary = MLang.Feature.ServiceStatus.AllowLanSummary,
                        checked = allowLanAccess,
                        onCheckedChange = { viewModel.setAllowLanAccess(it) },
                    )
                    PreferenceArrowItem(
                        title = "Sub-Store",
                        summary = subStoreUrl,
                        enabled = !DeviceUtil.is32BitDevice() && isServiceRunning,
                        onClick = {
                            if (!isServiceRunning) return@PreferenceArrowItem
                            when (panelOpenMode) {
                                LinkOpenMode.IN_APP -> onOpenInAppUrl(subStoreUrl)
                                LinkOpenMode.EXTERNAL_BROWSER -> onOpenExternalUrl(subStoreUrl)
                            }
                        }
                    )
                }
            }

            item {
                val currentPanelName = panelDisplayNames.getOrElse(selectedPanelType) {
                    MLang.Feature.Panel.Unknown
                }
                val panelUrl = panelUrlFor(selectedPanelType)
                val panelOpenModeItems = listOf(
                    MLang.ProfilesPage.LinkSettings.OpenModeInApp,
                    MLang.ProfilesPage.LinkSettings.OpenModeExternal,
                )
                val panelOpenModeIndex = when (panelOpenMode) {
                    LinkOpenMode.IN_APP -> 0
                    LinkOpenMode.EXTERNAL_BROWSER -> 1
                }

                Title(MLang.Feature.Panel.Section)
                Card {
                    val safeSelectedPanelType = selectedPanelType.coerceIn(0, panelDisplayNames.lastIndex)
                    YumeMd3DropdownPreference(
                        title = MLang.Feature.Panel.SelectPanel,
                        summary = null,
                        items = panelDisplayNames,
                        selectedIndex = safeSelectedPanelType,
                        onSelectedIndexChange = { viewModel.setSelectedPanelType(it) },
                    )

                    PreferenceListItem(
                        title = "URL",
                        summary = panelUrl.ifEmpty { currentPanelName },
                        onClick = {
                            if (panelUrl.isBlank()) return@PreferenceListItem
                            when (panelOpenMode) {
                                LinkOpenMode.IN_APP -> onOpenInAppUrl(panelUrl)
                                LinkOpenMode.EXTERNAL_BROWSER -> onOpenExternalUrl(panelUrl)
                            }
                        },
                    )

                    YumeMd3DropdownPreference(
                        title = MLang.ProfilesPage.LinkSettings.OpenMode,
                        summary = null,
                        items = panelOpenModeItems,
                        selectedIndex = panelOpenModeIndex,
                        onSelectedIndexChange = { index ->
                            viewModel.setPanelOpenMode(
                                when (index) {
                                    0 -> LinkOpenMode.IN_APP
                                    1 -> LinkOpenMode.EXTERNAL_BROWSER
                                    else -> LinkOpenMode.IN_APP
                                },
                            )
                        },
                    )
                }
            }

            item {
                Title(MLang.Feature.SubStore.SectionHint)
                Card {

                    PreferenceArrowItem(
                        title = if (isExtensionInstalled) {
                            MLang.Feature.SubStore.ExtensionInstalled
                        } else {
                            MLang.Feature.SubStore.ExtensionInstall
                        },
                        summary = when {
                            isExtensionInstalled && isJavetLoaded -> MLang.Feature.SubStore.JavetAvailable
                            isExtensionInstalled -> MLang.Feature.SubStore.JavetPending
                            else -> MLang.Feature.SubStore.DownloadHint
                        },
                        onClick = {
                            if (!isExtensionInstalled) {
                                onOpenExternalUrl("https://github.com/YumeLira/YumeBox/releases/tag/Expand")
                            } else {
                                viewModel.refreshExtensionStatus()
                            }
                        },
                    )
                    PreferenceArrowItem(
                        title = MLang.Feature.SubStore.DownloadResources,
                        summary = MLang.Feature.SubStore.DownloadResourcesSummary,
                        onClick = { viewModel.downloadSubStoreAll() },
                        enabled = !isDownloadingSubStoreFrontend && !isDownloadingSubStoreBackend,
                    )
                }
            }
        }
    }
}

private fun panelUrlFor(panelType: Int): String {
    return when (panelType) {
        0 -> "https://board.zash.run.place"
        1 -> "https://metacubex.github.io/metacubexd"
        2 -> "https://yacd.haishan.me"
        else -> "https://board.zash.run.place"
    }
}
