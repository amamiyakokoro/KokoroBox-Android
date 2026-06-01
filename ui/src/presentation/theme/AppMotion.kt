/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
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

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Central motion vocabulary for YumeBox.
 *
 * Prefer these semantic helpers in UI code instead of directly reaching for
 * MaterialTheme.motionScheme, spring(), or tween() in feature components.
 */
object AppMotion {

    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val Legacy = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val StandardEasing = FastOutSlowInEasing
    val EnterEasing = LinearOutSlowInEasing
    val ExitEasing = FastOutLinearInEasing

    const val DURATION_INSTANT = 120
    const val DURATION_FAST = 280

    val pressDown: FiniteAnimationSpec<Float> = tween(DURATION_INSTANT, easing = EmphasizedAccelerate)
    val pressReturn: SpringSpec<Float> = spring(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessLow,
    )
    val pressSettle: SpringSpec<Float> = spring(
        dampingRatio = 1f,
        stiffness = 500f,
    )
    val iconTransition: FiniteAnimationSpec<Float> = tween(320, easing = Legacy)

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> fastSpatial(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.fastSpatialSpec()

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> defaultSpatial(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultSpatialSpec()

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> slowSpatial(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.slowSpatialSpec()

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> fastEffects(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.fastEffectsSpec()

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> defaultEffects(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultEffectsSpec()

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun <T> slowEffects(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.slowEffectsSpec()

    @Composable
    fun <T> press(): FiniteAnimationSpec<T> = fastSpatial()

    @Composable
    fun <T> indicator(): FiniteAnimationSpec<T> = defaultSpatial()

    @Composable
    fun <T> card(): FiniteAnimationSpec<T> = defaultSpatial()

    @Composable
    fun <T> color(): FiniteAnimationSpec<T> = defaultEffects()

    @Composable
    fun <T> nav(): FiniteAnimationSpec<T> = defaultSpatial()

    object Proxy {
        const val VisibilityDuration = 180
        const val VisibilityFadeDuration = 140
        const val VisibilityInitialScale = 0.8f
        const val VisibilityTargetScale = 0.8f

        const val FabDuration = VisibilityDuration
        const val FabFadeDuration = VisibilityFadeDuration

        const val SheetSlideInDuration = 340
        const val SheetSlideOutDuration = 300
        const val SheetFadeInDuration = 140
        const val SheetFadeOutDuration = 140

        const val RefreshIndicatorDuration = 200
        const val RefreshIndicatorFadeDuration = 150
    }
}
