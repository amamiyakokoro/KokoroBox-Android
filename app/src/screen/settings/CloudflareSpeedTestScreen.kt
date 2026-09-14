/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License.
 */

package com.amamiyakokoro.box.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amamiyakokoro.box.data.integration.speedtest.CloudflareSpeedTestResult
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.data.integration.speedtest.CloudflareSpeedTestStage
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.ScreenLazyColumn
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3FilledButton
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3TextButton
import com.amamiyakokoro.box.presentation.component.rememberStandalonePageMainPadding
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.UiDp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
@Destination<RootGraph>
fun CloudflareSpeedTestScreen(navigator: DestinationsNavigator) {
    val viewModel = koinViewModel<CloudflareSpeedTestViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(
                title = stringResource(LocaleR.string.feature_speed_test_title),
                navigationIcon = {
                    IconButton(onClick = navigator::navigateUp) {
                        Icon(
                            imageVector = AppMd3Icons.Navigation.Back,
                            contentDescription = stringResource(LocaleR.string.component_navigation_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, rememberStandalonePageMainPadding()),
        ) {
            item {
                Card {
                    CloudflareSpeedTestCard(
                        state = state,
                        onStart = viewModel::start,
                        onCancel = viewModel::cancel,
                    )
                }
            }
        }
    }
}

@Composable
private fun CloudflareSpeedTestCard(
    state: CloudflareSpeedTestUiState,
    onStart: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
        verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = AppMd3Icons.Action.SpeedTest,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(UiDp.dp2)) {
                Text(stringResource(LocaleR.string.feature_speed_test_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(LocaleR.string.feature_speed_test_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            stringResource(LocaleR.string.feature_speed_test_data_usage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when {
            state.running -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = state.stage?.let { speedTestStageLabel(it) }.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            state.failed -> Text(
                stringResource(LocaleR.string.feature_speed_test_error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )

            state.result != null -> CloudflareSpeedTestResults(state.result)
        }

        Text(
            stringResource(LocaleR.string.feature_speed_test_privacy_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.running) {
            YumeMd3TextButton(
                text = stringResource(LocaleR.string.feature_speed_test_cancel),
                onClick = onCancel,
                modifier = Modifier.align(Alignment.End),
            )
        } else {
            YumeMd3FilledButton(
                text = stringResource(LocaleR.string.feature_speed_test_start),
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CloudflareSpeedTestResults(result: CloudflareSpeedTestResult) {
    Column(verticalArrangement = Arrangement.spacedBy(UiDp.dp8)) {
        SpeedTestMetric(stringResource(LocaleR.string.feature_speed_test_download), formatMegabits(result.downloadBitsPerSecond))
        SpeedTestMetric(stringResource(LocaleR.string.feature_speed_test_upload), formatMegabits(result.uploadBitsPerSecond))
        SpeedTestMetric(stringResource(LocaleR.string.feature_speed_test_latency), formatMilliseconds(result.latencyMs))
        SpeedTestMetric(stringResource(LocaleR.string.feature_speed_test_jitter), formatMilliseconds(result.jitterMs))
        SpeedTestMetric(
            stringResource(LocaleR.string.feature_speed_test_edge_location),
            result.cloudflareColo ?: stringResource(LocaleR.string.feature_speed_test_location_unknown),
        )
    }
}

@Composable
private fun SpeedTestMetric(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun speedTestStageLabel(stage: CloudflareSpeedTestStage): String = when (stage) {
    CloudflareSpeedTestStage.Preparing -> stringResource(LocaleR.string.feature_speed_test_preparing)
    CloudflareSpeedTestStage.Latency -> stringResource(LocaleR.string.feature_speed_test_testing_latency)
    CloudflareSpeedTestStage.Download -> stringResource(LocaleR.string.feature_speed_test_testing_download)
    CloudflareSpeedTestStage.Upload -> stringResource(LocaleR.string.feature_speed_test_testing_upload)
}

private fun formatMegabits(bitsPerSecond: Long): String {
    val megabits = bitsPerSecond.coerceAtLeast(0L) / 1_000_000.0
    val decimals = if (megabits < 10) 1 else 0
    return String.format(Locale.ROOT, "%.${decimals}f Mbps", megabits)
}

private fun formatMilliseconds(milliseconds: Double): String =
    String.format(Locale.ROOT, "%.0f ms", milliseconds.coerceAtLeast(0.0))
