/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.github.yumelira.yumebox.WebViewActivity
import com.github.yumelira.yumebox.common.util.openUrl
import com.github.yumelira.yumebox.data.store.LinkOpenMode
import com.github.yumelira.yumebox.data.store.SUPPORTED_HEALTH_CHECK_CONCURRENCY
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.PreferenceListItem
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DropdownPreference
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.webview.WebViewUtils.getPanelUrl
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.oom_wg.purejoy.mlang.MLang
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>
fun LabScreen() {
    val context = LocalContext.current
    val viewModel = koinViewModel<AppSettingsViewModel>()
    val selectedPanelType by viewModel.selectedPanelType.state.collectAsState()
    val panelOpenMode by viewModel.panelOpenMode.state.collectAsState()
    val healthCheckConcurrency by viewModel.healthCheckConcurrency.state.collectAsState()
    val panelDisplayNames = listOf("Zashboard", "MetaCubeXD", "Yacd")
    val safeSelectedPanelType = selectedPanelType.coerceIn(0, panelDisplayNames.lastIndex)
    val panelUrl = getPanelUrl(safeSelectedPanelType)
    val panelOpenModeItems = listOf(
        MLang.ProfilesPage.LinkSettings.OpenModeInApp,
        MLang.ProfilesPage.LinkSettings.OpenModeExternal,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = MLang.Feature.Title) },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
        ) {
            item {
                Title(MLang.Feature.Panel.Section)
                Card {
                    YumeMd3DropdownPreference(
                        title = MLang.Feature.Panel.SelectPanel,
                        summary = null,
                        items = panelDisplayNames,
                        selectedIndex = safeSelectedPanelType,
                        onSelectedIndexChange = viewModel::onSelectedPanelTypeChange,
                    )
                    PreferenceListItem(
                        title = "URL",
                        summary = panelUrl,
                        onClick = {
                            if (panelUrl.isBlank()) return@PreferenceListItem
                            when (panelOpenMode) {
                                LinkOpenMode.IN_APP -> WebViewActivity.start(context, panelUrl)
                                LinkOpenMode.EXTERNAL_BROWSER -> openUrl(context, panelUrl)
                            }
                        },
                    )
                    YumeMd3DropdownPreference(
                        title = MLang.ProfilesPage.LinkSettings.OpenMode,
                        summary = null,
                        items = panelOpenModeItems,
                        selectedIndex = if (panelOpenMode == LinkOpenMode.IN_APP) 0 else 1,
                        onSelectedIndexChange = { index ->
                            viewModel.onPanelOpenModeChange(
                                if (index == 0) LinkOpenMode.IN_APP else LinkOpenMode.EXTERNAL_BROWSER,
                            )
                        },
                    )
                }
            }

            item {
                Title(MLang.Feature.Node.Section)
                Card {
                    YumeMd3DropdownPreference(
                        title = MLang.Feature.Node.HealthCheckConcurrencyTitle,
                        summary = MLang.Feature.Node.HealthCheckConcurrencySummary.format(healthCheckConcurrency),
                        items = SUPPORTED_HEALTH_CHECK_CONCURRENCY.map(Int::toString),
                        selectedIndex = SUPPORTED_HEALTH_CHECK_CONCURRENCY.indexOf(healthCheckConcurrency)
                            .takeIf { it >= 0 } ?: 0,
                        onSelectedIndexChange = { index ->
                            SUPPORTED_HEALTH_CHECK_CONCURRENCY.getOrNull(index)
                                ?.let(viewModel::onHealthCheckConcurrencyChange)
                        },
                    )
                }
            }
        }
    }
}
