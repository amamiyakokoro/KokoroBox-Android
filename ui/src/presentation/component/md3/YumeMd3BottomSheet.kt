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

import android.os.Build
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import com.github.yumelira.yumebox.presentation.theme.UiDp
import dev.oom_wg.purejoy.mlang.MLang
import top.yukonga.miuix.kmp.window.WindowBottomSheet

object YumeMd3BottomSheetDefaults {
    val insideMargin: DpSize = DpSize(UiDp.dp24, UiDp.dp16)

    @Composable
    fun safeInsideMargin(
        insideMargin: DpSize = this.insideMargin,
        applyWindowInsets: Boolean = true,
    ): DpSize {
        if (!applyWindowInsets) return insideMargin

        val navigationBarBottom = WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()
        val systemGestureBottom = WindowInsets.systemGestures
            .asPaddingValues()
            .calculateBottomPadding()
        return DpSize(
            width = insideMargin.width,
            height = insideMargin.height + maxOf(navigationBarBottom, systemGestureBottom),
        )
    }

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

@Composable
@Suppress("DEPRECATION")
fun YumeMd3BottomSheetNavigationBarEffect(backgroundColor: Color) {
    val view = LocalView.current
    val useDarkIcons = backgroundColor.luminance() > 0.5f

    SideEffect {
        val window = view.findDialogWindowProvider()?.window ?: return@SideEffect
        window.navigationBarColor = backgroundColor.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = useDarkIcons
    }
}

private fun View.findDialogWindowProvider(): DialogWindowProvider? {
    var current: Any? = this
    while (current != null) {
        if (current is DialogWindowProvider) return current
        current = (current as? View)?.parent
    }
    return null
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
            icon = AppMd3Icons.Action.Close,
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
            icon = AppMd3Icons.Action.Check,
            contentDescription = contentDescription,
            enabled = enabled,
            onClick = onClick,
        ),
    )
}

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
    cornerRadius: Dp = UiDp.dp32,
    sheetMaxWidth: Dp = UiDp.dp560,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = DpSize(UiDp.dp16, UiDp.dp0),
    insideMargin: DpSize = YumeMd3BottomSheetDefaults.insideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    dragHandleColor: Color = Color.Unspecified,
    allowDismiss: Boolean = true,
    enableNestedScroll: Boolean = false,
    renderInRootScaffold: Boolean = true,
    contentScrollEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
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
    val safeInsideMargin = YumeMd3BottomSheetDefaults.safeInsideMargin(
        insideMargin = insideMargin,
        applyWindowInsets = defaultWindowInsetsPadding,
    )

    val maxSheetHeight = LocalConfiguration.current.screenHeightDp.let { screenHeightDp ->
        (screenHeightDp * 0.75f).dp
    }

    WindowBottomSheet(
        show = show,
        modifier = modifier.heightIn(max = maxSheetHeight),
        title = title,
        startAction = startAction,
        endAction = endAction,
        backgroundColor = resolvedBackgroundColor,
        enableWindowDim = enableWindowDim,
        cornerRadius = cornerRadius,
        sheetMaxWidth = sheetMaxWidth,
        onDismissRequest = {
            if (allowDismiss) onDismissRequest()
        },
        onDismissFinished = onDismissFinished,
        outsideMargin = outsideMargin,
        insideMargin = safeInsideMargin,
        defaultWindowInsetsPadding = defaultWindowInsetsPadding,
        dragHandleColor = resolvedDragHandleColor,
        allowDismiss = allowDismiss,
        enableNestedScroll = enableNestedScroll,
    ) {
        YumeMd3BottomSheetNavigationBarEffect(resolvedBackgroundColor)
        Box(
            modifier = if (contentScrollEnabled) {
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .verticalScroll(rememberScrollState())
            } else {
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
            },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}
