/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.data.integration.kokoro.*
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3OutlinedTextField
import com.amamiyakokoro.box.presentation.theme.UiDp

internal data class KokoroRuleEditorState(
    val rule: KokoroCustomRuleInput,
    val types: List<String>,
    val targets: List<String>,
    val providers: List<String>,
    val enabled: Boolean,
    val canConfirm: Boolean,
    val onTypeChange: (Int) -> Unit,
    val onPayloadChange: (String) -> Unit,
    val onProviderChange: (Int) -> Unit,
    val onTargetChange: (Int) -> Unit,
)

@Composable
internal fun rememberKokoroRuleEditorState(
    options: KokoroCustomRulesOptions,
    initialRule: KokoroCustomRuleInput?,
    fromConnection: Boolean = false,
    canAdd: Boolean = true,
    enabled: Boolean = true,
    resetKey: Any? = null,
): KokoroRuleEditorState {
    val defaultType = preferredKokoroDomainRuleType(options.ruleTypes)
    val defaultTarget = options.targets.firstOrNull() ?: "DIRECT"
    val types = (listOf("DOMAIN-SUFFIX", "DOMAIN").filter { it in options.ruleTypes } +
        options.ruleTypes.filterNot { it == "DOMAIN-SUFFIX" || it == "DOMAIN" })
        .ifEmpty { listOf(defaultType) }
    val targets = options.targets.ifEmpty { listOf(defaultTarget) }
    val providers = options.ruleProviders.filter { it.behavior == "domain" }.map { it.name }
    var type by rememberSaveable(resetKey, initialRule?.type, types) {
        mutableStateOf(initialRule?.type?.takeIf { it in types } ?: defaultType)
    }
    var payload by rememberSaveable(resetKey, initialRule?.payload, providers) {
        mutableStateOf(
            initialRule?.payload
                ?.let { if (fromConnection) kokoroConnectionRulePayload(it, initialRule.type) else it }
                ?.takeUnless { initialRule.type == "RULE-SET" && it !in providers }
                ?: if (initialRule?.type == "RULE-SET") providers.firstOrNull().orEmpty() else "",
        )
    }
    var target by rememberSaveable(resetKey, initialRule?.target, targets) {
        mutableStateOf(initialRule?.target?.takeIf { it in targets } ?: defaultTarget)
    }
    val canConfirm = enabled && canAdd && type in options.ruleTypes && target in options.targets &&
        (type == "MATCH" || payload.isNotEmpty()) &&
        (type != "RULE-SET" || payload in providers)

    return KokoroRuleEditorState(
        rule = KokoroCustomRuleInput(type, if (type == "MATCH") null else payload, target),
        types = types, targets = targets, providers = providers,
        enabled = enabled, canConfirm = canConfirm,
        onTypeChange = { index ->
            type = types.getOrElse(index) { defaultType }
            when (type) {
                "DOMAIN-SUFFIX", "DOMAIN" -> if (fromConnection) {
                    payload = kokoroConnectionRulePayload(initialRule?.payload.orEmpty(), type).orEmpty()
                }
                "MATCH" -> payload = ""
                "RULE-SET" -> if (payload !in providers) payload = providers.firstOrNull().orEmpty()
            }
        },
        onPayloadChange = { if (it.length <= options.maxPayloadLength) payload = it },
        onProviderChange = { index -> payload = providers.getOrNull(index).orEmpty() },
        onTargetChange = { index -> target = targets.getOrElse(index) { defaultTarget } },
    )
}

@Composable
internal fun KokoroRuleEditorContent(
    editor: KokoroRuleEditorState,
    canAdd: Boolean = true,
    applyHorizontalPadding: Boolean = true,
) {
    if (!canAdd) {
        Text(stringResource(LocaleR.string.meta_feature_custom_rules_limit_reached),
            modifier = Modifier.fillMaxWidth().padding(UiDp.dp16))
    }
    val rule = editor.rule
    Card(applyHorizontalPadding = applyHorizontalPadding) {
        YumeMd3DropdownPreference(
            title = stringResource(LocaleR.string.meta_feature_custom_rules_type),
            items = editor.types,
            selectedIndex = editor.types.indexOf(rule.type).coerceAtLeast(0),
            onSelectedIndexChange = editor.onTypeChange,
            enabled = editor.enabled,
        )
        when (rule.type) {
            "MATCH" -> Text(
                stringResource(LocaleR.string.meta_feature_custom_rules_match_payload_hint),
                modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            "RULE-SET" -> YumeMd3DropdownPreference(
                title = stringResource(LocaleR.string.meta_feature_custom_rules_provider),
                items = editor.providers,
                selectedIndex = editor.providers.indexOf(rule.payload).coerceAtLeast(0),
                onSelectedIndexChange = editor.onProviderChange,
                enabled = editor.enabled && editor.providers.isNotEmpty(),
            )
            else -> YumeMd3OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(UiDp.dp12),
                value = rule.payload.orEmpty(),
                onValueChange = editor.onPayloadChange,
                label = stringResource(LocaleR.string.meta_feature_custom_rules_payload),
                singleLine = true,
                enabled = editor.enabled,
            )
        }
        YumeMd3DropdownPreference(
            title = stringResource(LocaleR.string.meta_feature_custom_rules_target),
            items = editor.targets,
            selectedIndex = editor.targets.indexOf(rule.target).coerceAtLeast(0),
            onSelectedIndexChange = editor.onTargetChange,
            enabled = editor.enabled,
        )
    }
    Spacer(Modifier.height(UiDp.dp16))
}
