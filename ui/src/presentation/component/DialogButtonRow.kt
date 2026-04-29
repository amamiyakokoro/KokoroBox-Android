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

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DialogButtonRow
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DialogFilledButtonRow
import dev.oom_wg.purejoy.mlang.MLang

@Composable
fun DialogButtonRow(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    cancelText: String = MLang.Component.Button.Cancel,
    confirmText: String = MLang.Component.Button.Confirm,
    confirmEnabled: Boolean = true,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
    YumeMd3DialogButtonRow(
        onCancel = onCancel,
        onConfirm = onConfirm,
        modifier = modifier,
        cancelText = cancelText,
        confirmText = confirmText,
        confirmEnabled = confirmEnabled,
    )
}

@Composable
fun DialogFilledButtonRow(
    onSecondary: () -> Unit,
    onPrimary: () -> Unit,
    secondaryText: String = MLang.Component.Button.Clear,
    primaryText: String = MLang.Component.Button.Confirm,
    secondaryEnabled: Boolean = true,
    primaryEnabled: Boolean = true,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
    YumeMd3DialogFilledButtonRow(
        onSecondary = onSecondary,
        onPrimary = onPrimary,
        modifier = modifier,
        secondaryText = secondaryText,
        primaryText = primaryText,
        secondaryEnabled = secondaryEnabled,
        primaryEnabled = primaryEnabled,
    )
}
