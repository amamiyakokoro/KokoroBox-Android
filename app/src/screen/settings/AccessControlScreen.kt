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



package com.github.yumelira.yumebox.screen.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.graphics.drawable.toBitmap
import com.github.yumelira.yumebox.common.util.toast
import com.github.yumelira.yumebox.presentation.component.*
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DropdownPreference
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import com.github.yumelira.yumebox.presentation.theme.AppTheme.spacing
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>
fun AccessControlScreen(@Suppress("UNUSED_PARAMETER") navigator: DestinationsNavigator) {
    val layoutDirection = LocalLayoutDirection.current
    val spacing = spacing
    val componentSizes = AppTheme.sizes
    val mainLikePadding = rememberStandalonePageMainPadding()
    val combinedBottomPadding = mainLikePadding.calculateBottomPadding() + spacing.space12
    val viewModel = koinViewModel<AccessControlViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }
    var searchStatus by remember {
        mutableStateOf(
            SearchStatus(
                label = MLang.AccessControl.Search.Placeholder,
                searchText = uiState.searchQuery,
            )
        )
    }
    val dynamicTopPadding = spacing.space12
    val listStartPadding = spacing.screenHorizontal
    val listEndPadding = spacing.screenHorizontal
    val currentSearchStatus = remember(searchStatus, filteredApps) {
        searchStatus.copy(
            resultStatus = when {
                searchStatus.searchText.isBlank() -> SearchStatus.ResultStatus.DEFAULT
                filteredApps.isEmpty() -> SearchStatus.ResultStatus.EMPTY
                else -> SearchStatus.ResultStatus.SHOW
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onPermissionResult()
            }
        }
    )

    LaunchedEffect(uiState.needsMiuiPermission) {
        if (uiState.needsMiuiPermission) {
            permissionLauncher.launch("com.android.permission.GET_INSTALLED_APPS")
        }
    }

    LaunchedEffect(searchStatus.searchText) {
        if (searchStatus.searchText != uiState.searchQuery) {
            viewModel.onSearchQueryChange(searchStatus.searchText)
        }
    }

    LaunchedEffect(uiState.searchQuery) {
        if (searchStatus.searchText != uiState.searchQuery) {
            searchStatus = searchStatus.copy(searchText = uiState.searchQuery)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                currentSearchStatus.TopAppBarAnim {
                    TopBar(
                        title = MLang.AccessControl.Title,
                        actions = {
                            IconButton(
                                onClick = { showSettingsSheet = true }
                            ) {
                                Icon(AppMd3Icons.Action.Settings, contentDescription = MLang.AccessControl.Settings.Title)
                            }
                        }
                    )
                }
            },
        ) { innerPadding ->
        val combinedInnerPadding = combinePaddingValues(innerPadding, mainLikePadding)
        currentSearchStatus.SearchBox(
            onSearchStatusChange = { searchStatus = it },
            searchBarTopPadding = dynamicTopPadding,
            startPadding = listStartPadding,
            endPadding = listEndPadding,
            contentPadding = PaddingValues(
                top = combinedInnerPadding.calculateTopPadding(),
                start = combinedInnerPadding.calculateStartPadding(layoutDirection),
                end = combinedInnerPadding.calculateEndPadding(layoutDirection),
                ),
            collapseBar = { collapsedSearchStatus, topPadding, innerPadding ->
                FloatingAccessControlSearchBar(
                    label = collapsedSearchStatus.label,
                    searchBarTopPadding = topPadding,
                    startPadding = listStartPadding,
                    endPadding = listEndPadding,
                    innerPadding = innerPadding,
                )
            },
            ) { boxHeight ->
                if (uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = boxHeight + spacing.space6,
                                start = listStartPadding,
                                end = listEndPadding,
                                bottom = combinedBottomPadding,
                            ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(MLang.AccessControl.AppList.Loading, color = MaterialTheme.colorScheme.onSurface)
                    }
                } else {
                    ScreenLazyColumn(
                        innerPadding = combinedInnerPadding,
                        contentPadding = PaddingValues(
                            top = boxHeight + spacing.space6,
                            bottom = combinedBottomPadding,
                            start = listStartPadding,
                            end = listEndPadding,
                        ),
                    ) {
                        item {
                            Title(MLang.AccessControl.AppList.Title.format(uiState.selectedPackages.size))
                        }

                        items(
                            items = filteredApps,
                            key = { it.packageName }
                        ) { app ->
                            AppCard(
                                app = app,
                                selected = app.packageName in uiState.selectedPackages,
                                onSelectionChange = { checked ->
                                    viewModel.onAppSelectionChange(app.packageName, checked)
                                },
                                onClick = {
                                    viewModel.onAppSelectionChange(
                                        app.packageName,
                                        app.packageName !in uiState.selectedPackages,
                                    )
                                }
                            )
                        }
                    }
                }
            }

            AccessControlSettingsSheet(
                show = showSettingsSheet,
                uiState = uiState,
                onDismiss = { showSettingsSheet = false },
                onShowSystemAppsChange = viewModel::onShowSystemAppsChange,
                onSelectedFirstChange = viewModel::onSelectedFirstChange,
                onSortModeChange = viewModel::onSortModeChange,
                onSelectAll = viewModel::selectAll,
                onDeselectAll = viewModel::deselectAll,
                onInvertSelection = viewModel::invertSelection,
                onSelectChinaApps = viewModel::selectChinaAppsInCurrentList,
                onSelectNonChinaApps = viewModel::selectNonChinaAppsInCurrentList,
                onImportPackages = viewModel::importPackages,
                onExportPackages = viewModel::exportPackages,
            )
        }

        currentSearchStatus.SearchPager(
            onSearchStatusChange = { searchStatus = it },
            searchBarTopPadding = dynamicTopPadding,
            startPadding = listStartPadding,
            endPadding = listEndPadding,
            emptyResult = {
                SearchEmptyState(
                    text = MLang.AccessControl.Search.Empty,
                    modifier = Modifier.padding(bottom = combinedBottomPadding),
                )
            },
        ) {
            val searchListState = androidx.compose.foundation.lazy.rememberLazyListState()
            LaunchedEffect(currentSearchStatus.searchText) {
                if (currentSearchStatus.searchText.isNotBlank()) {
                    searchListState.scrollToItem(0)
                }
            }
            LazyColumn(
                state = searchListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = listStartPadding,
                    end = listEndPadding,
                    top = spacing.space6,
                    bottom = combinedBottomPadding,
                ),
            ) {
                items(
                    items = filteredApps,
                    key = { it.packageName }
                ) { app ->
                    AppCard(
                        app = app,
                        selected = app.packageName in uiState.selectedPackages,
                        onSelectionChange = { checked ->
                            viewModel.onAppSelectionChange(app.packageName, checked)
                        },
                        onClick = {
                            viewModel.onAppSelectionChange(
                                app.packageName,
                                app.packageName !in uiState.selectedPackages,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingAccessControlSearchBar(
    label: String,
    searchBarTopPadding: androidx.compose.ui.unit.Dp,
    startPadding: androidx.compose.ui.unit.Dp,
    endPadding: androidx.compose.ui.unit.Dp,
    innerPadding: PaddingValues,
) {
    val layoutDirection = LocalLayoutDirection.current
    val spacing = spacing
    val componentSizes = AppTheme.sizes

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding)
            .padding(
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection),
            )
            .padding(top = searchBarTopPadding, bottom = spacing.space6)
            .shadow(
                elevation = spacing.space8,
                shape = CircleShape,
                clip = false,
            )
            .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
            .heightIn(min = componentSizes.searchFieldMinHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppMd3Icons.Action.Search,
            contentDescription = MLang.Component.Editor.Action.Search,
            modifier = Modifier
                .size(componentSizes.searchIconTouchTarget)
                .padding(start = spacing.space16, end = spacing.space8),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AccessControlSettingsSheet(
    show: Boolean,
    uiState: AccessControlViewModel.UiState,
    onDismiss: () -> Unit,
    onShowSystemAppsChange: (Boolean) -> Unit,
    onSelectedFirstChange: (Boolean) -> Unit,
    onSortModeChange: (AccessControlViewModel.SortMode) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onInvertSelection: () -> Unit,
    onSelectChinaApps: () -> Int,
    onSelectNonChinaApps: () -> Int,
    onImportPackages: (String) -> Int,
    onExportPackages: () -> String,
) {
    val context = LocalContext.current
    val clipboardManager = remember(context) {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    val sortModeEntries = remember { AccessControlViewModel.SortMode.entries }

    AppActionBottomSheet(
        show = show,
        modifier = Modifier,
        title = MLang.AccessControl.Settings.Title,
        onDismissRequest = onDismiss,
        enableNestedScroll = true,
    ) {
        Column {
            Card {
                PreferenceSwitchItem(
                    title = MLang.AccessControl.Settings.ShowSystemApps,
                    checked = uiState.showSystemApps,
                    onCheckedChange = onShowSystemAppsChange,
                )
                PreferenceSwitchItem(
                    title = MLang.AccessControl.Settings.SelectedFirst,
                    checked = uiState.selectedFirst,
                    onCheckedChange = onSelectedFirstChange,
                )
                PreferenceEnumItem(
                    title = MLang.AccessControl.Settings.SortMode,
                    currentValue = uiState.sortMode,
                    items = sortModeEntries.map { it.displayName },
                    values = sortModeEntries,
                    onValueChange = onSortModeChange,
                )
                DropdownActionPreference(
                    title = MLang.AccessControl.Settings.BatchOperation,
                    items = listOf(
                        MLang.AccessControl.Settings.SelectAll,
                        MLang.AccessControl.Settings.DeselectAll,
                        MLang.AccessControl.Settings.Invert,
                    ),
                    onSelectedIndexChange = { index ->
                        when (index) {
                            0 -> onSelectAll()
                            1 -> onDeselectAll()
                            2 -> onInvertSelection()
                        }
                    },
                )
                DropdownActionPreference(
                    title = MLang.AccessControl.Settings.RegionQuickSelect,
                    items = listOf(
                        MLang.AccessControl.Settings.ChinaApps,
                        MLang.AccessControl.Settings.OverseasApps,
                    ),
                    onSelectedIndexChange = { index ->
                        val (label, selectedCount) = when (index) {
                            0 -> MLang.AccessControl.Settings.ChinaApps to onSelectChinaApps()
                            1 -> MLang.AccessControl.Settings.OverseasApps to onSelectNonChinaApps()
                            else -> "" to 0
                        }
                        context.toast(
                            MLang.AccessControl.Settings.RegionSelectResult.format(label, selectedCount)
                        )
                    },
                )
                DropdownActionPreference(
                    title = MLang.AccessControl.Settings.ImportExport,
                    items = listOf(
                        MLang.AccessControl.Settings.Import,
                        MLang.AccessControl.Settings.Export,
                    ),
                    onSelectedIndexChange = { index ->
                        when (index) {
                            0 -> {
                                val text = clipboardManager.primaryClip
                                    ?.takeIf { it.itemCount > 0 }
                                    ?.getItemAt(0)
                                    ?.text
                                    ?.toString()
                                    .orEmpty()
                                if (text.isNotEmpty()) {
                                    val count = onImportPackages(text)
                                    context.toast(MLang.AccessControl.Settings.ImportSuccess.format(count))
                                } else {
                                    context.toast(MLang.AccessControl.Settings.ImportFailed)
                                }
                            }

                            1 -> {
                                clipboardManager.setPrimaryClip(
                                    ClipData.newPlainText("packages", onExportPackages())
                                )
                                context.toast(
                                    MLang.AccessControl.Settings.ExportSuccess.format(uiState.selectedPackages.size)
                                )
                            }
                        }
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.space24))

        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space12)) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            ) {
                Text(MLang.AccessControl.Button.Cancel)
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(MLang.AccessControl.Button.Confirm, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun DropdownActionPreference(
    title: String,
    items: List<String>,
    onSelectedIndexChange: (Int) -> Unit,
) {
    val placeholder = MLang.AccessControl.Settings.SelectAction
    val displayItems = remember(items) { listOf(placeholder) + items }

    YumeMd3DropdownPreference(
        title = title,
        items = displayItems,
        selectedIndex = 0,
        onSelectedIndexChange = { index ->
            if (index > 0) {
                onSelectedIndexChange(index - 1)
            }
        },
    )
}

@Composable
private fun AppCard(
    app: AccessControlViewModel.AppInfo,
    selected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val spacing = spacing
    val componentSizes = AppTheme.sizes

    Card(
        modifier = Modifier.padding(vertical = spacing.space4),
        applyHorizontalPadding = false,
    ) {
        PreferenceListItem(
            title = app.label,
            summary = app.packageName,
            startAction = {
                AppIcon(
                    packageName = app.packageName,
                    contentDescription = app.label,
                    imageSize = componentSizes.iconBadgeLarge,
                    bitmapSize = 80,
                    modifier = Modifier.padding(end = spacing.space12)
                )
            },
            endActions = {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { checked -> onSelectionChange(checked) }
                )
            },
            onClick = onClick
        )
    }
}

@Composable
private fun AppIcon(
    packageName: String,
    contentDescription: String,
    imageSize: androidx.compose.ui.unit.Dp,
    bitmapSize: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = packageName, key2 = bitmapSize) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager
                    .getApplicationIcon(packageName)
                    .toBitmap(width = bitmapSize, height = bitmapSize)
                    .asImageBitmap()
            }.getOrNull()
        }
    }

    val bitmap = iconBitmap ?: return
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        modifier = modifier.size(imageSize)
    )
}

@Composable
private fun SearchEmptyState(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
