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

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.github.yumelira.yumebox.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon as MdIcon
import androidx.compose.material3.IconButton as MdIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as MdText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.core.model.TunnelState
import com.github.yumelira.yumebox.data.model.ProxySortMode
import com.github.yumelira.yumebox.domain.model.ProxyGroupInfo
import com.github.yumelira.yumebox.presentation.component.AppActionBottomSheet
import com.github.yumelira.yumebox.presentation.component.CenteredText
import com.github.yumelira.yumebox.presentation.component.LocalTopBarHazeState
import com.github.yumelira.yumebox.presentation.component.Md3ELoading
import com.github.yumelira.yumebox.presentation.component.SmallTopBar
import com.github.yumelira.yumebox.presentation.icon.Yume
import com.github.yumelira.yumebox.presentation.icon.yume.Chromium
import com.github.yumelira.yumebox.presentation.icon.yume.Folders
import com.github.yumelira.yumebox.presentation.icon.yume.Speed
import com.github.yumelira.yumebox.presentation.icon.yume.chevron
import com.github.yumelira.yumebox.presentation.icon.yume.`List-chevrons-up-down`
import com.github.yumelira.yumebox.presentation.screen.node.NodeCard
import com.github.yumelira.yumebox.presentation.screen.node.NodeGroupCard
import com.github.yumelira.yumebox.presentation.screen.node.NodeSortPopup
import com.github.yumelira.yumebox.presentation.theme.AnimationSpecs
import com.github.yumelira.yumebox.presentation.theme.LocalSpacing
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.github.yumelira.yumebox.presentation.viewmodel.ProxyViewModel
import dev.chrisbanes.haze.hazeSource
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior

@Composable
fun ProxyPager(
    mainInnerPadding: PaddingValues,
    onNavigateToProviders: (() -> Unit)?,
    onOpenPanel: (() -> Unit)?,
    isPageActive: Boolean,
    isProxyRunning: Boolean,
    onProxyStartRequested: (() -> Unit)? = null,
) {
    val proxyViewModel = koinViewModel<ProxyViewModel>()
    val proxyGroups by proxyViewModel.sortedProxyGroups.collectAsState()
    val testingGroupNames by proxyViewModel.testingGroupNames.collectAsState()
    val testingProxyNames by proxyViewModel.testingProxyNames.collectAsState()
    val sortMode by proxyViewModel.sortMode.collectAsState()
    val tunnelMode by proxyViewModel.tunnelMode.collectAsState()
    var displayTunnelMode by remember { mutableStateOf(tunnelMode) }
    val singleNodeTest by proxyViewModel.singleNodeTest.collectAsState()
    val groupScrollBehavior = MiuixScrollBehavior(snapAnimationSpec = null)
    val topBarHazeState = LocalTopBarHazeState.current

    var showSortPopup by remember { mutableStateOf(false) }
    var selectedGroupName by remember { mutableStateOf<String?>(null) }
    var pendingTestGroupName by remember { mutableStateOf<String?>(null) }
    var pendingTestProxyName by remember { mutableStateOf<String?>(null) }

    val selectedGroup = remember(proxyGroups, selectedGroupName) {
        proxyGroups.firstOrNull { it.name == selectedGroupName } ?: proxyGroups.firstOrNull()
    }
    val effectiveSelectedGroupName = selectedGroup?.name
    val onTestDelay = remember(effectiveSelectedGroupName, proxyViewModel) {
        { proxyViewModel.testDelay(effectiveSelectedGroupName) }
    }
    val onTestDelayAction: () -> Unit = remember(
        isProxyRunning,
        onProxyStartRequested,
        onTestDelay,
        effectiveSelectedGroupName,
    ) {
        {
            if (isProxyRunning) {
                onTestDelay()
            } else {
                pendingTestGroupName = effectiveSelectedGroupName
                pendingTestProxyName = null
                onProxyStartRequested?.invoke()
                Unit
            }
        }
    }
    val effectiveTestingGroupNames = remember(testingGroupNames, pendingTestGroupName) {
        pendingTestGroupName?.let { testingGroupNames + it } ?: testingGroupNames
    }
    val effectiveTestingProxyNames = remember(testingProxyNames, pendingTestProxyName) {
        pendingTestProxyName?.let { testingProxyNames + it } ?: testingProxyNames
    }
    val gridState = remember(displayTunnelMode, effectiveSelectedGroupName) { LazyGridState() }
    val modes = remember { listOf(TunnelState.Mode.Rule, TunnelState.Mode.Global, TunnelState.Mode.Direct) }

    LaunchedEffect(tunnelMode) {
        displayTunnelMode = tunnelMode
    }

    LaunchedEffect(proxyGroups, selectedGroupName) {
        when {
            proxyGroups.isEmpty() -> selectedGroupName = null
            selectedGroupName == null || proxyGroups.none { it.name == selectedGroupName } -> {
                selectedGroupName = proxyGroups.first().name
            }
        }
    }

    LaunchedEffect(sortMode, effectiveSelectedGroupName) {
        if (sortMode == ProxySortMode.BY_LATENCY) {
            gridState.scrollToItem(0)
        }
    }

    LaunchedEffect(isProxyRunning, pendingTestGroupName, pendingTestProxyName) {
        if (!isProxyRunning) return@LaunchedEffect
        val groupName = pendingTestGroupName ?: return@LaunchedEffect
        val proxyName = pendingTestProxyName
        if (proxyName != null) {
            proxyViewModel.testProxyDelay(groupName, proxyName)
        } else {
            proxyViewModel.testDelay(groupName)
        }
        delay(500)
        if (pendingTestGroupName == groupName && pendingTestProxyName == proxyName) {
            pendingTestGroupName = null
            pendingTestProxyName = null
        }
    }

    LaunchedEffect(isPageActive) {
        proxyViewModel.ensureCoreLoaded(isPageActive, source = "proxy_page")
    }

    DisposableEffect(proxyViewModel) {
        onDispose {
            proxyViewModel.ensureCoreLoaded(false, source = "proxy_page")
        }
    }

    Scaffold(
        topBar = {
            ProxyTopBar(
                title = MLang.Proxy.Title,
                scrollBehavior = groupScrollBehavior,
                onNavigateToProviders = onNavigateToProviders,
                onOpenPanel = onOpenPanel,
                onTestDelay = if (effectiveSelectedGroupName != null) onTestDelayAction else null,
                showSortPopup = showSortPopup,
                onShowSortPopupChange = { showSortPopup = it },
                sortMode = sortMode,
                onSortSelected = proxyViewModel::setSortMode,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .let { modifier -> if (topBarHazeState != null) modifier.hazeSource(state = topBarHazeState) else modifier }
        ) {
            ProxySurfboardContent(
                proxyGroups = proxyGroups,
                selectedGroup = selectedGroup,
                selectedGroupName = effectiveSelectedGroupName,
                testingGroupNames = effectiveTestingGroupNames,
                testingProxyNames = effectiveTestingProxyNames,
                gridState = gridState,
                modes = modes,
                tunnelMode = displayTunnelMode,
                onTunnelModeSelected = { mode ->
                    displayTunnelMode = mode
                    proxyViewModel.setTunnelMode(mode)
                },
                innerPadding = innerPadding,
                mainInnerPadding = mainInnerPadding,
                onGroupSelected = { selectedGroupName = it },
                onSelectProxy = { groupName, proxyName, onSuccess ->
                    proxyViewModel.selectProxy(groupName, proxyName, onSuccess = onSuccess)
                },
                onProxyStartRequested = onProxyStartRequested,
                isProxyRunning = isProxyRunning,
                onTestDelay = onTestDelayAction,
                onTestProxyDelay = { proxyName ->
                    if (!isProxyRunning) {
                        pendingTestGroupName = effectiveSelectedGroupName
                        pendingTestProxyName = proxyName
                        onProxyStartRequested?.invoke()
                    } else {
                        effectiveSelectedGroupName?.let { groupName ->
                            proxyViewModel.testProxyDelay(groupName, proxyName)
                        }
            }
                    Unit
                },
                singleNodeTestEnabled = singleNodeTest,
            )
        }
    }
}

@Composable
private fun ProxyTopBar(
    title: String,
    scrollBehavior: ScrollBehavior,
    onNavigateToProviders: (() -> Unit)?,
    onOpenPanel: (() -> Unit)?,
    onTestDelay: (() -> Unit)?,
    showSortPopup: Boolean,
    onShowSortPopupChange: (Boolean) -> Unit,
    sortMode: ProxySortMode,
    onSortSelected: (ProxySortMode) -> Unit,
) {
    SmallTopBar(
        title = title,
        scrollBehavior = scrollBehavior,
        navigationIconPadding = UiDp.dp12,
        actionIconPadding = UiDp.dp12,
        titlePadding = UiDp.dp12,
        enableHaze = false,
        navigationIcon = {
            Row(horizontalArrangement = Arrangement.spacedBy(UiDp.dp12)) {
                if (onNavigateToProviders != null) {
                    IconButton(onClick = onNavigateToProviders) {
                        Icon(Yume.Folders, contentDescription = MLang.Providers.Title)
                    }
                }
                if (onOpenPanel != null) {
                    IconButton(onClick = onOpenPanel) {
                        Icon(Yume.Chromium, contentDescription = MLang.Proxy.Action.Panel)
                    }
                }
            }
        },
        actions = {
            if (onTestDelay != null) {
                IconButton(
                    modifier = Modifier.padding(end = UiDp.dp12),
                    onClick = onTestDelay,
                ) {
                    Icon(Yume.Speed, contentDescription = MLang.Proxy.Action.Test)
                }
    }
            Box {
                MdIconButton(onClick = { onShowSortPopupChange(true) }) {
                    MdIcon(
                        imageVector = Yume.`List-chevrons-up-down`,
                        contentDescription = MLang.Proxy.Action.Sort,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                NodeSortPopup(
                    show = showSortPopup,
                    onDismiss = { onShowSortPopupChange(false) },
                    sortMode = sortMode,
                    onSortSelected = onSortSelected,
                )
            }
        },
    )
}

@Composable
private fun ProxySurfboardContent(
    proxyGroups: List<ProxyGroupInfo>,
    selectedGroup: ProxyGroupInfo?,
    selectedGroupName: String?,
    testingGroupNames: Set<String>,
    testingProxyNames: Set<String>,
    gridState: LazyGridState,
    modes: List<TunnelState.Mode>,
    tunnelMode: TunnelState.Mode,
    onTunnelModeSelected: (TunnelState.Mode) -> Unit,
    innerPadding: PaddingValues,
    mainInnerPadding: PaddingValues,
    onGroupSelected: (String) -> Unit,
    onSelectProxy: (String, String, (() -> Unit)?) -> Unit,
    onProxyStartRequested: (() -> Unit)?,
    isProxyRunning: Boolean,
    onTestDelay: () -> Unit,
    onTestProxyDelay: (String) -> Unit,
    singleNodeTestEnabled: Boolean,
) {
    val spacing = LocalSpacing.current
    val selectedName = selectedGroupName ?: selectedGroup?.name
    val isTesting = selectedName?.let(testingGroupNames::contains) == true
    val currentPage = modes.indexOf(tunnelMode).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = currentPage, pageCount = { modes.size })

    LaunchedEffect(tunnelMode) {
        val targetPage = modes.indexOf(tunnelMode).coerceAtLeast(0)
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding() + UiDp.dp8),
    ) {
        ProxyModeSelector(
            currentMode = tunnelMode,
            onModeSelected = onTunnelModeSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UiDp.dp12),
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            userScrollEnabled = false,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = UiDp.dp12,
                    end = UiDp.dp12,
                    top = UiDp.dp12,
                    bottom = mainInnerPadding.calculateBottomPadding() + spacing.space12,
                ),
                horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
                verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    ProxyGroupTabSection(
                        groups = proxyGroups,
                        selectedGroupName = selectedName,
                        onGroupSelected = onGroupSelected,
                    )
                }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                    AnimatedVisibility(
                        visible = isTesting,
                        enter = expandVertically(
                            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                            expandFrom = Alignment.Top,
                        ) + fadeIn(animationSpec = tween(durationMillis = AnimationSpecs.Proxy.RefreshIndicatorFadeDuration)),
                        exit = shrinkVertically(
                            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                            shrinkTowards = Alignment.Top,
                        ) + fadeOut(animationSpec = tween(durationMillis = AnimationSpecs.Proxy.RefreshIndicatorFadeDuration)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = UiDp.dp2, bottom = UiDp.dp4),
                            contentAlignment = Alignment.Center,
                        ) {
                            Md3ELoading()
                        }
                    }
                }

                if (selectedGroup == null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CenteredText(
                            firstLine = MLang.Proxy.Empty.NoNodes,
                            secondLine = MLang.Proxy.Empty.Hint,
                        )
                    }
                } else {
                    items(items = selectedGroup.proxies, key = { it.name }) { proxy ->
                        NodeCard(
                            proxy = proxy,
                            isSelected = proxy.name == selectedGroup.now,
                            onClick = { proxyName ->
                                if (selectedGroup.type == com.github.yumelira.yumebox.core.model.Proxy.Type.Selector) {
                                    onSelectProxy(selectedGroup.name, proxyName, null)
                                } else {
                                    onTestDelay()
                                }
                            },
                            isDelayTesting = isTesting,
                            isThisProxyTesting = proxy.name in testingProxyNames,
                            onSingleNodeTestClick = onTestProxyDelay,
                            showCountryFlag = true,
                            singleNodeTestEnabled = singleNodeTestEnabled,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProxyModeSelector(
    currentMode: TunnelState.Mode,
    onModeSelected: (TunnelState.Mode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = listOf(TunnelState.Mode.Rule, TunnelState.Mode.Global, TunnelState.Mode.Direct)
    val hapticFeedback = LocalHapticFeedback.current
    val shape = RoundedCornerShape(999.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f))
            .padding(UiDp.dp4),
        horizontalArrangement = Arrangement.spacedBy(UiDp.dp4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        modes.forEach { mode ->
            val selected = mode == currentMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(UiDp.dp40)
                    .clip(shape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent,
                    )
                    .clickable(enabled = !selected) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onModeSelected(mode)
                    },
                contentAlignment = Alignment.Center,
            ) {
                MdText(
                    text = mode.displayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun TunnelState.Mode.displayName(): String = when (this) {
    TunnelState.Mode.Direct -> MLang.Proxy.Mode.Direct
    TunnelState.Mode.Global -> MLang.Proxy.Mode.Global
    TunnelState.Mode.Rule -> MLang.Proxy.Mode.Rule
    TunnelState.Mode.Script -> "Script"
}

@Composable
private fun ProxyGroupTabSection(
    groups: List<ProxyGroupInfo>,
    selectedGroupName: String?,
    onGroupSelected: (String) -> Unit,
) {
    ProxyGroupTabs(
        groups = groups,
        selectedGroupName = selectedGroupName,
        onGroupSelected = onGroupSelected,
    )
}

@Composable
private fun ProxyGroupTabs(
    groups: List<ProxyGroupInfo>,
    selectedGroupName: String?,
    onGroupSelected: (String) -> Unit,
) {
    var showAllGroups by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiDp.dp8),
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(start = UiDp.dp0, end = UiDp.dp4),
        ) {
            items(items = groups, key = { it.name }) { group ->
                ProxyGroupTab(
                    title = group.name,
                    selected = group.name == selectedGroupName,
                    modifier = Modifier.widthIn(min = 84.dp),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                        onGroupSelected(group.name)
                    },
                )
            }
        }
        Box(
            modifier = Modifier
                .size(UiDp.dp36)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    showAllGroups = true
                }),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Yume.chevron,
                contentDescription = MLang.Proxy.Title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(UiDp.dp20)
                    .rotate(90f),
            )
        }
    }

    AppActionBottomSheet(
        show = showAllGroups,
        title = MLang.Proxy.Title,
        onDismissRequest = { showAllGroups = false },
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
            verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
            contentPadding = PaddingValues(bottom = UiDp.dp16),
        ) {
            items(items = groups, key = { it.name }) { group ->
                NodeGroupCard(
                    group = group,
                    isDelayTesting = false,
                    isSelected = group.name == selectedGroupName,
                    showTrailingIndicator = false,
                    onClick = {
                        onGroupSelected(group.name)
                        coroutineScope.launch {
                            delay(180)
                            showAllGroups = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ProxyGroupTab(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val colorScheme = MaterialTheme.colorScheme
    val activeColor = colorScheme.primary
    val defaultColor = colorScheme.onSurface
    val transition = updateTransition(targetState = selected, label = "proxy_group_tab_selection")
    val motionScheme = MaterialTheme.motionScheme
    val selectionScaleX = remember { Animatable(1f) }
    val animatedContainerColor by transition.animateColor(
        transitionSpec = { motionScheme.fastEffectsSpec() },
        label = "proxy_group_tab_container",
    ) { isSelected ->
        if (isSelected) activeColor.copy(alpha = 0.16f) else Color.Transparent
    }
    val animatedTextColor by transition.animateColor(
        transitionSpec = { motionScheme.fastEffectsSpec() },
        label = "proxy_group_tab_text",
    ) { isSelected ->
        if (isSelected) activeColor else defaultColor
    }
    LaunchedEffect(selected) {
        if (selected) {
            selectionScaleX.snapTo(1f)
            selectionScaleX.animateTo(1.06f, animationSpec = motionScheme.fastSpatialSpec())
            selectionScaleX.animateTo(1f, animationSpec = motionScheme.defaultSpatialSpec())
        } else {
            selectionScaleX.animateTo(1f, animationSpec = motionScheme.fastSpatialSpec())
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = selectionScaleX.value
                scaleY = 1f
            }
            .height(UiDp.dp40)
            .clip(shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(animatedContainerColor),
        )
        MdText(
            text = title,
            modifier = Modifier.padding(horizontal = UiDp.dp12),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = animatedTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


