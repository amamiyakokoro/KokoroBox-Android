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


package com.github.yumelira.yumebox

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleStartEffect

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.core.model.Proxy
import com.github.yumelira.yumebox.presentation.component.AppActionBottomSheet
import com.github.yumelira.yumebox.presentation.component.AppBottomSheetAction
import com.github.yumelira.yumebox.presentation.component.AppBottomSheetIconAction
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.screen.node.NodeGroupSheetContent
import com.github.yumelira.yumebox.presentation.screen.node.NodeSheetContent
import com.github.yumelira.yumebox.presentation.screen.node.NodeSortPopup
import com.github.yumelira.yumebox.presentation.screen.rememberProxyGroupSelectionState
import com.github.yumelira.yumebox.presentation.theme.AppMotion
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.github.yumelira.yumebox.presentation.viewmodel.ProxyViewModel
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val NOTIFICATION_PROXY_SHEET_HEIGHT_FRACTION = 0.55f

private fun LazyListState.isScrolledFromTop(): Boolean {
    return firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0
}

@Composable
fun ProxySheetContent(
    onDismiss: () -> Unit,
    proxyViewModel: ProxyViewModel = koinViewModel(),
) {
    val proxyGroups by proxyViewModel.sortedProxyGroups.collectAsStateWithLifecycle()
    val sortMode by proxyViewModel.sortMode.collectAsStateWithLifecycle()

    val showSheet = remember { mutableStateOf(true) }
    val showSortPopup = remember { mutableStateOf(false) }
    val groupSelection = rememberProxyGroupSelectionState(
        proxyGroups = proxyGroups,
        onRefreshGroup = proxyViewModel::refreshGroup,
        retainLastKnownGroup = false,
    )
    val selectedGroupName = groupSelection.selectedGroupName
    val selectedGroup = groupSelection.selectedGroup
    val coroutineScope = rememberCoroutineScope()
    val groupListState = rememberLazyListState()
    val nodeListState = rememberSaveable(selectedGroupName, saver = LazyListState.Saver) {
        LazyListState()
    }

    LifecycleStartEffect(proxyViewModel) {
        proxyViewModel.ensureCoreLoaded(true, source = "proxy_sheet")
        onStopOrDispose {
            proxyViewModel.ensureCoreLoaded(false, source = "proxy_sheet")
        }
    }

    val dismissSheet = remember(onDismiss) {
        {
            showSortPopup.value = false
            showSheet.value = false
        }
    }
    val triggerTopDelayTest = remember(coroutineScope, groupListState, proxyViewModel) {
        {
            coroutineScope.launch {
                if (groupListState.isScrolledFromTop()) {
                    groupListState.animateScrollToItem(0)
                }
                proxyViewModel.testDelay()
            }
        }
    }
    val triggerSelectedGroupDelayTest = remember(
        coroutineScope,
        nodeListState,
        proxyViewModel,
        selectedGroupName,
    ) {
        {
            val groupName = selectedGroupName ?: return@remember
            coroutineScope.launch {
                if (nodeListState.isScrolledFromTop()) {
                    nodeListState.animateScrollToItem(0)
                }
                proxyViewModel.testDelay(groupName)
            }
        }
    }
    LaunchedEffect(showSheet.value) {
        if (!showSheet.value) {
            onDismiss()
        }
    }

    AppActionBottomSheet(
        show = showSheet.value,
        title = selectedGroup?.name ?: MLang.Proxy.Title,
        backgroundColor = MiuixTheme.colorScheme.surface,
        startAction = {
            if (selectedGroup != null) {
                AppBottomSheetIconAction(
                    action = AppBottomSheetAction(
                        icon = MiuixIcons.Back,
                        contentDescription = MLang.Component.Navigation.Back,
                        onClick = groupSelection.clearSelection,
                    ),
                )
            } else {
                Box {
                    AppBottomSheetIconAction(
                        action = AppBottomSheetAction(
                            icon = AppMd3Icons.Action.Sort,
                            contentDescription = MLang.Proxy.Action.Sort,
                            onClick = { showSortPopup.value = true },
                        ),
                    )
                    NodeSortPopup(
                        show = showSortPopup.value,
                        onDismiss = { showSortPopup.value = false },
                        sortMode = sortMode,
                        onSortSelected = proxyViewModel::setSortMode,
                    )
                }
            }
        },
        endAction = {
            AppBottomSheetIconAction(
                action = AppBottomSheetAction(
                    icon = AppMd3Icons.Action.SpeedTest,
                    contentDescription = MLang.Proxy.Action.Test,
                    onClick = {
                        if (selectedGroup == null) {
                            triggerTopDelayTest()
                        } else {
                            triggerSelectedGroupDelayTest()
                        }
                    },
                ),
            )
        },
        onDismissRequest = {
            showSortPopup.value = false
            if (selectedGroup != null) {
                groupSelection.clearSelection()
            } else {
                dismissSheet()
            }
        },
        enableWindowDim = true,
        enableNestedScroll = false,
        contentScrollEnabled = false,
    ) {
        AnimatedContent(
            modifier = Modifier,
            targetState = selectedGroupName,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally(
                        animationSpec = tween(
                            durationMillis = AppMotion.Proxy.SheetSlideInDuration,
                            easing = AppMotion.Legacy
                        ),
                        initialOffsetX = { it },
                    ) + fadeIn(animationSpec = tween(durationMillis = AppMotion.Proxy.SheetFadeInDuration))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = AppMotion.Proxy.SheetSlideOutDuration,
                                easing = AppMotion.Legacy
                            ),
                            targetOffsetX = { -it / 3 },
                        ) + fadeOut(animationSpec = tween(durationMillis = AppMotion.Proxy.SheetFadeOutDuration)))
                } else {
                    (slideInHorizontally(
                        animationSpec = tween(
                            durationMillis = AppMotion.Proxy.SheetSlideOutDuration,
                            easing = AppMotion.Legacy
                        ),
                        initialOffsetX = { -it / 3 },
                    ) + fadeIn(animationSpec = tween(durationMillis = AppMotion.Proxy.SheetFadeInDuration - 20))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = AppMotion.Proxy.SheetSlideInDuration - 20,
                                easing = AppMotion.Legacy
                            ),
                            targetOffsetX = { it },
                        ) + fadeOut(animationSpec = tween(durationMillis = AppMotion.Proxy.SheetFadeOutDuration)))
                }
            },
            label = "notification_node_sheet_content",
        ) { targetGroupName ->
            val targetGroup = targetGroupName?.let { name ->
                proxyGroups.firstOrNull { group -> group.name == name }
            }
            if (targetGroup == null) {
                val testingGroupNames by proxyViewModel.testingGroupNames.collectAsStateWithLifecycle()
                NodeGroupSheetContent(
                    groups = proxyGroups,
                    onGroupClick = groupSelection.selectGroup,
                    testingGroupNames = testingGroupNames,
                    sheetHeightFraction = NOTIFICATION_PROXY_SHEET_HEIGHT_FRACTION,
                    listState = groupListState,
                )
            } else {
                ProxySheetNodeContent(
                    proxyViewModel = proxyViewModel,
                    group = targetGroup,
                    onTestDelay = triggerSelectedGroupDelayTest,
                    sheetHeightFraction = NOTIFICATION_PROXY_SHEET_HEIGHT_FRACTION,
                    listState = nodeListState,
                )
            }
        }
    }
}

@Composable
private fun ProxySheetNodeContent(
    proxyViewModel: ProxyViewModel,
    group: com.github.yumelira.yumebox.domain.model.ProxyGroupInfo,
    onTestDelay: () -> Unit,
    sheetHeightFraction: Float,
    listState: LazyListState,
) {
    val groupProxyNames = remember(group.proxies) {
        group.proxies.mapTo(linkedSetOf()) { it.name }
    }
    val isDelayTesting by remember(group.name, proxyViewModel) {
        proxyViewModel.testingGroupNames
            .map { testingGroupNames -> testingGroupNames.contains(group.name) }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = false)
    val testingProxyNames by remember(group.name, groupProxyNames, proxyViewModel) {
        if (groupProxyNames.isEmpty()) {
            flowOf(emptySet<String>())
        } else {
            proxyViewModel.testingProxyNames
                .map { names ->
                    names.filterTo(linkedSetOf()) { proxyName -> proxyName in groupProxyNames }
                }
                .distinctUntilChanged()
        }
    }.collectAsStateWithLifecycle(initialValue = emptySet())
    val onSelectProxy = remember(group.name, group.type, proxyViewModel, onTestDelay) {
        { proxyName: String ->
            if (group.type == Proxy.Type.Selector) {
                proxyViewModel.selectProxy(group.name, proxyName)
            } else {
                onTestDelay()
            }
        }
    }
    val onSingleNodeTestClick = remember(group.name, proxyViewModel) {
        { proxyName: String ->
            proxyViewModel.testProxyDelay(group.name, proxyName)
        }
    }

    NodeSheetContent(
        group = group,
        isDelayTesting = isDelayTesting,
        testingProxyNames = testingProxyNames,
        onSelectProxy = onSelectProxy,
        onTestDelay = onTestDelay,
        onTestProxyDelay = onSingleNodeTestClick,
        sheetHeightFraction = sheetHeightFraction,
        listState = listState,
    )
}
