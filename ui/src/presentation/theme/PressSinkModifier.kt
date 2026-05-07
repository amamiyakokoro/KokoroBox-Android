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

package com.github.yumelira.yumebox.presentation.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay

@Composable
fun Modifier.appPressSink(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    pressedTranslationY: Dp = UiDp.dp2,
    minimumVisibleMillis: Long = 110L,
    forcePressed: Boolean = false,
): Modifier {
    var interactionPressed by remember { mutableStateOf(false) }
    val active = enabled && (interactionPressed || forcePressed)

    LaunchedEffect(interactionSource, enabled, minimumVisibleMillis) {
        if (!enabled) {
            interactionPressed = false
            return@LaunchedEffect
        }

        var pressStartedAt = 0L
        interactionSource.interactions.collect { interaction: Interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    pressStartedAt = System.currentTimeMillis()
                    interactionPressed = true
                }

                is PressInteraction.Release,
                is PressInteraction.Cancel -> {
                    val elapsed = System.currentTimeMillis() - pressStartedAt
                    val remaining = minimumVisibleMillis - elapsed
                    if (remaining > 0) delay(remaining)
                    interactionPressed = false
                }
            }
        }
    }

    val pressSpec: FiniteAnimationSpec<Float> = if (active) {
        tween(durationMillis = 72, easing = AppMotion.EmphasizedDecelerate)
    } else {
        AppMotion.pressReturn
    }
    val density = LocalDensity.current
    val pressedTranslationYPx = with(density) { pressedTranslationY.toPx() }
    val translationYPx by animateFloatAsState(
        targetValue = if (active) pressedTranslationYPx else 0f,
        animationSpec = pressSpec,
        label = "appPressSinkTranslationY",
    )
    val scale by animateFloatAsState(
        targetValue = if (active) pressedScale else 1f,
        animationSpec = pressSpec,
        label = "appPressSinkScale",
    )

    return graphicsLayer {
        scaleX *= scale
        scaleY *= scale
        this.translationY += translationYPx
    }
}
