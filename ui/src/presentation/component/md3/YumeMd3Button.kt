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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.yumelira.yumebox.presentation.theme.UiDp
import dev.oom_wg.purejoy.mlang.MLang

@Composable
fun YumeMd3TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(text = text)
    }
}

@Composable
fun YumeMd3FilledButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(text = text)
    }
}

@Composable
fun YumeMd3DialogButtonRow(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String = MLang.Component.Button.Cancel,
    confirmText: String = MLang.Component.Button.Confirm,
    confirmEnabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
    ) {
        YumeMd3TextButton(
            text = cancelText,
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        )
        YumeMd3FilledButton(
            text = confirmText,
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            enabled = confirmEnabled,
        )
    }
}

@Composable
fun YumeMd3DialogFilledButtonRow(
    onSecondary: () -> Unit,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String = MLang.Component.Button.Clear,
    primaryText: String = MLang.Component.Button.Confirm,
    secondaryEnabled: Boolean = true,
    primaryEnabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
    ) {
        Button(
            onClick = onSecondary,
            enabled = secondaryEnabled,
            modifier = Modifier.weight(1f),
        ) {
            Text(text = secondaryText)
        }
        YumeMd3FilledButton(
            text = primaryText,
            onClick = onPrimary,
            modifier = Modifier.weight(1f),
            enabled = primaryEnabled,
        )
    }
}
