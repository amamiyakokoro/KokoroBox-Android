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



package com.github.yumelira.yumebox.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import com.github.yumelira.yumebox.presentation.component.CountryFlagCircle
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import com.github.yumelira.yumebox.presentation.util.extractFlaggedName
import dev.oom_wg.purejoy.mlang.MLang

@Composable
fun NodeInfoDisplay(
    serverName: String?,
    serverPing: Int?,
    modifier: Modifier = Modifier
) {
    val spacing = AppTheme.spacing
    val componentSizes = AppTheme.sizes

    val flagged = remember(serverName) {
        serverName?.let(::extractFlaggedName)
    }
    val hasKnownNode = flagged != null || !serverName.isNullOrBlank()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .padding(end = spacing.space16)
        ) {
            Text(
                text = MLang.Home.NodeInfo.Node,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(spacing.space4))
            if (hasKnownNode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(INFO_TEXT_HEIGHT),
                ) {
                    val countryCode = flagged?.countryCode
                    if (countryCode != null) {
                        CountryFlagCircle(countryCode = countryCode, size = spacing.space18)
                        Spacer(modifier = Modifier.width(spacing.space8))
                    }
                    Text(
                        text = flagged?.displayName ?: serverName.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Text(
                    text = MLang.Home.NodeInfo.Unknown,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(INFO_TEXT_HEIGHT)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(componentSizes.nodeDelayColumnWidth)
        ) {
            Text(
                text = MLang.Home.NodeInfo.Delay,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(spacing.space4))
            PingValue(ping = serverPing)
        }
    }
}

@Composable
private fun PingValue(ping: Int?) {
    val semanticColors = AppTheme.colors

    if (ping != null && ping <= 1000) {
        val color = if (ping < 500) {
            semanticColors.latency.fast
        } else {
            semanticColors.latency.moderate
        }
        Text(
            text = MLang.Home.NodeInfo.DelayValue.format(ping),
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
            color = color,
            modifier = Modifier.height(INFO_TEXT_HEIGHT)
        )
    } else {
        Text(
            text = "--",
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.height(INFO_TEXT_HEIGHT)
        )
    }
}
