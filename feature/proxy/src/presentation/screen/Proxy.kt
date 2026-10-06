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

package com.amamiyakokoro.box.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon as MdIcon
import androidx.compose.material3.IconButton as MdIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text as MdText
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.model.Proxy
import com.amamiyakokoro.box.core.model.TunnelState
import com.amamiyakokoro.box.data.model.ProxySortMode
import com.amamiyakokoro.box.data.model.ThemeMode
import com.amamiyakokoro.box.domain.model.ProxyGroupInfo
import com.amamiyakokoro.box.presentation.component.CenteredText
import com.amamiyakokoro.box.presentation.component.LocalBottomBarScrollBehavior
import com.amamiyakokoro.box.presentation.component.Md3ELoading
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.presentation.component.rememberRetainedLazyGridState
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.screen.node.NodeCard
import com.amamiyakokoro.box.presentation.screen.node.ProxyGroupInfoCard
import com.amamiyakokoro.box.presentation.screen.node.NodeSortPopup
import com.amamiyakokoro.box.presentation.theme.AppMotion
import com.amamiyakokoro.box.presentation.theme.LocalSpacing
import com.amamiyakokoro.box.presentation.theme.UiDp
import com.amamiyakokoro.box.presentation.theme.YumeTheme
import com.amamiyakokoro.box.presentation.viewmodel.ProxyViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior

@Composable
fun ProxyPager(
    mainInnerPadding: PaddingValues,
    onNavigateToProviders: (() -> Unit)?,
    isPageActive: Boolean,
    isProxyRunning: Boolean,
    onProxyStartRequested: (() -> Unit)? = null,
    dataLifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    val proxyViewModel = koinViewModel<ProxyViewModel>()
    val proxyGroups by proxyViewModel.sortedProxyGroups.collectAsStateWithLifecycle(lifecycleOwner = dataLifecycleOwner)
    val testingGroupNames by proxyViewModel.testingGroupNames.collectAsStateWithLifecycle()
    val testingProxyNames by proxyViewModel.testingProxyNames.collectAsStateWithLifecycle()
    val sortMode by proxyViewModel.sortMode.collectAsStateWithLifecycle(lifecycleOwner = dataLifecycleOwner)
    val tunnelMode by proxyViewModel.tunnelMode.collectAsStateWithLifecycle(lifecycleOwner = dataLifecycleOwner)
    var displayTunnelMode by remember { mutableStateOf(tunnelMode) }
    val singleNodeTest by proxyViewModel.singleNodeTest.collectAsStateWithLifecycle(lifecycleOwner = dataLifecycleOwner)
    val groupScrollBehavior = MiuixScrollBehavior(snapAnimationSpec = null)

    var showSortPopup by remember { mutableStateOf(false) }
    var selectedGroupName by rememberSaveable { mutableStateOf<String?>(null) }
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
            }
        }
    }
    val effectiveTestingGroupNames = remember(testingGroupNames, pendingTestGroupName) {
        pendingTestGroupName?.let { testingGroupNames + it } ?: testingGroupNames
    }
    val effectiveTestingProxyNames = remember(testingProxyNames, pendingTestProxyName) {
        pendingTestProxyName?.let { testingProxyNames + it } ?: testingProxyNames
    }
    val gridState = rememberRetainedLazyGridState(
        "main_proxy_groups_${displayTunnelMode.name}"
    )
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

    var previousSortMode by rememberSaveable { mutableStateOf<ProxySortMode?>(null) }
    LaunchedEffect(sortMode, gridState) {
        if (sortMode == ProxySortMode.BY_LATENCY && sortMode != previousSortMode) {
            gridState.scrollToItem(0)
        }
        previousSortMode = sortMode
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

    LifecycleStartEffect(proxyViewModel, isPageActive) {
        proxyViewModel.ensureCoreLoaded(isPageActive, source = "proxy_page")
        onStopOrDispose {
            proxyViewModel.ensureCoreLoaded(false, source = "proxy_page")
        }
    }

    Scaffold(
        topBar = {
            ProxyTopBar(
                title = stringResource(LocaleR.string.proxy_title),
                scrollBehavior = groupScrollBehavior,
                onNavigateToProviders = onNavigateToProviders,
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
        ) {
            ProxySurfboardContent(
                proxyGroups = proxyGroups,
                selectedGroup = selectedGroup,
                selectedGroupName = selectedGroupName,
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
    onTestDelay: (() -> Unit)?,
    showSortPopup: Boolean,
    onShowSortPopupChange: (Boolean) -> Unit,
    sortMode: ProxySortMode,
    onSortSelected: (ProxySortMode) -> Unit,
) {
    TopBar(
        title = title,
        scrollBehavior = scrollBehavior,
        actionIconPadding = UiDp.dp12,
        titlePadding = UiDp.dp12,
        actions = {
            if (onTestDelay != null) {
                MdIconButton(onClick = onTestDelay, modifier = Modifier.size(48.dp)) {
                    MdIcon(AppMd3Icons.Action.SpeedTest, stringResource(LocaleR.string.proxy_action_test),
                        modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (onNavigateToProviders != null) {
                MdIconButton(onClick = onNavigateToProviders, modifier = Modifier.size(48.dp)) {
                    MdIcon(AppMd3Icons.Proxy.Profiles, stringResource(LocaleR.string.providers_title),
                        modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box {
                MdIconButton(onClick = { onShowSortPopupChange(true) }, modifier = Modifier.size(48.dp)) {
                    MdIcon(AppMd3Icons.Action.Sort, stringResource(LocaleR.string.proxy_action_sort),
                        modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val bottomBarScrollBehavior = LocalBottomBarScrollBehavior.current
    val isGridAtTop by remember(gridState) {
        derivedStateOf {
            gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isGridAtTop, bottomBarScrollBehavior) {
        if (isGridAtTop) {
            bottomBarScrollBehavior?.showBottomBar()
        }
    }

    var optimisticSelectedProxyName by remember(selectedGroup?.name) { mutableStateOf<String?>(null) }
    val effectiveNow = optimisticSelectedProxyName ?: selectedGroup?.now
    var expandedGroupName by rememberSaveable(tunnelMode) { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedGroup?.now) {
        if (selectedGroup?.now == optimisticSelectedProxyName) {
            optimisticSelectedProxyName = null
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding()),
    ) {
        val columnCount = if (maxWidth < 380.dp || LocalDensity.current.fontScale >= 1.3f) 1 else 2
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .let { modifier ->
                        if (bottomBarScrollBehavior != null) {
                            modifier.nestedScroll(bottomBarScrollBehavior.nestedScrollConnection)
                        } else {
                            modifier
                        }
                    },
                contentPadding = PaddingValues(
                    start = UiDp.dp16,
                    end = UiDp.dp16,
                    top = 72.dp,
                    bottom = mainInnerPadding.calculateBottomPadding() + spacing.space12,
                ),
                horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
                verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
            ) {
                item(key = "groups_header", span = { GridItemSpan(maxLineSpan) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        MdText(
                            stringResource(LocaleR.string.proxy_groups_title),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        AnimatedVisibility(visible = testingGroupNames.isNotEmpty()) {
                            Md3ELoading(modifier = Modifier.size(20.dp))
                        }
                    }
                }
                proxyGroups.forEach { group ->
                    item(key = "group:${group.name}", span = { GridItemSpan(maxLineSpan) }, contentType = "group") {
                        ProxyGroupInfoCard(
                            group = group,
                            currentProxyName = if (group.name == selectedName) effectiveNow ?: group.now else group.now,
                            isSelected = group.name == selectedName,
                            isExpanded = group.name == expandedGroupName && group.name == selectedName,
                            onClick = {
                                expandedGroupName = group.name.takeUnless { it == expandedGroupName && it == selectedName }
                                onGroupSelected(group.name)
                            },
                            isTesting = group.name in testingGroupNames,
                        )
                    }
                    if (selectedGroup != null && selectedGroup.name == expandedGroupName &&
                        group.name == expandedGroupName
                    ) {
                        item(key = "nodes_header:${selectedGroup.name}", span = { GridItemSpan(maxLineSpan) }) {
                            MdText(
                                stringResource(LocaleR.string.proxy_nodes_title),
                                modifier = Modifier.padding(top = 4.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (selectedGroup.proxies.isEmpty()) {
                            item(key = "empty_nodes:${selectedGroup.name}", span = { GridItemSpan(maxLineSpan) }) {
                                CenteredText(
                                    firstLine = stringResource(LocaleR.string.proxy_empty_no_nodes),
                                    secondLine = stringResource(LocaleR.string.proxy_empty_hint),
                                )
                            }
                        }
                        items(selectedGroup.proxies, key = { "node:${selectedGroup.name}:${it.name}" }, contentType = { "node" }) { proxy ->
                            NodeCard(
                                proxy = proxy,
                                isSelected = proxy.name == effectiveNow,
                                onClick = { proxyName ->
                                    if (selectedGroup.type == com.amamiyakokoro.box.core.model.Proxy.Type.Selector) {
                                        optimisticSelectedProxyName = proxyName
                                        onSelectProxy(
                                            selectedGroup.name,
                                            proxyName,
                                            { optimisticSelectedProxyName = null },
                                        )
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
                if (proxyGroups.isEmpty()) {
                    item(key = "empty_groups", span = { GridItemSpan(maxLineSpan) }) {
                        CenteredText(
                            firstLine = stringResource(LocaleR.string.proxy_empty_no_nodes),
                            secondLine = stringResource(LocaleR.string.proxy_empty_hint),
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(top = UiDp.dp12, bottom = UiDp.dp12, start = UiDp.dp16, end = UiDp.dp16),
            contentAlignment = Alignment.Center,
        ) {
            ProxyModeSelector(
                currentMode = tunnelMode,
                onModeSelected = onTunnelModeSelected,
                modifier = Modifier.fillMaxWidth(),
            )
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
    val selectedIndex = modes.indexOf(currentMode).coerceAtLeast(0)
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    val shape = RoundedCornerShape(999.dp)
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val indicatorWidthPx = remember(containerWidthPx, modes.size) {
        (containerWidthPx.toFloat() / modes.size).coerceAtLeast(0f)
    }
    val indicatorWidth = with(density) { indicatorWidthPx.toDp() }
    val indicatorOffset by animateDpAsState(
        targetValue = with(density) { (indicatorWidthPx * selectedIndex).toDp() },
        animationSpec = AppMotion.indicator(),
        label = "proxy_mode_indicator_offset",
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(UiDp.dp4),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(UiDp.dp40)
                .onSizeChanged { containerWidthPx = it.width },
        ) {
            if (indicatorWidthPx > 0f) {
                Box(
                    modifier = Modifier
                        .graphicsLayer { translationX = indicatorOffset.toPx() }
                        .width(indicatorWidth)
                        .fillMaxHeight()
                        .clip(shape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                )
            }

            Row(
                modifier = Modifier.matchParentSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
            modes.forEach { mode ->
                val selected = mode == currentMode
                val textColor by animateColorAsState(
                    targetValue = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = AppMotion.color(),
                    label = "proxy_mode_text_color",
                )
                val textScale by animateFloatAsState(
                    targetValue = if (selected) 1.03f else 1f,
                    animationSpec = AppMotion.fastSpatial(),
                    label = "proxy_mode_text_scale",
                )
                val itemInteractionSource = remember(mode) { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(UiDp.dp40)
                        .clip(shape)
                        .clickable(
                            interactionSource = itemInteractionSource,
                            indication = null,
                            enabled = !selected,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                onModeSelected(mode)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    MdText(
                        text = mode.displayName(),
                        modifier = Modifier.graphicsLayer {
                            scaleX = textScale
                            scaleY = textScale
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
}

@Composable
private fun TunnelState.Mode.displayName(): String = when (this) {
    TunnelState.Mode.Direct -> stringResource(LocaleR.string.proxy_mode_direct)
    TunnelState.Mode.Global -> stringResource(LocaleR.string.proxy_mode_global)
    TunnelState.Mode.Rule -> stringResource(LocaleR.string.proxy_mode_rule)
    TunnelState.Mode.Script -> "Script"
}

@Preview(name = "Proxy groups / grid", widthDp = 400, heightDp = 800)
@Preview(name = "Compact proxies / narrow", widthDp = 320, heightDp = 720)
@Preview(name = "Compact proxies / dark", widthDp = 320, heightDp = 720, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Compact proxies / large text", widthDp = 320, heightDp = 720, fontScale = 1.5f)
@Composable
private fun ProxyUiPreview(themeMode: ThemeMode? = null) {
    val types = listOf(Proxy.Type.Vmess, Proxy.Type.Vless, Proxy.Type.Trojan, Proxy.Type.Hysteria2,
        Proxy.Type.Shadowsocks, Proxy.Type.WireGuard, Proxy.Type.TrustTunnel, Proxy.Type.Unknown)
    val nodes = remember {
        types.mapIndexed { index, type ->
            val name = if (index == 0) "Direct-JP.ririmu" else "Direct-JP.a-very-long-node-name-$index"
            Proxy(name, name, "", type, if (index == 7) -1 else 911 + index * 71)
        }
    }
    var groups by remember {
        mutableStateOf(listOf("Direct-JP", "Direct-US", "Direct-SG", "Direct+", "A very long proxy group name")
            .mapIndexed { index, name ->
                val groupTypes = listOf(Proxy.Type.Selector, Proxy.Type.Fallback, Proxy.Type.LoadBalance, Proxy.Type.URLTest, Proxy.Type.Smart)
                val groupNodes = nodes.take(if (index == 0) 7 else nodes.size).map { node ->
                    node.copy(name = node.name.replace("Direct-JP", name), title = node.title.replace("Direct-JP", name))
                }
                ProxyGroupInfo(name, groupTypes[index], groupNodes, groupNodes.first().name)
            })
    }
    var selectedName by remember { mutableStateOf(groups.first().name) }
    var mode by remember { mutableStateOf(TunnelState.Mode.Rule) }
    var testing by remember { mutableStateOf(false) }
    val selectedGroup = groups.first { it.name == selectedName }
    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val scrollBehavior = MiuixScrollBehavior(snapAnimationSpec = null)
    YumeTheme(themeMode = themeMode) {
        Scaffold(topBar = {
            ProxyTopBar("Proxy", scrollBehavior, {}, { testing = !testing }, false, {}, ProxySortMode.BY_LATENCY, {})
        }) { padding ->
            ProxySurfboardContent(groups, selectedGroup, selectedName,
                if (testing) setOf(selectedName) else emptySet(), emptySet(), gridState,
                listOf(TunnelState.Mode.Rule, TunnelState.Mode.Global, TunnelState.Mode.Direct),
                mode, { mode = it }, padding, PaddingValues(0.dp), { selectedName = it },
                { groupName, nodeName, success ->
                    groups = groups.map { if (it.name == groupName) it.copy(now = nodeName) else it }
                    success?.invoke()
                }, null, true, { testing = !testing }, {}, true)
        }
    }
}
