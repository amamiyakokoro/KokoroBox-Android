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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons

@Composable
fun YumeMd3DropdownPreference(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    showDivider: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentIndex = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    val selectedLabel = items.getOrNull(currentIndex).orEmpty()

    YumeMd3PreferenceItem(
        title = title,
        modifier = modifier,
        summary = summary,
        enabled = enabled,
        onClick = null,
        showDivider = showDivider,
        trailingContent = {
            val chipShape = RoundedCornerShape(999.dp)
            val chipBackground = if (enabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
            val chipContentColor = if (enabled) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            }

            Box {
                Row(
                    modifier = Modifier
                        .clip(chipShape)
                        .background(chipBackground)
                        .clickable(
                            enabled = enabled,
                            role = Role.Button,
                            onClick = { expanded = true },
                        )
                        .padding(start = 12.dp, top = 7.dp, end = 8.dp, bottom = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = chipContentColor,
                        maxLines = 1,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = AppMd3Icons.Navigation.DownAngle,
                        contentDescription = null,
                        tint = chipContentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    items.forEachIndexed { index, item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item,
                                    color = if (index == currentIndex) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            },
                            onClick = {
                                onSelectedIndexChange(index)
                                expanded = false
                            },
                        )
                    }
                }
            }
        },
    )
}
