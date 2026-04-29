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

package com.github.yumelira.yumebox.miuix

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur

typealias YumeMiuixLayerBackdrop = LayerBackdrop
typealias YumeMiuixBlendColorEntry = BlendColorEntry
typealias YumeMiuixBlurBlendMode = BlurBlendMode
typealias YumeMiuixBlurColors = BlurColors
typealias YumeMiuixBlurDefaults = BlurDefaults

@Composable
fun rememberYumeMiuixLayerBackdrop(): YumeMiuixLayerBackdrop = rememberLayerBackdrop()

fun Modifier.yumeMiuixLayerBackdrop(
    backdrop: YumeMiuixLayerBackdrop,
): Modifier = layerBackdrop(backdrop)

fun Modifier.yumeMiuixTextureBlur(
    backdrop: YumeMiuixLayerBackdrop,
    shape: Shape,
    blurRadius: Float,
    noiseCoefficient: Float,
    blurColors: YumeMiuixBlurColors,
    enabled: Boolean = true,
): Modifier = textureBlur(
    backdrop,
    shape,
    blurRadius,
    noiseCoefficient,
    blurColors,
    enabled = enabled,
)
