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



package com.amamiyakokoro.box.presentation.screen.node

import com.amamiyakokoro.box.presentation.util.extractFlaggedName

internal data class ProxyDisplayPresentation(
    val countryCode: String?,
    val displayName: String,
)

internal fun resolveProxyDisplayPresentation(
    name: String,
    title: String?,
): ProxyDisplayPresentation {
    val normalizedName = name.trim()
    val normalizedTitle = title?.trim().orEmpty()
    val primary = extractFlaggedName(normalizedTitle.ifBlank { normalizedName })
    val fallback = extractFlaggedName(normalizedName)

    return ProxyDisplayPresentation(
        countryCode = primary.countryCode ?: fallback.countryCode,
        displayName = primary.displayName.ifBlank {
            fallback.displayName.ifBlank { normalizedName }
        },
    )
}
