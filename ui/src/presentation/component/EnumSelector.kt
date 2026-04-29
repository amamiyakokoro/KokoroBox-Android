/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  YumeLira 2025 - Present
 *
 */

package com.github.yumelira.yumebox.presentation.component

import androidx.compose.runtime.Composable
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3EnumSelector

@Composable
fun <T> EnumSelector(
    title: String,
    summary: String? = null,
    currentValue: T,
    items: List<String>,
    values: List<T>,
    onValueChange: (T) -> Unit,
) {
    YumeMd3EnumSelector(
        title = title,
        summary = summary,
        currentValue = currentValue,
        items = items,
        values = values,
        onValueChange = onValueChange,
    )
}
