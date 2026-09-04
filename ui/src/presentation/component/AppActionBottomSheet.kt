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

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3ActionBottomSheet
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3BottomSheetAction
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3BottomSheetCloseAction
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3BottomSheetConfirmAction
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3BottomSheetDefaults
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3BottomSheetIconAction
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3BottomSheetNavigationBarEffect
import com.github.yumelira.yumebox.presentation.theme.UiDp
import dev.oom_wg.purejoy.mlang.MLang

object AppBottomSheetDefaults {
    val insideMargin: DpSize = YumeMd3BottomSheetDefaults.insideMargin

    @Composable
    fun backgroundColor(): Color = YumeMd3BottomSheetDefaults.backgroundColor()

    @Composable
    fun dragHandleColor(): Color = YumeMd3BottomSheetDefaults.dragHandleColor()

    @Composable
    fun actionIconTint(enabled: Boolean): Color = YumeMd3BottomSheetDefaults.actionIconTint(enabled)
}

@Composable
fun AppBottomSheetNavigationBarEffect(backgroundColor: Color) {
    YumeMd3BottomSheetNavigationBarEffect(backgroundColor)
}

data class AppBottomSheetAction(
    val icon: ImageVector,
    val contentDescription: String,
    val enabled: Boolean = true,
    val tint: Color = Color.Unspecified,
    val onClick: () -> Unit,
)

@Composable
fun AppBottomSheetIconAction(
    action: AppBottomSheetAction,
) {
    YumeMd3BottomSheetIconAction(
        action = YumeMd3BottomSheetAction(
            icon = action.icon,
            contentDescription = action.contentDescription,
            enabled = action.enabled,
            tint = action.tint,
            onClick = action.onClick,
        ),
    )
}

@Composable
fun AppBottomSheetCloseAction(
    onClick: () -> Unit,
    enabled: Boolean = true,
    contentDescription: String = MLang.Component.Button.Cancel,
) {
    YumeMd3BottomSheetCloseAction(
        onClick = onClick,
        enabled = enabled,
        contentDescription = contentDescription,
    )
}

@Composable
fun AppBottomSheetConfirmAction(
    onClick: () -> Unit,
    enabled: Boolean = true,
    contentDescription: String = MLang.Component.Button.Confirm,
) {
    YumeMd3BottomSheetConfirmAction(
        onClick = onClick,
        enabled = enabled,
        contentDescription = contentDescription,
    )
}

@Composable
fun AppActionBottomSheet(
    show: Boolean,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    startAction: (@Composable (() -> Unit))? = null,
    endAction: (@Composable (() -> Unit))? = null,
    backgroundColor: Color = Color.Unspecified,
    enableWindowDim: Boolean = true,
    cornerRadius: androidx.compose.ui.unit.Dp = UiDp.dp32,
    sheetMaxWidth: androidx.compose.ui.unit.Dp = UiDp.dp560,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = DpSize(UiDp.dp16, UiDp.dp0),
    insideMargin: DpSize = AppBottomSheetDefaults.insideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    dragHandleColor: Color = Color.Unspecified,
    allowDismiss: Boolean = true,
    enableNestedScroll: Boolean = false,
    renderInRootScaffold: Boolean = true,
    contentScrollEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    YumeMd3ActionBottomSheet(
        show = show,
        modifier = modifier,
        title = title,
        startAction = startAction,
        endAction = endAction,
        backgroundColor = backgroundColor,
        enableWindowDim = enableWindowDim,
        cornerRadius = cornerRadius,
        sheetMaxWidth = sheetMaxWidth,
        onDismissRequest = onDismissRequest,
        onDismissFinished = onDismissFinished,
        outsideMargin = outsideMargin,
        insideMargin = insideMargin,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        dragHandleColor = dragHandleColor,
        allowDismiss = allowDismiss,
        enableNestedScroll = enableNestedScroll,
        renderInRootScaffold = renderInRootScaffold,
        contentScrollEnabled = contentScrollEnabled,
        content = content,
    )
}
