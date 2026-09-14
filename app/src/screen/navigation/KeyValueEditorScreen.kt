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

package com.amamiyakokoro.box.screen.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.AppConfirmDialog
import com.amamiyakokoro.box.presentation.component.AppFormDialog
import com.amamiyakokoro.box.presentation.component.AppTextFieldDialog
import com.amamiyakokoro.box.presentation.component.EditorAction
import com.amamiyakokoro.box.presentation.component.EditorEmptyState
import com.amamiyakokoro.box.presentation.component.EditorListItem
import com.amamiyakokoro.box.presentation.component.EditorScaffold
import com.amamiyakokoro.box.presentation.component.HapticSwitch
import com.amamiyakokoro.box.presentation.component.PreferenceValueItem
import com.amamiyakokoro.box.presentation.component.ScreenLazyColumn
import com.amamiyakokoro.box.presentation.component.Title
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.component.rememberStandalonePageMainPadding
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3OutlinedTextField
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.util.UUID

object EditorDataHolder {
    var listEditorTitle: String = ""
    var listEditorPlaceholder: String = ""
    var listEditorItems: MutableList<String> = mutableListOf()
    var listEditorCallback: ((List<String>?) -> Unit)? = null

    var mapEditorTitle: String = ""
    var mapEditorKeyPlaceholder: String = ""
    var mapEditorValuePlaceholder: String = ""
    var mapEditorItems: MutableMap<String, String> = mutableMapOf()
    var mapEditorCallback: ((Map<String, String>?) -> Unit)? = null

    fun setupListEditor(
        title: String,
        placeholder: String,
        items: List<String>?,
        callback: (List<String>?) -> Unit,
    ) {
        listEditorTitle = title
        listEditorPlaceholder = placeholder
        listEditorItems = items?.toMutableList() ?: mutableListOf()
        listEditorCallback = callback
    }

    fun setupMapEditor(
        title: String,
        keyPlaceholder: String,
        valuePlaceholder: String,
        items: Map<String, String>?,
        callback: (Map<String, String>?) -> Unit,
    ) {
        mapEditorTitle = title
        mapEditorKeyPlaceholder = keyPlaceholder
        mapEditorValuePlaceholder = valuePlaceholder
        mapEditorItems = items?.toMutableMap() ?: mutableMapOf()
        mapEditorCallback = callback
    }

    fun clearListEditor() {
        listEditorTitle = ""
        listEditorPlaceholder = ""
        listEditorItems = mutableListOf()
        listEditorCallback = null
    }

    fun clearMapEditor() {
        mapEditorTitle = ""
        mapEditorKeyPlaceholder = ""
        mapEditorValuePlaceholder = ""
        mapEditorItems = mutableMapOf()
        mapEditorCallback = null
    }
}

private data class TextDraftItem(
    val id: String,
    val value: String,
)

private data class KeyValueDraftItem(
    val id: String,
    val key: String,
    val value: String,
)

private sealed interface StringListDialogState {
    data object None : StringListDialogState
    data object Add : StringListDialogState
    data class Edit(val itemId: String) : StringListDialogState
    data object Reset : StringListDialogState
    data object AddRule : StringListDialogState
}

private sealed interface KeyValueDialogState {
    data object None : KeyValueDialogState
    data object Add : KeyValueDialogState
    data class Edit(val itemId: String) : KeyValueDialogState
    data object Reset : KeyValueDialogState
}

@Destination<RootGraph>
@Composable
fun StringListEditorScreen(
    navigator: DestinationsNavigator,
) {
    val items = remember { mutableStateListOf<TextDraftItem>() }
    val title = EditorDataHolder.listEditorTitle
    val placeholder = EditorDataHolder.listEditorPlaceholder
    val isOverrideRuleEditor = title == stringResource(LocaleR.string.override_label_rules_replace)
    var dialogState by remember { mutableStateOf<StringListDialogState>(StringListDialogState.None) }

    LaunchedEffect(title, placeholder) {
        items.clear()
        items.addAll(
            EditorDataHolder.listEditorItems.map { value ->
                TextDraftItem(id = UUID.randomUUID().toString(), value = value)
            },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            EditorDataHolder.listEditorCallback?.invoke(items.map(TextDraftItem::value).ifEmpty { null })
            EditorDataHolder.clearListEditor()
        }
    }

    val listState = rememberLazyListState()
    EditorScaffold(
        title = title,
        actions = listOf(
            EditorAction(
                icon = AppMd3Icons.Action.Undo,
                contentDescription = stringResource(LocaleR.string.component_editor_action_reset),
                onClick = { dialogState = StringListDialogState.Reset },
            ),
            EditorAction(
                icon = AppMd3Icons.Action.Add,
                contentDescription = stringResource(LocaleR.string.component_editor_action_add),
                onClick = {
                    dialogState = if (isOverrideRuleEditor) {
                        StringListDialogState.AddRule
                    } else {
                        StringListDialogState.Add
                    }
                },
            ),
        ),
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        val combinedInnerPadding = combinePaddingValues(innerPadding, mainLikePadding)
        if (items.isEmpty()) {
            EditorEmptyState(
                title = stringResource(LocaleR.string.component_editor_empty_title),
                hint = stringResource(LocaleR.string.component_editor_empty_hint),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(combinedInnerPadding),
            )
        } else {
            ScreenLazyColumn(
                lazyListState = listState,
                innerPadding = combinedInnerPadding,
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Title(stringResource(LocaleR.string.component_editor_count_items).format(items.size))
                }
                items(
                    items = items,
                    key = { it.id },
                ) { item ->
                    val index = remember(items, item.id) { items.indexOfFirst { it.id == item.id } + 1 }
                    EditorListItem(
                        index = index,
                        title = item.value,
                        onClick = { dialogState = StringListDialogState.Edit(item.id) },
                        onDelete = { items.removeAll { it.id == item.id } },
                        deleteIcon = AppMd3Icons.Action.Delete,
                        deleteContentDescription = stringResource(
                            LocaleR.string.component_editor_action_delete,
                        ),
                    )
                }
            }
        }
    }

    when (val state = dialogState) {
        StringListDialogState.None -> Unit
        StringListDialogState.Add -> {
            SimpleTextEditorDialog(
                title = stringResource(LocaleR.string.component_editor_dialog_add_title),
                placeholder = placeholder,
                initialValue = "",
                onDismiss = { dialogState = StringListDialogState.None },
                onConfirm = { value ->
                    items.add(TextDraftItem(UUID.randomUUID().toString(), value))
                    dialogState = StringListDialogState.None
                },
            )
        }

        is StringListDialogState.Edit -> {
            val currentItem = items.firstOrNull { it.id == state.itemId }
            if (currentItem != null) {
                SimpleTextEditorDialog(
                    title = stringResource(LocaleR.string.component_editor_dialog_edit_title),
                    placeholder = placeholder,
                    initialValue = currentItem.value,
                    onDismiss = { dialogState = StringListDialogState.None },
                    onConfirm = { value ->
                        val index = items.indexOfFirst { it.id == state.itemId }
                        if (index >= 0) {
                            items[index] = items[index].copy(value = value)
                        }
                        dialogState = StringListDialogState.None
                    },
                )
            } else {
                dialogState = StringListDialogState.None
            }
        }

        StringListDialogState.Reset -> {
            AppConfirmDialog(
                show = true,
                title = stringResource(LocaleR.string.component_editor_dialog_reset_title),
                message = stringResource(LocaleR.string.component_editor_dialog_reset_message),
                onDismissRequest = { dialogState = StringListDialogState.None },
                onConfirm = {
                    dialogState = StringListDialogState.None
                    EditorDataHolder.listEditorCallback?.invoke(null)
                    EditorDataHolder.clearListEditor()
                    navigator.popBackStack()
                },
                confirmDestructive = true,
            )
        }

        StringListDialogState.AddRule -> {
            RuleEditorDialog(
                title = stringResource(LocaleR.string.component_editor_dialog_add_title),
                onDismiss = { dialogState = StringListDialogState.None },
                onConfirm = { value ->
                    items.add(TextDraftItem(UUID.randomUUID().toString(), value))
                    dialogState = StringListDialogState.None
                },
            )
        }
    }
}

@Destination<RootGraph>
@Composable
fun KeyValueEditorScreen(
    navigator: DestinationsNavigator,
) {
    val items = remember { mutableStateListOf<KeyValueDraftItem>() }
    val title = EditorDataHolder.mapEditorTitle
    val keyPlaceholder = EditorDataHolder.mapEditorKeyPlaceholder
    val valuePlaceholder = EditorDataHolder.mapEditorValuePlaceholder
    var dialogState by remember { mutableStateOf<KeyValueDialogState>(KeyValueDialogState.None) }

    LaunchedEffect(title, keyPlaceholder, valuePlaceholder) {
        items.clear()
        items.addAll(
            EditorDataHolder.mapEditorItems.map { (key, value) ->
                KeyValueDraftItem(
                    id = UUID.randomUUID().toString(),
                    key = key,
                    value = value,
                )
            },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            EditorDataHolder.mapEditorCallback?.invoke(
                items.associate { it.key to it.value }.ifEmpty { null },
            )
            EditorDataHolder.clearMapEditor()
        }
    }

    val listState = rememberLazyListState()
    EditorScaffold(
        title = title,
        actions = listOf(
            EditorAction(
                icon = AppMd3Icons.Action.Undo,
                contentDescription = stringResource(LocaleR.string.component_editor_action_reset),
                onClick = { dialogState = KeyValueDialogState.Reset },
            ),
            EditorAction(
                icon = AppMd3Icons.Action.Add,
                contentDescription = stringResource(LocaleR.string.component_editor_action_add),
                onClick = { dialogState = KeyValueDialogState.Add },
            ),
        ),
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        val combinedInnerPadding = combinePaddingValues(innerPadding, mainLikePadding)
        if (items.isEmpty()) {
            EditorEmptyState(
                title = stringResource(LocaleR.string.component_editor_empty_title),
                hint = stringResource(LocaleR.string.component_editor_empty_hint),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(combinedInnerPadding),
            )
        } else {
            ScreenLazyColumn(
                lazyListState = listState,
                innerPadding = combinedInnerPadding,
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Title(stringResource(LocaleR.string.component_editor_count_items).format(items.size))
                }
                items(
                    items = items,
                    key = { it.id },
                ) { item ->
                    val index = remember(items, item.id) { items.indexOfFirst { it.id == item.id } + 1 }
                    EditorListItem(
                        index = index,
                        title = item.key,
                        summary = item.value,
                        onClick = { dialogState = KeyValueDialogState.Edit(item.id) },
                        onDelete = { items.removeAll { it.id == item.id } },
                        deleteIcon = AppMd3Icons.Action.Delete,
                        deleteContentDescription = stringResource(
                            LocaleR.string.component_editor_action_delete,
                        ),
                    )
                }
            }
        }
    }

    when (val state = dialogState) {
        KeyValueDialogState.None -> Unit
        KeyValueDialogState.Add -> {
            KeyValueFormDialog(
                title = stringResource(LocaleR.string.component_editor_dialog_add_title),
                keyPlaceholder = keyPlaceholder,
                valuePlaceholder = valuePlaceholder,
                existingKeys = items.map(KeyValueDraftItem::key).toSet(),
                initialKey = "",
                initialValue = "",
                onDismiss = { dialogState = KeyValueDialogState.None },
                onConfirm = { key, value ->
                    items.add(KeyValueDraftItem(UUID.randomUUID().toString(), key, value))
                    dialogState = KeyValueDialogState.None
                },
            )
        }

        is KeyValueDialogState.Edit -> {
            val currentItem = items.firstOrNull { it.id == state.itemId }
            if (currentItem != null) {
                KeyValueFormDialog(
                    title = stringResource(LocaleR.string.component_editor_dialog_edit_title),
                    keyPlaceholder = keyPlaceholder,
                    valuePlaceholder = valuePlaceholder,
                    existingKeys = items.map(KeyValueDraftItem::key).toSet(),
                    currentEditingKey = currentItem.key,
                    initialKey = currentItem.key,
                    initialValue = currentItem.value,
                    onDismiss = { dialogState = KeyValueDialogState.None },
                    onConfirm = { key, value ->
                        val index = items.indexOfFirst { it.id == state.itemId }
                        if (index >= 0) {
                            items[index] = items[index].copy(key = key, value = value)
                        }
                        dialogState = KeyValueDialogState.None
                    },
                )
            } else {
                dialogState = KeyValueDialogState.None
            }
        }

        KeyValueDialogState.Reset -> {
            AppConfirmDialog(
                show = true,
                title = stringResource(LocaleR.string.component_editor_dialog_reset_title),
                message = stringResource(LocaleR.string.component_editor_dialog_reset_message),
                onDismissRequest = { dialogState = KeyValueDialogState.None },
                onConfirm = {
                    dialogState = KeyValueDialogState.None
                    EditorDataHolder.mapEditorCallback?.invoke(null)
                    EditorDataHolder.clearMapEditor()
                    navigator.popBackStack()
                },
                confirmDestructive = true,
            )
        }
    }
}

private val RULE_TYPE_PRESETS = listOf(
    "DOMAIN",
    "DOMAIN-SUFFIX",
    "DOMAIN-KEYWORD",
    "DOMAIN-WILDCARD",
    "DOMAIN-REGEX",
    "GEOSITE",
    "IP-CIDR",
    "IP-CIDR6",
    "IP-SUFFIX",
    "IP-ASN",
    "GEOIP",
    "SRC-GEOIP",
    "SRC-IP-ASN",
    "SRC-IP-CIDR",
    "SRC-IP-SUFFIX",
    "DST-PORT",
    "SRC-PORT",
    "IN-PORT",
    "IN-TYPE",
    "IN-USER",
    "IN-NAME",
    "PROCESS-PATH",
    "PROCESS-PATH-WILDCARD",
    "PROCESS-PATH-REGEX",
    "PROCESS-NAME",
    "PROCESS-NAME-WILDCARD",
    "PROCESS-NAME-REGEX",
    "UID",
    "NETWORK",
    "DSCP",
    "RULE-SET",
    "AND",
    "OR",
    "NOT",
    "SUB-RULE",
    "MATCH",
)

private val RULE_EXTRA_SUPPORTED_TYPES = setOf(
    "IP-CIDR",
    "IP-CIDR6",
    "IP-SUFFIX",
    "IP-ASN",
    "GEOIP",
)

private fun supportsRuleExtra(ruleType: String): Boolean = ruleType.uppercase() in RULE_EXTRA_SUPPORTED_TYPES

@Composable
private fun SimpleTextEditorDialog(
    title: String,
    placeholder: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    AppTextFieldDialog(
        show = true,
        title = title,
        value = value,
        onValueChange = { value = it },
        onDismissRequest = onDismiss,
        onConfirm = {
            val normalizedValue = value.trim()
            if (normalizedValue.isNotBlank()) {
                onConfirm(normalizedValue)
            }
        },
        label = placeholder,
    )
}

@Composable
private fun KeyValueFormDialog(
    title: String,
    keyPlaceholder: String,
    valuePlaceholder: String,
    existingKeys: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    initialKey: String,
    initialValue: String,
    currentEditingKey: String? = null,
) {
    var key by remember(initialKey) { mutableStateOf(initialKey) }
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    var error by remember { mutableStateOf<String?>(null) }
    val keyEmptyError = stringResource(LocaleR.string.component_editor_error_key_empty)
    val keyExistsError = stringResource(LocaleR.string.component_editor_error_key_exists)

    AppFormDialog(
        show = true,
        title = title,
        onDismissRequest = onDismiss,
        onConfirm = {
            val normalizedKey = key.trim()
            val normalizedValue = value.trim()
            error = when {
                normalizedKey.isBlank() -> keyEmptyError
                normalizedKey != currentEditingKey && normalizedKey in existingKeys -> keyExistsError
                else -> null
            }
            if (error == null) {
                onConfirm(normalizedKey, normalizedValue)
            }
        },
        error = error,
    ) {
        YumeMd3OutlinedTextField(
            value = key,
            onValueChange = {
                key = it
                error = null
            },
            label = keyPlaceholder,
            modifier = Modifier.fillMaxWidth(),
        )
        YumeMd3OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = valuePlaceholder,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RuleEditorDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var ruleType by remember { mutableStateOf("DOMAIN-SUFFIX") }
    var payload by remember { mutableStateOf("") }
    val targetReject = stringResource(LocaleR.string.component_editor_rule_target_reject)
    val targetDirect = stringResource(LocaleR.string.component_editor_rule_target_direct)
    val targetMatch = stringResource(LocaleR.string.component_editor_rule_target_match)
    val contentRequiredError = stringResource(LocaleR.string.component_editor_rule_error_content_required)
    var target by remember(targetReject) { mutableStateOf(targetReject) }
    var useSrc by remember { mutableStateOf(false) }
    var useNoResolve by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val targetItems = remember(targetReject, targetDirect, targetMatch) {
        listOf(
            targetReject,
            targetDirect,
            targetMatch,
        )
    }
    val selectedRuleTypeIndex = remember(ruleType) {
        RULE_TYPE_PRESETS.indexOfFirst { it.equals(ruleType, ignoreCase = true) }.coerceAtLeast(0)
    }
    val selectedTargetIndex = remember(target) {
        targetItems.indexOf(target).coerceAtLeast(0)
    }

    AppFormDialog(
        show = true,
        title = title,
        onDismissRequest = onDismiss,
        onConfirm = {
            val normalizedType = ruleType.trim().uppercase()
            val normalizedPayload = payload.trim()

            if (target != targetMatch && normalizedPayload.isBlank()) {
                error = contentRequiredError
                return@AppFormDialog
            }

            val result = if (target == targetMatch) {
                "MATCH"
            } else {
                buildList {
                    add(normalizedType)
                    add(normalizedPayload)
                    add(target)
                    if (supportsRuleExtra(normalizedType)) {
                        if (useSrc) add("src")
                        if (useNoResolve) add("no-resolve")
                    }
                }.joinToString(",")
            }
            onConfirm(result)
        },
        error = error,
    ) {
        YumeMd3DropdownPreference(
            title = stringResource(LocaleR.string.component_editor_rule_type),
            items = RULE_TYPE_PRESETS,
            selectedIndex = selectedRuleTypeIndex,
            onSelectedIndexChange = { index ->
                ruleType = RULE_TYPE_PRESETS.getOrElse(index) { ruleType }
                error = null
            },
        )
        YumeMd3DropdownPreference(
            title = stringResource(LocaleR.string.component_editor_rule_target),
            items = targetItems,
            selectedIndex = selectedTargetIndex,
            onSelectedIndexChange = { index ->
                target = targetItems.getOrElse(index) { target }
                error = null
            },
        )
        YumeMd3OutlinedTextField(
            value = payload,
            onValueChange = {
                payload = it
                error = null
            },
            label = stringResource(LocaleR.string.component_editor_rule_content),
            modifier = Modifier.fillMaxWidth(),
        )
        if (supportsRuleExtra(ruleType)) {
            PreferenceValueItem(
                title = stringResource(LocaleR.string.component_editor_rule_src),
                summary = null,
                onClick = { useSrc = !useSrc },
                endActions = {
                    Checkbox(
                        checked = useSrc,
                        onCheckedChange = { checked -> useSrc = checked },
                    )
                },
            )
            PreferenceValueItem(
                title = stringResource(LocaleR.string.component_editor_rule_no_resolve),
                summary = null,
                onClick = { useNoResolve = !useNoResolve },
                endActions = {
                    HapticSwitch(
                        checked = useNoResolve,
                        onCheckedChange = { checked -> useNoResolve = checked },
                    )
                },
            )
        }
    }
}
