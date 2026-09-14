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



package com.amamiyakokoro.box.presentation.util

import androidx.annotation.StringRes
import com.amamiyakokoro.box.core.locale.R as LocaleR

enum class OverrideEditorSection(
    @StringRes val titleRes: Int,
    @StringRes val summaryRes: Int,
) {
    General(LocaleR.string.override_section_general_title, LocaleR.string.override_section_general_summary),
    Dns(LocaleR.string.override_section_dns_title, LocaleR.string.override_section_dns_summary),
    Sniffer(LocaleR.string.override_section_sniffer_title, LocaleR.string.override_section_sniffer_summary),
    Inbound(LocaleR.string.override_section_inbound_title, LocaleR.string.override_section_inbound_summary),
    Rules(LocaleR.string.override_section_rules_title, LocaleR.string.override_section_rules_summary),
    Proxies(LocaleR.string.override_section_proxies_title, LocaleR.string.override_section_proxies_summary),
    ProxyProviders(LocaleR.string.override_section_proxy_providers_title, LocaleR.string.override_section_proxy_providers_summary),
    ProxyGroups(LocaleR.string.override_section_proxy_groups_title, LocaleR.string.override_section_proxy_groups_summary),
    RuleProviders(LocaleR.string.override_section_rule_providers_title, LocaleR.string.override_section_rule_providers_summary),
    SubRules(LocaleR.string.override_section_sub_rules_title, LocaleR.string.override_section_sub_rules_summary),
}

enum class OverrideModifierVisualMode { Replace, Start, End, Merge, Force }

sealed interface OverrideSaveState {
    data object Idle : OverrideSaveState
    data object Saving : OverrideSaveState
}

sealed interface OverrideSaveEvent {
    data class Saved(
        val configId: String,
    ) : OverrideSaveEvent

    data class Failed(
        val message: String,
    ) : OverrideSaveEvent
}

data class OverrideSectionSummary(
    val modifiedCount: Int,
    val visualModes: Set<OverrideModifierVisualMode>,
)

data class OverrideEditorOverview(
    val changedFieldCount: Int,
    val activeSectionCount: Int,
    val replaceCount: Int,
    val appendCount: Int,
    val mergeCount: Int,
    val forceCount: Int,
    val sectionSummaries: Map<OverrideEditorSection, OverrideSectionSummary>,
    val warnings: List<String>,
)
