/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c)  YumeLira 2025 - Present
 *
 */

package com.github.yumelira.yumebox.feature.meta.presentation.component
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.github.yumelira.yumebox.core.model.ConnectionInfo
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.PreferenceListItem
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConnectionCard(
    connectionInfo: ConnectionInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    val sizes = AppTheme.sizes
    val opacity = AppTheme.opacity

    val host = remember(connectionInfo.metadata) {
        connectionInfo.metadata["host"]?.jsonPrimitive?.content ?: ""
    }
    val network = remember(connectionInfo.metadata) {
        connectionInfo.metadata["network"]?.jsonPrimitive?.content ?: "TCP"
    }
    val destinationPort = remember(connectionInfo.metadata) {
        connectionInfo.metadata["destinationPort"]?.jsonPrimitive?.content ?: ""
    }
    val sourceIP = remember(connectionInfo.metadata) {
        connectionInfo.metadata["sourceIP"]?.jsonPrimitive?.content ?: ""
    }
    val sourcePort = remember(connectionInfo.metadata) {
        connectionInfo.metadata["sourcePort"]?.jsonPrimitive?.content ?: ""
    }

    val destinationIp = remember(connectionInfo.metadata) {
        connectionInfo.metadata["destinationIP"]?.jsonPrimitive?.content ?: ""
    }

    val displayHost = remember(host, destinationIp, destinationPort, sourceIP, sourcePort) {
        if (host.isNotEmpty() && destinationPort.isNotEmpty()) {
            "$host:$destinationPort"
        } else if (host.isNotEmpty()) {
            host
        } else if (destinationIp.isNotEmpty() && destinationPort.isNotEmpty()) {
            "$destinationIp:$destinationPort"
        } else if (destinationIp.isNotEmpty()) {
            destinationIp
        } else {
            "$sourceIP:$sourcePort"
        }
    }

    val relativeTime = formatRelativeTime(connectionInfo.start)
    val summaryText = remember(sourceIP, sourcePort, destinationIp, destinationPort) {
        val source = listOf(sourceIP, sourcePort).filter(String::isNotBlank).joinToString(":")
        val destination = listOf(destinationIp, destinationPort).filter(String::isNotBlank).joinToString(":")
        when {
            source.isNotBlank() && destination.isNotBlank() -> "$source → $destination"
            source.isNotBlank() -> source
            destination.isNotBlank() -> destination
            else -> null
        }
    }

    Card(
        modifier = modifier.padding(vertical = spacing.space4),
        applyHorizontalPadding = false,
    ) {
        PreferenceListItem(
            title = displayHost,
            summary = summaryText,
            startAction = {
                ConnectionLeadingIcon(
                    metadata = connectionInfo.metadata,
                    network = network,
                    modifier = Modifier.padding(end = spacing.space12),
                )
            },
            bottomAction = {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = sizes.connectionLeadingIconSize + spacing.space12),
                    horizontalArrangement = Arrangement.spacedBy(sizes.listItemVerticalMinimal),
                    verticalArrangement = Arrangement.spacedBy(spacing.space4),
                ) {
                    ConnectionTagChip(
                        label = network.uppercase(),
                        backgroundColor = getProtocolColor(network),
                    )

                    if (connectionInfo.rule.isNotEmpty()) {
                        ConnectionTagChip(label = connectionInfo.rule)
                    }

                    if (connectionInfo.chains.isNotEmpty()) {
                        ConnectionTagChip(label = MLang.Connection.ChainCount.format(connectionInfo.chains.size))
                    }

                    if (relativeTime.isNotEmpty()) {
                        ConnectionTagChip(
                            label = relativeTime,
                            backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            onClick = onClick,
        )
    }
}

@Composable
private fun ConnectionTagChip(
    label: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
) {
    val spacing = AppTheme.spacing
    val radii = AppTheme.radii
    val opacity = AppTheme.opacity
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = backgroundColor,
        modifier = Modifier
            .clip(RoundedCornerShape(radii.full))
            .background(backgroundColor.copy(alpha = opacity.subtle))
            .padding(horizontal = spacing.space4, vertical = spacing.space2),
    )
}

private fun formatRelativeTime(start: String): String {
    if (start.isEmpty()) return ""

    return try {
        val startTime = java.time.OffsetDateTime.parse(start).toInstant()
        val now = java.time.Instant.now()
        val duration = java.time.Duration.between(startTime, now)

        val seconds = duration.seconds
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> MLang.Connection.RelativeTime.JustNow
            minutes < 60 -> MLang.Connection.RelativeTime.MinutesAgo.format(minutes)
            hours < 24 -> MLang.Connection.RelativeTime.HoursAgo.format(hours)
            days < 7 -> MLang.Connection.RelativeTime.DaysAgo.format(days)
            else -> {
                val date = java.time.LocalDateTime.ofInstant(startTime, java.time.ZoneId.systemDefault())
                MLang.Connection.RelativeTime.Date.format(date.monthValue, date.dayOfMonth)
            }
        }
    } catch (e: Exception) {
        ""
    }
}
