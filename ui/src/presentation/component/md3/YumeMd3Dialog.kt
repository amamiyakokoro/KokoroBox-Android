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

import com.github.yumelira.yumebox.presentation.component.AppSnackbarSurface

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.yumelira.yumebox.presentation.theme.UiDp

object YumeMd3DialogDefaults {
    @Composable
    fun titleColor(): Color = MaterialTheme.colorScheme.onSurface

    @Composable
    fun summaryColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun backgroundColor(): Color = MaterialTheme.colorScheme.surface

    val outsideMargin: DpSize
        get() = DpSize(UiDp.dp24, UiDp.dp24)

    val insideMargin: DpSize
        get() = DpSize(UiDp.dp24, UiDp.dp20)
}

@Composable
fun YumeMd3Dialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    titleColor: Color = YumeMd3DialogDefaults.titleColor(),
    summary: String? = null,
    summaryColor: Color = YumeMd3DialogDefaults.summaryColor(),
    backgroundColor: Color = YumeMd3DialogDefaults.backgroundColor(),
    enableWindowDim: Boolean = true,
    onDismissFinished: (() -> Unit)? = null,
    outsideMargin: DpSize = YumeMd3DialogDefaults.outsideMargin,
    insideMargin: DpSize = YumeMd3DialogDefaults.insideMargin,
    defaultWindowInsetsPadding: Boolean = true,
    renderInRootScaffold: Boolean = false,
    content: @Composable () -> Unit,
) {
    var hasBeenShown by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            hasBeenShown = true
        } else if (hasBeenShown) {
            onDismissFinished?.invoke()
            hasBeenShown = false
        }
    }

    if (!show) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = defaultWindowInsetsPadding,
            dismissOnClickOutside = true,
        ),
    ) {
        AppSnackbarSurface(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest,
                )
                .padding(horizontal = outsideMargin.width, vertical = outsideMargin.height),
        ) {
            Surface(
                modifier = modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .widthIn(max = UiDp.dp560)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                shape = MaterialTheme.shapes.extraLarge,
                color = backgroundColor,
                tonalElevation = UiDp.dp6,
                shadowElevation = UiDp.dp6,
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = insideMargin.width,
                        vertical = insideMargin.height,
                    ),
                    verticalArrangement = Arrangement.spacedBy(UiDp.dp16),
                ) {
                    if (title.isNotBlank() || !summary.isNullOrBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(UiDp.dp8)) {
                            if (title.isNotBlank()) {
                                Text(
                                    text = title,
                                    color = titleColor,
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }
                            if (!summary.isNullOrBlank()) {
                                Text(
                                    text = summary,
                                    color = summaryColor,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                    content()
                }
            }
        }
    }
}

@Composable
fun YumeMd3DialogColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UiDp.dp16),
        content = content,
    )
}

@Composable
fun YumeMd3DialogMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
}
