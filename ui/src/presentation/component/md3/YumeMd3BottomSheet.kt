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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults as MaterialBottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.github.yumelira.yumebox.presentation.icon.Yume
import com.github.yumelira.yumebox.presentation.icon.yume.Check
import com.github.yumelira.yumebox.presentation.icon.yume.Close
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import com.github.yumelira.yumebox.presentation.theme.UiDp
import dev.oom_wg.purejoy.mlang.MLang

object YumeMd3BottomSheetDefaults {
    val insideMargin: DpSize = DpSize(UiDp.dp24, UiDp.dp16)

    @Composable
    fun backgroundColor(): Color = MaterialTheme.colorScheme.surface

    @Composable
    fun dragHandleColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun actionIconTint(enabled: Boolean): Color = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = AppTheme.opacity.disabled)
    }
}

data class YumeMd3BottomSheetAction(
    val icon: ImageVector,
    val contentDescription: String,
    val enabled: Boolean = true,
    val tint: Color = Color.Unspecified,
    val onClick: () -> Unit,
)

@Composable
fun YumeMd3BottomSheetIconAction(
    action: YumeMd3BottomSheetAction,
) {
    IconButton(
        enabled = action.enabled,
        onClick = action.onClick,
    ) {
        Icon(
            modifier = Modifier.alpha(if (action.enabled) 1f else AppTheme.opacity.medium),
            imageVector = action.icon,
            contentDescription = action.contentDescription,
            tint = if (action.tint == Color.Unspecified) {
                YumeMd3BottomSheetDefaults.actionIconTint(action.enabled)
            } else {
                action.tint
            },
        )
    }
}

@Composable
fun YumeMd3BottomSheetCloseAction(
    onClick: () -> Unit,
    enabled: Boolean = true,
    contentDescription: String = MLang.Component.Button.Cancel,
) {
    YumeMd3BottomSheetIconAction(
        action = YumeMd3BottomSheetAction(
            icon = Yume.Close,
            contentDescription = contentDescription,
            enabled = enabled,
            onClick = onClick,
        ),
    )
}

@Composable
fun YumeMd3BottomSheetConfirmAction(
    onClick: () -> Unit,
    enabled: Boolean = true,
    contentDescription: String = MLang.Component.Button.Confirm,
) {
    YumeMd3BottomSheetIconAction(
        action = YumeMd3BottomSheetAction(
            icon = Yume.Check,
            contentDescription = contentDescription,
            enabled = enabled,
            onClick = onClick,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YumeMd3ActionBottomSheet(
    show: Boolean,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    startAction: (@Composable (() -> Unit))? = null,
    endAction: (@Composable (() -> Unit))? = null,
    backgroundColor: Color = Color.Unspecified,
    enableWindowDim: Boolean = true,
    cornerRadius: Dp = UiDp.dp28,
    sheetMaxWidth: Dp = UiDp.dp560,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = DpSize(UiDp.dp16, UiDp.dp0),
    insideMargin: DpSize = YumeMd3BottomSheetDefaults.insideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    dragHandleColor: Color = Color.Unspecified,
    allowDismiss: Boolean = true,
    enableNestedScroll: Boolean = true,
    renderInRootScaffold: Boolean = true,
    content: @Composable () -> Unit,
) {
    val resolvedBackgroundColor = if (backgroundColor == Color.Unspecified) {
        YumeMd3BottomSheetDefaults.backgroundColor()
    } else {
        backgroundColor
    }
    val resolvedDragHandleColor = if (dragHandleColor == Color.Unspecified) {
        YumeMd3BottomSheetDefaults.dragHandleColor()
    } else {
        dragHandleColor
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { allowDismiss || it != SheetValue.Hidden },
    )
    var hasBeenShown by remember { mutableStateOf(false) }
    var keepSheetInComposition by remember { mutableStateOf(show) }

    LaunchedEffect(show) {
        if (show) {
            keepSheetInComposition = true
            hasBeenShown = true
        } else if (hasBeenShown) {
            sheetState.hide()
            onDismissFinished?.invoke()
            hasBeenShown = false
            keepSheetInComposition = false
        }
    }

    if (!keepSheetInComposition) return

    ModalBottomSheet(
        onDismissRequest = {
            if (allowDismiss) onDismissRequest()
        },
        modifier = modifier.padding(horizontal = outsideMargin.width),
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
        containerColor = resolvedBackgroundColor,
        scrimColor = if (enableWindowDim) {
            MaterialTheme.colorScheme.scrim.copy(alpha = AppTheme.opacity.disabled)
        } else {
            Color.Transparent
        },
        dragHandle = {
            MaterialBottomSheetDefaults.DragHandle(color = resolvedDragHandleColor)
        },
        contentWindowInsets = {
            if (defaultWindowInsetsPadding) MaterialBottomSheetDefaults.windowInsets else WindowInsets(0, 0, 0, 0)
        },
    ) {
        Column(
            modifier = Modifier.padding(
                start = insideMargin.width,
                end = insideMargin.width,
                bottom = insideMargin.height,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = UiDp.dp12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.width(UiDp.dp48),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    startAction?.invoke()
                }
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier.width(UiDp.dp48),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    endAction?.invoke()
                }
            }
            content()
            Spacer(modifier = Modifier.height(outsideMargin.height))
        }
    }
}
