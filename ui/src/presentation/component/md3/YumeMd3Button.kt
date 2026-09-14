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

package com.amamiyakokoro.box.presentation.component.md3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.theme.UiDp
import com.amamiyakokoro.box.presentation.theme.yumeDestructiveActionColors

@Composable
fun YumeMd3TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = yumeMd3TextButtonColors(destructive = destructive),
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
    destructive: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = yumeMd3FilledButtonColors(destructive = destructive),
    ) {
        Text(text = text)
    }
}

@Composable
fun YumeMd3DialogButtonRow(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    cancelText: String? = null,
    confirmText: String? = null,
    confirmEnabled: Boolean = true,
    confirmDestructive: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
    ) {
        YumeMd3TextButton(
            text = cancelText ?: stringResource(LocaleR.string.component_button_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        )
        YumeMd3FilledButton(
            text = confirmText ?: stringResource(LocaleR.string.component_button_confirm),
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            enabled = confirmEnabled,
            destructive = confirmDestructive,
        )
    }
}

@Composable
fun YumeMd3DialogFilledButtonRow(
    onSecondary: () -> Unit,
    onPrimary: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    primaryText: String? = null,
    secondaryEnabled: Boolean = true,
    primaryEnabled: Boolean = true,
    secondaryDestructive: Boolean = true,
    primaryDestructive: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
    ) {
        Button(
            onClick = onSecondary,
            enabled = secondaryEnabled,
            modifier = Modifier.weight(1f),
            colors = yumeMd3FilledButtonColors(destructive = secondaryDestructive),
        ) {
            Text(text = secondaryText ?: stringResource(LocaleR.string.component_button_clear))
        }
        YumeMd3FilledButton(
            text = primaryText ?: stringResource(LocaleR.string.component_button_confirm),
            onClick = onPrimary,
            modifier = Modifier.weight(1f),
            enabled = primaryEnabled,
            destructive = primaryDestructive,
        )
    }
}

@Composable
private fun yumeMd3FilledButtonColors(destructive: Boolean): ButtonColors = if (destructive) {
    val destructiveColors = yumeDestructiveActionColors()
    ButtonDefaults.buttonColors(
        containerColor = destructiveColors.containerColor,
        contentColor = destructiveColors.contentColor,
    )
} else {
    ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    )
}

@Composable
private fun yumeMd3TextButtonColors(destructive: Boolean): ButtonColors = if (destructive) {
    ButtonDefaults.textButtonColors(
        contentColor = yumeDestructiveActionColors().contentColor,
    )
} else {
    ButtonDefaults.textButtonColors()
}
