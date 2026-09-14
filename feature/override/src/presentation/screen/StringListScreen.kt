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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.*
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3FilledButton
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3OutlinedTextField
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3TextButton
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.util.OverrideListEditorMode
import com.amamiyakokoro.box.presentation.util.OverrideListModeValues
import com.amamiyakokoro.box.presentation.util.OverrideStructuredEditorStore
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.compose.material3.Scaffold


@Composable
fun OverrideStringListEditorScreen(
    navigator: DestinationsNavigator,
) {
    val listState = rememberLazyListState()
    val listLabel = stringResource(LocaleR.string.override_editor_list)
    val title = OverrideStructuredEditorStore.stringListEditorTitle.ifBlank { listLabel }
    val placeholder = OverrideStructuredEditorStore.stringListEditorPlaceholder
    val availableModes = OverrideStructuredEditorStore.stringListEditorAvailableModes
    var showItemDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableIntStateOf(-1) }
    var currentDraftValue by remember { mutableStateOf("") }
    val addFabController = rememberOverrideFabController()
    val selectedMode = OverrideStructuredEditorStore.stringListEditorSelectedMode
    val editorValues = remember(OverrideStructuredEditorStore.stringListEditorValues) {
        OverrideListModeValues(
            replaceValue = OverrideStructuredEditorStore.stringListEditorValues.replaceValue?.toList(),
            startValue = OverrideStructuredEditorStore.stringListEditorValues.startValue?.toList(),
            endValue = OverrideStructuredEditorStore.stringListEditorValues.endValue?.toList(),
        )
    }
    val currentItems = editorValues.valueFor(selectedMode).orEmpty()
    val selectedModeIndex = availableModes.indexOf(selectedMode).coerceAtLeast(0)
    val showAddFab = !showItemDialog && !showResetDialog

    fun currentStringListValues(): OverrideListModeValues<List<String>> {
        val latestValues = OverrideStructuredEditorStore.stringListEditorValues
        return OverrideListModeValues(
            replaceValue = latestValues.replaceValue?.toList(),
            startValue = latestValues.startValue?.toList(),
            endValue = latestValues.endValue?.toList(),
        )
    }

    fun applyStringListValues(values: OverrideListModeValues<List<String>>) {
        OverrideStructuredEditorStore.applyStringListValues(
            OverrideListModeValues(
                replaceValue = values.replaceValue?.toList(),
                startValue = values.startValue?.toList(),
                endValue = values.endValue?.toList(),
            ),
        )
    }

    Scaffold(
        floatingActionButton = {
            OverrideAnimatedFab(
                controller = addFabController,
                visible = showAddFab,
                imageVector = AppMd3Icons.Action.Add,
                contentDescription = stringResource(LocaleR.string.override_editor_add_item),
                onClick = {
                    editingIndex = -1
                    currentDraftValue = ""
                    showItemDialog = true
                },
            )
        },
        topBar = {
            TopBar(
                title = title,
                actions = {
                    OverrideTopBarAction(
                        icon = AppMd3Icons.Action.Undo,
                        contentDescription = stringResource(LocaleR.string.override_editor_clear_current_mode),
                        destructive = true,
                        onClick = { showResetDialog = true },
                    )
                },
            )
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(

            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
            lazyListState = listState,
            onScrollDirectionChanged = addFabController::onScrollDirectionChanged,
        ) {
            item {
                Card {
                    YumeMd3DropdownPreference(
                        title = stringResource(LocaleR.string.override_editor_mode_title),
                        items = availableModes.map { stringResource(it.labelRes) },
                        selectedIndex = selectedModeIndex,
                        onSelectedIndexChange = { index ->
                            val newMode = availableModes.getOrElse(index) { selectedMode }
                            OverrideStructuredEditorStore.updateStringListEditorSession(selectedMode = newMode)
                        },
                    )
                }
            }

            if (currentItems.isNotEmpty()) {
                itemsIndexed(
                    items = currentItems,
                    key = { index, itemValue -> "$index:$itemValue" },
                ) { index, itemValue ->
                    StringListEntryCard(
                        index = index + 1,
                        value = itemValue,
                        onEdit = {
                            editingIndex = index
                            currentDraftValue = itemValue
                            showItemDialog = true
                        },
                        onDelete = {
                            val mode = OverrideStructuredEditorStore.stringListEditorSelectedMode
                            val latestValues = currentStringListValues()
                            val updatedValues = latestValues.update(
                                mode,
                                latestValues.valueFor(mode).orEmpty().toMutableList().also { items ->
                                    items.removeAt(index)
                                },
                            )
                            applyStringListValues(updatedValues)
                        },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(OverrideSectionBottomSpacing))
            }
        }

        StringListEntryDialog(
            show = showItemDialog,
            title = stringResource(
                if (editingIndex >= 0) LocaleR.string.override_editor_edit_item
                else LocaleR.string.override_editor_add_item,
            ),
            placeholder = placeholder,
            initialValue = currentDraftValue,
            onConfirm = { updatedValue ->
                val normalizedValue = updatedValue.trim()
                if (normalizedValue.isBlank()) {
                    return@StringListEntryDialog
                }
                val updatedItems = currentItems.toMutableList().also { items ->
                    if (editingIndex in items.indices) {
                        items[editingIndex] = normalizedValue
                    } else {
                        items.add(normalizedValue)
                    }
                }
                val mode = OverrideStructuredEditorStore.stringListEditorSelectedMode
                val updatedValues = currentStringListValues().update(mode, updatedItems)
                applyStringListValues(updatedValues)
                editingIndex = -1
                currentDraftValue = ""
                showItemDialog = false
            },
            onDismiss = {
                editingIndex = -1
                currentDraftValue = ""
                showItemDialog = false
            },
        )

        AppDialog(
            show = showResetDialog,
            title = stringResource(LocaleR.string.override_editor_clear_current_mode),
            summary = stringResource(LocaleR.string.override_editor_clear_dialog_summary).format(listLabel),
            onDismissRequest = { showResetDialog = false },
        ) {
            DialogButtonRow(
                onCancel = { showResetDialog = false },
                onConfirm = {
                    showResetDialog = false
                    val mode = OverrideStructuredEditorStore.stringListEditorSelectedMode
                    applyStringListValues(currentStringListValues().update(mode, emptyList()))
                },
                cancelText = stringResource(LocaleR.string.override_dialog_button_cancel),
                confirmText = stringResource(LocaleR.string.override_editor_clear),
                confirmDestructive = true,
            )
        }
    }
}

@Composable
private fun StringListEntryCard(
    index: Int,
    value: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(top = UiDp.dp12),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(horizontal = UiDp.dp16, vertical = UiDp.dp14),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(
                text = "$index.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(UiDp.dp40),
            )
            AppText(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = UiDp.dp8),
            )
            OverrideCardActionIconButton(
                imageVector = AppMd3Icons.Action.Delete,
                contentDescription = stringResource(LocaleR.string.override_card_delete),
                onClick = onDelete,
                tone = OverrideActionTone.Danger,
            )
        }
    }
}

@Composable
private fun StringListEntryDialog(
    show: Boolean,
    title: String,
    placeholder: String,
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!show) {
        return
    }

    var draftValue by remember(show, initialValue) { mutableStateOf(initialValue) }

    AppDialog(
        show = show,
        title = title,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(UiDp.dp16),
        ) {
            YumeMd3OutlinedTextField(
                value = draftValue,
                onValueChange = { draftValue = it },
                label = placeholder,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
            ) {
                YumeMd3TextButton(
                    text = stringResource(LocaleR.string.override_dialog_button_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                YumeMd3FilledButton(
                    text = stringResource(LocaleR.string.override_editor_confirm),
                    onClick = { onConfirm(draftValue) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
