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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardColors as MaterialCardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3Card
import com.github.yumelira.yumebox.presentation.theme.UiDp

@Composable
fun Card(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 16,
    insideMargin: PaddingValues = PaddingValues(UiDp.dp0),
    applyHorizontalPadding: Boolean = true,
    colors: MaterialCardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    content: @Composable () -> Unit,
) {
    YumeMd3Card(
        modifier = modifier,
        cornerRadius = cornerRadius,
        insideMargin = insideMargin,
        applyHorizontalPadding = applyHorizontalPadding,
        colors = colors,
        content = content,
    )
}
