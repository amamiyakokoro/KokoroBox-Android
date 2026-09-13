/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.screen.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amamiyakokoro.box.common.util.toast
import com.amamiyakokoro.box.data.store.SUPPORTED_HEALTH_CHECK_CONCURRENCY
import com.amamiyakokoro.box.feature.editor.presentation.language.LanguageScope
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.PreferenceArrowItem
import com.amamiyakokoro.box.presentation.component.ScreenLazyColumn
import com.amamiyakokoro.box.presentation.component.Title
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.component.rememberStandalonePageMainPadding
import com.amamiyakokoro.box.presentation.util.OverrideStructuredEditorStore
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.CloudflareSpeedTestScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OverrideConfigPreviewRouteDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>
fun LabScreen(navigator: DestinationsNavigator) {
    val appSettingsViewModel = koinViewModel<AppSettingsViewModel>()
    val labViewModel = koinViewModel<LabViewModel>()
    val healthCheckConcurrency by appSettingsViewModel.healthCheckConcurrency.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { TopBar(title = MLang.Feature.Title) },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
        ) {
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
                                ?.let(appSettingsViewModel::onHealthCheckConcurrencyChange)
                        },
                    )
                }
            }
            item {
                RuntimeConfigurationSection(
                    navigator = navigator,
                    viewModel = labViewModel,
                )
            }
            item {
                Title(MLang.Feature.SpeedTest.Section)
                Card {
                    PreferenceArrowItem(
                        title = MLang.Feature.SpeedTest.Title,
                        summary = MLang.Feature.SpeedTest.Summary,
                        onClick = {
                            navigator.navigate(CloudflareSpeedTestScreenDestination) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RuntimeConfigurationSection(
    navigator: DestinationsNavigator,
    viewModel: LabViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val opening = remember { mutableStateOf(false) }

    Title(MLang.Feature.RuntimeConfig.Section)
    Card {
        PreferenceArrowItem(
            title = MLang.Feature.RuntimeConfig.Title,
            summary = MLang.Feature.RuntimeConfig.Summary,
            onClick = {
                if (opening.value) return@PreferenceArrowItem
                opening.value = true
                scope.launch {
                    try {
                        when (val result = viewModel.loadRunningConfiguration()) {
                            is RuntimeConfigLoadResult.Loaded -> {
                                val profileName = result.profileName.ifBlank {
                                    MLang.Feature.RuntimeConfig.UnknownProfile
                                }
                                OverrideStructuredEditorStore.setupConfigPreview(
                                    title = MLang.Feature.RuntimeConfig.PreviewTitle.format(profileName),
                                    content = result.content,
                                    language = LanguageScope.Yaml,
                                )
                                navigator.navigate(OverrideConfigPreviewRouteDestination) {
                                    launchSingleTop = true
                                }
                            }

                            RuntimeConfigLoadResult.NotRunning -> {
                                context.toast(MLang.Feature.RuntimeConfig.NotRunning)
                            }

                            RuntimeConfigLoadResult.NotReady -> {
                                context.toast(MLang.Feature.RuntimeConfig.NotReady)
                            }

                            RuntimeConfigLoadResult.Unavailable -> {
                                context.toast(MLang.Feature.RuntimeConfig.Unavailable)
                            }

                            RuntimeConfigLoadResult.Empty -> {
                                context.toast(MLang.Feature.RuntimeConfig.Empty)
                            }

                            RuntimeConfigLoadResult.RuntimeChanged -> {
                                context.toast(MLang.Feature.RuntimeConfig.RuntimeChanged)
                            }
                        }
                    } finally {
                        opening.value = false
                    }
                }
            },
        )
    }
}
