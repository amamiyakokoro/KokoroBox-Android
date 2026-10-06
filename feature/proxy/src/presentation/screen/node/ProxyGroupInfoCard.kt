/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */


package com.amamiyakokoro.box.presentation.screen.node

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.domain.model.ProxyGroupInfo
import com.amamiyakokoro.box.presentation.component.Md3ELoading
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.AppMotion

@Composable
internal fun ProxyGroupInfoCard(
    group: ProxyGroupInfo,
    currentProxyName: String,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTesting: Boolean = false,
) {
    val currentProxy = remember(group.proxies, currentProxyName) {
        group.proxies.firstOrNull { it.name == currentProxyName }
    }
    val presentation = remember(currentProxy, currentProxyName) {
        resolveProxyDisplayPresentation(currentProxyName, currentProxy?.title)
    }
    val currentNodeLabel = stringResource(LocaleR.string.proxy_group_current_node)
    val currentNode = presentation.displayName.ifBlank { "—" }
    val latency = nodeLatencyLabel(currentProxy?.delay)
    val latencyText = latency?.first?.replace(Regex("\\s*ms$"), " ms")
    val palette = rememberProxySelectionPalette(selected = isSelected)
    val expansionState = stringResource(if (isExpanded) LocaleR.string.proxy_group_expanded else LocaleR.string.proxy_group_collapsed)
    val arrowRotation by animateFloatAsState(if (isExpanded) 180f else 0f,
        animationSpec = AppMotion.fastSpatial(), label = "proxy_group_expansion")
    NodeSelectableCard(
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier.semantics { stateDescription = expansionState },
        paddingVertical = 12.dp,
        paddingHorizontal = 14.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    group.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = palette.contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(AppMd3Icons.Navigation.DownAngle, contentDescription = null,
                    modifier = Modifier.size(18.dp).rotate(arrowRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                currentNode,
                modifier = Modifier.semantics { contentDescription = "$currentNodeLabel: $currentNode" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val density = LocalDensity.current
                val measurer = rememberTextMeasurer()
                val typeStyle = MaterialTheme.typography.labelMedium
                val latencyStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp)
                val typeLabel = group.type.displayName()
                val typeWidth = measurer.measure(typeLabel, style = typeStyle, maxLines = 1).size.width
                val latencyWidth = if (isTesting) with(density) { 16.dp.roundToPx() } else {
                    latencyText?.let { measurer.measure(it, style = latencyStyle, maxLines = 1).size.width } ?: 0
                }
                val fitsInRow = with(density) { typeWidth.toDp() + 12.dp + latencyWidth.toDp() + 8.dp } <= maxWidth
                val typeBadge: @Composable () -> Unit = {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(typeLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                            style = typeStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                val delay: @Composable () -> Unit = {
                    if (isTesting) {
                        Md3ELoading(Modifier.size(16.dp))
                    } else if (latencyText != null) {
                        Text(latencyText, style = latencyStyle, color = latency.second, maxLines = 1)
                    }
                }
                if (fitsInRow) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        typeBadge()
                        delay()
                    }
                } else {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        typeBadge()
                        Row(Modifier.align(Alignment.End)) { delay() }
                    }
                }
            }
        }
    }
}
