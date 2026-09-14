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

private const val SubRuleReorderHeaderCount = 2

@Composable
fun OverrideSubRuleMapEditorScreen(
    navigator: DestinationsNavigator,
    onOpenDraftEditor: (
        title: String,
        initialValue: OverrideSubRuleGroupDraft?,
        onConfirm: (OverrideSubRuleGroupDraft) -> Unit,
    ) -> Unit,
) {
    val listState = rememberLazyListState()
    val subRulesTitle = stringResource(LocaleR.string.override_structured_sub_rules_title)
    val subRuleItemLabel = stringResource(LocaleR.string.override_structured_sub_rules_item_label)
    val newSubRuleGroupLabel = stringResource(LocaleR.string.override_editor_new_sub_rule_group)
    val editSubRuleGroupLabel = stringResource(LocaleR.string.override_editor_edit_sub_rule_group)
    val unnamedSubRuleGroupLabel = stringResource(LocaleR.string.override_editor_unnamed_sub_rule_group)
    val title = OverrideStructuredEditorStore.subRuleGroupEditorTitle.ifBlank { subRulesTitle }
    val availableModes = OverrideStructuredEditorStore.subRuleGroupEditorAvailableModes
    var showResetDialog by remember { mutableStateOf(false) }
    val addFabController = rememberOverrideFabController()
    var isDeleteMode by rememberSaveable { mutableStateOf(false) }
    var selectedUiIds by remember { mutableStateOf(emptySet<String>()) }
    val selectedMode = OverrideStructuredEditorStore.subRuleGroupEditorSelectedMode
    val editorValues = OverrideStructuredEditorStore.subRuleGroupEditorDraftValues

    val selectedModeIndex = availableModes.indexOf(selectedMode).coerceAtLeast(0)
    val currentDrafts = editorValues.valueFor(selectedMode).orEmpty()

    fun applySubRuleValues(values: OverrideListModeValues<List<OverrideSubRuleGroupDraft>>) {
        OverrideStructuredEditorStore.applySubRuleDraftValues(values)
    }

    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        val fromIndex = (from.index - SubRuleReorderHeaderCount).coerceAtLeast(0)
        val toIndex = (to.index - SubRuleReorderHeaderCount).coerceAtLeast(0)
        val mode = OverrideStructuredEditorStore.subRuleGroupEditorSelectedMode
        val latestValues = OverrideStructuredEditorStore.subRuleGroupEditorDraftValues
        val updatedValues = latestValues.update(
            mode,
            reorderDraftList(latestValues.valueFor(mode).orEmpty(), fromIndex, toIndex),
        )
        selectedUiIds = emptySet()
        applySubRuleValues(updatedValues)
    }
    val showAddFab = !isDeleteMode && !showResetDialog

    Scaffold(
        floatingActionButton = {
            OverrideAnimatedFab(
                controller = addFabController,
                visible = showAddFab,
                imageVector = AppMd3Icons.Action.Add,
                contentDescription = newSubRuleGroupLabel,
                onClick = {
                    onOpenDraftEditor(newSubRuleGroupLabel, null) { createdDraft ->
                        val mode = OverrideStructuredEditorStore.subRuleGroupEditorSelectedMode
                        val latestValues = OverrideStructuredEditorStore.subRuleGroupEditorDraftValues
                        val updatedValues = latestValues.update(
                            mode,
                            latestValues.valueFor(mode).orEmpty().toMutableList().also { it.add(createdDraft) },
                        )
                        applySubRuleValues(updatedValues)
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
                                selectedUiIds = emptySet()
                            },
                        )
                        OverrideTopBarAction(
                            icon = AppMd3Icons.Action.Delete,
                            contentDescription = stringResource(LocaleR.string.override_editor_delete_selected),
                            destructive = true,
                            onClick = {
                                if (selectedUiIds.isNotEmpty()) {
                                    val mode = OverrideStructuredEditorStore.subRuleGroupEditorSelectedMode
                                    val latestValues = OverrideStructuredEditorStore.subRuleGroupEditorDraftValues
                                    val updatedValues = latestValues.update(
                                        mode,
                                        latestValues.valueFor(mode).orEmpty().filterNot { it.uiId in selectedUiIds },
                                    )
                                    selectedUiIds = emptySet()
                                    isDeleteMode = false
                                    applySubRuleValues(updatedValues)
                                }
                            },
                        )
                    } else {
                        OverrideTopBarAction(
                            icon = AppMd3Icons.Action.Undo,
                            contentDescription = stringResource(LocaleR.string.override_editor_clear_mode),
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
                                selectedUiIds = emptySet()
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
                        items = availableModes.map { stringResource(it.labelRes) },
                        selectedIndex = selectedModeIndex,
                        onSelectedIndexChange = { index ->
                            val newMode = availableModes.getOrElse(index) { selectedMode }
                            OverrideStructuredEditorStore.updateSubRuleGroupEditorSession(selectedMode = newMode)
                            isDeleteMode = false
                            selectedUiIds = emptySet()
                        },
                    )
                }
            }

            item(key = "modifier-card-gap") {
    Spacer(modifier = Modifier.height(UiDp.dp12))
            }

            if (currentDrafts.isNotEmpty()) {
                items(
                    count = currentDrafts.size,
                    key = { index -> currentDrafts[index].uiId },
                ) { index ->
                    val draft = currentDrafts[index]
                    ReorderableItem(
                        state = reorderState,
                        key = draft.uiId,
                    ) { isDragging ->
                        SubRuleGroupCard(
                            title = draft.name.ifBlank { unnamedSubRuleGroupLabel },
                            isDragging = isDragging,
                            isDeleteMode = isDeleteMode,
                            isSelected = draft.uiId in selectedUiIds,
                            onClick = {
                                if (isDeleteMode) {
                                    selectedUiIds = selectedUiIds.toggle(draft.uiId)
                                } else {
                                    val draftUiId = draft.uiId
                                    val editMode = selectedMode
                                    onOpenDraftEditor(editSubRuleGroupLabel, draft) { updatedDraft ->
                                        val latestValues = OverrideStructuredEditorStore.subRuleGroupEditorDraftValues
                                        val updatedValues = latestValues.update(
                                            editMode,
                                            latestValues.valueFor(editMode).orEmpty().map { currentDraft ->
                                                if (currentDraft.uiId == draftUiId) {
                                                    updatedDraft.copy(uiId = draftUiId)
                                                } else {
                                                    currentDraft
                                                }
                                            },
                                        )
                                        applySubRuleValues(updatedValues)
                                    }
                                }
                            },
                            onSelectedChange = { checked ->
                                selectedUiIds = if (checked) {
                                    selectedUiIds + draft.uiId
                                } else {
                                    selectedUiIds - draft.uiId
                                }
                            },
                        )
                    }
                }
            }

            item(key = "sub-rule-bottom-spacer") {
                Spacer(modifier = Modifier.height(OverrideSectionBottomSpacing))
            }
        }

    AppDialog(
            show = showResetDialog,
            title = stringResource(LocaleR.string.override_editor_clear_sub_rules),
            summary = stringResource(LocaleR.string.override_editor_clear_dialog_summary).format(subRuleItemLabel),
            onDismissRequest = { showResetDialog = false },
        ) {
            DialogButtonRow(
                onCancel = { showResetDialog = false },
                onConfirm = {
                    showResetDialog = false
                    isDeleteMode = false
                    selectedUiIds = emptySet()
                    val mode = OverrideStructuredEditorStore.subRuleGroupEditorSelectedMode
                    applySubRuleValues(OverrideStructuredEditorStore.subRuleGroupEditorDraftValues.update(mode, emptyList()))
                },
                cancelText = stringResource(LocaleR.string.override_dialog_button_cancel),
                confirmText = stringResource(LocaleR.string.override_editor_clear),
                confirmDestructive = true,
            )
        }
    }
}

@Composable
private fun ReorderableCollectionItemScope.SubRuleGroupCard(
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

private fun Set<String>.toggle(uiId: String): Set<String> {
    return if (uiId in this) this - uiId else this + uiId
}
