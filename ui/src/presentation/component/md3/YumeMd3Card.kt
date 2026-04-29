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

package com.github.yumelira.yumebox.presentation.component.md3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card as MaterialCard
import androidx.compose.material3.CardColors as MaterialCardColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.github.yumelira.yumebox.presentation.theme.horizontalPadding

@Composable
fun YumeMd3Card(
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
    MaterialCard(
        modifier = if (applyHorizontalPadding) {
            modifier.horizontalPadding()
        } else {
            modifier
        },
        shape = RoundedCornerShape(cornerRadius.dp),
        colors = colors,
    ) {
        Column(modifier = Modifier.padding(insideMargin)) {
            content()
        }
    }
}
