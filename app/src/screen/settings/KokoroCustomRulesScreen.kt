/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.screen.settings

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
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.common.util.toast
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.data.integration.kokoro.KokoroCustomRuleInput
import com.amamiyakokoro.box.data.integration.kokoro.KokoroCustomRulesOptions
import com.amamiyakokoro.box.screen.profiles.KokoroAuthState
import com.amamiyakokoro.box.presentation.component.AppDialog
import com.amamiyakokoro.box.presentation.component.AppActionBottomSheet
import com.amamiyakokoro.box.presentation.component.AppBottomSheetCloseAction
import com.amamiyakokoro.box.presentation.component.AppBottomSheetConfirmAction
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.DialogButtonRow
import com.amamiyakokoro.box.presentation.component.Md3EIndeterminateCircularWavyProgressIndicator
import com.amamiyakokoro.box.presentation.component.ScreenLazyColumn
import com.amamiyakokoro.box.presentation.component.Title
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3OutlinedTextField
import com.amamiyakokoro.box.presentation.component.rememberStandalonePageMainPadding
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.UiDp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
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
    var showRuleSheet by remember { mutableStateOf(false) }
    val savedMessage = stringResource(LocaleR.string.meta_feature_custom_rules_saved)
    val validationMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_validation)
    val validationGeneralMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_validation_general)
    val notFoundMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_not_found)
    val rateLimitedMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_rate_limited)
    val unknownMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_unknown)
    val requestFailedMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_request)

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.status) {
        val message = when (state.status) {
            KokoroRulesStatus.SAVED -> savedMessage
            KokoroRulesStatus.VALIDATION_FAILED -> state.validationRuleIndex?.let {
                validationMessage.format(it + 1)
            } ?: validationGeneralMessage
            KokoroRulesStatus.NOT_FOUND -> notFoundMessage
            KokoroRulesStatus.RATE_LIMITED -> rateLimitedMessage
            KokoroRulesStatus.SAVE_OUTCOME_UNKNOWN -> unknownMessage
            KokoroRulesStatus.REQUEST_FAILED -> requestFailedMessage
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
            PendingRulesAction.Reload -> viewModel.refresh()
            PendingRulesAction.Exit -> navigator.navigateUp()
        }
    }

    fun requestAction(action: PendingRulesAction) {
        if (state.dirty) pendingAction = action else performPendingAction(action)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(LocaleR.string.meta_feature_custom_rules_title),
                actions = {
                    IconButton(
                        enabled = !state.loading && !state.saving,
                        onClick = { requestAction(PendingRulesAction.Reload) },
                    ) {
                        Icon(
                            AppMd3Icons.Action.Refresh,
                            stringResource(LocaleR.string.meta_feature_custom_rules_refresh),
                        )
                    }
                    IconButton(
                        enabled = state.authState is KokoroAuthState.Authenticated &&
                            state.defaultRuleSet != null &&
                            state.draftRules.size < state.options.maxRulesPerSet &&
                            !state.loading && !state.saving,
                        onClick = {
                            editingRuleIndex = -1
                            showRuleSheet = true
                        },
                    ) {
                        Icon(
                            AppMd3Icons.Action.Add,
                            stringResource(LocaleR.string.meta_feature_custom_rules_add_rule),
                        )
                    }
                    IconButton(
                        enabled = state.dirty && !state.saving && state.defaultRuleSet != null,
                        onClick = viewModel::save,
                    ) {
                        Icon(
                            AppMd3Icons.Action.Save,
                            stringResource(LocaleR.string.meta_feature_custom_rules_save),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, rememberStandalonePageMainPadding()),
        ) {
            if (state.authState is KokoroAuthState.Authenticated) {
                item("rules-title") { Title(stringResource(LocaleR.string.meta_feature_custom_rules_rules)) }
                when {
                    state.loading -> item("loading") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(UiDp.dp24),
                            horizontalArrangement = Arrangement.spacedBy(UiDp.dp12, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Md3EIndeterminateCircularWavyProgressIndicator()
                            Text(stringResource(LocaleR.string.meta_feature_custom_rules_loading))
                        }
                    }

                    state.status == KokoroRulesStatus.LOAD_FAILED -> item("error") {
                        Card {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
                                verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
                            ) {
                                Text(stringResource(LocaleR.string.meta_feature_custom_rules_error_load))
                                Button(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth()) {
                                    Text(stringResource(LocaleR.string.meta_feature_custom_rules_retry))
                                }
                            }
                        }
                    }

                    else -> {
                        if (state.draftRules.isEmpty()) {
                            item("empty") {
                                Card {
                                    Text(
                                        text = stringResource(LocaleR.string.meta_feature_custom_rules_empty),
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
                                        showRuleSheet = true
                                    },
                                    onDelete = { viewModel.deleteRule(index) },
                                    onMoveUp = { viewModel.moveRule(index, -1) },
                                    onMoveDown = { viewModel.moveRule(index, 1) },
                                )
                            }
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
                            Text(stringResource(LocaleR.string.profiles_page_kokoro_login_required))
                            Button(onClick = navigator::navigateUp, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(LocaleR.string.meta_feature_custom_rules_back_to_kokoro_settings))
                            }
                        }
                    }
                }
            }
        }
    }

    RuleEditorSheet(
        show = showRuleSheet,
        options = state.options,
        initialRule = state.draftRules.getOrNull(editingRuleIndex),
        onDismiss = { showRuleSheet = false },
        onConfirm = { rule ->
            if (editingRuleIndex >= 0) viewModel.updateRule(editingRuleIndex, rule)
            else viewModel.addRule(rule)
            showRuleSheet = false
        },
    )

    AppDialog(
        show = pendingAction != null,
        title = stringResource(LocaleR.string.meta_feature_custom_rules_discard_title),
        summary = stringResource(LocaleR.string.meta_feature_custom_rules_discard_message),
        onDismissRequest = { pendingAction = null },
    ) {
        DialogButtonRow(
            onCancel = { pendingAction = null },
            onConfirm = {
                pendingAction?.let(::performPendingAction)
                pendingAction = null
            },
            cancelText = stringResource(LocaleR.string.meta_feature_custom_rules_cancel),
            confirmText = stringResource(LocaleR.string.meta_feature_custom_rules_confirm),
            confirmDestructive = true,
        )
    }

    AppDialog(
        show = state.conflict != null,
        title = stringResource(LocaleR.string.meta_feature_custom_rules_conflict_title),
        summary = stringResource(LocaleR.string.meta_feature_custom_rules_conflict_message),
        onDismissRequest = {},
    ) {
        DialogButtonRow(
            onCancel = viewModel::useRemoteConflict,
            onConfirm = viewModel::keepLocalConflict,
            cancelText = stringResource(LocaleR.string.meta_feature_custom_rules_use_remote),
            confirmText = stringResource(LocaleR.string.meta_feature_custom_rules_keep_local),
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
                        Icon(AppMd3Icons.Action.MoveUp, stringResource(LocaleR.string.meta_feature_custom_rules_move_up))
                    }
                }
                if (canMoveDown) {
                    IconButton(onClick = onMoveDown) {
                        Icon(AppMd3Icons.Action.MoveDown, stringResource(LocaleR.string.meta_feature_custom_rules_move_down))
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(AppMd3Icons.Action.Edit, stringResource(LocaleR.string.meta_feature_custom_rules_edit_rule))
                }
                IconButton(onClick = onDelete) {
                    Icon(AppMd3Icons.Action.Delete, stringResource(LocaleR.string.meta_feature_custom_rules_delete_rule))
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
private fun RuleEditorSheet(
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
    val canConfirm = type in options.ruleTypes && target in options.targets &&
        (type == "MATCH" || payload.isNotEmpty()) &&
        (type != "RULE-SET" || payload in providers)

    AppActionBottomSheet(
        show = show,
        title = if (initialRule == null) {
            stringResource(LocaleR.string.meta_feature_custom_rules_add_rule)
        } else {
            stringResource(LocaleR.string.meta_feature_custom_rules_edit_rule)
        },
        onDismissRequest = onDismiss,
        startAction = {
            AppBottomSheetCloseAction(
                onClick = onDismiss,
                contentDescription = stringResource(LocaleR.string.meta_feature_custom_rules_cancel),
            )
        },
        endAction = {
            AppBottomSheetConfirmAction(
                enabled = canConfirm,
                contentDescription = stringResource(LocaleR.string.meta_feature_custom_rules_confirm),
                onClick = {
                    onConfirm(
                        KokoroCustomRuleInput(
                            type = type,
                            payload = if (type == "MATCH") null else payload,
                            target = target,
                        ),
                    )
                },
            )
        },
    ) {
        Card {
            YumeMd3DropdownPreference(
                title = stringResource(LocaleR.string.meta_feature_custom_rules_type),
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
                    text = stringResource(LocaleR.string.meta_feature_custom_rules_match_payload_hint),
                    modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                "RULE-SET" -> YumeMd3DropdownPreference(
                    title = stringResource(LocaleR.string.meta_feature_custom_rules_provider),
                    items = providers,
                    selectedIndex = providers.indexOf(payload).coerceAtLeast(0),
                    onSelectedIndexChange = { index -> payload = providers.getOrNull(index).orEmpty() },
                    enabled = providers.isNotEmpty(),
                )
                else -> YumeMd3OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().padding(UiDp.dp12),
                    value = payload,
                    onValueChange = { if (it.length <= options.maxPayloadLength) payload = it },
                    label = stringResource(LocaleR.string.meta_feature_custom_rules_payload),
                    singleLine = true,
                )
            }
            YumeMd3DropdownPreference(
                title = stringResource(LocaleR.string.meta_feature_custom_rules_target),
                items = targets,
                selectedIndex = targets.indexOf(target).coerceAtLeast(0),
                onSelectedIndexChange = { index -> target = targets.getOrElse(index) { defaultTarget } },
            )
        }
        Spacer(Modifier.height(UiDp.dp16))
    }
}
