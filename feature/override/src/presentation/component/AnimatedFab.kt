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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.yumelira.yumebox.presentation.theme.AppMotion
import com.github.yumelira.yumebox.presentation.theme.UiDp

@Stable
class OverrideFabController internal constructor() {
    var isHiddenByScroll by mutableStateOf(false)
        private set

    fun onScrollDirectionChanged(hidden: Boolean) {
        isHiddenByScroll = hidden
    }
}

@Composable
fun rememberOverrideFabController(): OverrideFabController {
    return remember {
        OverrideFabController()
    }
}

@Composable
fun OverrideAnimatedFab(
    controller: OverrideFabController,
    visible: Boolean,
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val fabVisibilityState = remember {
        MutableTransitionState(false)
    }
    val hiddenByScroll = controller.isHiddenByScroll
    val actualVisible = visible && !hiddenByScroll
    fabVisibilityState.targetState = actualVisible

    AnimatedVisibility(
        visibleState = fabVisibilityState,
        enter = slideInVertically(
            animationSpec = tween(
                durationMillis = AppMotion.Proxy.VisibilityDuration,
                easing = AppMotion.EmphasizedDecelerate,
            ),
            initialOffsetY = { it / 2 },
        ) + scaleIn(
            initialScale = AppMotion.Proxy.VisibilityInitialScale,
            animationSpec = tween(
                durationMillis = AppMotion.Proxy.VisibilityDuration,
                easing = LinearEasing,
            ),
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AppMotion.Proxy.VisibilityFadeDuration,
                easing = AppMotion.EnterEasing,
            ),
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = AppMotion.Proxy.VisibilityDuration,
                easing = AppMotion.EmphasizedAccelerate,
            ),
            targetOffsetY = { it / 2 },
        ) + scaleOut(
            targetScale = AppMotion.Proxy.VisibilityTargetScale,
            animationSpec = tween(
                durationMillis = AppMotion.Proxy.VisibilityDuration,
                easing = LinearEasing,
            ),
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = AppMotion.Proxy.VisibilityFadeDuration,
                easing = AppMotion.ExitEasing,
            ),
        ),
        label = "override_shared_fab_visibility",
    ) {
        FloatingActionButton(
            modifier = Modifier.padding(end = UiDp.dp20, bottom = UiDp.dp85),
            onClick = onClick,
        ) {
            AppIcon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
