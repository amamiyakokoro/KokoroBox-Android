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



package com.github.yumelira.yumebox.presentation.screen.node

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.data.model.ProxySortMode

internal val NodeSortModes = listOf(
    ProxySortMode.DEFAULT,
    ProxySortMode.BY_NAME,
    ProxySortMode.BY_LATENCY,
)

@Composable
internal fun NodeSortPopup(
    show: Boolean,
    onDismiss: () -> Unit,
    sortMode: ProxySortMode,
    onSortSelected: (ProxySortMode) -> Unit,
) {
    val selectedSortIndex = NodeSortModes.indexOf(sortMode).coerceAtLeast(0)
    DropdownMenu(
        expanded = show,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
    ) {
        NodeSortModes.forEachIndexed { index, mode ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = mode.displayName,
                        color = if (selectedSortIndex == index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                },
                onClick = {
                    if (mode != sortMode) onSortSelected(mode)
                    onDismiss()
                },
            )
        }
    }
}
