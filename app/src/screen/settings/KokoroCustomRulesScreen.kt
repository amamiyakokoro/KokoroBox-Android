/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.settings

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.yumelira.yumebox.MainActivity
import com.github.yumelira.yumebox.common.util.toast
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroCustomRuleInput
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroCustomRulesOptions
import com.github.yumelira.yumebox.presentation.component.AppDialog
import com.github.yumelira.yumebox.presentation.component.AppTextFieldDialog
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.DialogButtonRow
import com.github.yumelira.yumebox.presentation.component.Md3EIndeterminateCircularWavyProgressIndicator
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DropdownPreference
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3OutlinedTextField
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private sealed interface PendingRulesAction {
    data object Reload : PendingRulesAction
    data object Exit : PendingRulesAction
    data class SelectSet(val setId: Long) : PendingRulesAction
}

@Composable
@Destination<RootGraph>
fun KokoroCustomRulesScreen(navigator: DestinationsNavigator) {
    val viewModel = koinViewModel<KokoroCustomRulesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authResult by MainActivity.kokoroAuthResult.collectAsStateWithLifecycle()
    var pendingAction by remember { mutableStateOf<PendingRulesAction?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editingRuleIndex by remember { mutableIntStateOf(-1) }
    var showRuleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(authResult) {
        if (authResult == true) viewModel.load()
    }
    LaunchedEffect(state.status) {
        val message = when (state.status) {
            KokoroRulesStatus.SAVED -> MLang.MetaFeature.CustomRules.Saved
            KokoroRulesStatus.VALIDATION_FAILED -> state.validationRuleIndex?.let {
                MLang.MetaFeature.CustomRules.ErrorValidation.format(it + 1)
            } ?: MLang.MetaFeature.CustomRules.ErrorValidationGeneral
            KokoroRulesStatus.NOT_FOUND -> MLang.MetaFeature.CustomRules.ErrorNotFound
            KokoroRulesStatus.RATE_LIMITED -> MLang.MetaFeature.CustomRules.ErrorRateLimited
            KokoroRulesStatus.SET_CONFLICT -> MLang.MetaFeature.CustomRules.ErrorConflict
            KokoroRulesStatus.SAVE_OUTCOME_UNKNOWN -> MLang.MetaFeature.CustomRules.ErrorUnknown
            KokoroRulesStatus.REQUEST_FAILED -> MLang.MetaFeature.CustomRules.ErrorRequest
            else -> null
        }
        if (message != null) {
            context.toast(message)
            viewModel.clearStatus()
        }
    }
    BackHandler(enabled = state.dirty) {
        pendingAction = PendingRulesAction.Exit
    }

    fun performPendingAction(action: PendingRulesAction) {
        when (action) {
            PendingRulesAction.Reload -> viewModel.load()
            PendingRulesAction.Exit -> navigator.navigateUp()
            is PendingRulesAction.SelectSet -> viewModel.selectSet(action.setId)
        }
    }

    fun requestAction(action: PendingRulesAction) {
        if (state.dirty) pendingAction = action else performPendingAction(action)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = MLang.MetaFeature.CustomRules.Title,
                actions = {
                    IconButton(
                        enabled = !state.loading && !state.saving,
                        onClick = { requestAction(PendingRulesAction.Reload) },
                    ) {
                        Icon(AppMd3Icons.Action.Refresh, MLang.MetaFeature.CustomRules.Refresh)
                    }
                    IconButton(
                        enabled = state.dirty && !state.saving && state.selectedSet != null,
                        onClick = viewModel::save,
                    ) {
                        Icon(AppMd3Icons.Action.Save, MLang.MetaFeature.CustomRules.Save)
                    }
                },
            )
        },
    ) { innerPadding ->
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, rememberStandalonePageMainPadding()),
        ) {
            when {
                state.loading -> item("loading") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(UiDp.dp24),
                        horizontalArrangement = Arrangement.spacedBy(UiDp.dp12, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Md3EIndeterminateCircularWavyProgressIndicator()
                        Text(MLang.MetaFeature.CustomRules.Loading)
                    }
                }

                state.status == KokoroRulesStatus.AUTH_REQUIRED -> item("auth") {
                    Card {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
                            verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
                        ) {
                            Text(MLang.MetaFeature.CustomRules.SignInRequired)
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    scope.launch {
                                        var loginUrl: String? = null
                                        try {
                                            loginUrl = viewModel.beginLogin()
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, loginUrl.toUri()).apply {
                                                    addCategory(Intent.CATEGORY_BROWSABLE)
                                                },
                                            )
                                        } catch (error: Exception) {
                                            loginUrl?.let { viewModel.cancelLogin(it) }
                                            if (error is CancellationException) throw error
                                            context.toast(MLang.MetaFeature.CustomRules.ErrorRequest)
                                        }
                                    }
                                },
                            ) { Text(MLang.MetaFeature.CustomRules.SignIn) }
                        }
                    }
                }

                state.status == KokoroRulesStatus.LOAD_FAILED -> item("error") {
                    Card {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
                            verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
                        ) {
                            Text(MLang.MetaFeature.CustomRules.ErrorLoad)
                            Button(onClick = viewModel::load, modifier = Modifier.fillMaxWidth()) {
                                Text(MLang.MetaFeature.CustomRules.Retry)
                            }
                        }
                    }
                }

                else -> {
                    item("set-title") { Title(MLang.MetaFeature.CustomRules.Set) }
                    item("set-selector") {
                        Card {
                            YumeMd3DropdownPreference(
                                title = MLang.MetaFeature.CustomRules.Set,
                                items = state.sets.map { it.name },
                                selectedIndex = state.sets.indexOfFirst { it.id == state.selectedSetId }
                                    .coerceAtLeast(0),
                                onSelectedIndexChange = { index ->
                                    state.sets.getOrNull(index)?.let {
                                        requestAction(PendingRulesAction.SelectSet(it.id))
                                    }
                                },
                                enabled = state.sets.isNotEmpty() && !state.saving,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(UiDp.dp12),
                                horizontalArrangement = Arrangement.spacedBy(UiDp.dp8),
                            ) {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    enabled = state.sets.size < state.options.maxRuleSets &&
                                        !state.saving && !state.dirty,
                                    onClick = { showCreateDialog = true },
                                ) { Text(MLang.MetaFeature.CustomRules.CreateSet) }
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    enabled = state.selectedSet?.name != "default" && !state.saving,
                                    onClick = { showRenameDialog = true },
                                ) { Text(MLang.MetaFeature.CustomRules.RenameSet) }
                            }
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = UiDp.dp12, vertical = UiDp.dp4),
                                enabled = state.selectedSet?.name != "default" && !state.saving,
                                onClick = { showDeleteDialog = true },
                            ) { Text(MLang.MetaFeature.CustomRules.DeleteSet) }
                        }
                    }
                    item("rules-title") { Title(MLang.MetaFeature.CustomRules.Rules) }
                    if (state.draftRules.isEmpty()) {
                        item("empty") {
                            Card {
                                Text(
                                    text = MLang.MetaFeature.CustomRules.Empty,
                                    modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        items(state.draftRules.size, key = { "rule-$it-${state.draftRules[it]}" }) { index ->
                            val rule = state.draftRules[index]
                            RuleCard(
                                rule = rule,
                                canMoveUp = index > 0,
                                canMoveDown = index < state.draftRules.lastIndex,
                                onEdit = {
                                    editingRuleIndex = index
                                    showRuleDialog = true
                                },
                                onDelete = { viewModel.deleteRule(index) },
                                onMoveUp = { viewModel.moveRule(index, -1) },
                                onMoveDown = { viewModel.moveRule(index, 1) },
                            )
                        }
                    }
                    item("add-rule") {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.selectedSet != null &&
                                state.draftRules.size < state.options.maxRulesPerSet && !state.saving,
                            onClick = {
                                editingRuleIndex = -1
                                showRuleDialog = true
                            },
                        ) { Text(MLang.MetaFeature.CustomRules.AddRule) }
                    }
                    item("bottom-space") { Spacer(Modifier.height(UiDp.dp32)) }
                }
            }
        }
    }

    RuleEditorDialog(
        show = showRuleDialog,
        options = state.options,
        initialRule = state.draftRules.getOrNull(editingRuleIndex),
        onDismiss = { showRuleDialog = false },
        onConfirm = { rule ->
            if (editingRuleIndex >= 0) viewModel.updateRule(editingRuleIndex, rule)
            else viewModel.addRule(rule)
            showRuleDialog = false
        },
    )

    RuleSetNameDialog(
        show = showCreateDialog,
        title = MLang.MetaFeature.CustomRules.CreateSet,
        initialName = "",
        maxLength = state.options.maxNameLength,
        onDismiss = { showCreateDialog = false },
        onConfirm = {
            viewModel.createSet(it)
            showCreateDialog = false
        },
    )
    RuleSetNameDialog(
        show = showRenameDialog,
        title = MLang.MetaFeature.CustomRules.RenameSet,
        initialName = state.selectedSet?.name.orEmpty(),
        maxLength = state.options.maxNameLength,
        onDismiss = { showRenameDialog = false },
        onConfirm = {
            viewModel.renameSelectedSet(it)
            showRenameDialog = false
        },
    )

    AppDialog(
        show = showDeleteDialog,
        title = MLang.MetaFeature.CustomRules.DeleteSetTitle,
        summary = MLang.MetaFeature.CustomRules.DeleteSetMessage.format(state.selectedSet?.name.orEmpty()),
        onDismissRequest = { showDeleteDialog = false },
    ) {
        DialogButtonRow(
            onCancel = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteSelectedSet()
            },
            cancelText = MLang.MetaFeature.CustomRules.Cancel,
            confirmText = MLang.MetaFeature.CustomRules.DeleteSet,
            confirmDestructive = true,
        )
    }

    AppDialog(
        show = pendingAction != null,
        title = MLang.MetaFeature.CustomRules.DiscardTitle,
        summary = MLang.MetaFeature.CustomRules.DiscardMessage,
        onDismissRequest = { pendingAction = null },
    ) {
        DialogButtonRow(
            onCancel = { pendingAction = null },
            onConfirm = {
                pendingAction?.let(::performPendingAction)
                pendingAction = null
            },
            cancelText = MLang.MetaFeature.CustomRules.Cancel,
            confirmText = MLang.MetaFeature.CustomRules.Confirm,
            confirmDestructive = true,
        )
    }

    AppDialog(
        show = state.conflict != null,
        title = MLang.MetaFeature.CustomRules.ConflictTitle,
        summary = MLang.MetaFeature.CustomRules.ConflictMessage,
        onDismissRequest = {},
    ) {
        DialogButtonRow(
            onCancel = viewModel::useRemoteConflict,
            onConfirm = viewModel::keepLocalConflict,
            cancelText = MLang.MetaFeature.CustomRules.UseRemote,
            confirmText = MLang.MetaFeature.CustomRules.KeepLocal,
        )
    }
}

@Composable
private fun RuleCard(
    rule: KokoroCustomRuleInput,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Card {
        Column(modifier = Modifier.fillMaxWidth().padding(UiDp.dp12)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.type, style = MaterialTheme.typography.titleMedium)
                    rule.payload?.takeIf { it.isNotEmpty() }?.let {
                        Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(rule.target, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(AppMd3Icons.Action.MoveUp, MLang.MetaFeature.CustomRules.MoveUp)
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(AppMd3Icons.Action.MoveDown, MLang.MetaFeature.CustomRules.MoveDown)
                }
            }
            Row(modifier = Modifier.align(Alignment.End)) {
                IconButton(onClick = onEdit) {
                    Icon(AppMd3Icons.Action.Edit, MLang.MetaFeature.CustomRules.EditRule)
                }
                IconButton(onClick = onDelete) {
                    Icon(AppMd3Icons.Action.Delete, MLang.MetaFeature.CustomRules.DeleteRule)
                }
            }
        }
    }
    Spacer(Modifier.height(UiDp.dp8))
}

@Composable
private fun RuleSetNameDialog(
    show: Boolean,
    title: String,
    initialName: String,
    maxLength: Int,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember(show, initialName) { mutableStateOf(initialName) }
    AppTextFieldDialog(
        show = show,
        title = title,
        value = name,
        onValueChange = { if (it.length <= maxLength) name = it },
        onDismissRequest = onDismiss,
        onConfirm = { name.trim().takeIf { it.isNotEmpty() }?.let(onConfirm) },
        label = MLang.MetaFeature.CustomRules.SetName,
        singleLine = true,
    )
}

@Composable
private fun RuleEditorDialog(
    show: Boolean,
    options: KokoroCustomRulesOptions,
    initialRule: KokoroCustomRuleInput?,
    onDismiss: () -> Unit,
    onConfirm: (KokoroCustomRuleInput) -> Unit,
) {
    val defaultType = options.ruleTypes.firstOrNull() ?: "DOMAIN-SUFFIX"
    val defaultTarget = options.targets.firstOrNull() ?: "DIRECT"
    val types = options.ruleTypes.ifEmpty { listOf(defaultType) }
    val targets = options.targets.ifEmpty { listOf(defaultTarget) }
    val providers = options.ruleProviders.filter { it.behavior == "domain" }.map { it.name }
    var type by remember(show, initialRule, types) {
        mutableStateOf(initialRule?.type?.takeIf { it in types } ?: defaultType)
    }
    var payload by remember(show, initialRule, providers) {
        mutableStateOf(
            initialRule?.payload
                ?.takeUnless { initialRule.type == "RULE-SET" && it !in providers }
                ?: if (initialRule?.type == "RULE-SET") providers.firstOrNull().orEmpty() else "",
        )
    }
    var target by remember(show, initialRule, targets) {
        mutableStateOf(initialRule?.target?.takeIf { it in targets } ?: defaultTarget)
    }

    AppDialog(
        show = show,
        title = if (initialRule == null) MLang.MetaFeature.CustomRules.AddRule
        else MLang.MetaFeature.CustomRules.EditRule,
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(UiDp.dp12)) {
            Card {
                YumeMd3DropdownPreference(
                    title = MLang.MetaFeature.CustomRules.Type,
                    items = types,
                    selectedIndex = types.indexOf(type).coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        type = types.getOrElse(index) { defaultType }
                        when (type) {
                            "MATCH" -> payload = ""
                            "RULE-SET" -> if (payload !in providers) {
                                payload = providers.firstOrNull().orEmpty()
                            }
                        }
                    },
                )
                when (type) {
                    "MATCH" -> Text(
                        text = MLang.MetaFeature.CustomRules.MatchPayloadHint,
                        modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    "RULE-SET" -> YumeMd3DropdownPreference(
                        title = MLang.MetaFeature.CustomRules.Provider,
                        items = providers,
                        selectedIndex = providers.indexOf(payload).coerceAtLeast(0),
                        onSelectedIndexChange = { index -> payload = providers.getOrNull(index).orEmpty() },
                        enabled = providers.isNotEmpty(),
                    )
                    else -> YumeMd3OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(UiDp.dp12),
                        value = payload,
                        onValueChange = { if (it.length <= options.maxPayloadLength) payload = it },
                        label = MLang.MetaFeature.CustomRules.Payload,
                        singleLine = true,
                    )
                }
                YumeMd3DropdownPreference(
                    title = MLang.MetaFeature.CustomRules.Target,
                    items = targets,
                    selectedIndex = targets.indexOf(target).coerceAtLeast(0),
                    onSelectedIndexChange = { index -> target = targets.getOrElse(index) { defaultTarget } },
                )
            }
            DialogButtonRow(
                onCancel = onDismiss,
                onConfirm = {
                    onConfirm(
                        KokoroCustomRuleInput(
                            type = type,
                            payload = if (type == "MATCH") null else payload,
                            target = target,
                        ),
                    )
                },
                cancelText = MLang.MetaFeature.CustomRules.Cancel,
                confirmText = MLang.MetaFeature.CustomRules.Confirm,
                confirmEnabled = type in options.ruleTypes && target in options.targets &&
                    (type == "MATCH" || payload.isNotEmpty()) &&
                    (type != "RULE-SET" || payload in providers),
            )
        }
    }
}
