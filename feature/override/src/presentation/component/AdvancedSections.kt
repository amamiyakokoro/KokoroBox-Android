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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.model.ConfigurationOverride
import com.amamiyakokoro.box.presentation.util.*
import kotlinx.serialization.json.JsonElement

typealias OpenRuleListEditor = (
    title: String,
    values: OverrideListModeValues<List<String>>,
    availableModes: List<OverrideListEditorMode>,
    selectedMode: OverrideListEditorMode,
    referenceCatalog: OverrideReferenceCatalog,
    onValueChange: (OverrideListModeValues<List<String>>) -> Unit,
) -> Unit

typealias OpenStructuredObjectListEditor = (
    type: OverrideStructuredObjectType,
    title: String,
    values: OverrideListModeValues<List<Map<String, JsonElement>>>,
    availableModes: List<OverrideListEditorMode>,
    selectedMode: OverrideListEditorMode,
    referenceCatalog: OverrideReferenceCatalog,
    onValueChange: (OverrideListModeValues<List<Map<String, JsonElement>>>) -> Unit,
) -> Unit

typealias OpenObjectMapEditor = (
    type: OverrideStructuredMapType,
    title: String,
    values: OverrideListModeValues<Map<String, Map<String, JsonElement>>>,
    availableModes: List<OverrideListEditorMode>,
    selectedMode: OverrideListEditorMode,
    onValueChange: (OverrideListModeValues<Map<String, Map<String, JsonElement>>>) -> Unit,
) -> Unit

typealias OpenSubRulesEditor = (
    title: String,
    values: OverrideListModeValues<Map<String, List<String>>>,
    availableModes: List<OverrideListEditorMode>,
    selectedMode: OverrideListEditorMode,
    referenceCatalog: OverrideReferenceCatalog,
    onValueChange: (OverrideListModeValues<Map<String, List<String>>>) -> Unit,
) -> Unit

@Composable
fun RulesEditor(
    config: ConfigurationOverride,
    onConfigChange: (ConfigurationOverride) -> Unit,
    onEditRuleList: OpenRuleListEditor,
) {
    val rulesTitle = stringResource(LocaleR.string.override_editor_rules)
    Column {
        Title(stringResource(LocaleR.string.override_form_rule_chain))
        OverrideSelectorCard {
            StructuredEditorEntry(
                title = rulesTitle,
                summary = buildModifierSummary(
                    replaceCount = config.rules?.size ?: 0,
                    startCount = config.rulesStart?.size ?: 0,
                    endCount = config.rulesEnd?.size ?: 0,
                    emptyHint = stringResource(LocaleR.string.override_form_rule_chain_not_set),
                ),
                onClick = {
                    val values = OverrideListModeValues(
                        replaceValue = config.rules,
                        startValue = config.rulesStart,
                        endValue = config.rulesEnd,
                    )
                    onEditRuleList(
                        rulesTitle,
                        values,
                        listOf(
                            OverrideListEditorMode.Replace,
                            OverrideListEditorMode.Start,
                            OverrideListEditorMode.End,
                        ),
                        resolveInitialEditorMode(
                            availableModes = listOf(
                                OverrideListEditorMode.Replace,
                                OverrideListEditorMode.Start,
                                OverrideListEditorMode.End,
                            ),
                            values = values,
                        ),
                        buildOverrideReferenceCatalog(config),
                    ) { updatedValues ->
                        onConfigChange(
                            config.copy(
                                rules = updatedValues.replaceValue,
                                rulesStart = updatedValues.startValue,
                                rulesEnd = updatedValues.endValue,
                            ),
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun SubRulesEditorSection(
    config: ConfigurationOverride,
    onConfigChange: (ConfigurationOverride) -> Unit,
    onEditSubRules: OpenSubRulesEditor,
    onEditJson: OpenJsonEditor,
) {
    val subRulesTitle = stringResource(LocaleR.string.override_form_sub_rules)
    Column {
        Title(subRulesTitle)
        StructuredInputContent(
            title = subRulesTitle,
            summary = buildMergeModifierSummary(
                replaceCount = config.subRules?.size ?: 0,
                mergeCount = config.subRulesMerge?.size ?: 0,
                emptyHint = stringResource(LocaleR.string.override_form_sub_rules_hint),
            ),
            advancedSummary = stringResource(LocaleR.string.override_form_sub_rules_advanced),
            onStructuredClick = {
                val values = OverrideListModeValues(
                    replaceValue = config.subRules,
                    mergeValue = config.subRulesMerge,
                )
                    onEditSubRules(
                        subRulesTitle,
                        values,
                        listOf(
                            OverrideListEditorMode.Replace,
                        OverrideListEditorMode.Merge,
                    ),
                        resolveInitialEditorMode(
                            availableModes = listOf(
                                OverrideListEditorMode.Replace,
                                OverrideListEditorMode.Merge,
                            ),
                            values = values,
                        ),
                        buildOverrideReferenceCatalog(config),
                    ) { updatedValues ->
                        onConfigChange(
                            config.copy(
                                subRules = updatedValues.replaceValue,
                            subRulesMerge = updatedValues.mergeValue,
                        ),
                    )
                }
            },
            onAdvancedClick = {
                onEditJson(
                    subRulesTitle,
                    "{\n  \"sub-rule\": [\"DOMAIN,example.com,DIRECT\"]\n}",
                    encodeSubRules(config.subRules),
                ) {
                    onConfigChange(config.copy(subRules = decodeSubRules(it)))
                }
            },
        )
    }
}

@Composable
fun RuleProvidersEditor(
    config: ConfigurationOverride,
    onConfigChange: (ConfigurationOverride) -> Unit,
    onEditObjectMap: OpenObjectMapEditor,
    onEditJson: OpenJsonEditor,
) {
    val ruleProvidersTitle = stringResource(LocaleR.string.override_form_rule_providers)
    Column {
        Title(ruleProvidersTitle)
        StructuredInputContent(
            title = ruleProvidersTitle,
            summary = buildMergeModifierSummary(
                replaceCount = config.ruleProviders?.size ?: 0,
                mergeCount = config.ruleProvidersMerge?.size ?: 0,
                emptyHint = stringResource(LocaleR.string.override_form_rule_providers_hint),
            ),
            advancedSummary = stringResource(LocaleR.string.override_form_rule_providers_advanced),
            onStructuredClick = {
                val values = OverrideListModeValues(
                    replaceValue = config.ruleProviders,
                    mergeValue = config.ruleProvidersMerge,
                )
                onEditObjectMap(
                    OverrideStructuredMapType.RuleProviders,
                    ruleProvidersTitle,
                    values,
                    listOf(
                        OverrideListEditorMode.Replace,
                        OverrideListEditorMode.Merge,
                    ),
                    resolveInitialEditorMode(
                        availableModes = listOf(
                            OverrideListEditorMode.Replace,
                            OverrideListEditorMode.Merge,
                        ),
                        values = values,
                    ),
                ) { updatedValues ->
                    onConfigChange(
                        config.copy(
                            ruleProviders = updatedValues.replaceValue,
                            ruleProvidersMerge = updatedValues.mergeValue,
                        ),
                    )
                }
            },
            onAdvancedClick = {
                onEditJson(
                    ruleProvidersTitle,
                    "{\n  \"google\": {\n    \"type\": \"http\"\n  }\n}",
                    encodeObjectMap(config.ruleProviders),
                ) {
                    onConfigChange(config.copy(ruleProviders = decodeObjectMap(it)))
                }
            },
        )
    }
}

@Composable
fun ProxiesEditor(
    config: ConfigurationOverride,
    onConfigChange: (ConfigurationOverride) -> Unit,
    onEditObjectList: OpenStructuredObjectListEditor,
    onEditJson: OpenJsonEditor,
) {
    val proxyNodesTitle = stringResource(LocaleR.string.override_form_proxy_nodes)
    Column {
        Title(proxyNodesTitle)
        OverrideSelectorCard {
            StructuredEditorEntry(
                title = proxyNodesTitle,
                summary = buildModifierSummary(
                    replaceCount = config.proxies?.size ?: 0,
                    startCount = config.proxiesStart?.size ?: 0,
                    endCount = config.proxiesEnd?.size ?: 0,
                    emptyHint = stringResource(LocaleR.string.override_form_proxy_nodes_hint),
                ),
                onClick = {
                    val values = OverrideListModeValues(
                        replaceValue = config.proxies,
                        startValue = config.proxiesStart,
                        endValue = config.proxiesEnd,
                    )
                    onEditObjectList(
                        OverrideStructuredObjectType.Proxies,
                        proxyNodesTitle,
                        values,
                        listOf(
                            OverrideListEditorMode.Replace,
                            OverrideListEditorMode.Start,
                            OverrideListEditorMode.End,
                        ),
                        resolveInitialEditorMode(
                            availableModes = listOf(
                                OverrideListEditorMode.Replace,
                                OverrideListEditorMode.Start,
                                OverrideListEditorMode.End,
                            ),
                            values = values,
                        ),
                        buildOverrideReferenceCatalog(config),
                    ) { updatedValues ->
                        onConfigChange(
                            config.copy(
                                proxies = updatedValues.replaceValue,
                                proxiesStart = updatedValues.startValue,
                                proxiesEnd = updatedValues.endValue,
                            ),
                        )
                    }
                },
            )
        }

    }
}

@Composable
fun ProxyProvidersEditor(
    config: ConfigurationOverride,
    onConfigChange: (ConfigurationOverride) -> Unit,
    onEditObjectMap: OpenObjectMapEditor,
    onEditJson: OpenJsonEditor,
) {
    val proxyProvidersTitle = stringResource(LocaleR.string.override_form_proxy_providers)
    Column {
        Title(proxyProvidersTitle)
        StructuredInputContent(
            title = proxyProvidersTitle,
            summary = buildMergeModifierSummary(
                replaceCount = config.proxyProviders?.size ?: 0,
                mergeCount = config.proxyProvidersMerge?.size ?: 0,
                emptyHint = stringResource(LocaleR.string.override_form_proxy_providers_hint),
            ),
            advancedSummary = stringResource(LocaleR.string.override_form_proxy_providers_advanced),
            onStructuredClick = {
                val values = OverrideListModeValues(
                    replaceValue = config.proxyProviders,
                    mergeValue = config.proxyProvidersMerge,
                )
                onEditObjectMap(
                    OverrideStructuredMapType.ProxyProviders,
                    proxyProvidersTitle,
                    values,
                    listOf(
                        OverrideListEditorMode.Replace,
                        OverrideListEditorMode.Merge,
                    ),
                    resolveInitialEditorMode(
                        availableModes = listOf(
                            OverrideListEditorMode.Replace,
                            OverrideListEditorMode.Merge,
                        ),
                        values = values,
                    ),
                ) { updatedValues ->
                    onConfigChange(
                        config.copy(
                            proxyProviders = updatedValues.replaceValue,
                            proxyProvidersMerge = updatedValues.mergeValue,
                        ),
                    )
                }
            },
            onAdvancedClick = {
                onEditJson(
                    proxyProvidersTitle,
                    "{\n  \"provider\": {\n    \"type\": \"http\"\n  }\n}",
                    encodeObjectMap(config.proxyProviders),
                ) {
                    onConfigChange(config.copy(proxyProviders = decodeObjectMap(it)))
                }
            },
        )
    }
}

@Composable
fun ProxyGroupsEditor(
    config: ConfigurationOverride,
    onConfigChange: (ConfigurationOverride) -> Unit,
    onEditObjectList: OpenStructuredObjectListEditor,
    onEditJson: OpenJsonEditor,
) {
    val proxyGroupsTitle = stringResource(LocaleR.string.override_form_proxy_groups)
    Column {
        Title(proxyGroupsTitle)
        OverrideSelectorCard {
            StructuredEditorEntry(
                title = proxyGroupsTitle,
                summary = buildModifierSummary(
                    replaceCount = config.proxyGroups?.size ?: 0,
                    startCount = config.proxyGroupsStart?.size ?: 0,
                    endCount = config.proxyGroupsEnd?.size ?: 0,
                    emptyHint = stringResource(LocaleR.string.override_form_proxy_groups_hint),
                ),
                onClick = {
                    val values = OverrideListModeValues(
                        replaceValue = config.proxyGroups,
                        startValue = config.proxyGroupsStart,
                        endValue = config.proxyGroupsEnd,
                    )
                    onEditObjectList(
                        OverrideStructuredObjectType.ProxyGroups,
                        proxyGroupsTitle,
                        values,
                        listOf(
                            OverrideListEditorMode.Replace,
                            OverrideListEditorMode.Start,
                            OverrideListEditorMode.End,
                        ),
                        resolveInitialEditorMode(
                            availableModes = listOf(
                                OverrideListEditorMode.Replace,
                                OverrideListEditorMode.Start,
                                OverrideListEditorMode.End,
                            ),
                            values = values,
                        ),
                        buildOverrideReferenceCatalog(config),
                    ) { updatedValues ->
                        onConfigChange(
                            config.copy(
                                proxyGroups = updatedValues.replaceValue,
                                proxyGroupsStart = updatedValues.startValue,
                                proxyGroupsEnd = updatedValues.endValue,
                            ),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun StructuredInputContent(
    title: String,
    summary: String,
    advancedSummary: String,
    onStructuredClick: () -> Unit,
    onAdvancedClick: () -> Unit,
) {
    var advancedExpanded by remember { mutableStateOf(false) }

    Column {
        OverrideSelectorCard {
            PreferenceArrowItem(
                title = stringResource(LocaleR.string.override_form_structured_edit).format(title),
                summary = summary,
                onClick = onStructuredClick,
            )
        }
        OverrideAdvancedCard(
            title = stringResource(LocaleR.string.override_form_advanced_json).format(title),
            summary = advancedSummary,
            expanded = advancedExpanded,
            onExpandedChange = { advancedExpanded = it },
        ) {
            PreferenceArrowItem(
                title = stringResource(LocaleR.string.override_form_open_advanced_edit),
                summary = stringResource(LocaleR.string.override_form_open_advanced_edit_summary),
                onClick = onAdvancedClick,
            )
        }
    }
}

@Composable
private fun StructuredEditorEntry(
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    PreferenceArrowItem(
        title = title,
        summary = summary,
        onClick = onClick,
    )
}

@Composable
private fun buildStructuredSummary(
    count: Int,
    emptyHint: String,
): String {
    return if (count > 0) {
        stringResource(LocaleR.string.override_form_items_configured).format(count)
    } else {
        emptyHint
    }
}

@Composable
private fun buildModifierSummary(
    replaceCount: Int,
    startCount: Int,
    endCount: Int,
    emptyHint: String,
): String {
    return buildList {
        if (replaceCount > 0) {
            add(stringResource(LocaleR.string.override_modifier_items_count).format(replaceCount))
        }
        if (startCount > 0) {
            add(stringResource(LocaleR.string.override_modifier_start))
        }
        if (endCount > 0) {
            add(stringResource(LocaleR.string.override_modifier_end))
        }
    }.joinToString(" · ").ifEmpty { emptyHint }
}

@Composable
private fun buildMergeModifierSummary(
    replaceCount: Int,
    mergeCount: Int,
    emptyHint: String,
): String {
    return buildList {
        if (replaceCount > 0) {
            add(stringResource(LocaleR.string.override_modifier_items_count).format(replaceCount))
        }
        if (mergeCount > 0) {
            add(stringResource(LocaleR.string.override_modifier_merge))
        }
    }.joinToString(" · ").ifEmpty { emptyHint }
}
