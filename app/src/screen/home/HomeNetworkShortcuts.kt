package com.amamiyakokoro.box.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amamiyakokoro.box.common.util.formatBytes
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.YumeTheme

@Composable
internal fun HomeNetworkShortcuts(
    onTrafficClick: () -> Unit,
    onConnectionsClick: () -> Unit,
    totalTrafficBytes: Long = 0L,
    activeConnectionCount: Int? = null,
    modifier: Modifier = Modifier,
    controlAction: (@Composable () -> Unit)? = null,
) {
    BoxWithConstraints(modifier = modifier) {
        val stackControl = maxWidth < 300.dp || LocalDensity.current.fontScale > 1.25f
        val metrics: @Composable (Modifier) -> Unit = { metricModifier ->
            NetworkMetricSurface(onTrafficClick, onConnectionsClick, totalTrafficBytes, activeConnectionCount, metricModifier)
        }
        when {
            controlAction == null -> metrics(Modifier.fillMaxWidth())
            stackControl -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                metrics(Modifier.fillMaxWidth())
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { controlAction() }
            }
            else -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                metrics(Modifier.weight(1f))
                controlAction()
            }
        }
    }
}

@Composable
private fun NetworkMetricSurface(
    onTrafficClick: () -> Unit,
    onConnectionsClick: () -> Unit,
    totalTrafficBytes: Long,
    activeConnectionCount: Int?,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ShortcutMetric(
                label = stringResource(LocaleR.string.home_shortcut_traffic),
                value = formatBytes(totalTrafficBytes),
                onClick = onTrafficClick,
                modifier = Modifier.weight(1f),
            )
            VerticalDivider(modifier = Modifier.height(28.dp), color = MaterialTheme.colorScheme.outlineVariant)
            ShortcutMetric(
                label = stringResource(LocaleR.string.connection_title),
                value = activeConnectionCount?.toString() ?: "—",
                onClick = onConnectionsClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ShortcutMetric(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    value,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(AppMd3Icons.Navigation.Forward, null, modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(widthDp = 320, showBackground = true)
@Preview(widthDp = 320, showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Preview(widthDp = 320, showBackground = true, fontScale = 1.5f)
@Composable
private fun HomeNetworkShortcutsPreview() {
    YumeTheme {
        HomeNetworkShortcuts({}, {}, totalTrafficBytes = 1_280_000_000L, activeConnectionCount = 24,
            controlAction = { FloatingActionButton(onClick = {}) { Icon(AppMd3Icons.Shell.StartProxy, null) } })
    }
}
