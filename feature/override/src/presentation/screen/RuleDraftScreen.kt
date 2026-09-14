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



package com.amamiyakokoro.box.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.*
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.util.OverrideRuleDraft
import com.amamiyakokoro.box.presentation.util.OverrideRuleTypePresets
import com.amamiyakokoro.box.presentation.util.OverrideStructuredEditorStore
import com.amamiyakokoro.box.presentation.util.rememberCurrentReferenceCatalog
import com.amamiyakokoro.box.presentation.util.supportsRuleExtra
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.compose.material3.Scaffold

@Composable
fun OverrideRuleDraftEditorScreen(
    navigator: DestinationsNavigator,
) {
    val listState = rememberLazyListState()
    val ruleTypeLabel = stringResource(LocaleR.string.override_editor_rule_type)
    val payloadLabel = stringResource(LocaleR.string.override_editor_payload)
    val matchResultLabel = stringResource(LocaleR.string.override_editor_match_result)
    val ruleTypeEmptyMessage = stringResource(LocaleR.string.override_editor_rule_type_empty)
    val payloadEmptyMessage = stringResource(LocaleR.string.override_editor_payload_empty)
    val targetEmptyMessage = stringResource(LocaleR.string.override_editor_target_empty)
    val title = OverrideStructuredEditorStore.ruleDraftEditorTitle.ifBlank {
        stringResource(LocaleR.string.override_editor_rule_edit)
    }
    val initialValue = remember { OverrideStructuredEditorStore.ruleDraftEditorValue }
    val saveFabController = rememberOverrideFabController()

    var ruleType by remember {
        mutableStateOf(initialValue?.type?.ifBlank { "DOMAIN-SUFFIX" } ?: "DOMAIN-SUFFIX")
    }
    var payload by remember { mutableStateOf(initialValue?.payload.orEmpty()) }
    var target by remember { mutableStateOf(initialValue?.target.orEmpty()) }
    var useSrc by remember {
        mutableStateOf(initialValue?.extras.orEmpty().any { it.equals("src", ignoreCase = true) })
    }
    var useNoResolve by remember {
        mutableStateOf(initialValue?.extras.orEmpty().any { it.equals("no-resolve", ignoreCase = true) })
    }
    var extraText by remember {
        mutableStateOf(
            initialValue
                ?.extras
                .orEmpty()
                .filterNot { it.equals("src", ignoreCase = true) || it.equals("no-resolve", ignoreCase = true) }
                .joinToString(","),
        )
    }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showTargetSelector by remember { mutableStateOf(false) }
    var showRuleProviderSelector by remember { mutableStateOf(false) }
    val selectedPresetIndex = OverrideRuleTypePresets.indexOfFirst {
        it.equals(ruleType, ignoreCase = true)
    }.coerceAtLeast(0)
    val canUseExtraSwitches = supportsRuleExtra(ruleType)
    val referenceCatalog = rememberCurrentReferenceCatalog()
    val isSubRuleTarget = ruleType.equals("SUB-RULE", ignoreCase = true)
    val isRuleSetType = ruleType.equals("RULE-SET", ignoreCase = true)
    val targetCandidates = if (isSubRuleTarget) {
        referenceCatalog.subRuleNames
    } else {
        referenceCatalog.proxyGroupNames
    }
    val ruleProviderCandidates = referenceCatalog.ruleProviderNames
    var selectedRuleProvider by remember(initialValue?.payload, ruleProviderCandidates) {
        mutableStateOf(
            initialValue?.payload
                ?.takeIf { candidate -> candidate.isNotBlank() && candidate in ruleProviderCandidates }
                .orEmpty(),
        )
    }
    val normalizedPayloadInput = payload.trim()
    val selectedRuleProviderValue = normalizedPayloadInput
        .takeIf { candidate -> candidate.isNotBlank() && candidate in ruleProviderCandidates }
        ?: selectedRuleProvider.trim()
    val targetLabel = stringResource(
        if (isSubRuleTarget) LocaleR.string.override_editor_sub_rule_target
        else LocaleR.string.override_editor_proxy_group_target,
    )
    val typeErrorText = errorText?.takeIf { it == ruleTypeEmptyMessage }
    val payloadErrorText = errorText?.takeIf { it == payloadEmptyMessage }
    val targetErrorText = errorText?.takeIf { it == targetEmptyMessage }

    DisposableEffect(Unit) {
        onDispose {
            OverrideStructuredEditorStore.clearRuleDraftEditor()
        }
    }

    Scaffold(
        floatingActionButton = {
            OverrideAnimatedFab(
                controller = saveFabController,
                visible = true,
                imageVector = AppMd3Icons.Action.Save,
                contentDescription = stringResource(LocaleR.string.override_editor_save_rule),
                onClick = {
                    val normalizedType = ruleType.trim().uppercase()
                    val normalizedPayload = payload.trim()
                    val resolvedPayload = if (normalizedPayload.isNotBlank()) {
                        normalizedPayload
                    } else if (normalizedType == "RULE-SET") {
                        selectedRuleProvider.trim()
                    } else {
                        normalizedPayload
                    }
                    val normalizedTarget = target.trim()
                    val extraValues = extraText
                        .split(',')
                        .map(String::trim)
                        .filter(String::isNotBlank)
                        .toMutableList()

                    if (normalizedType.isBlank()) {
                        errorText = ruleTypeEmptyMessage
                        return@OverrideAnimatedFab
                    }
                    if (!normalizedType.equals("MATCH", ignoreCase = true) && resolvedPayload.isBlank()) {
                        errorText = payloadEmptyMessage
                        return@OverrideAnimatedFab
                    }
                    if (normalizedTarget.isBlank()) {
                        errorText = targetEmptyMessage
                        return@OverrideAnimatedFab
                    }
                    if (canUseExtraSwitches) {
                        if (useSrc) {
                            extraValues += "src"
                        }
                        if (useNoResolve) {
                            extraValues += "no-resolve"
                        }
                    }
                    OverrideStructuredEditorStore.submitRuleDraft(
                        OverrideRuleDraft(
                            type = normalizedType,
                            payload = resolvedPayload,
                            target = normalizedTarget,
                            extras = extraValues.distinct(),
                        ),
                    )
                    navigator.navigateUp()
                },
            )
        },
        topBar = {
            TopBar(
                title = title,
            )
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(

            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
            lazyListState = listState,
            onScrollDirectionChanged = saveFabController::onScrollDirectionChanged,
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(OverrideSectionSpacing),
                ) {
                    OverrideSection(stringResource(LocaleR.string.override_editor_rule_body)) {
                        OverrideSelectorCard {
                            YumeMd3DropdownPreference(
                                title = ruleTypeLabel,
                                items = OverrideRuleTypePresets,
                                selectedIndex = selectedPresetIndex,
                                onSelectedIndexChange = { index ->
                                    if (index in OverrideRuleTypePresets.indices) {
                                        ruleType = OverrideRuleTypePresets[index]
                                        errorText = null
                                    }
                                },
                            )
                        }
                        if (isRuleSetType) {
                            OverrideSelectorCard {
                                PreferenceArrowItem(
                                    title = stringResource(LocaleR.string.override_form_rule_providers),
                                    onClick = {
                                        showRuleProviderSelector = true
                                        errorText = null
                                    },
                                    holdDownState = showRuleProviderSelector,
                                )
                            }
                        }
                        OverrideFormFieldColumn {
                            if (!ruleType.equals("MATCH", ignoreCase = true)) {
                                OverrideFormField(
                                    value = payload,
                                    onValueChange = {
                                        payload = it
                                        errorText = null
                                    },
                                    label = payloadLabel,
                                    supportText = if (isRuleSetType) {
                                        stringResource(LocaleR.string.override_editor_rule_provider_input_hint)
                                    } else {
                                        stringResource(LocaleR.string.override_editor_logical_rule_hint)
                                    },
                                    errorText = payloadErrorText,
                                )
                            }
                        }
                        OverrideSelectorCard {
                            PreferenceArrowItem(
                                title = if (ruleType.equals("MATCH", ignoreCase = true)) matchResultLabel else targetLabel,
                                onClick = {
                                    showTargetSelector = true
                                    errorText = null
                                },
                                holdDownState = showTargetSelector,
                            )
                        }
                        targetErrorText?.let { message ->
                            OverrideFieldAssistText(
                                text = message,
                                color = appErrorColor(),
                            )
                        }
                    }
                    if (canUseExtraSwitches) {
                        OverrideCardSection(stringResource(LocaleR.string.override_structured_proxies_title)) {
                            RuleExtraSwitchRow(
                                title = "src",
                                checked = useSrc,
                                onCheckedChange = { useSrc = it },
                            )
                            RuleExtraSwitchRow(
                                title = "no-resolve",
                                checked = useNoResolve,
                                onCheckedChange = { useNoResolve = it },
                            )
                        }
                    }
                    OverridePlainFormSection(stringResource(LocaleR.string.override_editor_additional_params)) {
                        OverrideFormField(
                            value = extraText,
                            onValueChange = {
                                extraText = it
                                errorText = null
                            },
                            label = stringResource(LocaleR.string.override_editor_other_extra_params),
                            supportText = stringResource(LocaleR.string.override_editor_extra_params_hint),
                        )
                    }
                    Spacer(modifier = Modifier.height(OverrideSectionBottomSpacing))
                }
            }
        }
        OverrideSingleValueSelectionSheet(
            show = showTargetSelector,
            title = stringResource(
                if (ruleType.equals("MATCH", ignoreCase = true)) LocaleR.string.override_editor_select_match_result
                else LocaleR.string.override_editor_select_sub_rule_target,
            ),
            value = target,
            groups = listOf(
                OverrideSelectionGroup(
                    title = stringResource(
                        if (isSubRuleTarget) LocaleR.string.override_structured_sub_rules_title
                        else LocaleR.string.override_editor_proxy_group,
                    ),
                    items = targetCandidates,
                ),
            ),
            customInputLabel = stringResource(
                if (ruleType.equals("MATCH", ignoreCase = true)) LocaleR.string.override_editor_custom_match_result
                else LocaleR.string.override_editor_custom_sub_rule_target,
            ),
            onDismiss = { showTargetSelector = false },
            onConfirm = { selectedValue ->
                target = selectedValue
                errorText = null
                showTargetSelector = false
            },
        )
        OverrideSingleValueSelectionSheet(
            show = showRuleProviderSelector,
            title = stringResource(LocaleR.string.override_editor_select_rule_provider),
            value = selectedRuleProviderValue,
            groups = listOf(
                OverrideSelectionGroup(
                    title = stringResource(LocaleR.string.override_form_rule_providers),
                    items = ruleProviderCandidates,
                ),
            ),
            customInputLabel = "",
            allowCustomValue = false,
            onDismiss = { showRuleProviderSelector = false },
            onConfirm = { selectedValue ->
                selectedRuleProvider = selectedValue.trim()
                errorText = null
                showRuleProviderSelector = false
            },
        )
    }
}

@Composable
private fun RuleExtraSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    PreferenceSwitchItem(
        title = title,
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}
