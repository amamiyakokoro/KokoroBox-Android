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

@Composable
fun DnsEditor(
    config: ConfigurationOverride,
    onConfigChange: (ConfigurationOverride) -> Unit,
    onEditStringList: OpenStringListModifiersEditor,
    onEditStringMap: OpenStringMapEditor,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(OverrideSectionSpacing),
    ) {
        OverrideCardSection(stringResource(LocaleR.string.override_form_dns_basic_switch)) {
            NullableEnumSelector(
                title = stringResource(LocaleR.string.override_dns_policy),
                value = config.dns.enable,
                items = listOf(
                    stringResource(LocaleR.string.override_dns_policy_not_modify),
                    stringResource(LocaleR.string.override_dns_policy_force_enable),
                    stringResource(LocaleR.string.override_dns_policy_use_builtin),
                ),
                values = listOf(null, true, false),
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(enable = it)))
                },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_dns_prefer_h3),
                value = config.dns.preferH3,
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(preferH3 = it)))
                },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_dns_ipv6),
                value = config.dns.ipv6,
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(ipv6 = it)))
                },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_dns_use_hosts),
                value = config.dns.useHosts,
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(useHosts = it)))
                },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_label_use_system_hosts),
                value = config.dns.useSystemHosts,
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(useSystemHosts = it)))
                },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_dns_append_system),
                value = config.app.appendSystemDns,
                onValueChange = {
                    onConfigChange(config.copy(app = config.app.copy(appendSystemDns = it)))
                },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_label_respect_rules),
                value = config.dns.respectRules,
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(respectRules = it)))
                },
            )
            NullableEnumSelector(
                title = stringResource(LocaleR.string.override_dns_enhanced_mode),
                value = config.dns.enhancedMode,
                items = listOf(
                    stringResource(LocaleR.string.override_dns_enhanced_not_modify),
                    stringResource(LocaleR.string.override_dns_enhanced_disable),
                    stringResource(LocaleR.string.override_dns_enhanced_fakeip),
                    stringResource(LocaleR.string.override_dns_enhanced_mapping),
                ),
                values = listOf(
                    null,
                    ConfigurationOverride.DnsEnhancedMode.None,
                    ConfigurationOverride.DnsEnhancedMode.FakeIp,
                    ConfigurationOverride.DnsEnhancedMode.Mapping,
                ),
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(enhancedMode = it)))
                },
            )
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_form_direct_follow_policy),
                value = config.dns.directFollowPolicy,
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(directFollowPolicy = it)))
                },
            )
        }

        OverrideFormSection(stringResource(LocaleR.string.override_form_dns_basic_params)) {
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_dns_listen),
                value = config.dns.listen,
                placeholder = stringResource(LocaleR.string.override_dns_listen_hint),
                onValueChange = { onConfigChange(config.copy(dns = config.dns.copy(listen = it))) },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_label_cache_algorithm),
                value = config.dns.cacheAlgorithm,
                placeholder = "lru / arc",
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(cacheAlgorithm = it)))
                },
            )
            OverrideIntInputContent(
                title = stringResource(LocaleR.string.override_form_ipv6_timeout),
                value = config.dns.ipv6Timeout,
                placeholder = "100",
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(ipv6Timeout = it)))
                },
            )
            OverrideIntInputContent(
                title = stringResource(LocaleR.string.override_form_cache_limit),
                value = config.dns.cacheMaxSize,
                placeholder = "4096",
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(cacheMaxSize = it)))
                },
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_fake_ip_mode)) {
            NullableEnumSelector(
                title = stringResource(LocaleR.string.override_dns_fakeip_filter_mode),
                value = config.dns.fakeIPFilterMode,
                items = listOf(
                    stringResource(LocaleR.string.override_dns_enhanced_not_modify),
                    stringResource(LocaleR.string.override_dns_fakeip_blacklist),
                    stringResource(LocaleR.string.override_dns_fakeip_whitelist),
                    "Rule",
                ),
                values = listOf(
                    null,
                    ConfigurationOverride.FilterMode.BlackList,
                    ConfigurationOverride.FilterMode.WhiteList,
                    ConfigurationOverride.FilterMode.Rule,
                ),
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fakeIPFilterMode = it)))
                },
            )
        }

        OverrideFormSection(stringResource(LocaleR.string.override_form_fake_ip_params)) {
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_label_fake_ip_range),
                value = config.dns.fakeIpRange,
                placeholder = "198.18.0.1/16",
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fakeIpRange = it)))
                },
            )
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_form_fake_ip_ipv6_range),
                value = config.dns.fakeIpRange6,
                placeholder = "fdfe:dcba:9876::1/64",
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fakeIpRange6 = it)))
                },
            )
            OverrideIntInputContent(
                title = "Fake-IP TTL",
                value = config.dns.fakeIpTtl,
                placeholder = "1",
                onValueChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fakeIpTtl = it)))
                },
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_dns_upstream)) {
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_dns_servers),
                replaceValue = config.dns.nameServer,
                startValue = config.dns.nameServerStart,
                endValue = config.dns.nameServerEnd,
                placeholder = stringResource(LocaleR.string.override_dns_servers_hint),
                onReplaceChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(nameServer = it)))
                },
                onStartChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(nameServerStart = it)))
                },
                onEndChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(nameServerEnd = it)))
                },
                onEditListGroup = onEditStringList,
            )
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_dns_fallback),
                replaceValue = config.dns.fallback,
                startValue = config.dns.fallbackStart,
                endValue = config.dns.fallbackEnd,
                placeholder = stringResource(LocaleR.string.override_dns_fallback_hint),
                onReplaceChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fallback = it)))
                },
                onStartChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fallbackStart = it)))
                },
                onEndChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fallbackEnd = it)))
                },
                onEditListGroup = onEditStringList,
            )
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_dns_default),
                replaceValue = config.dns.defaultServer,
                startValue = config.dns.defaultServerStart,
                endValue = config.dns.defaultServerEnd,
                placeholder = stringResource(LocaleR.string.override_dns_default_hint),
                onReplaceChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(defaultServer = it)))
                },
                onStartChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(defaultServerStart = it)))
                },
                onEndChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(defaultServerEnd = it)))
                },
                onEditListGroup = onEditStringList,
            )
            StringListWithModifiersInput(
                title = "Proxy Server Nameserver",
                replaceValue = config.dns.proxyServerNameserver,
                startValue = config.dns.proxyServerNameserverStart,
                endValue = config.dns.proxyServerNameserverEnd,
                placeholder = "https://doh.pub/dns-query",
                onReplaceChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(proxyServerNameserver = it)))
                },
                onStartChange = {
                    onConfigChange(
                        config.copy(dns = config.dns.copy(proxyServerNameserverStart = it)),
                    )
                },
                onEndChange = {
                    onConfigChange(
                        config.copy(dns = config.dns.copy(proxyServerNameserverEnd = it)),
                    )
                },
                onEditListGroup = onEditStringList,
            )
            StringListWithModifiersInput(
                title = "Direct Nameserver",
                replaceValue = config.dns.directNameserver,
                startValue = config.dns.directNameserverStart,
                endValue = config.dns.directNameserverEnd,
                placeholder = "system",
                onReplaceChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(directNameserver = it)))
                },
                onStartChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(directNameserverStart = it)))
                },
                onEndChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(directNameserverEnd = it)))
                },
                onEditListGroup = onEditStringList,
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_nameserver_policy_section)) {
            StringMapWithModifiersInput(
                title = stringResource(LocaleR.string.override_dns_nameserver_policy),
                replaceValue = config.dns.nameserverPolicy,
                mergeValue = config.dns.nameserverPolicyMerge,
                keyPlaceholder = stringResource(LocaleR.string.override_dns_nameserver_policy_key),
                valuePlaceholder = stringResource(LocaleR.string.override_dns_nameserver_policy_value),
                onReplaceChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(nameserverPolicy = it)))
                },
                onMergeChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(nameserverPolicyMerge = it)))
                },
                onEditMap = { _, title, keyPlaceholder, valuePlaceholder, value, callback ->
                    onEditStringMap(title, keyPlaceholder, valuePlaceholder, value, callback)
                },
            )
            StringMapWithModifiersInput(
                title = stringResource(LocaleR.string.override_form_proxy_server_nameserver_policy),
                replaceValue = config.dns.proxyServerNameserverPolicy,
                mergeValue = config.dns.proxyServerNameserverPolicyMerge,
                keyPlaceholder = stringResource(LocaleR.string.override_dns_nameserver_policy_key),
                valuePlaceholder = stringResource(LocaleR.string.override_dns_nameserver_policy_value),
                onReplaceChange = {
                    onConfigChange(
                        config.copy(dns = config.dns.copy(proxyServerNameserverPolicy = it)),
                    )
                },
                onMergeChange = {
                    onConfigChange(
                        config.copy(dns = config.dns.copy(proxyServerNameserverPolicyMerge = it)),
                    )
                },
                onEditMap = { _, title, keyPlaceholder, valuePlaceholder, value, callback ->
                    onEditStringMap(title, keyPlaceholder, valuePlaceholder, value, callback)
                },
            )
            StringMapWithModifiersInput(
                title = "Hosts",
                replaceValue = config.hosts,
                mergeValue = config.hostsMerge,
                keyPlaceholder = "domain",
                valuePlaceholder = "ip",
                onReplaceChange = { onConfigChange(config.copy(hosts = it)) },
                onMergeChange = { onConfigChange(config.copy(hostsMerge = it)) },
                onEditMap = { _, title, keyPlaceholder, valuePlaceholder, value, callback ->
                    onEditStringMap(title, keyPlaceholder, valuePlaceholder, value, callback)
                },
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_filter_list)) {
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_dns_fakeip_filter),
                replaceValue = config.dns.fakeIpFilter,
                startValue = config.dns.fakeIpFilterStart,
                endValue = config.dns.fakeIpFilterEnd,
                placeholder = stringResource(LocaleR.string.override_dns_fakeip_filter_hint),
                onReplaceChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fakeIpFilter = it)))
                },
                onStartChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fakeIpFilterStart = it)))
                },
                onEndChange = {
                    onConfigChange(config.copy(dns = config.dns.copy(fakeIpFilterEnd = it)))
                },
                onEditListGroup = onEditStringList,
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_fallback_switch)) {
            NullableBooleanSelector(
                title = stringResource(LocaleR.string.override_dns_fallback_geoip),
                value = config.dns.fallbackFilter.geoIp,
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(geoIp = it),
                            ),
                        ),
                    )
                },
            )
        }

        OverrideFormSection(stringResource(LocaleR.string.override_form_fallback_params)) {
            OverrideTextInputContent(
                title = stringResource(LocaleR.string.override_dns_fallback_geoip_code),
                value = config.dns.fallbackFilter.geoIpCode,
                placeholder = stringResource(LocaleR.string.override_dns_fallback_geoip_code_hint),
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(geoIpCode = it),
                            ),
                        ),
                    )
                },
            )
        }

        OverrideCardSection(stringResource(LocaleR.string.override_form_fallback_filter)) {
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_dns_fallback_domain),
                replaceValue = config.dns.fallbackFilter.domain,
                startValue = config.dns.fallbackFilter.domainStart,
                endValue = config.dns.fallbackFilter.domainEnd,
                placeholder = stringResource(LocaleR.string.override_dns_fallback_domain_hint),
                onReplaceChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(domain = it),
                            ),
                        ),
                    )
                },
                onStartChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(domainStart = it),
                            ),
                        ),
                    )
                },
                onEndChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(domainEnd = it),
                            ),
                        ),
                    )
                },
                onEditListGroup = onEditStringList,
            )
            StringListWithModifiersInput(
                title = stringResource(LocaleR.string.override_dns_fallback_ipcidr),
                replaceValue = config.dns.fallbackFilter.ipcidr,
                startValue = config.dns.fallbackFilter.ipcidrStart,
                endValue = config.dns.fallbackFilter.ipcidrEnd,
                placeholder = stringResource(LocaleR.string.override_dns_fallback_ipcidr_hint),
                onReplaceChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(ipcidr = it),
                            ),
                        ),
                    )
                },
                onStartChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(ipcidrStart = it),
                            ),
                        ),
                    )
                },
                onEndChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(ipcidrEnd = it),
                            ),
                        ),
                    )
                },
                onEditListGroup = onEditStringList,
            )
            StringListWithModifiersInput(
                title = "Fallback Geosite",
                replaceValue = config.dns.fallbackFilter.geosite,
                startValue = config.dns.fallbackFilter.geositeStart,
                endValue = config.dns.fallbackFilter.geositeEnd,
                placeholder = "gfw",
                onReplaceChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(geosite = it),
                            ),
                        ),
                    )
                },
                onStartChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(geositeStart = it),
                            ),
                        ),
                    )
                },
                onEndChange = {
                    onConfigChange(
                        config.copy(
                            dns = config.dns.copy(
                                fallbackFilter = config.dns.fallbackFilter.copy(geositeEnd = it),
                            ),
                        ),
                    )
                },
                onEditListGroup = onEditStringList,
            )
        }
    }
}
