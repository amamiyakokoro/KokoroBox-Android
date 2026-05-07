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

@Deprecated(
    message = "Use AppMotion semantic helpers for new UI motion.",
    replaceWith = ReplaceWith("AppMotion"),
)
object AnimationSpecs {

    val EmphasizedDecelerate = AppMotion.EmphasizedDecelerate
    val EmphasizedAccelerate = AppMotion.EmphasizedAccelerate
    val Legacy = AppMotion.Legacy
    val StandardEasing = AppMotion.StandardEasing
    val EnterEasing = AppMotion.EnterEasing
    val ExitEasing = AppMotion.ExitEasing

    const val DURATION_INSTANT = AppMotion.DURATION_INSTANT
    const val DURATION_FAST = AppMotion.DURATION_FAST

    val ButtonPress = AppMotion.pressDown
    val ButtonPressSpring = AppMotion.pressReturn
    val IconTransition = AppMotion.iconTransition

    object Proxy {

        const val VisibilityDuration = AppMotion.Proxy.VisibilityDuration
        const val VisibilityFadeDuration = AppMotion.Proxy.VisibilityFadeDuration
        const val VisibilityInitialScale = AppMotion.Proxy.VisibilityInitialScale
        const val VisibilityTargetScale = AppMotion.Proxy.VisibilityTargetScale

        const val FabDuration = AppMotion.Proxy.FabDuration
        const val FabFadeDuration = AppMotion.Proxy.FabFadeDuration

        const val SheetSlideInDuration = AppMotion.Proxy.SheetSlideInDuration
        const val SheetSlideOutDuration = AppMotion.Proxy.SheetSlideOutDuration
        const val SheetFadeInDuration = AppMotion.Proxy.SheetFadeInDuration
        const val SheetFadeOutDuration = AppMotion.Proxy.SheetFadeOutDuration

        const val RefreshIndicatorDuration = AppMotion.Proxy.RefreshIndicatorDuration
        const val RefreshIndicatorFadeDuration = AppMotion.Proxy.RefreshIndicatorFadeDuration
    }
}
