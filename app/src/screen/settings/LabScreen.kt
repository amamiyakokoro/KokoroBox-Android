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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amamiyakokoro.box.data.store.SUPPORTED_HEALTH_CHECK_CONCURRENCY
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.PreferenceArrowItem
import com.amamiyakokoro.box.presentation.component.ScreenLazyColumn
import com.amamiyakokoro.box.presentation.component.Title
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.component.rememberStandalonePageMainPadding
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.CloudflareSpeedTestScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>
fun LabScreen(navigator: DestinationsNavigator) {
    val viewModel = koinViewModel<AppSettingsViewModel>()
    val healthCheckConcurrency by viewModel.healthCheckConcurrency.state.collectAsStateWithLifecycle()

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
                                ?.let(viewModel::onHealthCheckConcurrencyChange)
                        },
                    )
                }
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
