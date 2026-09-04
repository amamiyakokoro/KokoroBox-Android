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
import com.github.yumelira.yumebox.data.store.SUPPORTED_HEALTH_CHECK_CONCURRENCY
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DropdownPreference
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.oom_wg.purejoy.mlang.MLang
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>
fun LabScreen() {
    val viewModel = koinViewModel<AppSettingsViewModel>()
    val healthCheckConcurrency by viewModel.healthCheckConcurrency.state.collectAsState()

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
        }
    }
}
