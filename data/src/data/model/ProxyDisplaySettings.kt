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



package com.amamiyakokoro.box.data.model

import com.amamiyakokoro.box.core.locale.R as LocaleR

const val PROXY_SHEET_HEIGHT_FRACTION_MIN = 0.5f
const val PROXY_SHEET_HEIGHT_FRACTION_MAX = 0.8f
const val PROXY_SHEET_HEIGHT_FRACTION_DEFAULT = 0.55f

fun normalizeProxySheetHeightFraction(value: Float): Float =
    value.coerceIn(PROXY_SHEET_HEIGHT_FRACTION_MIN, PROXY_SHEET_HEIGHT_FRACTION_MAX)

enum class ProxyDisplayMode(val labelRes: Int) {
    SINGLE_DETAILED(LocaleR.string.proxy_display_mode_single_detailed),
    SINGLE_SIMPLE(LocaleR.string.proxy_display_mode_single_simple),
    DOUBLE_DETAILED(LocaleR.string.proxy_display_mode_double_detailed),
    DOUBLE_SIMPLE(LocaleR.string.proxy_display_mode_double_simple);

    val isSingleColumn: Boolean
        get() = this == SINGLE_DETAILED || this == SINGLE_SIMPLE

    val showDetail: Boolean
        get() = this == SINGLE_DETAILED || this == DOUBLE_DETAILED
}

enum class ProxySortMode(val labelRes: Int) {
    DEFAULT(LocaleR.string.proxy_sort_mode_default),
    BY_NAME(LocaleR.string.proxy_sort_mode_by_name),
    BY_LATENCY(LocaleR.string.proxy_sort_mode_by_latency);
}
