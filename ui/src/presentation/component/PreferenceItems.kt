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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3PreferenceItem
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3SwitchPreferenceItem
import com.github.yumelira.yumebox.presentation.icon.Yume
import com.github.yumelira.yumebox.presentation.icon.yume.chevron

@Composable
fun PreferenceSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
) {
    YumeMd3SwitchPreferenceItem(
        title = title,
        checked = checked,
        onCheckedChange = onCheckedChange,
        summary = summary,
        enabled = enabled,
    )
}

@Composable
fun PreferenceArrowItem(
    title: String,
    onClick: () -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    holdDownState: Boolean = false,
    startAction: @Composable (() -> Unit)? = null,
    endActions: @Composable (RowScope.() -> Unit)? = null,
    bottomAction: @Composable (() -> Unit)? = null,
) {
    val resolvedContainerColor = if (holdDownState) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
    } else {
        Color.Transparent
    }

    YumeMd3PreferenceItem(
        title = title,
        summary = summary,
        enabled = enabled,
        onClick = onClick,
        leadingContent = startAction,
        trailingContent = {
            endActions?.invoke(this)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Yume.chevron,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
            )
        },
        bottomContent = bottomAction,
        containerColor = resolvedContainerColor,
    )
}

@Composable
fun PreferenceValueItem(
    title: String,
    onClick: () -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    endActions: @Composable (RowScope.() -> Unit)? = null,
) {
    YumeMd3PreferenceItem(
        title = title,
        summary = summary,
        enabled = enabled,
        onClick = onClick,
        trailingContent = endActions,
    )
}

@Composable
fun <T> PreferenceEnumItem(
    title: String,
    currentValue: T,
    items: List<String>,
    values: List<T>,
    onValueChange: (T) -> Unit,
    summary: String? = null,
) {
    EnumSelector(
        title = title,
        summary = summary,
        currentValue = currentValue,
        items = items,
        values = values,
        onValueChange = onValueChange,
    )
}

@Composable
fun PreferenceListItem(
    title: String,
    summary: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    role: Role? = null,
    startAction: @Composable (() -> Unit)? = null,
    endActions: @Composable (RowScope.() -> Unit)? = null,
    bottomAction: @Composable (() -> Unit)? = null,
    containerColor: Color = Color.Transparent,
) {
    YumeMd3PreferenceItem(
        title = title,
        summary = summary,
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        role = role,
        leadingContent = startAction,
        trailingContent = endActions,
        bottomContent = bottomAction,
        containerColor = containerColor,
    )
}
