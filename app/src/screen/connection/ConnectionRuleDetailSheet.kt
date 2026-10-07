/*
 * This file is part of KokoroBox.
 * Licensed under the GNU Affero General Public License, version 3 or later.
 */
package com.amamiyakokoro.box.screen.connection

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amamiyakokoro.box.MainActivity
import com.amamiyakokoro.box.common.util.toast
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.model.ConnectionInfo
import com.amamiyakokoro.box.data.integration.kokoro.*
import com.amamiyakokoro.box.feature.meta.presentation.component.ConnectionDetailSheet
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.Md3EIndeterminateCircularWavyProgressIndicator
import com.amamiyakokoro.box.screen.profiles.KokoroAccountCard
import com.amamiyakokoro.box.screen.profiles.KokoroAuthState
import com.amamiyakokoro.box.screen.settings.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun ConnectionRuleDetailSheet(
    show: Boolean,
    connectionInfo: ConnectionInfo?,
    canInterrupt: Boolean,
    onInterruptConnection: suspend (String) -> Boolean,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit,
) {
    val info = connectionInfo ?: return
    // Isolate this editor from the settings screen's drafts, and release it with the sheet.
    val owner = remember(info.id) {
        object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() }
    }
    DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
    val rules = koinViewModel<KokoroCustomRulesViewModel>(viewModelStoreOwner = owner)
    val login = koinViewModel<KokoroSettingsViewModel>(viewModelStoreOwner = owner)
    ConnectionDetailSheet(
        show = show,
        connectionInfo = info,
        canInterrupt = canInterrupt,
        onInterruptConnection = onInterruptConnection,
        onDismiss = onDismiss,
        onDismissFinished = onDismissFinished,
        ruleEditor = { connection, onBack, onSavingChanged ->
            ConnectionKokoroRuleEditor(connection, rules, login, onBack, onSavingChanged)
        },
    )
}

@Composable
private fun ConnectionKokoroRuleEditor(
    connection: ConnectionInfo,
    viewModel: KokoroCustomRulesViewModel,
    login: KokoroSettingsViewModel,
    onBack: () -> Unit,
    onSavingChanged: (Boolean) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val loginState by login.authState.collectAsStateWithLifecycle()
    val authResult by MainActivity.kokoroAuthResult.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(LocaleR.string.meta_feature_custom_rules_saved)
    SideEffect { onSavingChanged(state.saving) }
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(authResult) {
        if (authResult == true) viewModel.refresh()
        if (authResult == false) login.reportLoginFailure()
        if (authResult != null) MainActivity.clearKokoroAuthResult()
    }
    LaunchedEffect(state.status) {
        if (state.status == KokoroRulesStatus.SAVED) {
            context.toast(savedMessage)
            viewModel.clearStatus()
            onSavingChanged(false)
            onBack()
        }
    }
    ConnectionRuleEditorContent(
        state = state,
        host = connection.metadata["host"]?.jsonPrimitive?.content.orEmpty(),
        loginState = loginState,
        onSave = viewModel::saveConnectionRule,
        onReload = viewModel::refresh,
        onRetrySubscriptionUpdate = viewModel::retrySubscriptionUpdate,
        onUseRemote = viewModel::useRemoteConflict,
        onKeepLocal = { viewModel.keepLocalConflict(); viewModel.save() },
        onLogin = {
            scope.launch {
                var loginUrl: String? = null
                try {
                    loginUrl = login.beginLogin()
                    context.startActivity(Intent(Intent.ACTION_VIEW, loginUrl.toUri()).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                    })
                } catch (error: Exception) {
                    loginUrl?.let { login.cancelLogin(it) }
                    if (error is CancellationException) throw error
                    login.reportLoginFailure()
                }
            }
        },
    )
}

@Composable
internal fun ConnectionRuleEditorContent(
    state: KokoroCustomRulesUiState,
    host: String,
    loginState: KokoroAuthState = KokoroAuthState.Checking,
    onSave: (KokoroCustomRuleInput) -> Unit,
    onReload: () -> Unit,
    onRetrySubscriptionUpdate: () -> Unit,
    onUseRemote: () -> Unit,
    onKeepLocal: () -> Unit,
    onLogin: () -> Unit,
) {
    val canAdd = (state.defaultRuleSet?.rules?.size ?: 0) < state.options.maxRulesPerSet
    val editor = rememberKokoroRuleEditorState(
        options = state.options,
        initialRule = KokoroCustomRuleInput(
            preferredKokoroDomainRuleType(state.options.ruleTypes),
            kokoroConnectionRuleHost(host).orEmpty(),
            state.options.targets.firstOrNull().orEmpty(),
        ),
        fromConnection = true,
        canAdd = canAdd,
        enabled = !state.loading && !state.saving && !state.subscriptionUpdatePending && state.defaultRuleSet != null &&
            state.authState is KokoroAuthState.Authenticated && state.conflict == null &&
            state.status != KokoroRulesStatus.SAVE_OUTCOME_UNKNOWN && state.status != KokoroRulesStatus.AUTH_REQUIRED,
    )
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)
            .imePadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            state.loading -> Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Md3EIndeterminateCircularWavyProgressIndicator()
                Text(stringResource(LocaleR.string.meta_feature_custom_rules_loading))
            }
            state.status == KokoroRulesStatus.AUTH_REQUIRED || state.authState !is KokoroAuthState.Authenticated -> {
                val authState = if (loginState is KokoroAuthState.Error) loginState else
                    if (state.status == KokoroRulesStatus.AUTH_REQUIRED) KokoroAuthState.LoggedOut else state.authState
                KokoroAccountCard(authState, onLogin, {}, onReload)
            }
            state.defaultRuleSet == null || state.status == KokoroRulesStatus.LOAD_FAILED -> {
                Text(stringResource(LocaleR.string.meta_feature_custom_rules_error_request),
                    modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onReload, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text(stringResource(LocaleR.string.meta_feature_custom_rules_retry))
                }
            }
            else -> {
                KokoroRuleEditorContent(editor, canAdd = canAdd, applyHorizontalPadding = false)
                val errorMessage = when (state.status) {
                    KokoroRulesStatus.VALIDATION_FAILED -> state.validationRuleIndex?.let {
                        stringResource(LocaleR.string.meta_feature_custom_rules_error_validation).format(it + 1)
                    } ?: stringResource(LocaleR.string.meta_feature_custom_rules_error_validation_general)
                    KokoroRulesStatus.NOT_FOUND -> stringResource(LocaleR.string.meta_feature_custom_rules_error_not_found)
                    KokoroRulesStatus.RATE_LIMITED -> stringResource(LocaleR.string.meta_feature_custom_rules_error_rate_limited)
                    KokoroRulesStatus.SAVE_OUTCOME_UNKNOWN -> stringResource(LocaleR.string.meta_feature_custom_rules_error_unknown)
                    KokoroRulesStatus.REQUEST_FAILED -> stringResource(LocaleR.string.meta_feature_custom_rules_error_request)
                    KokoroRulesStatus.SUBSCRIPTION_UPDATE_FAILED -> stringResource(LocaleR.string.meta_feature_custom_rules_subscription_update_failed)
                    else -> null
                }
                errorMessage?.let { Text(it, Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.error) }
                if (state.conflict != null) {
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(LocaleR.string.meta_feature_custom_rules_conflict_title), style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(LocaleR.string.meta_feature_custom_rules_conflict_message), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedButton(onClick = onUseRemote, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(LocaleR.string.meta_feature_custom_rules_use_remote))
                            }
                            Button(onClick = onKeepLocal, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(LocaleR.string.meta_feature_custom_rules_keep_local))
                            }
                        }
                    }
                } else if (state.status == KokoroRulesStatus.SAVE_OUTCOME_UNKNOWN) {
                    OutlinedButton(onClick = onReload, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Text(stringResource(LocaleR.string.meta_feature_custom_rules_refresh))
                    }
                } else if (state.subscriptionUpdatePending) {
                    Button(onClick = onRetrySubscriptionUpdate, enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        if (state.saving) {
                            Md3EIndeterminateCircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(stringResource(if (state.saving) LocaleR.string.meta_feature_custom_rules_updating_subscription
                            else LocaleR.string.meta_feature_custom_rules_retry_subscription_update), maxLines = 1)
                    }
                } else {
                    Button(onClick = { onSave(editor.rule) }, enabled = editor.canConfirm,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        if (state.saving) {
                            Md3EIndeterminateCircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(stringResource(LocaleR.string.meta_feature_custom_rules_add_rule))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Connection rule / narrow", widthDp = 320, heightDp = 560)
@androidx.compose.ui.tooling.preview.Preview(name = "Connection rule / dark", widthDp = 320, heightDp = 560, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConnectionRuleEditorPreview() {
    com.amamiyakokoro.box.presentation.theme.YumeTheme {
        ConnectionRuleEditorContent(
            state = KokoroCustomRulesUiState(
                loading = false,
                authState = KokoroAuthState.Authenticated(com.amamiyakokoro.box.screen.profiles.KokoroAccount("Preview", null, emptyList())),
                options = KokoroCustomRulesOptions(ruleTypes = listOf("DOMAIN-SUFFIX", "DOMAIN", "MATCH"), targets = listOf("DIRECT", "REJECT")),
                defaultRuleSet = KokoroRuleSet(12, "default", 1, "", ""),
            ),
            host = "api.ip.sb",
            onSave = {}, onReload = {}, onRetrySubscriptionUpdate = {}, onUseRemote = {}, onKeepLocal = {}, onLogin = {},
        )
    }
}
