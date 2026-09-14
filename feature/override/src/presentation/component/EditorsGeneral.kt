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

package com.amamiyakokoro.box.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.model.ConfigurationOverride
import com.amamiyakokoro.box.core.model.LogMessage
import com.amamiyakokoro.box.core.model.TunnelState

@Composable
fun GeneralEditor(
    config: ConfigurationOverride,
    onConfigChange: (ConfigurationOverride) -> Unit,
    onEditStringList: OpenStringListModifiersEditor,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(OverrideSectionSpacing),
    ) {
        OverrideCardSection(stringResource(LocaleR.string.override_form_run_and_log)) {
            NullableEnumSelector(
                title = stringResource(LocaleR.string.override_general_proxy_mode),
                value = config.mode,
                items = listOf(
                    stringResource(LocaleR.string.component_selector_not_modify),
                    stringResource(LocaleR.string.proxy_mode_direct),
                    stringResource(LocaleR.string.proxy_mode_global),
                    stringResource(LocaleR.string.proxy_mode_rule),
                ),
                values = listOf(
                    null,
                    TunnelState.Mode.Direct,
                    TunnelState.Mode.Global,
                    TunnelState.Mode.Rule,
                ),
                onValueChange = { onConfigChange(config.copy(mode = it)) },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_general_ipv6),
                value = config.ipv6,
                onValueChange = { onConfigChange(config.copy(ipv6 = it)) },
            )
            NullableEnumSelector(
                title = stringResource(LocaleR.string.override_general_log_level),
                value = config.logLevel,
                items = listOf(
                    stringResource(LocaleR.string.component_selector_not_modify),
                    "Info",
                    "Warning",
                    "Error",
                    "Debug",
                    "Silent",
                ),
                values = listOf(
                    null,
                    LogMessage.Level.Info,
                    LogMessage.Level.Warning,
                    LogMessage.Level.Error,
                    LogMessage.Level.Debug,
                    LogMessage.Level.Silent,
                ),
                onValueChange = { onConfigChange(config.copy(logLevel = it)) },
            )
            NullableEnumSelector(
                title = stringResource(LocaleR.string.override_form_process_mode),
                value = config.findProcessMode,
                items = listOf(stringResource(LocaleR.string.override_form_not_modify), "Always", "Strict", "Off"),
                values = listOf(
                    null,
                    ConfigurationOverride.FindProcessMode.Always,
                    ConfigurationOverride.FindProcessMode.Strict,
                    ConfigurationOverride.FindProcessMode.Off,
                ),
                onValueChange = { onConfigChange(config.copy(findProcessMode = it)) },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_form_unified_delay),
                value = config.unifiedDelay,
                onValueChange = { onConfigChange(config.copy(unifiedDelay = it)) },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_form_tcp_concurrent),
                value = config.tcpConcurrent,
                onValueChange = { onConfigChange(config.copy(tcpConcurrent = it)) },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_form_geodata_mode),
                value = config.geodataMode,
                onValueChange = { onConfigChange(config.copy(geodataMode = it)) },
            )
        }

        OverrideFormSection(stringResource(LocaleR.string.override_form_run_and_log_extra)) {
            OverrideIntInputContent(
                title = stringResource(LocaleR.string.override_label_keep_alive_interval),
                value = config.keepAliveInterval,
                placeholder = stringResource(LocaleR.string.override_form_seconds),
                onValueChange = { onConfigChange(config.copy(keepAliveInterval = it)) },
            )
            OverrideIntInputContent(
                title = stringResource(LocaleR.string.override_label_keep_alive_idle),
                value = config.keepAliveIdle,
                placeholder = stringResource(LocaleR.string.override_form_seconds),
                onValueChange = { onConfigChange(config.copy(keepAliveIdle = it)) },
            )
        }

        OverrideFormSection(stringResource(LocaleR.string.override_form_connection_network)) {
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_outbound_interface),
                value = config.interfaceName,
                placeholder = "en0 / wlan0",
                onValueChange = { onConfigChange(config.copy(interfaceName = it)) },
            )
            OverrideIntInputContent(
                title = stringResource(LocaleR.string.override_form_routing_mark),
                value = config.routingMark,
                placeholder = "6666",
                onValueChange = { onConfigChange(config.copy(routingMark = it)) },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_geosite_matcher),
                value = config.geositeMatcher,
                placeholder = "standard / succinct",
                onValueChange = { onConfigChange(config.copy(geositeMatcher = it)) },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_global_client_fingerprint),
                value = config.globalClientFingerprint,
                placeholder = "chrome / safari",
                onValueChange = { onConfigChange(config.copy(globalClientFingerprint = it)) },
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_lan_access)) {
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_general_allow_lan),
                value = config.allowLan,
                onValueChange = { onConfigChange(config.copy(allowLan = it)) },
            )
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_form_allowed_ips),
                replaceValue = config.lanAllowedIps,
                startValue = config.lanAllowedIpsStart,
                endValue = config.lanAllowedIpsEnd,
                placeholder = "0.0.0.0/0",
                onReplaceChange = { onConfigChange(config.copy(lanAllowedIps = it)) },
                onStartChange = { onConfigChange(config.copy(lanAllowedIpsStart = it)) },
                onEndChange = { onConfigChange(config.copy(lanAllowedIpsEnd = it)) },
                onEditListGroup = onEditStringList,
            )
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_form_disallowed_ips),
                replaceValue = config.lanDisallowedIps,
                startValue = config.lanDisallowedIpsStart,
                endValue = config.lanDisallowedIpsEnd,
                placeholder = "192.168.0.3/32",
                onReplaceChange = { onConfigChange(config.copy(lanDisallowedIps = it)) },
                onStartChange = { onConfigChange(config.copy(lanDisallowedIpsStart = it)) },
                onEndChange = { onConfigChange(config.copy(lanDisallowedIpsEnd = it)) },
                onEditListGroup = onEditStringList,
            )
        }

        OverrideFormSection(stringResource(LocaleR.string.override_form_lan_address)) {
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_bind_address),
                value = config.bindAddress,
                placeholder = "* / 192.168.1.1 / [::1]",
                onValueChange = { onConfigChange(config.copy(bindAddress = it)) },
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_user_auth)) {
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_form_user_auth),
                replaceValue = config.authentication,
                startValue = config.authenticationStart,
                endValue = config.authenticationEnd,
                placeholder = "user:password",
                onReplaceChange = { onConfigChange(config.copy(authentication = it)) },
                onStartChange = { onConfigChange(config.copy(authenticationStart = it)) },
                onEndChange = { onConfigChange(config.copy(authenticationEnd = it)) },
                onEditListGroup = onEditStringList,
            )
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_form_skip_auth_ips),
                replaceValue = config.skipAuthPrefixes,
                startValue = config.skipAuthPrefixesStart,
                endValue = config.skipAuthPrefixesEnd,
                placeholder = "127.0.0.1/8",
                onReplaceChange = { onConfigChange(config.copy(skipAuthPrefixes = it)) },
                onStartChange = { onConfigChange(config.copy(skipAuthPrefixesStart = it)) },
                onEndChange = { onConfigChange(config.copy(skipAuthPrefixesEnd = it)) },
                onEditListGroup = onEditStringList,
            )
        }

        OverrideFormSection(stringResource(LocaleR.string.override_form_external_control)) {
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_external_controller),
                value = config.externalController,
                placeholder = "127.0.0.1:9090",
                onValueChange = { onConfigChange(config.copy(externalController = it)) },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_external_controller_https),
                value = config.externalControllerTLS,
                placeholder = "127.0.0.1:9443",
                onValueChange = { onConfigChange(config.copy(externalControllerTLS = it)) },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_external_do_h),
                value = config.externalDohServer,
                placeholder = "/dns-query",
                onValueChange = { onConfigChange(config.copy(externalDohServer = it)) },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_api_secret),
                value = config.secret,
                placeholder = stringResource(LocaleR.string.override_form_api_secret),
                onValueChange = { onConfigChange(config.copy(secret = it)) },
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_controller_cors)) {
            StringListWithModifiersInput(
                title = "CORS Allow Origins",
                replaceValue = config.externalControllerCors.allowOrigins,
                startValue = config.externalControllerCors.allowOriginsStart,
                endValue = config.externalControllerCors.allowOriginsEnd,
                placeholder = "*",
                onReplaceChange = {
                    onConfigChange(
                        config.copy(
                            externalControllerCors = config.externalControllerCors.copy(
                                allowOrigins = it,
                            ),
                        ),
                    )
                },
                onStartChange = {
                    onConfigChange(
                        config.copy(
                            externalControllerCors = config.externalControllerCors.copy(
                                allowOriginsStart = it,
                            ),
                        ),
                    )
                },
                onEndChange = {
                    onConfigChange(
                        config.copy(
                            externalControllerCors = config.externalControllerCors.copy(
                                allowOriginsEnd = it,
                            ),
                        ),
                    )
                },
                onEditListGroup = onEditStringList,
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_form_allow_private_network),
                value = config.externalControllerCors.allowPrivateNetwork,
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            externalControllerCors = config.externalControllerCors.copy(
                                allowPrivateNetwork = it,
                            ),
                        ),
                    )
                },
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_config_persistence)) {
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_form_save_group_selection),
                value = config.profile.storeSelected,
                onValueChange = {
                    onConfigChange(config.copy(profile = config.profile.copy(storeSelected = it)))
                },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_form_save_fake_ip_mapping),
                value = config.profile.storeFakeIp,
                onValueChange = {
                    onConfigChange(config.copy(profile = config.profile.copy(storeFakeIp = it)))
                },
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_geo_resources)) {
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_form_auto_update_geo),
                value = config.geoAutoUpdate,
                onValueChange = { onConfigChange(config.copy(geoAutoUpdate = it)) },
            )
        }

        OverrideFormSection(stringResource(LocaleR.string.override_form_geo_resources)) {
            OverrideIntInputContent(
                title = stringResource(LocaleR.string.override_form_geo_update_interval),
                value = config.geoUpdateInterval,
                placeholder = stringResource(LocaleR.string.override_form_hours),
                onValueChange = { onConfigChange(config.copy(geoUpdateInterval = it)) },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_geoip_url),
                value = config.geoxurl.geoip,
                placeholder = "https://...",
                onValueChange = {
                    onConfigChange(config.copy(geoxurl = config.geoxurl.copy(geoip = it)))
                },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_geosite_url),
                value = config.geoxurl.geosite,
                placeholder = "https://...",
                onValueChange = {
                    onConfigChange(config.copy(geoxurl = config.geoxurl.copy(geosite = it)))
                },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_mmdb_url),
                value = config.geoxurl.mmdb,
                placeholder = "https://...",
                onValueChange = {
                    onConfigChange(config.copy(geoxurl = config.geoxurl.copy(mmdb = it)))
                },
            )
        }
    }
}
