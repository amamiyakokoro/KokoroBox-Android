/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License.
 */

package com.github.yumelira.yumebox.screen.settings

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.yumelira.yumebox.data.integration.speedtest.CloudflareSpeedTestResult
import com.github.yumelira.yumebox.data.integration.speedtest.CloudflareSpeedTestStage
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3FilledButton
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3TextButton
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
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
                title = MLang.Feature.SpeedTest.Title,
                navigationIcon = {
                    IconButton(onClick = navigator::navigateUp) {
                        Icon(
                            imageVector = AppMd3Icons.Navigation.Back,
                            contentDescription = MLang.Component.Navigation.Back,
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
private fun CloudflareSpeedTestResults(result: CloudflareSpeedTestResult) {
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

private fun speedTestStageLabel(stage: CloudflareSpeedTestStage): String = when (stage) {
    CloudflareSpeedTestStage.Preparing -> MLang.Feature.SpeedTest.Preparing
    CloudflareSpeedTestStage.Latency -> MLang.Feature.SpeedTest.TestingLatency
    CloudflareSpeedTestStage.Download -> MLang.Feature.SpeedTest.TestingDownload
    CloudflareSpeedTestStage.Upload -> MLang.Feature.SpeedTest.TestingUpload
}

private fun formatMegabits(bitsPerSecond: Long): String {
    val megabits = bitsPerSecond.coerceAtLeast(0L) / 1_000_000.0
    val decimals = if (megabits < 10) 1 else 0
    return String.format(Locale.ROOT, "%.${decimals}f Mbps", megabits)
}

private fun formatMilliseconds(milliseconds: Double): String =
    String.format(Locale.ROOT, "%.0f ms", milliseconds.coerceAtLeast(0.0))
