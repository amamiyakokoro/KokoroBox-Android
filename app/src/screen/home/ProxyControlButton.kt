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


package com.github.yumelira.yumebox.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import com.github.yumelira.yumebox.common.AppConstants
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.AppMotion
import com.github.yumelira.yumebox.presentation.theme.UiDp
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.launch

@Composable
fun ProxyControlButton(
    isRunning: Boolean,
    isEnabled: Boolean,
    hasEnabledProfile: Boolean,
    hasProfiles: Boolean,
    profilesLoaded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val cornerRadius = AppConstants.UI.BUTTON_CORNER_RADIUS
    val buttonWidthFraction = 0.3f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(UiDp.dp16)
    ) {
        if (profilesLoaded) {
            if (!hasProfiles) {
                HintText(MLang.Home.Control.HintAddProfile)
            } else if (!hasEnabledProfile) {
                HintText(MLang.Home.Control.HintEnableProfile)
            }
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    scaleAnim.animateTo(
                        targetValue = 0.90f,
                        animationSpec = AppMotion.pressDown
                    )
                    scaleAnim.animateTo(
                        targetValue = 1.02f,
                        animationSpec = AppMotion.pressReturn
                    )
                    scaleAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = AppMotion.pressSettle
                    )
                }
                onClick()
            },
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth(buttonWidthFraction)
                .scale(scaleAnim.value)
                .heightIn(min = UiDp.dp36)
                .shadow(
                    elevation = UiDp.dp1,
                    shape = RoundedCornerShape(cornerRadius),
                    clip = false
                )
                .border(
                    width = UiDp.dp0_2,
                    color = MaterialTheme.colorScheme.outline,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)
                ),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(cornerRadius),
        ) {
            AnimatedContent(
                targetState = isRunning,
                transitionSpec = {
                    val enterTransition = slideInVertically(
                        initialOffsetY = { it / 5 },
                        animationSpec = tween(AppMotion.DURATION_INSTANT + 40, easing = AppMotion.EnterEasing)
                    ) + fadeIn(
                        animationSpec = tween(AppMotion.DURATION_INSTANT + 40, easing = AppMotion.EnterEasing)
                    ) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = AppMotion.iconTransition
                    )

                    val exitTransition = slideOutVertically(
                        targetOffsetY = { -it / 5 },
                        animationSpec = tween(AppMotion.DURATION_INSTANT + 20, easing = AppMotion.ExitEasing)
                    ) + fadeOut(
                        animationSpec = tween(AppMotion.DURATION_INSTANT + 20, easing = AppMotion.ExitEasing)
                    ) + scaleOut(
                        targetScale = 0.8f,
                        animationSpec = AppMotion.iconTransition
                    )

                    enterTransition.togetherWith(exitTransition)
                },
                label = "IconTransition"
            ) { running ->
                Icon(
                    imageVector = if (running) AppMd3Icons.Shell.StopProxy else AppMd3Icons.Shell.StartProxy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
