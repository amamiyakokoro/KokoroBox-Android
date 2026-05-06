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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Md3ELoading(
    modifier: Modifier = Modifier,
) {
    ContainedLoadingIndicator(
        modifier = modifier.size(36.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Md3ELinearWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    stroke: Stroke = WavyProgressIndicatorDefaults.linearIndicatorStroke,
    trackStroke: Stroke = WavyProgressIndicatorDefaults.linearTrackStroke,
    gapSize: Dp = WavyProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
    stopSize: Dp = WavyProgressIndicatorDefaults.LinearTrackStopIndicatorSize,
    amplitude: (progress: Float) -> Float = WavyProgressIndicatorDefaults.indicatorAmplitude,
    wavelength: Dp = WavyProgressIndicatorDefaults.LinearDeterminateWavelength,
    waveSpeed: Dp = wavelength,
    animateProgress: Boolean = true,
) {
    val coercedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = coercedProgress,
        animationSpec = WavyProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Md3ELinearWavyProgressIndicatorProgress",
    )
    LinearWavyProgressIndicator(
        progress = { if (animateProgress) animatedProgress else coercedProgress },
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = trackColor,
        stroke = stroke,
        trackStroke = trackStroke,
        gapSize = gapSize,
        stopSize = stopSize,
        amplitude = amplitude,
        wavelength = wavelength,
        waveSpeed = waveSpeed,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Md3EIndeterminateLinearWavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    stroke: Stroke = WavyProgressIndicatorDefaults.linearIndicatorStroke,
    trackStroke: Stroke = WavyProgressIndicatorDefaults.linearTrackStroke,
    gapSize: Dp = WavyProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
    amplitude: Float = 1f,
    wavelength: Dp = WavyProgressIndicatorDefaults.LinearIndeterminateWavelength,
    waveSpeed: Dp = wavelength,
) {
    LinearWavyProgressIndicator(
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = trackColor,
        stroke = stroke,
        trackStroke = trackStroke,
        gapSize = gapSize,
        amplitude = amplitude,
        wavelength = wavelength,
        waveSpeed = waveSpeed,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Md3ECircularWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    stroke: Stroke = WavyProgressIndicatorDefaults.circularIndicatorStroke,
    trackStroke: Stroke = WavyProgressIndicatorDefaults.circularTrackStroke,
    gapSize: Dp = WavyProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
    amplitude: (progress: Float) -> Float = WavyProgressIndicatorDefaults.indicatorAmplitude,
    wavelength: Dp = WavyProgressIndicatorDefaults.CircularWavelength,
    waveSpeed: Dp = wavelength,
    animateProgress: Boolean = true,
) {
    val coercedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = coercedProgress,
        animationSpec = WavyProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Md3ECircularWavyProgressIndicatorProgress",
    )
    CircularWavyProgressIndicator(
        progress = { if (animateProgress) animatedProgress else coercedProgress },
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        stroke = stroke,
        trackStroke = trackStroke,
        gapSize = gapSize,
        amplitude = amplitude,
        wavelength = wavelength,
        waveSpeed = waveSpeed,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Md3EIndeterminateCircularWavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    stroke: Stroke = WavyProgressIndicatorDefaults.circularIndicatorStroke,
    trackStroke: Stroke = WavyProgressIndicatorDefaults.circularTrackStroke,
    gapSize: Dp = WavyProgressIndicatorDefaults.CircularIndicatorTrackGapSize,
    amplitude: Float = 1f,
    wavelength: Dp = WavyProgressIndicatorDefaults.CircularWavelength,
    waveSpeed: Dp = wavelength,
) {
    CircularWavyProgressIndicator(
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        stroke = stroke,
        trackStroke = trackStroke,
        gapSize = gapSize,
        amplitude = amplitude,
        wavelength = wavelength,
        waveSpeed = waveSpeed,
    )
}
