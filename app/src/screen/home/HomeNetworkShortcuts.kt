package com.amamiyakokoro.box.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.YumeTheme

@Composable
internal fun HomeNetworkShortcuts(
    onTrafficClick: () -> Unit,
    onConnectionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(
            onClick = onTrafficClick,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = colors,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(AppMd3Icons.Home.Traffic, null, modifier = Modifier.size(18.dp))
                Text(stringResource(LocaleR.string.home_shortcut_traffic), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        FilledTonalButton(
            onClick = onConnectionsClick,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = colors,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(AppMd3Icons.Home.Connections, null, modifier = Modifier.size(18.dp))
                Text(stringResource(LocaleR.string.connection_title), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Preview(widthDp = 320, showBackground = true)
@Preview(widthDp = 320, showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeNetworkShortcutsPreview() {
    YumeTheme { HomeNetworkShortcuts({}, {}) }
}
