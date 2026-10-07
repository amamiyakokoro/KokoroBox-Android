/*
 * This file is part of KokoroBox.
 * Licensed under the GNU Affero General Public License, version 3 or later.
 */
package com.amamiyakokoro.box.screen.log

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.model.LogMessage
import com.amamiyakokoro.box.core.util.LogToken
import com.amamiyakokoro.box.core.util.LogTokenKind
import com.amamiyakokoro.box.core.util.parseLogMessage
import com.amamiyakokoro.box.data.store.LogStore
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.theme.AppTheme
import com.amamiyakokoro.box.presentation.theme.YumeTheme
import java.util.Locale

@Composable
internal fun LogEntryCard(entry: LogStore.LogEntry) {
    val spacing = AppTheme.spacing
    val palette = AppTheme.colors
    val scheme = MaterialTheme.colorScheme
    val parsed = remember(entry.message) { parseLogMessage(entry.message) }
    val levelColor = when (entry.level) {
        LogMessage.Level.Debug -> palette.logLevel.debug
        LogMessage.Level.Warning -> palette.logLevel.warning
        LogMessage.Level.Error -> palette.logLevel.error
        else -> scheme.onSurfaceVariant
    }
    val levelLabel = stringResource(when (entry.level) {
        LogMessage.Level.Debug -> LocaleR.string.log_level_debug
        LogMessage.Level.Info -> LocaleR.string.log_level_info
        LogMessage.Level.Warning -> LocaleR.string.log_level_warning
        LogMessage.Level.Error -> LocaleR.string.log_level_error
        LogMessage.Level.Silent -> LocaleR.string.log_level_silent
        LogMessage.Level.Unknown -> LocaleR.string.log_level_unknown
    })
    Card(modifier = Modifier.padding(vertical = spacing.space4)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = spacing.space12, vertical = spacing.space10)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space8)) {
                Text(
                    text = entry.time,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = levelLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                    color = levelColor,
                    modifier = Modifier.clip(RoundedCornerShape(AppTheme.radii.radius4))
                        .background(levelColor.copy(alpha = AppTheme.opacity.subtle))
                        .padding(horizontal = spacing.space6, vertical = spacing.space2),
                )
            }
            Spacer(Modifier.height(spacing.space6))
            LogTokenText(parsed.primary)
            if (parsed.secondary.isNotEmpty()) {
                Spacer(Modifier.height(spacing.space2))
                LogTokenText(parsed.secondary, secondary = true)
            }
        }
    }
}

@Composable
private fun LogTokenText(tokens: List<LogToken>, secondary: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    val palette = AppTheme.colors
    val tintOpacity = AppTheme.opacity.subtle
    val text = remember(tokens, scheme, palette, tintOpacity) {
        buildAnnotatedString {
            for (token in tokens) {
                val style = when (token.kind) {
                    LogTokenKind.Protocol -> SpanStyle(color = palette.protocol.tcp, fontWeight = FontWeight.SemiBold)
                    LogTokenKind.Address -> SpanStyle(color = palette.latency.fast, fontWeight = FontWeight.Medium)
                    LogTokenKind.Domain -> SpanStyle(color = scheme.onSurface, fontWeight = FontWeight.SemiBold)
                    LogTokenKind.Process -> SpanStyle(color = scheme.onSurface, background = scheme.surfaceContainerHighest,
                        fontWeight = FontWeight.Medium)
                    LogTokenKind.Rule -> SpanStyle(color = palette.logLevel.warning,
                        background = palette.logLevel.warning.copy(alpha = tintOpacity), fontWeight = FontWeight.Medium)
                    LogTokenKind.Route -> {
                        val routeColor = when (token.value.uppercase(Locale.ROOT)) {
                            "DIRECT" -> palette.latency.fast
                            "REJECT", "REJECT-DROP" -> palette.logLevel.error
                            else -> palette.protocol.tcp
                        }
                        SpanStyle(color = routeColor, fontWeight = FontWeight.SemiBold,
                            background = if (token.value.uppercase(Locale.ROOT) in setOf("DIRECT", "REJECT", "REJECT-DROP", "PASS"))
                                routeColor.copy(alpha = tintOpacity) else Color.Unspecified)
                    }
                    LogTokenKind.Error -> SpanStyle(color = palette.logLevel.error, fontWeight = FontWeight.SemiBold)
                    else -> SpanStyle(color = scheme.onSurfaceVariant)
                }
                withStyle(style) { append(token.value) }
            }
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = if (secondary) 11.sp else 12.sp,
            lineHeight = if (secondary) 17.sp else 19.sp,
            fontFamily = FontFamily.Monospace,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(name = "Highlighted logs", widthDp = 360, heightDp = 650)
@Preview(name = "Highlighted logs / narrow dark", widthDp = 320, heightDp = 750, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LogEntryCardPreview() {
    YumeTheme {
        Column {
            listOf(
                LogMessage.Level.Info to "[TCP] 127.0.0.1:51670(com.amamiyakokoro.box uid=10285) --> api.ip.sb:443 match Match using DIRECT",
                LogMessage.Level.Info to "[TCP] 127.0.0.1:63064(codex) --> chatgpt.com:443 match RuleSet(openai) using Direct-Special.anytls[Direct-Special.kokoro.anytls]",
                LogMessage.Level.Warning to "[UDP] [::1]:51534 --> ads.example.com:443 match DomainSuffix(example.com) using REJECT",
                LogMessage.Level.Error to "error: connection failed after timeout; upstream refused",
            ).forEach { (level, message) -> LogEntryCard(LogStore.LogEntry("2026-10-07 11:44:20.671", level, message)) }
        }
    }
}
