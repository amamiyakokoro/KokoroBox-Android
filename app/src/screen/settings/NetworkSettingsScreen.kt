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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.amamiyakokoro.box.common.util.VpnUtils
import com.amamiyakokoro.box.common.util.toast
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.model.RootTunDnsMode
import com.amamiyakokoro.box.data.model.AccessControlMode
import com.amamiyakokoro.box.data.model.ProxyMode
import com.amamiyakokoro.box.data.model.TunStack
import com.amamiyakokoro.box.presentation.component.AppTextFieldDialog
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.PreferenceArrowItem
import com.amamiyakokoro.box.presentation.component.PreferenceEnumItem
import com.amamiyakokoro.box.presentation.component.PreferenceSwitchItem
import com.amamiyakokoro.box.presentation.component.PreferenceValueItem
import com.amamiyakokoro.box.presentation.component.ScreenLazyColumn
import com.amamiyakokoro.box.presentation.component.TextEditBottomSheet
import com.amamiyakokoro.box.presentation.component.Title
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.component.rememberStandalonePageMainPadding
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.service.root.RootAccessSupport
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AccessControlScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>
fun NetworkSettingsScreen(
    navigator: DestinationsNavigator,
) {
    val viewModel = koinViewModel<NetworkSettingsViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tunServiceOptionsUiState by viewModel.tunServiceOptionsUiState.collectAsStateWithLifecycle()
    val rootTunServiceOptionsUiState by viewModel.rootTunServiceOptionsUiState.collectAsStateWithLifecycle()
    val accessControlMode by viewModel.accessControlMode.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val vpnDenied = stringResource(LocaleR.string.network_settings_error_vpn_denied)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.errors.collect { message ->
                context.toast(message)
            }
        }
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onProxyModeChange(ProxyMode.Tun)
        } else {
            context.toast(vpnDenied)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(title = stringResource(LocaleR.string.network_settings_title))
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
        ) {
            item {
                NetworkVpnServiceSection(
                    viewModel = viewModel,
                    configuredMode = uiState.configuredMode,
                    vpnPermissionLauncher = vpnPermissionLauncher,
                )
            }
            item {
                NetworkServiceOptionsSection(
                    viewModel = viewModel,
                    uiState = uiState,
                    tunServiceOptionsUiState = tunServiceOptionsUiState,
                    rootTunServiceOptionsUiState = rootTunServiceOptionsUiState,
                )
            }
            item {
                NetworkProxyOptionsSection(
                    navigator = navigator,
                    accessControlMode = accessControlMode,
                    showAccessControlMode = uiState.showAccessControlMode,
                    onAccessControlModeChange = viewModel::onAccessControlModeChange,
                )
            }
            item { NetworkHttpSettingsSection(viewModel) }
            item { NetworkExperimentalSettingsSection(viewModel) }
        }
    }
}

@Composable
private fun NetworkVpnServiceSection(
    viewModel: NetworkSettingsViewModel,
    configuredMode: ProxyMode,
    vpnPermissionLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Title(stringResource(LocaleR.string.network_settings_section_vpn_service))
    Card {
        PreferenceEnumItem(
            title = stringResource(LocaleR.string.network_settings_vpn_service_route_traffic_title),
            summary = stringResource(LocaleR.string.network_settings_vpn_service_route_traffic_summary),
            currentValue = configuredMode,
            items = listOf(
                stringResource(LocaleR.string.network_settings_vpn_service_system_proxy),
                stringResource(LocaleR.string.network_settings_vpn_service_vpn_mode),
                stringResource(LocaleR.string.network_settings_vpn_service_root_tun_mode),
            ),
            values = listOf(
                ProxyMode.Http,
                ProxyMode.Tun,
                ProxyMode.RootTun,
            ),
            onValueChange = { mode ->
                when (mode) {
                    ProxyMode.Tun -> {
                        if (!VpnUtils.checkVpnPermission(context)) {
                            VpnUtils.getVpnPermissionIntent(context)?.let(vpnPermissionLauncher::launch)
                                ?: viewModel.onProxyModeChange(mode)
                        } else {
                            viewModel.onProxyModeChange(mode)
                        }
                    }

                    ProxyMode.RootTun -> {
                        coroutineScope.launch {
                            val rootStatus = RootAccessSupport.evaluateAsync(context)
                            if (!rootStatus.canStartRootTun) {
                                context.toast(rootStatus.rootTunBlockedMessage(context))
                                return@launch
                            }
                            viewModel.onProxyModeChange(mode)
                        }
                    }

                    ProxyMode.Http -> {
                        viewModel.onProxyModeChange(mode)
                    }
                }
            },
        )
    }
}

@Composable
private fun NetworkServiceOptionsSection(
    viewModel: NetworkSettingsViewModel,
    uiState: NetworkSettingsUiState,
    tunServiceOptionsUiState: TunServiceOptionsUiState,
    rootTunServiceOptionsUiState: RootTunServiceOptionsUiState,
) {
    if (!uiState.showServiceOptions) return

    val commonActions = remember(viewModel) {
        CommonTunOptionActions(
            onBypassPrivateNetworkChange = viewModel::onBypassPrivateNetworkChange,
            onDnsHijackChange = viewModel::onDnsHijackChange,
            onEnableIPv6Change = viewModel::onEnableIPv6Change,
            onTunStackChange = viewModel::onTunStackChange,
        )
    }

    Title(stringResource(LocaleR.string.network_settings_section_vpn_options))
    Card {
        when (uiState.configuredMode) {
            ProxyMode.Tun -> {
                TunServiceOptions(
                    state = tunServiceOptionsUiState,
                    actions = TunServiceOptionActions(
                        common = commonActions,
                        onAllowBypassChange = viewModel::onAllowBypassChange,
                        onSystemProxyChange = viewModel::onSystemProxyChange,
                    ),
                )
            }

            ProxyMode.RootTun -> {
                RootTunServiceOptions(
                    state = rootTunServiceOptionsUiState,
                    showFakeIpRange = uiState.showFakeIpRange,
                    actions = remember(viewModel, commonActions) {
                        RootTunServiceOptionActions(
                            common = commonActions,
                            onRootTunAutoRouteChange = viewModel::onRootTunAutoRouteChange,
                            onRootTunStrictRouteChange = viewModel::onRootTunStrictRouteChange,
                            onRootTunAutoRedirectChange = viewModel::onRootTunAutoRedirectChange,
                            onRootTunDnsModeChange = viewModel::onRootTunDnsModeChange,
                            onRootTunIfNameDraftChange = viewModel::onRootTunIfNameDraftChange,
                            onRootTunMtuDraftChange = viewModel::onRootTunMtuDraftChange,
                            onRootTunFakeIpRangeDraftChange = viewModel::onRootTunFakeIpRangeDraftChange,
                            onRootTunFakeIpRange6DraftChange = viewModel::onRootTunFakeIpRange6DraftChange,
                            commitRootTunIfName = viewModel::commitRootTunIfName,
                            commitRootTunMtu = viewModel::commitRootTunMtu,
                            commitRootTunFakeIpRange = viewModel::commitRootTunFakeIpRange,
                            commitRootTunFakeIpRange6 = viewModel::commitRootTunFakeIpRange6,
                        )
                    },
                )
            }

            ProxyMode.Http -> Unit
        }
    }
}

@Composable
private fun NetworkProxyOptionsSection(
    navigator: DestinationsNavigator,
    accessControlMode: AccessControlMode,
    showAccessControlMode: Boolean,
    onAccessControlModeChange: (AccessControlMode) -> Unit,
) {
    Title(stringResource(LocaleR.string.network_settings_section_proxy_options))
    Card {
        if (showAccessControlMode) {
            PreferenceEnumItem(
                title = stringResource(LocaleR.string.network_settings_proxy_options_access_control_mode_title),
                currentValue = accessControlMode,
                items = listOf(
                    stringResource(LocaleR.string.network_settings_proxy_options_allow_all),
                    stringResource(LocaleR.string.network_settings_proxy_options_allow_selected),
                    stringResource(LocaleR.string.network_settings_proxy_options_reject_selected),
                ),
                values = AccessControlMode.entries,
                onValueChange = onAccessControlModeChange,
            )
        }
        PreferenceArrowItem(
            title = stringResource(LocaleR.string.network_settings_proxy_options_manage_access_control_title),
            summary = stringResource(LocaleR.string.network_settings_proxy_options_manage_access_control_summary),
            onClick = {
                navigator.navigate(AccessControlScreenDestination)
            },
        )
    }
}

@Composable
private fun NetworkHttpSettingsSection(viewModel: NetworkSettingsViewModel) {
    val customUserAgent by viewModel.customUserAgent.state.collectAsStateWithLifecycle()
    val customUserAgentSummary = customUserAgent.ifEmpty {
        stringResource(LocaleR.string.network_settings_network_custom_user_agent_summary_default)
    }
    val showEditUserAgent = remember { mutableStateOf(false) }
    val userAgentTextField = remember { mutableStateOf(TextFieldValue()) }

    Title(stringResource(LocaleR.string.network_settings_section_network))
    Card {
        PreferenceValueItem(
            title = stringResource(LocaleR.string.network_settings_network_custom_user_agent_title),
            summary = customUserAgentSummary,
            onClick = {
                userAgentTextField.value = TextFieldValue(customUserAgent)
                showEditUserAgent.value = true
            },
        )
    }

    TextEditBottomSheet(
        show = showEditUserAgent,
        title = stringResource(LocaleR.string.network_settings_network_user_agent_dialog_title),
        textFieldValue = userAgentTextField,
        onConfirm = viewModel::applyCustomUserAgent,
    )
}

@Composable
private fun NetworkExperimentalSettingsSection(viewModel: NetworkSettingsViewModel) {
    val antiPollutionDns by viewModel.antiPollutionDns.state.collectAsStateWithLifecycle()

    Title(stringResource(LocaleR.string.network_settings_section_experimental))
    Card {
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.network_settings_experimental_anti_pollution_dns_title),
            summary = stringResource(LocaleR.string.network_settings_experimental_anti_pollution_dns_summary),
            checked = antiPollutionDns,
            onCheckedChange = viewModel::onAntiPollutionDnsChange,
        )
    }
}

@Composable
private fun TunServiceOptions(
    state: TunServiceOptionsUiState,
    actions: TunServiceOptionActions,
) {
    CommonTunServiceOptions(
        state = state.common,
        actions = actions.common,
        extraOptions = {
            PreferenceSwitchItem(
                title = stringResource(LocaleR.string.network_settings_vpn_options_allow_bypass_title),
                summary = stringResource(LocaleR.string.network_settings_vpn_options_allow_bypass_summary),
                checked = state.allowBypass,
                onCheckedChange = actions.onAllowBypassChange,
            )
            PreferenceSwitchItem(
                title = stringResource(LocaleR.string.network_settings_vpn_options_system_proxy_title),
                summary = stringResource(LocaleR.string.network_settings_vpn_options_system_proxy_summary),
                checked = state.systemProxy,
                onCheckedChange = actions.onSystemProxyChange,
            )
        },
    )
}

@Composable
private fun RootTunServiceOptions(
    state: RootTunServiceOptionsUiState,
    showFakeIpRange: Boolean,
    actions: RootTunServiceOptionActions,
) {
    CommonTunServiceOptions(
        state = state.common,
        actions = actions.common,
        extraOptions = {
            RootTunAdvancedOptions(
                state = state,
                showFakeIpRange = showFakeIpRange,
                actions = actions,
            )
        },
    )
}

@Composable
private fun RootTunAdvancedOptions(
    state: RootTunServiceOptionsUiState,
    showFakeIpRange: Boolean,
    actions: RootTunServiceOptionActions,
) {
    var editDialog by remember { mutableStateOf<RootTunEditDialogState?>(null) }

    RootTunIdentityOptions(
        rootTunIfNameDraft = state.rootTunIfNameDraft,
        rootTunMtuDraft = state.rootTunMtuDraft,
        onEditIfName = { editDialog = RootTunEditDialogState.IfName },
        onEditMtu = { editDialog = RootTunEditDialogState.Mtu },
    )
    RootTunRoutingOptions(
        rootTunAutoRoute = state.rootTunAutoRoute,
        rootTunStrictRoute = state.rootTunStrictRoute,
        rootTunAutoRedirect = state.rootTunAutoRedirect,
        rootTunDnsMode = state.rootTunDnsMode,
        onRootTunAutoRouteChange = actions.onRootTunAutoRouteChange,
        onRootTunStrictRouteChange = actions.onRootTunStrictRouteChange,
        onRootTunAutoRedirectChange = actions.onRootTunAutoRedirectChange,
        onRootTunDnsModeChange = actions.onRootTunDnsModeChange,
    )
    RootTunFakeIpOptions(
        showFakeIpRange = showFakeIpRange,
        rootTunFakeIpRangeDraft = state.rootTunFakeIpRangeDraft,
        rootTunFakeIpRange6Draft = state.rootTunFakeIpRange6Draft,
        onEditFakeIpRange = { editDialog = RootTunEditDialogState.FakeIpRange },
        onEditFakeIpRange6 = { editDialog = RootTunEditDialogState.FakeIpRange6 },
    )

    RootTunEditDialogs(
        editDialog = editDialog,
        state = state,
        actions = actions,
        onDismiss = { editDialog = null },
    )
}

@Composable
private fun RootTunIdentityOptions(
    rootTunIfNameDraft: String,
    rootTunMtuDraft: String,
    onEditIfName: () -> Unit,
    onEditMtu: () -> Unit,
) {
    PreferenceArrowItem(
        title = stringResource(LocaleR.string.network_settings_root_tun_if_name_title),
        summary = rootTunIfNameDraft.ifBlank { stringResource(LocaleR.string.network_settings_root_tun_if_name_summary) },
        onClick = onEditIfName,
    )
    PreferenceArrowItem(
        title = stringResource(LocaleR.string.network_settings_root_tun_mtu_title),
        summary = rootTunMtuDraft.ifBlank { stringResource(LocaleR.string.network_settings_root_tun_mtu_summary) },
        onClick = onEditMtu,
    )
}

@Composable
private fun RootTunRoutingOptions(
    rootTunAutoRoute: Boolean,
    rootTunStrictRoute: Boolean,
    rootTunAutoRedirect: Boolean,
    rootTunDnsMode: RootTunDnsMode,
    onRootTunAutoRouteChange: (Boolean) -> Unit,
    onRootTunStrictRouteChange: (Boolean) -> Unit,
    onRootTunAutoRedirectChange: (Boolean) -> Unit,
    onRootTunDnsModeChange: (RootTunDnsMode) -> Unit,
) {
    PreferenceSwitchItem(
        title = stringResource(LocaleR.string.network_settings_root_tun_auto_route_title),
        summary = stringResource(LocaleR.string.network_settings_root_tun_auto_route_summary),
        checked = rootTunAutoRoute,
        onCheckedChange = onRootTunAutoRouteChange,
    )
    PreferenceSwitchItem(
        title = stringResource(LocaleR.string.network_settings_root_tun_strict_route_title),
        summary = stringResource(LocaleR.string.network_settings_root_tun_strict_route_summary),
        checked = rootTunStrictRoute,
        onCheckedChange = onRootTunStrictRouteChange,
    )
    PreferenceSwitchItem(
        title = stringResource(LocaleR.string.network_settings_root_tun_auto_redirect_title),
        summary = stringResource(LocaleR.string.network_settings_root_tun_auto_redirect_summary),
        checked = rootTunAutoRedirect,
        onCheckedChange = onRootTunAutoRedirectChange,
    )
    PreferenceEnumItem(
        title = stringResource(LocaleR.string.network_settings_root_tun_dns_mode_title),
        summary = stringResource(LocaleR.string.network_settings_root_tun_dns_mode_summary),
        currentValue = rootTunDnsMode,
        items = listOf(
            stringResource(LocaleR.string.network_settings_root_tun_dns_mode_redir_host),
            stringResource(LocaleR.string.network_settings_root_tun_dns_mode_fake_ip),
        ),
        values = RootTunDnsMode.entries,
        onValueChange = onRootTunDnsModeChange,
    )
}

@Composable
private fun RootTunFakeIpOptions(
    showFakeIpRange: Boolean,
    rootTunFakeIpRangeDraft: String,
    rootTunFakeIpRange6Draft: String,
    onEditFakeIpRange: () -> Unit,
    onEditFakeIpRange6: () -> Unit,
) {
    AnimatedVisibility(
        visible = showFakeIpRange,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column {
            PreferenceArrowItem(
                title = stringResource(LocaleR.string.network_settings_root_tun_fake_ip_range_title),
                summary = rootTunFakeIpRangeDraft.ifBlank { stringResource(LocaleR.string.network_settings_root_tun_fake_ip_range_summary) },
                onClick = onEditFakeIpRange,
            )
            PreferenceArrowItem(
                title = stringResource(LocaleR.string.network_settings_root_tun_fake_ip_range6_title),
                summary = rootTunFakeIpRange6Draft.ifBlank { stringResource(LocaleR.string.network_settings_root_tun_fake_ip_range6_summary) },
                onClick = onEditFakeIpRange6,
            )
        }
    }
}

@Composable
private fun RootTunEditDialogs(
    editDialog: RootTunEditDialogState?,
    state: RootTunServiceOptionsUiState,
    actions: RootTunServiceOptionActions,
    onDismiss: () -> Unit,
) {
    when (editDialog) {
        RootTunEditDialogState.IfName -> RootTunTextEditDialog(
            title = stringResource(LocaleR.string.network_settings_root_tun_if_name_title),
            value = state.rootTunIfNameDraft,
            onValueChange = actions.onRootTunIfNameDraftChange,
            onDismiss = onDismiss,
            onCommit = actions.commitRootTunIfName,
        )

        RootTunEditDialogState.Mtu -> RootTunTextEditDialog(
            title = stringResource(LocaleR.string.network_settings_root_tun_mtu_title),
            value = state.rootTunMtuDraft,
            onValueChange = actions.onRootTunMtuDraftChange,
            onDismiss = onDismiss,
            onCommit = actions.commitRootTunMtu,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
        )

        RootTunEditDialogState.FakeIpRange -> RootTunTextEditDialog(
            title = stringResource(LocaleR.string.network_settings_root_tun_fake_ip_range_title),
            value = state.rootTunFakeIpRangeDraft,
            onValueChange = actions.onRootTunFakeIpRangeDraftChange,
            onDismiss = onDismiss,
            onCommit = actions.commitRootTunFakeIpRange,
        )

        RootTunEditDialogState.FakeIpRange6 -> RootTunTextEditDialog(
            title = stringResource(LocaleR.string.network_settings_root_tun_fake_ip_range6_title),
            value = state.rootTunFakeIpRange6Draft,
            onValueChange = actions.onRootTunFakeIpRange6DraftChange,
            onDismiss = onDismiss,
            onCommit = actions.commitRootTunFakeIpRange6,
        )

        null -> Unit
    }
}

@Composable
private fun CommonTunServiceOptions(
    state: CommonTunOptionsUiState,
    actions: CommonTunOptionActions,
    extraOptions: @Composable ColumnScope.() -> Unit = {},
) {
    Column {
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.network_settings_vpn_options_bypass_private_title),
            summary = stringResource(LocaleR.string.network_settings_vpn_options_bypass_private_summary),
            checked = state.bypassPrivateNetwork,
            onCheckedChange = actions.onBypassPrivateNetworkChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.network_settings_vpn_options_dns_hijack_title),
            summary = stringResource(LocaleR.string.network_settings_vpn_options_dns_hijack_summary),
            checked = state.dnsHijack,
            onCheckedChange = actions.onDnsHijackChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.network_settings_vpn_options_enable_ipv6_title),
            summary = stringResource(LocaleR.string.network_settings_vpn_options_enable_ipv6_summary),
            checked = state.enableIPv6,
            onCheckedChange = actions.onEnableIPv6Change,
        )
        PreferenceEnumItem(
            title = stringResource(LocaleR.string.network_settings_proxy_options_tun_stack_title),
            currentValue = state.tunStack,
            items = listOf("System", "GVisor", "Mixed", "MIPS"),
            values = TunStack.entries,
            onValueChange = actions.onTunStackChange,
        )
        extraOptions()
    }
}

@Composable
private fun RootTunTextEditDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCommit: () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done,
    ),
) {
    val focusManager = LocalFocusManager.current
    AppTextFieldDialog(
        show = true,
        title = title,
        value = value,
        onValueChange = onValueChange,
        onDismissRequest = onDismiss,
        onConfirm = {
            onCommit()
            focusManager.clearFocus()
            onDismiss()
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = {
                onCommit()
                onDismiss()
                focusManager.clearFocus()
            },
        ),
    )
}

private data class CommonTunOptionActions(
    val onBypassPrivateNetworkChange: (Boolean) -> Unit,
    val onDnsHijackChange: (Boolean) -> Unit,
    val onEnableIPv6Change: (Boolean) -> Unit,
    val onTunStackChange: (TunStack) -> Unit,
)

private data class TunServiceOptionActions(
    val common: CommonTunOptionActions,
    val onAllowBypassChange: (Boolean) -> Unit,
    val onSystemProxyChange: (Boolean) -> Unit,
)

private data class RootTunServiceOptionActions(
    val common: CommonTunOptionActions,
    val onRootTunAutoRouteChange: (Boolean) -> Unit,
    val onRootTunStrictRouteChange: (Boolean) -> Unit,
    val onRootTunAutoRedirectChange: (Boolean) -> Unit,
    val onRootTunDnsModeChange: (RootTunDnsMode) -> Unit,
    val onRootTunIfNameDraftChange: (String) -> Unit,
    val onRootTunMtuDraftChange: (String) -> Unit,
    val onRootTunFakeIpRangeDraftChange: (String) -> Unit,
    val onRootTunFakeIpRange6DraftChange: (String) -> Unit,
    val commitRootTunIfName: () -> Unit,
    val commitRootTunMtu: () -> Unit,
    val commitRootTunFakeIpRange: () -> Unit,
    val commitRootTunFakeIpRange6: () -> Unit,
)

private enum class RootTunEditDialogState {
    IfName,
    Mtu,
    FakeIpRange,
    FakeIpRange6,
}
