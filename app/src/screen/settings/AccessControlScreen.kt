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



package com.amamiyakokoro.box.screen.settings

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalLayoutDirection
import com.amamiyakokoro.box.common.util.toast
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.*
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.AppTheme
import com.amamiyakokoro.box.presentation.theme.AppTheme.spacing
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>
fun AccessControlScreen(@Suppress("UNUSED_PARAMETER") navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val spacing = spacing
    val componentSizes = AppTheme.sizes
    val mainLikePadding = rememberStandalonePageMainPadding()
    val combinedBottomPadding = mainLikePadding.calculateBottomPadding() + spacing.space12
    val viewModel = koinViewModel<AccessControlViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val searchPlaceholder = stringResource(LocaleR.string.access_control_search_placeholder)
    val chinaApps = stringResource(LocaleR.string.access_control_settings_china_apps)
    val overseasApps = stringResource(LocaleR.string.access_control_settings_overseas_apps)
    val regionSelectResult = stringResource(LocaleR.string.access_control_settings_region_select_result)

    var showSettingsSheet by remember { mutableStateOf(false) }
    var searchStatus by remember {
        mutableStateOf(
            SearchStatus(
                label = searchPlaceholder,
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

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is AccessControlViewModel.AccessControlUiEffect.RegionalSelectionCompleted -> {
                        val label = if (effect.selectChina) {
                            chinaApps
                        } else {
                            overseasApps
                        }
                        context.toast(
                            regionSelectResult.format(
                                label,
                                effect.selectedCount,
                            )
                        )
                    }

                    is AccessControlViewModel.AccessControlUiEffect.ShowError -> context.toast(effect.message, copyable = true)
                    is AccessControlViewModel.AccessControlUiEffect.ShowMessage -> context.toast(effect.message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                currentSearchStatus.TopAppBarAnim {
                    TopBar(
                        title = stringResource(LocaleR.string.access_control_title),
                        actions = {
                            IconButton(
                                onClick = { showSettingsSheet = true }
                            ) {
                                Icon(
                                    AppMd3Icons.Action.Settings,
                                    contentDescription = stringResource(LocaleR.string.access_control_settings_title),
                                )
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
                        Text(
                            stringResource(LocaleR.string.access_control_app_list_loading),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
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
                            Title(
                                stringResource(LocaleR.string.access_control_app_list_title)
                                    .format(uiState.selectedPackages.size),
                            )
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
                    text = stringResource(LocaleR.string.access_control_search_empty),
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
            contentDescription = stringResource(LocaleR.string.component_editor_action_search),
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
    onSelectChinaApps: () -> Unit,
    onSelectNonChinaApps: () -> Unit,
    onImportPackages: (String) -> Int,
    onExportPackages: () -> String,
) {
    val context = LocalContext.current
    val clipboardManager = remember(context) {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    val sortModeEntries = remember { AccessControlViewModel.SortMode.entries }
    val importSuccess = stringResource(LocaleR.string.access_control_settings_import_success)
    val importFailed = stringResource(LocaleR.string.access_control_settings_import_failed)
    val exportSuccess = stringResource(LocaleR.string.access_control_settings_export_success)

    AppActionBottomSheet(
        show = show,
        modifier = Modifier,
        title = stringResource(LocaleR.string.access_control_settings_title),
        onDismissRequest = onDismiss,
        enableNestedScroll = true,
    ) {
        Column {
            Card {
                PreferenceSwitchItem(
                    title = stringResource(LocaleR.string.access_control_settings_show_system_apps),
                    checked = uiState.showSystemApps,
                    onCheckedChange = onShowSystemAppsChange,
                )
                PreferenceSwitchItem(
                    title = stringResource(LocaleR.string.access_control_settings_selected_first),
                    checked = uiState.selectedFirst,
                    onCheckedChange = onSelectedFirstChange,
                )
                PreferenceEnumItem(
                    title = stringResource(LocaleR.string.access_control_settings_sort_mode),
                    currentValue = uiState.sortMode,
                    items = sortModeEntries.map { stringResource(it.labelRes) },
                    values = sortModeEntries,
                    onValueChange = onSortModeChange,
                )
                DropdownActionPreference(
                    title = stringResource(LocaleR.string.access_control_settings_batch_operation),
                    items = listOf(
                        stringResource(LocaleR.string.access_control_settings_select_all),
                        stringResource(LocaleR.string.access_control_settings_deselect_all),
                        stringResource(LocaleR.string.access_control_settings_invert),
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
                    title = stringResource(LocaleR.string.access_control_settings_region_quick_select),
                    items = listOf(
                        stringResource(LocaleR.string.access_control_settings_china_apps),
                        stringResource(LocaleR.string.access_control_settings_overseas_apps),
                    ),
                    onSelectedIndexChange = { index ->
                        when (index) {
                            0 -> onSelectChinaApps()
                            1 -> onSelectNonChinaApps()
                        }
                    },
                )
                DropdownActionPreference(
                    title = stringResource(LocaleR.string.access_control_settings_import_export),
                    items = listOf(
                        stringResource(LocaleR.string.access_control_settings_import),
                        stringResource(LocaleR.string.access_control_settings_export),
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
                                    context.toast(importSuccess.format(count))
                                } else {
                                    context.toast(importFailed)
                                }
                            }

                            1 -> {
                                clipboardManager.setPrimaryClip(
                                    ClipData.newPlainText("packages", onExportPackages())
                                )
                                context.toast(
                                    exportSuccess.format(uiState.selectedPackages.size)
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
                Text(stringResource(LocaleR.string.access_control_button_cancel))
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    stringResource(LocaleR.string.access_control_button_confirm),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
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
    val placeholder = stringResource(LocaleR.string.access_control_settings_select_action)
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
    val iconBitmap = rememberInstalledAppIcon(packageName, bitmapSize)

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
