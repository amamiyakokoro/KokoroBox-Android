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

package com.github.yumelira.yumebox.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Immutable
data class YumeDestructiveActionColors(
    val containerColor: Color,
    val contentColor: Color,
)

/**
 * Destructive / stop action colors tuned from the ACG launch button:
 * light surfaces use the softer error container, while dark surfaces keep
 * the error hue visible without making the action too loud.
 */
@Composable
fun yumeDestructiveActionColors(): YumeDestructiveActionColors {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkSurface = colorScheme.surface.luminance() < 0.5f

    return YumeDestructiveActionColors(
        containerColor = if (isDarkSurface) {
            colorScheme.error.copy(alpha = 0.22f)
        } else {
            colorScheme.errorContainer
        },
        contentColor = if (isDarkSurface) {
            colorScheme.error.copy(alpha = 0.92f)
        } else {
            colorScheme.onErrorContainer
        },
    )
}
