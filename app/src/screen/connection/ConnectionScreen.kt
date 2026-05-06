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



package com.github.yumelira.yumebox.screen.connection

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.feature.meta.presentation.component.ConnectionCard
import com.github.yumelira.yumebox.feature.meta.presentation.component.ConnectionDetailSheet
import com.github.yumelira.yumebox.feature.meta.presentation.component.TabRowWithContour
import com.github.yumelira.yumebox.feature.meta.presentation.viewmodel.ConnectionSort
import com.github.yumelira.yumebox.feature.meta.presentation.viewmodel.ConnectionTab
import com.github.yumelira.yumebox.feature.meta.presentation.viewmodel.ConnectionViewModel
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import org.koin.androidx.compose.koinViewModel

private val SortModes = listOf(
    ConnectionSort.Time,
    ConnectionSort.Upload,
    ConnectionSort.Download,
    ConnectionSort.Host,
)

private fun ConnectionSort.getDisplayName(): String = when (this) {
    ConnectionSort.Time -> MLang.Connection.Sort.Time
    ConnectionSort.Upload -> MLang.Connection.Sort.Upload
    ConnectionSort.Download -> MLang.Connection.Sort.Download
    ConnectionSort.Host -> MLang.Connection.Sort.Host
}

@Destination<RootGraph>
@Composable
fun ConnectionScreen(
    navigator: DestinationsNavigator,
) {
    val viewModel = koinViewModel<ConnectionViewModel>()
    val state by viewModel.state.collectAsState()
    val filteredConnections by viewModel.filteredConnections.collectAsState()
    val spacing = AppTheme.spacing

    var showSearchBar by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(state.searchQuery) }
    var showSortPopup by remember { mutableStateOf(false) }

    var selectedConnection by remember { mutableStateOf<com.github.yumelira.yumebox.core.model.ConnectionInfo?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    val tabs = listOf(MLang.Connection.Tab.Active, MLang.Connection.Tab.Closed)
    var selectedTabIndex by rememberSaveable(state.selectedTab) {
        mutableIntStateOf(
            when (state.selectedTab) {
                ConnectionTab.ACTIVE -> 0
                ConnectionTab.CLOSED -> 1
            }
        )
    }
    val selectedSortIndex = remember(state.sortBy) {
        SortModes.indexOf(state.sortBy).coerceAtLeast(0)
    }
    val emptyStateText =
        when {
            state.isLoading -> MLang.Connection.Loading
            state.searchQuery.isNotEmpty() -> MLang.Connection.NoResults
            else -> MLang.Connection.Empty
        }

    LaunchedEffect(selectedTabIndex) {
        val tab = if (selectedTabIndex == 0) ConnectionTab.ACTIVE else ConnectionTab.CLOSED
        viewModel.setTab(tab)
    }

    LaunchedEffect(searchText) {
        if (searchText != state.searchQuery) {
            viewModel.setSearchQuery(searchText)
        }
    }

    LaunchedEffect(state.searchQuery) {
        if (searchText != state.searchQuery) {
            searchText = state.searchQuery
        }
    }

    LaunchedEffect(showDetailSheet, selectedConnection) {
        if (showDetailSheet && selectedConnection == null) {
            showDetailSheet = false
        }
    }
    LaunchedEffect(showDetailSheet) {
        if (showDetailSheet) {
            viewModel.stopPolling()
        } else {
            viewModel.startPolling()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = MLang.Connection.Title,
                actions = {
                    Box {
                        IconButton(
                            modifier = Modifier.padding(end = spacing.space12),
                            onClick = { showSortPopup = true }) {
                            Icon(
                                imageVector = AppMd3Icons.Connection.SortBy,
                                contentDescription = MLang.Connection.SortBy.trimEnd(':', '：'),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        DropdownMenu(
                            expanded = showSortPopup,
                            onDismissRequest = { showSortPopup = false },
                            modifier = Modifier.widthIn(min = 180.dp),
                            offset = DpOffset(x = (-144).dp, y = spacing.space4),
                            shape = RoundedCornerShape(20.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ) {
                            SortModes.forEachIndexed { index, mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mode.getDisplayName(),
                                            color = if (selectedSortIndex == index) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                        )
                                    },
                                    onClick = {
                                        if (mode != state.sortBy) viewModel.setSortBy(mode)
                                        showSortPopup = false
                                    },
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = AppMd3Icons.Connection.SearchConnection,
                            contentDescription = MLang.Connection.Search,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(
            innerPadding = innerPadding,
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                top = innerPadding.calculateTopPadding(),
                bottom = mainLikePadding.calculateBottomPadding() + spacing.space12,
            ),
        ) {

            item {
                TabRowWithContour(
                    modifier = Modifier.padding(top = spacing.space20),
                    tabs = tabs,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it },
                )
            }

            item {
                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = expandVertically(
                        animationSpec = tween(200, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Top,
                    ) + fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)),
                    exit = shrinkVertically(
                        animationSpec = tween(200, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.Top,
                    ) + fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing)),
                ) {
                    ConnectionSearchField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.space8),
                    )
                }
            }

            if (filteredConnections.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.space32),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = emptyStateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {

                items(
                    items = filteredConnections,
                    key = { it.id },
                ) { connection ->
                    ConnectionCard(
                        connectionInfo = connection,
                        onClick = {
                            selectedConnection = connection
                            showDetailSheet = true
                        },
                        modifier = Modifier,
                    )
                }
            }
        }

        ConnectionDetailSheet(
            show = showDetailSheet,
            connectionInfo = selectedConnection,
            canInterrupt = state.selectedTab == ConnectionTab.ACTIVE,
            onInterruptConnection = { id -> viewModel.closeConnection(id) },
            onDismiss = { showDetailSheet = false },
            onDismissFinished = { selectedConnection = null },
        )
    }
}

@Composable
private fun ConnectionSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    val componentSizes = AppTheme.sizes

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier
            .shadow(
                elevation = spacing.space8,
                shape = CircleShape,
                clip = false,
            )
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .heightIn(min = componentSizes.searchFieldMinHeight),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = spacing.space16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = AppMd3Icons.Connection.SearchConnection,
                    contentDescription = MLang.Component.Editor.Action.Search,
                    modifier = Modifier
                        .size(componentSizes.searchIconTouchTarget)
                        .padding(start = spacing.space16, end = spacing.space8),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = MLang.Connection.SearchHint,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}
