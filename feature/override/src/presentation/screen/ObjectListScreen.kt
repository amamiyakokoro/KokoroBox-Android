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
import com.amamiyakokoro.box.presentation.theme.UiDp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.*
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.util.*
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.material3.Scaffold

private const val ObjectListReorderHeaderCount = 2

@Composable
fun OverrideObjectListEditorScreen(
    navigator: DestinationsNavigator,
    onOpenProxyDraftEditor: (
        title: String,
        initialValue: OverrideProxyDraft?,
        onConfirm: (OverrideProxyDraft) -> Unit,
    ) -> Unit,
    onOpenProxyGroupDraftEditor: (
        title: String,
        initialValue: OverrideProxyGroupDraft?,
        onConfirm: (OverrideProxyGroupDraft) -> Unit,
    ) -> Unit,
) {
    val listState = rememberLazyListState()
    val editorType = OverrideStructuredEditorStore.objectEditorType
    val editorTypeTitle = stringResource(editorType.titleRes)
    val editorTypeItemLabel = stringResource(editorType.itemLabelRes)
    val title = OverrideStructuredEditorStore.objectEditorTitle.ifBlank { editorTypeTitle }
    val newProxyNodeLabel = stringResource(LocaleR.string.override_editor_new_proxy_node)
    val newProxyGroupLabel = stringResource(LocaleR.string.override_editor_new_proxy_group)
    val editProxyNodeLabel = stringResource(LocaleR.string.override_editor_edit_proxy_node)
    val editProxyGroupLabel = stringResource(LocaleR.string.override_editor_edit_proxy_group)
    val availableModes = OverrideStructuredEditorStore.objectEditorAvailableModes
    var showResetDialog by remember { mutableStateOf(false) }
    val addFabController = rememberOverrideFabController()
    var isDeleteMode by rememberSaveable { mutableStateOf(false) }
    val selectedUiIds = remember { mutableStateMapOf<String, Boolean>() }
    val selectedMode = OverrideStructuredEditorStore.objectEditorSelectedMode

    val proxyModeValues = OverrideStructuredEditorStore.objectEditorProxyDraftValues
    val proxyGroupModeValues = OverrideStructuredEditorStore.objectEditorProxyGroupDraftValues

    val modeLabels = availableModes.map { stringResource(it.labelRes) }
    val selectedModeIndex = remember(availableModes, selectedMode) {
        availableModes.indexOf(selectedMode).coerceAtLeast(0)
    }
    val currentProxyDrafts = remember(proxyModeValues, selectedMode) {
        proxyModeValues.valueFor(selectedMode).orEmpty()
    }
    val currentProxyGroupDrafts = remember(proxyGroupModeValues, selectedMode) {
        proxyGroupModeValues.valueFor(selectedMode).orEmpty()
    }
    val currentItemCount = remember(editorType, currentProxyDrafts, currentProxyGroupDrafts) {
        when (editorType) {
            OverrideStructuredObjectType.Proxies -> currentProxyDrafts.size
            OverrideStructuredObjectType.ProxyGroups -> currentProxyGroupDrafts.size
        }
    }

    fun applyProxyModeValue(
        mode: OverrideListEditorMode,
        values: List<OverrideProxyDraft>,
    ) {
        OverrideStructuredEditorStore.applyProxyDraftModeValue(mode, values)
    }

    fun applyProxyGroupModeValue(
        mode: OverrideListEditorMode,
        values: List<OverrideProxyGroupDraft>,
    ) {
        OverrideStructuredEditorStore.applyProxyGroupDraftModeValue(mode, values)
    }

    fun clearSelection() {
        selectedUiIds.clear()
    }

    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val fromIndex = (from.index - ObjectListReorderHeaderCount).coerceAtLeast(0)
        val toIndex = (to.index - ObjectListReorderHeaderCount).coerceAtLeast(0)
        when (editorType) {
            OverrideStructuredObjectType.Proxies -> {
                val mode = OverrideStructuredEditorStore.objectEditorSelectedMode
                applyProxyModeValue(
                    mode,
                    reorderDraftList(
                        OverrideStructuredEditorStore.objectEditorProxyDraftValues.valueFor(mode).orEmpty(),
                        fromIndex,
                        toIndex,
                    ),
                )
            }

            OverrideStructuredObjectType.ProxyGroups -> {
                val mode = OverrideStructuredEditorStore.objectEditorSelectedMode
                applyProxyGroupModeValue(
                    mode,
                    reorderDraftList(
                        OverrideStructuredEditorStore.objectEditorProxyGroupDraftValues.valueFor(mode).orEmpty(),
                        fromIndex,
                        toIndex,
                    ),
                )
            }
        }
        clearSelection()
    }
    val showAddFab = !isDeleteMode && !showResetDialog

    Scaffold(
        floatingActionButton = {
            OverrideAnimatedFab(
                controller = addFabController,
                visible = showAddFab,
                imageVector = AppMd3Icons.Action.Add,
                contentDescription = stringResource(LocaleR.string.override_editor_new) + editorTypeItemLabel,
                onClick = {
                    when (editorType) {
                        OverrideStructuredObjectType.Proxies -> {
                            onOpenProxyDraftEditor(newProxyNodeLabel, null) { createdDraft ->
                                val mode = OverrideStructuredEditorStore.objectEditorSelectedMode
                                applyProxyModeValue(
                                    mode,
                                    OverrideStructuredEditorStore.objectEditorProxyDraftValues.valueFor(mode)
                                        .orEmpty()
                                        .toMutableList()
                                        .also { it.add(createdDraft) },
                                )
                            }
                        }

                        OverrideStructuredObjectType.ProxyGroups -> {
                            onOpenProxyGroupDraftEditor(newProxyGroupLabel, null) { createdDraft ->
                                val mode = OverrideStructuredEditorStore.objectEditorSelectedMode
                                applyProxyGroupModeValue(
                                    mode,
                                    OverrideStructuredEditorStore.objectEditorProxyGroupDraftValues.valueFor(mode)
                                        .orEmpty()
                                        .toMutableList()
                                        .also { it.add(createdDraft) },
                                )
                            }
                        }
                    }
                },
            )
        },
        topBar = {
            TopBar(
                title = title,
                actions = {
                    if (isDeleteMode) {
                        OverrideTopBarAction(
                            icon = AppMd3Icons.Action.Cancel,
                            contentDescription = stringResource(LocaleR.string.override_editor_cancel_delete),
                            spacedFromNext = true,
                            onClick = {
                                isDeleteMode = false
                                clearSelection()
                            },
                        )
                        OverrideTopBarAction(
                            icon = AppMd3Icons.Action.Delete,
                            contentDescription = stringResource(LocaleR.string.override_editor_delete_selected),
                            destructive = true,
                            onClick = {
                                if (selectedUiIds.isNotEmpty()) {
                                    when (editorType) {
                                        OverrideStructuredObjectType.Proxies -> {
                                            val mode = OverrideStructuredEditorStore.objectEditorSelectedMode
                                            applyProxyModeValue(
                                                mode,
                                                OverrideStructuredEditorStore.objectEditorProxyDraftValues.valueFor(mode)
                                                    .orEmpty()
                                                    .filterNot { selectedUiIds.containsKey(it.uiId) },
                                            )
                                        }

                                        OverrideStructuredObjectType.ProxyGroups -> {
                                            val mode = OverrideStructuredEditorStore.objectEditorSelectedMode
                                            applyProxyGroupModeValue(
                                                mode,
                                                OverrideStructuredEditorStore.objectEditorProxyGroupDraftValues.valueFor(mode)
                                                    .orEmpty()
                                                    .filterNot { selectedUiIds.containsKey(it.uiId) },
                                            )
                                        }
                                    }
                                    clearSelection()
                                    isDeleteMode = false
                                }
                            },
                        )
                    } else {
                        OverrideTopBarAction(
                            icon = AppMd3Icons.Action.Undo,
                            contentDescription = stringResource(LocaleR.string.override_editor_clear_current_mode),
                            spacedFromNext = true,
                            destructive = true,
                            onClick = { showResetDialog = true },
                        )
                        OverrideTopBarAction(
                            icon = AppMd3Icons.Action.Delete,
                            contentDescription = stringResource(LocaleR.string.override_editor_enter_delete_mode),
                            destructive = true,
                            onClick = {
                                isDeleteMode = true
                                clearSelection()
                            },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(

            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
            modifier = Modifier.fillMaxWidth(),
            lazyListState = listState,
            onScrollDirectionChanged = addFabController::onScrollDirectionChanged,
        ) {
            item(key = "modifier-card") {
                Card {
                    YumeMd3DropdownPreference(
                        title = stringResource(LocaleR.string.override_editor_mode_title),
                        items = modeLabels,
                        selectedIndex = selectedModeIndex,
                        onSelectedIndexChange = { index ->
                            val newMode = availableModes.getOrElse(index) { selectedMode }
                            OverrideStructuredEditorStore.updateObjectEditorSession(selectedMode = newMode)
                            isDeleteMode = false
                            clearSelection()
                        },
                    )
                }
            }

            item(key = "modifier-card-gap") {
    Spacer(modifier = Modifier.height(UiDp.dp12))
            }

            if (currentItemCount > 0) {
                when (editorType) {
                    OverrideStructuredObjectType.Proxies -> {
                        items(
                            items = currentProxyDrafts,
                            key = OverrideProxyDraft::uiId,
                            contentType = { "proxy-draft-card" },
                        ) { draft ->
                            ReorderableItem(
                                state = reorderState,
                                key = draft.uiId,
                            ) { isDragging ->
                                StructuredObjectCard(
                                    title = draft.name.ifBlank {
                                        stringResource(LocaleR.string.override_editor_unnamed_proxy_node)
                                    },
                                    isDragging = isDragging,
                                    isDeleteMode = isDeleteMode,
                                    isSelected = selectedUiIds[draft.uiId] == true,
                                    onClick = {
                                        if (isDeleteMode) {
                                            if (selectedUiIds[draft.uiId] == true) {
                                                selectedUiIds.remove(draft.uiId)
                                            } else {
                                                selectedUiIds[draft.uiId] = true
                                            }
                                        } else {
                                            val draftUiId = draft.uiId
                                            val editMode = selectedMode
                                            onOpenProxyDraftEditor(editProxyNodeLabel, draft) { updatedDraft ->
                                                applyProxyModeValue(
                                                    editMode,
                                                    OverrideStructuredEditorStore.objectEditorProxyDraftValues
                                                        .valueFor(editMode)
                                                        .orEmpty()
                                                        .map { currentDraft ->
                                                        if (currentDraft.uiId == draftUiId) {
                                                            updatedDraft.copy(uiId = draftUiId)
                                                        } else {
                                                            currentDraft
                                                        }
                                                    },
                                                )
                                            }
                                        }
                                    },
                                    onSelectedChange = { checked ->
                                        if (checked) {
                                            selectedUiIds[draft.uiId] = true
                                        } else {
                                            selectedUiIds.remove(draft.uiId)
                                        }
                                    },
                                )
                            }
                        }
                    }

                    OverrideStructuredObjectType.ProxyGroups -> {
                        items(
                            items = currentProxyGroupDrafts,
                            key = OverrideProxyGroupDraft::uiId,
                            contentType = { "proxy-group-draft-card" },
                        ) { draft ->
                            ReorderableItem(
                                state = reorderState,
                                key = draft.uiId,
                            ) { isDragging ->
                                StructuredObjectCard(
                                    title = draft.name.ifBlank {
                                        stringResource(LocaleR.string.override_editor_unnamed_proxy_group)
                                    },
                                    isDragging = isDragging,
                                    isDeleteMode = isDeleteMode,
                                    isSelected = selectedUiIds[draft.uiId] == true,
                                    onClick = {
                                        if (isDeleteMode) {
                                            if (selectedUiIds[draft.uiId] == true) {
                                                selectedUiIds.remove(draft.uiId)
                                            } else {
                                                selectedUiIds[draft.uiId] = true
                                            }
                                        } else {
                                            val draftUiId = draft.uiId
                                            val editMode = selectedMode
                                            onOpenProxyGroupDraftEditor(editProxyGroupLabel, draft) { updatedDraft ->
                                                applyProxyGroupModeValue(
                                                    editMode,
                                                    OverrideStructuredEditorStore.objectEditorProxyGroupDraftValues
                                                        .valueFor(editMode)
                                                        .orEmpty()
                                                        .map { currentDraft ->
                                                        if (currentDraft.uiId == draftUiId) {
                                                            updatedDraft.copy(uiId = draftUiId)
                                                        } else {
                                                            currentDraft
                                                        }
                                                    },
                                                )
                                            }
                                        }
                                    },
                                    onSelectedChange = { checked ->
                                        if (checked) {
                                            selectedUiIds[draft.uiId] = true
                                        } else {
                                            selectedUiIds.remove(draft.uiId)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            item(key = "object-list-bottom-spacer") {
                Spacer(modifier = Modifier.height(OverrideSectionBottomSpacing))
            }
        }

    AppDialog(
            show = showResetDialog,
            title = stringResource(LocaleR.string.override_editor_clear_dialog_title).format(editorTypeItemLabel),
            summary = stringResource(LocaleR.string.override_editor_clear_dialog_summary).format(editorTypeItemLabel),
            onDismissRequest = { showResetDialog = false },
        ) {
            DialogButtonRow(
                onCancel = { showResetDialog = false },
                onConfirm = {
                    showResetDialog = false
                    isDeleteMode = false
                    clearSelection()
                    when (editorType) {
                        OverrideStructuredObjectType.Proxies -> {
                            val mode = OverrideStructuredEditorStore.objectEditorSelectedMode
                            applyProxyModeValue(mode, emptyList())
                        }

                        OverrideStructuredObjectType.ProxyGroups -> {
                            val mode = OverrideStructuredEditorStore.objectEditorSelectedMode
                            applyProxyGroupModeValue(mode, emptyList())
                        }
                    }
                },
                cancelText = stringResource(LocaleR.string.override_dialog_button_cancel),
                confirmText = stringResource(LocaleR.string.override_editor_clear),
                confirmDestructive = true,
            )
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.StructuredObjectCard(
    title: String,
    isDragging: Boolean,
    isDeleteMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onSelectedChange: (Boolean) -> Unit,
) {
    Column {
        Card(
            modifier = Modifier
                .longPressDraggableHandle(enabled = !isDeleteMode)
                .alpha(if (isDragging) 0.92f else 1f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = UiDp.dp14, vertical = UiDp.dp14),
                horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    imageVector = AppMd3Icons.Action.List,
                    contentDescription = stringResource(LocaleR.string.override_editor_drag_to_sort),
                    
                )
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    AppText(
                        text = title,
                        
                    )
                }
                Box(
                    modifier = Modifier.height(UiDp.dp32),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isDeleteMode) {
                        AppCheckbox(
                            checked = isSelected,
                            onCheckedChange = onSelectedChange,
                        )
                    } else {
                        AppIcon(
                            imageVector = AppMd3Icons.Navigation.Forward,
                            contentDescription = stringResource(LocaleR.string.override_editor_edit),
                            
                        )
                    }
                }
            }
        }
    Spacer(modifier = Modifier.height(UiDp.dp12))
    }
}
