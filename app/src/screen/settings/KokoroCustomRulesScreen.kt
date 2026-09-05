/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.settings

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.yumelira.yumebox.common.util.toast
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroCustomRuleInput
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroCustomRulesOptions
import com.github.yumelira.yumebox.screen.profiles.KokoroAuthState
import com.github.yumelira.yumebox.presentation.component.AppDialog
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
import org.koin.androidx.compose.koinViewModel

private sealed interface PendingRulesAction {
    data object Reload : PendingRulesAction
    data object Exit : PendingRulesAction
}

@Composable
@Destination<RootGraph>
fun KokoroCustomRulesScreen(navigator: DestinationsNavigator) {
    val viewModel = koinViewModel<KokoroCustomRulesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingAction by remember { mutableStateOf<PendingRulesAction?>(null) }
    var editingRuleIndex by remember { mutableIntStateOf(-1) }
    var showRuleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.status) {
        val message = when (state.status) {
            KokoroRulesStatus.SAVED -> MLang.MetaFeature.CustomRules.Saved
            KokoroRulesStatus.VALIDATION_FAILED -> state.validationRuleIndex?.let {
                MLang.MetaFeature.CustomRules.ErrorValidation.format(it + 1)
            } ?: MLang.MetaFeature.CustomRules.ErrorValidationGeneral
            KokoroRulesStatus.NOT_FOUND -> MLang.MetaFeature.CustomRules.ErrorNotFound
            KokoroRulesStatus.RATE_LIMITED -> MLang.MetaFeature.CustomRules.ErrorRateLimited
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
                        enabled = state.dirty && !state.saving && state.defaultRuleSet != null,
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
            if (state.authState is KokoroAuthState.Authenticated) {
                item("rules-title") { Title(MLang.MetaFeature.CustomRules.Rules) }
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
                                enabled = state.defaultRuleSet != null &&
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
            } else if (!state.loading) {
                item("authentication-required") {
                    Card {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
                            verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
                        ) {
                            Text(MLang.ProfilesPage.Kokoro.LoginRequired)
                            Button(onClick = navigator::navigateUp, modifier = Modifier.fillMaxWidth()) {
                                Text(MLang.MetaFeature.CustomRules.BackToKokoroSettings)
                            }
                        }
                    }
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
        Column(
            modifier = Modifier.fillMaxWidth().padding(UiDp.dp12),
            verticalArrangement = Arrangement.spacedBy(UiDp.dp8),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = rule.type,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (canMoveUp) {
                    IconButton(onClick = onMoveUp) {
                        Icon(AppMd3Icons.Action.MoveUp, MLang.MetaFeature.CustomRules.MoveUp)
                    }
                }
                if (canMoveDown) {
                    IconButton(onClick = onMoveDown) {
                        Icon(AppMd3Icons.Action.MoveDown, MLang.MetaFeature.CustomRules.MoveDown)
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(AppMd3Icons.Action.Edit, MLang.MetaFeature.CustomRules.EditRule)
                }
                IconButton(onClick = onDelete) {
                    Icon(AppMd3Icons.Action.Delete, MLang.MetaFeature.CustomRules.DeleteRule)
                }
            }
            rule.payload?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = rule.target,
                    modifier = Modifier.padding(horizontal = UiDp.dp8, vertical = UiDp.dp4),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
    Spacer(Modifier.height(UiDp.dp8))
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
