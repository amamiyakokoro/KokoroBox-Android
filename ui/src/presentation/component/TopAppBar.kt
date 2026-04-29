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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBar as MaterialTopAppBar
import androidx.compose.material3.CenterAlignedTopAppBar as MaterialCenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.github.yumelira.yumebox.presentation.theme.UiDp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect

val LocalTopBarHazeState = compositionLocalOf<HazeState?> { null }
val LocalTopBarHazeStyle = compositionLocalOf<HazeStyle?> { null }

@OptIn(ExperimentalHazeApi::class)
private fun Modifier.topBarHazeEffect(
    state: HazeState?,
    style: HazeStyle?,
): Modifier {
    if (state == null || style == null) return this

    return hazeEffect(state) {
        this.style = style
        blurRadius = UiDp.dp20
        inputScale = HazeInputScale.Fixed(0.35f)
        noiseFactor = 0f
        forceInvalidateOnPreDraw = false
    }
}

@Composable
private fun defaultTopBarContainerColor(): Color {
    return MaterialTheme.colorScheme.surface
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    @Suppress("UNUSED_PARAMETER") scrollBehavior: Any? = null,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") titlePadding: Dp = UiDp.dp16,
    @Suppress("UNUSED_PARAMETER") navigationIconPadding: Dp = UiDp.dp16,
    @Suppress("UNUSED_PARAMETER") actionIconPadding: Dp = UiDp.dp16,
    containerColor: Color = Color.Unspecified,
    enableHaze: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val hazeState = LocalTopBarHazeState.current
    val hazeStyle = LocalTopBarHazeStyle.current
    val hazeEnabled = enableHaze && hazeState != null && hazeStyle != null
    val resolvedContainerColor = if (containerColor == Color.Unspecified) {
        defaultTopBarContainerColor()
    } else {
        containerColor
    }

    MaterialTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        modifier = if (hazeEnabled) modifier.topBarHazeEffect(hazeState, hazeStyle) else modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = resolvedContainerColor,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallTopBar(
    title: String,
    @Suppress("UNUSED_PARAMETER") scrollBehavior: Any? = null,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") titlePadding: Dp = UiDp.dp16,
    @Suppress("UNUSED_PARAMETER") navigationIconPadding: Dp = UiDp.dp16,
    @Suppress("UNUSED_PARAMETER") actionIconPadding: Dp = UiDp.dp16,
    containerColor: Color = Color.Unspecified,
    enableHaze: Boolean = false,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val hazeState = LocalTopBarHazeState.current
    val hazeStyle = LocalTopBarHazeStyle.current
    val hazeEnabled = enableHaze && hazeState != null && hazeStyle != null
    val resolvedContainerColor = if (containerColor == Color.Unspecified) {
        defaultTopBarContainerColor()
    } else {
        containerColor
    }

    MaterialCenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        modifier = if (hazeEnabled) modifier.topBarHazeEffect(hazeState, hazeStyle) else modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = resolvedContainerColor,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
