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


import com.amamiyakokoro.box.presentation.theme.UiDp
import com.amamiyakokoro.box.presentation.theme.yumeDestructiveActionColors
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.amamiyakokoro.box.presentation.component.AppIcon
import com.amamiyakokoro.box.presentation.component.AppIconButton

@Composable
internal fun RowScope.OverrideTopBarAction(
    icon: ImageVector,
    contentDescription: String,
    spacedFromNext: Boolean = false,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val destructiveActionColors = yumeDestructiveActionColors()

    AppIconButton(
        modifier = if (spacedFromNext) Modifier.padding(end = UiDp.dp12) else Modifier,
        onClick = onClick,
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (destructive) destructiveActionColors.contentColor else Color.Unspecified,
        )
    }
}
