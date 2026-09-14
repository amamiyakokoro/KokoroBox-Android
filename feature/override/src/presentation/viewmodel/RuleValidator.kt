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



package com.amamiyakokoro.box.presentation.viewmodel

import com.amamiyakokoro.box.core.model.ConfigurationOverride
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.locale.UiText

internal object RuleValidator {

    fun validate(config: ConfigurationOverride): List<UiText> {
        val warnings = mutableListOf<UiText>()
        val rules = config.rules.orEmpty()
        rules.forEachIndexed { index, raw ->
            val rule = raw.trim()
            if (rule.isEmpty()) {
                warnings += UiText.Resource(
                    LocaleR.string.override_rule_empty_warning,
                    listOf(index + 1),
                )
                return@forEachIndexed
            }
            val parts = rule.split(',').map { it.trim() }
            if (parts.size < 2) {
                warnings += UiText.Resource(
                    LocaleR.string.override_rule_invalid_format_warning,
                    listOf(index + 1, rule),
                )
                return@forEachIndexed
            }
            if (parts.first().equals("RULE-SET", ignoreCase = true) && parts.size < 3) {
                warnings += UiText.Resource(
                    LocaleR.string.override_rule_missing_target_warning,
                    listOf(index + 1, rule),
                )
            }
        }
        return warnings
    }
}
