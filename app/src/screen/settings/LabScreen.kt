/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.yumelira.yumebox.data.store.SUPPORTED_HEALTH_CHECK_CONCURRENCY
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DropdownPreference
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3FilledButton
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3TextButton
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.oom_wg.purejoy.mlang.MLang
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
@Destination<RootGraph>
fun LabScreen() {
    val viewModel = koinViewModel<AppSettingsViewModel>()
    val speedTestViewModel = koinViewModel<CloudflareSpeedTestViewModel>()
    val healthCheckConcurrency by viewModel.healthCheckConcurrency.state.collectAsStateWithLifecycle()
    val speedTestState by speedTestViewModel.state.collectAsStateWithLifecycle()

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
                    CloudflareSpeedTestCard(
                        state = speedTestState,
                        onStart = speedTestViewModel::start,
                        onCancel = speedTestViewModel::cancel,
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
                Text(MLang.Feature.SpeedTest.Title, style = MaterialTheme.typography.titleMedium)
                Text(
                    MLang.Feature.SpeedTest.Summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            MLang.Feature.SpeedTest.DataUsage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when {
            state.running -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = state.stage?.let(::speedTestStageLabel).orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            state.failed -> Text(
                MLang.Feature.SpeedTest.Error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )

            state.result != null -> CloudflareSpeedTestResults(state.result)
        }

        Text(
            MLang.Feature.SpeedTest.PrivacyNotice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (state.running) {
            YumeMd3TextButton(
                text = MLang.Feature.SpeedTest.Cancel,
                onClick = onCancel,
                modifier = Modifier.align(Alignment.End),
            )
        } else {
            YumeMd3FilledButton(
                text = MLang.Feature.SpeedTest.Start,
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CloudflareSpeedTestResults(
    result: com.github.yumelira.yumebox.data.integration.speedtest.CloudflareSpeedTestResult,
) {
    Column(verticalArrangement = Arrangement.spacedBy(UiDp.dp8)) {
        SpeedTestMetric(MLang.Feature.SpeedTest.Download, formatMegabits(result.downloadBitsPerSecond))
        SpeedTestMetric(MLang.Feature.SpeedTest.Upload, formatMegabits(result.uploadBitsPerSecond))
        SpeedTestMetric(MLang.Feature.SpeedTest.Latency, formatMilliseconds(result.latencyMs))
        SpeedTestMetric(MLang.Feature.SpeedTest.Jitter, formatMilliseconds(result.jitterMs))
        SpeedTestMetric(
            MLang.Feature.SpeedTest.EdgeLocation,
            result.cloudflareColo ?: MLang.Feature.SpeedTest.LocationUnknown,
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

private fun speedTestStageLabel(
    stage: com.github.yumelira.yumebox.data.integration.speedtest.CloudflareSpeedTestStage,
): String = when (stage) {
    com.github.yumelira.yumebox.data.integration.speedtest.CloudflareSpeedTestStage.Preparing -> MLang.Feature.SpeedTest.Preparing
    com.github.yumelira.yumebox.data.integration.speedtest.CloudflareSpeedTestStage.Latency -> MLang.Feature.SpeedTest.TestingLatency
    com.github.yumelira.yumebox.data.integration.speedtest.CloudflareSpeedTestStage.Download -> MLang.Feature.SpeedTest.TestingDownload
    com.github.yumelira.yumebox.data.integration.speedtest.CloudflareSpeedTestStage.Upload -> MLang.Feature.SpeedTest.TestingUpload
}

private fun formatMegabits(bitsPerSecond: Long): String {
    val megabits = bitsPerSecond.coerceAtLeast(0L) / 1_000_000.0
    val decimals = if (megabits < 10) 1 else 0
    return String.format(Locale.ROOT, "%.${decimals}f Mbps", megabits)
}

private fun formatMilliseconds(milliseconds: Double): String =
    String.format(Locale.ROOT, "%.0f ms", milliseconds.coerceAtLeast(0.0))
