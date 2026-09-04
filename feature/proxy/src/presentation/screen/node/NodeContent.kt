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


package com.github.yumelira.yumebox.presentation.screen.node

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.core.model.Proxy
import com.github.yumelira.yumebox.data.model.normalizeProxySheetHeightFraction
import com.github.yumelira.yumebox.domain.model.ProxyGroupInfo
import com.github.yumelira.yumebox.presentation.component.Md3ELoading
import com.github.yumelira.yumebox.presentation.theme.AppMotion
import com.github.yumelira.yumebox.presentation.theme.UiDp
import top.yukonga.miuix.kmp.utils.overScrollHorizontal
import top.yukonga.miuix.kmp.utils.overScrollVertical

val NodeSheetContentPadding = PaddingValues(
    start = UiDp.dp0,
    end = UiDp.dp0,
    top = UiDp.dp8,
    bottom = UiDp.dp16,
)

private fun LazyListState.isScrolledFromTop(): Boolean {
    return firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0
}

@Suppress("unused")
@Composable
internal fun NodeTabs(
    groups: List<ProxyGroupInfo>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex, groups.size) {
        if (groups.isEmpty()) return@LaunchedEffect
        val target = (selectedIndex - 1).coerceAtLeast(0).coerceAtMost(groups.lastIndex)
        if (target != listState.firstVisibleItemIndex) {
            listState.animateScrollToItem(target)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .overScrollHorizontal(),
        contentPadding = PaddingValues(start = UiDp.dp14, end = UiDp.dp14, top = UiDp.dp10, bottom = UiDp.dp10),
        horizontalArrangement = Arrangement.spacedBy(UiDp.dp8),
        overscrollEffect = null,
    ) {
        itemsIndexed(groups, key = { _, group -> group.name }) { index, group ->
            val selected = index == selectedIndex
            val fastEffectsSpec = AppMotion.fastEffects<Color>()
            val fastSpatialSpec = AppMotion.fastSpatial<Float>()
            val indicatorSpec = AppMotion.indicator<Float>()
            val selectionScaleX = remember { Animatable(1f) }
            val background = animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
                animationSpec = fastEffectsSpec,
                label = "node_tab_background",
            ).value
            val textColor = animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = fastEffectsSpec,
                label = "node_tab_text",
            ).value
            LaunchedEffect(selected) {
                if (selected) {
                    selectionScaleX.snapTo(1f)
                    selectionScaleX.animateTo(1.06f, animationSpec = fastSpatialSpec)
                    selectionScaleX.animateTo(1f, animationSpec = indicatorSpec)
                } else {
                    selectionScaleX.animateTo(1f, animationSpec = fastSpatialSpec)
                }
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = selectionScaleX.value
                        scaleY = 1f
                    }
                    .clip(RoundedCornerShape(UiDp.dp999))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onSelect(index)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(background),
                )
                Text(
                    text = group.name,
                    modifier = Modifier.padding(horizontal = UiDp.dp11, vertical = UiDp.dp6),
                    color = textColor,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
internal fun rememberNodeSheetHeight(sheetHeightFraction: Float): Dp {
    val normalized = normalizeProxySheetHeightFraction(sheetHeightFraction)
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    return remember(screenHeightDp, normalized) { screenHeightDp.dp * normalized }
}

@Composable
internal fun NodeGroupSheetContent(
    groups: List<ProxyGroupInfo>,
    testingGroupNames: Set<String>,
    sheetHeightFraction: Float,
    onGroupClick: (ProxyGroupInfo) -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    val sheetHeight = rememberNodeSheetHeight(sheetHeightFraction)

    LaunchedEffect(testingGroupNames) {
        if (testingGroupNames.isNotEmpty() && listState.isScrolledFromTop()) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .overScrollVertical(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
        contentPadding = NodeSheetContentPadding,
        overscrollEffect = null,
    ) {
        nodeGroupItems(
            groups = groups,
            onGroupClick = onGroupClick,
            testingGroupNames = testingGroupNames,
            itemVerticalPadding = UiDp.dp0,
        )
    }
}

@Composable
fun NodeSheetContent(
    group: ProxyGroupInfo,
    onSelectProxy: (String) -> Unit,
    isDelayTesting: Boolean,
    testingProxyNames: Set<String>,
    onTestDelay: () -> Unit,
    onTestProxyDelay: (String) -> Unit,
    sheetHeightFraction: Float,
    listState: LazyListState = rememberLazyListState(),
    singleNodeTestEnabled: Boolean = true,
) {
    val sheetHeight = rememberNodeSheetHeight(sheetHeightFraction)

    LaunchedEffect(isDelayTesting) {
        if (isDelayTesting && listState.isScrolledFromTop()) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetHeight)
            .overScrollVertical(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
        contentPadding = NodeSheetContentPadding,
        overscrollEffect = null,
    ) {
        item(key = "__refresh_indicator__") {
            AnimatedVisibility(
                visible = isDelayTesting,
                enter = expandVertically(
                    animationSpec = AppMotion.fastSpatial<IntSize>(),
                    expandFrom = Alignment.Top,
                ) + fadeIn(animationSpec = tween(durationMillis = 150)),
                exit = shrinkVertically(
                    animationSpec = AppMotion.fastSpatial<IntSize>(),
                    shrinkTowards = Alignment.Top,
                ) + fadeOut(animationSpec = tween(durationMillis = 150)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = UiDp.dp8),
                    contentAlignment = Alignment.Center,
                ) {
                    Md3ELoading()
                }
            }
        }

        nodeGridItems(
            proxies = group.proxies,
            selectedProxyName = group.now,
            onProxyClick = { proxyName ->
                if (group.type == Proxy.Type.Selector) {
                    onSelectProxy(proxyName)
                } else {
                    onTestDelay()
                }
            },
            isDelayTesting = isDelayTesting,
            testingProxyNames = testingProxyNames,
            onSingleNodeTestClick = onTestProxyDelay,
            singleNodeTestEnabled = singleNodeTestEnabled,
        )
    }
}
