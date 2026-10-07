/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.screen.settings

import android.content.Intent
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amamiyakokoro.box.data.model.ThemeMode
import com.amamiyakokoro.box.presentation.theme.YumeTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.MainActivity
import com.amamiyakokoro.box.screen.profiles.KokoroAccountCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.amamiyakokoro.box.data.integration.kokoro.kokoroConnectionRuleHost
import com.amamiyakokoro.box.data.integration.kokoro.preferredKokoroDomainRuleType
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
fun KokoroCustomRulesScreen(navigator: DestinationsNavigator, initialHost: String? = null) {
    val viewModel = koinViewModel<KokoroCustomRulesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingAction by remember { mutableStateOf<PendingRulesAction?>(null) }
    var editingRuleIndex by rememberSaveable { mutableIntStateOf(-1) }
    var showRuleSheet by rememberSaveable { mutableStateOf(false) }
    var connectionSeedConsumed by rememberSaveable { mutableStateOf(false) }
    var useConnectionSeed by rememberSaveable { mutableStateOf(false) }
    val seedHost = remember(initialHost) { initialHost?.let(::kokoroConnectionRuleHost).orEmpty() }
    val loginViewModel = koinViewModel<KokoroSettingsViewModel>()
    val loginState by loginViewModel.authState.collectAsStateWithLifecycle()
    val authResult by MainActivity.kokoroAuthResult.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    fun beginLogin() {
        scope.launch {
            var loginUrl: String? = null
            try {
                loginUrl = loginViewModel.beginLogin()
                context.startActivity(Intent(Intent.ACTION_VIEW, loginUrl.toUri()).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                })
            } catch (error: Exception) {
                loginUrl?.let { loginViewModel.cancelLogin(it) }
                if (error is CancellationException) throw error
                loginViewModel.reportLoginFailure()
            }
        }
    }
    val savedMessage = stringResource(LocaleR.string.meta_feature_custom_rules_saved)
    val validationMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_validation)
    val validationGeneralMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_validation_general)
    val notFoundMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_not_found)
    val rateLimitedMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_rate_limited)
    val unknownMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_unknown)
    val requestFailedMessage = stringResource(LocaleR.string.meta_feature_custom_rules_error_request)

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(authResult) {
        if (authResult == true) viewModel.refresh()
        if (authResult == false) loginViewModel.reportLoginFailure()
        if (authResult != null) MainActivity.clearKokoroAuthResult()
    }
    LaunchedEffect(state.loading, state.defaultRuleSet, state.authState) {
        if (initialHost != null && !connectionSeedConsumed && !state.loading &&
            state.defaultRuleSet != null && state.authState is KokoroAuthState.Authenticated &&
            state.status != KokoroRulesStatus.LOAD_FAILED
        ) {
            connectionSeedConsumed = true
            useConnectionSeed = true
            editingRuleIndex = -1
            showRuleSheet = true
        }
    }
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
                            useConnectionSeed = false
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
                if (state.subscriptionUpdatePending) {
                    item("subscription-update") {
                        Card {
                            Column(Modifier.padding(UiDp.dp16), verticalArrangement = Arrangement.spacedBy(UiDp.dp8)) {
                                if (state.saving) {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(UiDp.dp12)) {
                                        Md3EIndeterminateCircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
                                        Text(stringResource(LocaleR.string.meta_feature_custom_rules_updating_subscription))
                                    }
                                } else {
                                    Text(stringResource(LocaleR.string.meta_feature_custom_rules_subscription_update_failed),
                                        color = MaterialTheme.colorScheme.error)
                                    Button(onClick = viewModel::retrySubscriptionUpdate, enabled = !state.loading) {
                                        Text(stringResource(LocaleR.string.meta_feature_custom_rules_retry_subscription_update))
                                    }
                                }
                            }
                        }
                    }
                }
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
                                        useConnectionSeed = false
                                        editingRuleIndex = index
                                        showRuleSheet = true
                                    },
                                    onDelete = { viewModel.deleteRule(index) },
                                    onMoveUp = { viewModel.moveRule(index, -1) },
                                    onMoveDown = { viewModel.moveRule(index, 1) },
                                )
                            }
                        }
                        item("bottom-space") { Spacer(Modifier.height(UiDp.dp16)) }
                    }
                }
            } else if (!state.loading) {
                item("authentication-required") {
                    Card {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
                            verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
                        ) {
                            KokoroAccountCard(
                                authState = if (loginState is KokoroAuthState.Error) loginState else state.authState,
                                onLogin = ::beginLogin,
                                onLogout = {},
                                onRetry = viewModel::refresh,
                            )
                        }
                    }
                }
            }
        }
    }

    RuleEditorSheet(
        show = showRuleSheet,
        options = state.options,
        initialRule = if (useConnectionSeed) KokoroCustomRuleInput(
            type = preferredKokoroDomainRuleType(state.options.ruleTypes),
            payload = seedHost,
            target = state.options.targets.firstOrNull().orEmpty(),
        ) else state.draftRules.getOrNull(editingRuleIndex),
        fromConnection = useConnectionSeed,
        canAdd = editingRuleIndex >= 0 || state.draftRules.size < state.options.maxRulesPerSet,
        onDismiss = { showRuleSheet = false },
        onConfirm = { rule ->
            if (editingRuleIndex >= 0) viewModel.updateRule(editingRuleIndex, rule)
            else if (useConnectionSeed) viewModel.addConnectionRule(rule)
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
    var menuExpanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val typeLabel = ruleTypeLabel(rule.type)
    Card(modifier = Modifier.fillMaxWidth(), cornerRadius = 20) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = rule.payload?.takeIf(String::isNotBlank)
                            ?: if (rule.type == "MATCH") {
                                stringResource(LocaleR.string.meta_feature_custom_rules_all_traffic)
                            } else typeLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // Keep the standard 48dp touch targets, with restrained 20dp icons.
                IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                    Icon(
                        AppMd3Icons.Action.Edit,
                        stringResource(LocaleR.string.meta_feature_custom_rules_edit_rule),
                        modifier = Modifier.size(20.dp),
                        tint = colors.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            AppMd3Icons.Action.More,
                            stringResource(LocaleR.string.meta_feature_custom_rules_more_actions),
                            modifier = Modifier.size(20.dp),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        if (canMoveUp) {
                            DropdownMenuItem(
                                text = { Text(stringResource(LocaleR.string.meta_feature_custom_rules_move_up)) },
                                leadingIcon = { Icon(AppMd3Icons.Action.MoveUp, null) },
                                onClick = { menuExpanded = false; onMoveUp() },
                            )
                        }
                        if (canMoveDown) {
                            DropdownMenuItem(
                                text = { Text(stringResource(LocaleR.string.meta_feature_custom_rules_move_down)) },
                                leadingIcon = { Icon(AppMd3Icons.Action.MoveDown, null) },
                                onClick = { menuExpanded = false; onMoveDown() },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(LocaleR.string.meta_feature_custom_rules_delete_rule), color = colors.error) },
                            leadingIcon = { Icon(AppMd3Icons.Action.Delete, null, tint = colors.error) },
                            onClick = { menuExpanded = false; onDelete() },
                        )
                    }
                }
            }
            // A non-interactive chip avoids implying that the target is a separate action.
            Surface(
                color = colors.surfaceContainerHigh,
                contentColor = colors.onSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.heightIn(min = 28.dp),
            ) {
                Text(
                    text = rule.target,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ruleTypeLabel(type: String): String = when (type) {
    "RULE-SET" -> stringResource(LocaleR.string.meta_feature_custom_rules_type_rule_set)
    "DOMAIN" -> stringResource(LocaleR.string.meta_feature_custom_rules_type_domain)
    "DOMAIN-SUFFIX" -> stringResource(LocaleR.string.meta_feature_custom_rules_type_domain_suffix)
    "DOMAIN-KEYWORD" -> stringResource(LocaleR.string.meta_feature_custom_rules_type_domain_keyword)
    "MATCH" -> stringResource(LocaleR.string.meta_feature_custom_rules_type_match)
    "IP-CIDR" -> "IP CIDR"
    "IP-CIDR6" -> "IPv6 CIDR"
    "GEOIP" -> "GeoIP"
    "GEOSITE" -> "GeoSite"
    else -> type.replace('-', ' ').lowercase().replaceFirstChar { it.titlecase() }
}

@Preview(name = "Compact rules / light / narrow", widthDp = 320, showBackground = true)
@Preview(name = "Compact rules / dark / narrow", widthDp = 320, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(name = "Compact rules / large text", widthDp = 320, fontScale = 1.5f, showBackground = true)
@Composable
private fun RuleCardsPreview(themeMode: ThemeMode? = null) {
    YumeTheme(themeMode = themeMode) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                listOf(
                    KokoroCustomRuleInput("RULE-SET", "osu", "TW"),
                    KokoroCustomRuleInput("DOMAIN", "reject-ads", "REJECT"),
                    KokoroCustomRuleInput("DOMAIN-SUFFIX", "a-very-long-rule-name.example.co.uk", "A very long target name"),
                    KokoroCustomRuleInput("MATCH", null, "DIRECT"),
                ).forEachIndexed { index, rule ->
                    RuleCard(rule, index > 0, index < 3, {}, {}, {}, {})
                }
            }
        }
    }
}

@Composable
private fun RuleEditorSheet(
    show: Boolean,
    options: KokoroCustomRulesOptions,
    initialRule: KokoroCustomRuleInput?,
    fromConnection: Boolean = false,
    canAdd: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (KokoroCustomRuleInput) -> Unit,
) {
    val editor = rememberKokoroRuleEditorState(options, initialRule, fromConnection, canAdd, resetKey = show)

    AppActionBottomSheet(
        show = show,
        title = if (initialRule == null || fromConnection) {
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
                enabled = editor.canConfirm,
                contentDescription = stringResource(LocaleR.string.meta_feature_custom_rules_confirm),
                onClick = {
                    onConfirm(editor.rule)
                },
            )
        },
    ) {
        KokoroRuleEditorContent(editor, canAdd)
    }
}
