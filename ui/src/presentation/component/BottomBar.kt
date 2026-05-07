
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

import com.github.yumelira.yumebox.presentation.theme.UiDp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.AppMotion
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import com.kyant.shapes.Capsule
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.shapes.SmoothUnevenRoundedCornerShape
import kotlin.math.max
import kotlin.math.roundToInt

class MainPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()
        selectedPage = targetIndex
        isNavigating = true
        val layoutInfo = pagerState.layoutInfo
        val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
        val currentDistanceInPages =
            targetIndex - pagerState.currentPage - pagerState.currentPageOffsetFraction
        val scrollPixels = currentDistanceInPages * pageSize

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.animateScrollBy(
                    value = scrollPixels,
                    animationSpec = MainBottomBarDefaults.PagerAnimationSpec,
                )
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
fun rememberMainPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): MainPagerState {
    return remember(pagerState, coroutineScope) {
        MainPagerState(pagerState, coroutineScope)
    }
}

val LocalPagerState = compositionLocalOf<PagerState> { error("LocalPagerState is not provided") }
val LocalMainPagerState = compositionLocalOf<MainPagerState> { error("LocalMainPagerState is not provided") }
val LocalHandlePageChange = compositionLocalOf<(Int) -> Unit> { error("LocalHandlePageChange is not provided") }
val LocalNavigator = compositionLocalOf<DestinationsNavigator> { error("LocalNavigator is not provided") }
val LocalBottomBarUseLegacyStyle = compositionLocalOf { false }

object MainBottomBarDefaults {
    val CornerRadius = UiDp.dp24
    val Shape = SmoothUnevenRoundedCornerShape(
        topStart = CornerRadius,
        topEnd = CornerRadius,
    )
    val BorderWidth = UiDp.dp0_26
    val OutlineHorizontalInset = UiDp.dp0
    val ItemHeight = UiDp.dp64
    val IconSize = UiDp.dp24
    val LabelFontSize = 12.sp
    val IconLabelSpacing = UiDp.dp4
    val HorizontalPadding = UiDp.dp32
    val TopPadding = UiDp.dp8
    val FloatingBottomPadding = UiDp.dp12
    val EnterOffset = UiDp.dp68
    val ExitOffset = UiDp.dp84
    val ModernReservedHeight = UiDp.dp80
    val LegacyReservedHeight = UiDp.dp68
    val PagerAnimationSpec: AnimationSpec<Float> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = Int.VisibilityThreshold.toFloat(),
        )
}

@Composable
fun rememberMainPagerFlingBehavior(
    pagerState: PagerState,
): TargetedFlingBehavior {
    return PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = MainBottomBarDefaults.PagerAnimationSpec,
    )
}

@Composable
fun rememberBottomBarReservedHeight(
    useLegacyStyle: Boolean = LocalBottomBarUseLegacyStyle.current,
): Dp {
    val density = LocalDensity.current
    val systemBottomInset = with(density) {
        max(
            WindowInsets.navigationBars.getBottom(this),
            WindowInsets.systemGestures.getBottom(this),
        ).toDp()
    }
    return remember(systemBottomInset, useLegacyStyle) {
        if (useLegacyStyle) {
            MainBottomBarDefaults.LegacyReservedHeight + systemBottomInset
        } else {
            MainBottomBarDefaults.ModernReservedHeight
        }
    }
}

@Composable
fun BottomBarContent(
    isVisible: Boolean = true,
    useLegacyStyle: Boolean = false,
) {
    if (useLegacyStyle) {
        LegacyBottomBarContent(isVisible = isVisible)
    } else {
        ModernBottomBarContent(isVisible = isVisible)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernBottomBarContent(
    isVisible: Boolean = true,
) {
    val bottomBarScrollBehavior = LocalBottomBarScrollBehavior.current
    val mainPagerState = LocalMainPagerState.current
    val page by remember(mainPagerState) {
        derivedStateOf { mainPagerState.selectedPage }
    }
    val bottomBarVisible = isVisible && (bottomBarScrollBehavior?.isBottomBarVisible ?: true)

    val handlePageChange = LocalHandlePageChange.current
    val hapticFeedback = LocalHapticFeedback.current
    val onItemClick: (Int) -> Unit = { index ->
        hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
        if (index != mainPagerState.selectedPage) {
            handlePageChange(index)
        }
    }
    val indicatorColor = MaterialTheme.colorScheme.primaryContainer

    AnimatedVisibility(
        visible = bottomBarVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 240,
                easing = AppMotion.EmphasizedDecelerate,
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 240,
                easing = AppMotion.EmphasizedDecelerate,
            ),
            initialOffsetY = { fullHeight -> (fullHeight * 0.92f).toInt() },
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 180,
                easing = AppMotion.EmphasizedAccelerate,
            )
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = 220,
                easing = AppMotion.EmphasizedAccelerate,
            ),
            targetOffsetY = { fullHeight -> (fullHeight * 1.08f).toInt() },
        ),
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = UiDp.dp3,
        ) {
            CompositionLocalProvider(LocalRippleConfiguration provides null) {
                BottomBarDestination.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = page == index,
                        onClick = { onItemClick(index) },
                        icon = {
                            MaterialIcon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { MaterialText(destination.label) },
                        enabled = bottomBarVisible,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = indicatorColor,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun LegacyBottomBarContent(
    isVisible: Boolean = true,
) {
    val bottomBarScrollBehavior = LocalBottomBarScrollBehavior.current
    val mainPagerState = LocalMainPagerState.current
    val pagerState = mainPagerState.pagerState
    val page by remember(mainPagerState) {
        derivedStateOf { mainPagerState.selectedPage }
    }
    val indicatorProgress by remember(pagerState) {
        derivedStateOf {
            (
                pagerState.currentPage.toFloat() + pagerState.currentPageOffsetFraction
                ).coerceIn(0f, (BottomBarDestination.entries.size - 1).toFloat())
        }
    }
    val bottomBarVisible = isVisible && (bottomBarScrollBehavior?.isBottomBarVisible ?: true)
    val density = LocalDensity.current
    val enterOffsetPx = remember(density) { with(density) { MainBottomBarDefaults.EnterOffset.toPx() } }
    val exitOffsetPx = remember(density) { with(density) { MainBottomBarDefaults.ExitOffset.toPx() } }
    val animatedTranslationY = remember { Animatable(if (bottomBarVisible) 0f else exitOffsetPx) }
    val animatedScale by animateFloatAsState(
        targetValue = if (bottomBarVisible) 1f else 0.98f,
        animationSpec = tween(
            durationMillis = 240,
            easing = if (bottomBarVisible) {
                AppMotion.EmphasizedDecelerate
            } else {
                AppMotion.EmphasizedAccelerate
            },
        ),
        label = "legacy_bottom_bar_scale",
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = if (bottomBarVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 180,
            easing = if (bottomBarVisible) {
                AppMotion.EmphasizedDecelerate
            } else {
                AppMotion.EmphasizedAccelerate
            },
        ),
        label = "legacy_bottom_bar_alpha",
    )

    LaunchedEffect(bottomBarVisible, enterOffsetPx, exitOffsetPx) {
        if (bottomBarVisible) {
            animatedTranslationY.snapTo(enterOffsetPx)
            animatedTranslationY.animateTo(
                targetValue = 0f,
                animationSpec = AppMotion.pressReturn,
            )
        } else {
            animatedTranslationY.animateTo(
                targetValue = exitOffsetPx,
                animationSpec = tween(
                    durationMillis = 220,
                    easing = AppMotion.EmphasizedAccelerate,
                ),
            )
        }
    }

    val handlePageChange = LocalHandlePageChange.current
    val hapticFeedback = LocalHapticFeedback.current
    val onItemClick: (Int) -> Unit = { index ->
        hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
        if (index != mainPagerState.selectedPage) {
            handlePageChange(index)
        }
    }

    val bottomSafeInset = with(density) {
        val navigationBottom = WindowInsets.navigationBars.getBottom(this)
        val gestureBottom = WindowInsets.systemGestures.getBottom(this)
        max(navigationBottom, gestureBottom).toDp()
    }
    val selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
    val selectedTextColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    val indicatorContainerColor = MaterialTheme.colorScheme.primaryContainer

    LegacyBottomNavigationBar(
        selectedIndex = page,
        indicatorProgress = indicatorProgress,
        tabsCount = BottomBarDestination.entries.size,
        containerColor = containerColor,
        indicatorContainerColor = indicatorContainerColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MainBottomBarDefaults.HorizontalPadding,
                end = MainBottomBarDefaults.HorizontalPadding,
                top = MainBottomBarDefaults.TopPadding,
                bottom = bottomSafeInset + MainBottomBarDefaults.FloatingBottomPadding,
            )
            .graphicsLayer {
                alpha = animatedAlpha
                scaleX = animatedScale
                scaleY = animatedScale
                translationY = animatedTranslationY.value
                transformOrigin = TransformOrigin(0.5f, 1f)
            },
    ) {
        BottomBarDestination.entries.forEachIndexed { index, destination ->
            val itemIconColor = if (page == index) selectedIconColor else unselectedColor
            val itemTextColor = if (page == index) selectedTextColor else unselectedColor
            LegacyBottomNavigationTabItem(
                enabled = bottomBarVisible,
                onClick = { onItemClick(index) },
            ) {
                Box(
                    modifier = Modifier.size(UiDp.dp20),
                    contentAlignment = Alignment.Center,
                ) {
                    MaterialIcon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        tint = itemIconColor,
                    )
                }
                BasicText(
                    text = destination.label,
                    style = TextStyle(
                        color = itemTextColor,
                        fontSize = 11.sp,
                        fontWeight = if (page == index) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

@Composable
private fun LegacyBottomNavigationBar(
    selectedIndex: Int,
    indicatorProgress: Float,
    tabsCount: Int,
    containerColor: Color,
    indicatorContainerColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val opacity = AppTheme.opacity
    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val surfaceWidthPx = remember { mutableIntStateOf(0) }
    val safeSelectedIndex = selectedIndex.coerceIn(0, tabsCount - 1)
    val safeIndicatorProgress = indicatorProgress.coerceIn(0f, (tabsCount - 1).toFloat())
    val contentInsetPx = with(density) { (UiDp.dp4 * 2).toPx() }
    val innerWidthPx = (surfaceWidthPx.intValue - contentInsetPx).coerceAtLeast(0f)
    val tabWidthPx = if (tabsCount > 0) innerWidthPx / tabsCount else 0f
    val indicatorOffsetPx = if (isLtr) {
        safeIndicatorProgress * tabWidthPx
    } else {
        innerWidthPx - (safeIndicatorProgress + 1f) * tabWidthPx
    }
    val indicatorScale = remember { Animatable(1f) }
    val borderShadowColor = Black.copy(alpha = opacity.surfaceSoft)

    LaunchedEffect(safeSelectedIndex) {
        launch {
            indicatorScale.animateTo(0.9f, tween(120, easing = FastOutSlowInEasing))
            indicatorScale.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { surfaceWidthPx.intValue = it.width }
            .graphicsLayer {
                shape = Capsule()
                clip = false
                shadowElevation = with(density) { UiDp.dp7.toPx() }
                ambientShadowColor = borderShadowColor
                spotShadowColor = borderShadowColor
            }
            .height(UiDp.dp56)
            .clip(Capsule())
            .background(containerColor, Capsule()),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (tabWidthPx > 0f) {
            LegacyBottomNavigationIndicator(
                modifier = Modifier
                    .padding(UiDp.dp4)
                    .align(Alignment.CenterStart),
                indicatorOffsetPx = indicatorOffsetPx,
                indicatorWidthPx = tabWidthPx,
                indicatorScale = indicatorScale.value,
                indicatorContainerColor = indicatorContainerColor,
            )
        }

        Row(
            modifier = Modifier
                .padding(UiDp.dp4)
                .height(UiDp.dp48)
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun LegacyBottomNavigationIndicator(
    modifier: Modifier = Modifier,
    indicatorOffsetPx: Float,
    indicatorWidthPx: Float,
    indicatorScale: Float,
    indicatorContainerColor: Color,
) {
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .offset { IntOffset(indicatorOffsetPx.roundToInt(), 0) }
            .width(with(density) { indicatorWidthPx.toDp() })
            .height(UiDp.dp48)
            .graphicsLayer {
                scaleX = indicatorScale
                scaleY = indicatorScale
            }
            .background(indicatorContainerColor, Capsule()),
    )
}

@Composable
private fun RowScope.LegacyBottomNavigationTabItem(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(Capsule())
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .fillMaxHeight()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(UiDp.dp2, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

enum class BottomBarDestination(
    val icon: ImageVector,
) {
    Home(AppMd3Icons.Shell.OpenHome),
    Proxy(AppMd3Icons.Shell.OpenProxy),
    Config(AppMd3Icons.Shell.OpenProfileConfig),
    Setting(AppMd3Icons.Shell.OpenSettings),
    ;

    val label: String
        get() = when (this) {
            Home -> MLang.Component.BottomBar.Home
            Proxy -> MLang.Component.BottomBar.Proxy
            Config -> MLang.Component.BottomBar.Config
            Setting -> MLang.Component.BottomBar.Setting
        }
}
