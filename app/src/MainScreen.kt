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

package com.github.yumelira.yumebox

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle


import com.github.yumelira.yumebox.presentation.theme.UiDp
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.presentation.component.*
import com.github.yumelira.yumebox.presentation.screen.ProxyPager
import com.github.yumelira.yumebox.screen.acg.AcgHomePage
import com.github.yumelira.yumebox.screen.acg.calculateHomeVisibility
import com.github.yumelira.yumebox.screen.home.HomeViewModel
import com.github.yumelira.yumebox.screen.home.HomePager
import com.github.yumelira.yumebox.screen.profiles.ProfilesPager
import com.github.yumelira.yumebox.screen.settings.AppSettingsViewModel
import com.github.yumelira.yumebox.screen.settings.SettingPager
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ProvidersScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.collect
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>
fun MainScreen(
    navigator: DestinationsNavigator,
    initialPage: Int = 0,
) {
    val initialMainPage = initialPage.coerceIn(0, 3)
    val pagerState = rememberPagerState(initialPage = initialMainPage, pageCount = { 4 })
    val mainPagerState = rememberMainPagerState(pagerState)
    val profilesListState = rememberRetainedLazyListState("main_profiles")
    val settingsListState = rememberRetainedLazyListState("main_settings")

    val appSettingsViewModel = koinViewModel<AppSettingsViewModel>()
    val homeViewModel = koinViewModel<HomeViewModel>()
    val lifecycleOwner = LocalLifecycleOwner.current
    val isProxyRunning by homeViewModel.isRunning.collectAsStateWithLifecycle()
    val bottomBarAutoHideEnabled by appSettingsViewModel.bottomBarAutoHide.state.collectAsStateWithLifecycle()
    val bottomBarUseLegacyStyle by appSettingsViewModel.bottomBarUseLegacyStyle.state.collectAsStateWithLifecycle()
    val acgMainUiEnabled by appSettingsViewModel.acgMainUiEnabled.state.collectAsStateWithLifecycle()
    val acgWallpaperUri by appSettingsViewModel.acgWallpaperUri.state.collectAsStateWithLifecycle()
    val acgWallpaperZoom by appSettingsViewModel.acgWallpaperZoom.state.collectAsStateWithLifecycle()
    val acgWallpaperBiasX by appSettingsViewModel.acgWallpaperBiasX.state.collectAsStateWithLifecycle()
    val acgWallpaperBiasY by appSettingsViewModel.acgWallpaperBiasY.state.collectAsStateWithLifecycle()
    val bottomBarScrollBehavior = rememberBottomBarScrollBehavior(autoHideEnabled = bottomBarAutoHideEnabled)
    val pagerFlingBehavior = rememberMainPagerFlingBehavior(mainPagerState.pagerState)
    var settledMainPage by remember { mutableIntStateOf(initialMainPage) }
    val homeVisibility by remember(mainPagerState) {
        derivedStateOf {
            calculateHomeVisibility(
                currentPage = mainPagerState.pagerState.currentPage,
                currentPageOffsetFraction = mainPagerState.pagerState.currentPageOffsetFraction,
            )
        }
    }
    val acgBottomBarVisible by remember(acgMainUiEnabled, settledMainPage, mainPagerState.selectedPage) {
        derivedStateOf {
            if (!acgMainUiEnabled) {
                true
            } else if (mainPagerState.selectedPage == 0) {
                false
            } else {
                settledMainPage != 0
            }
        }
    }
    LaunchedEffect(mainPagerState.pagerState.currentPage) {
        mainPagerState.syncPage()
    }

    LaunchedEffect(mainPagerState.pagerState.currentPage, mainPagerState.pagerState.isScrollInProgress) {
        if (!mainPagerState.pagerState.isScrollInProgress) {
            settledMainPage = mainPagerState.pagerState.currentPage
        }
    }

    LaunchedEffect(settledMainPage, acgMainUiEnabled, bottomBarScrollBehavior) {
        if (!acgMainUiEnabled || settledMainPage != 0) {
            bottomBarScrollBehavior.forceShowBottomBar()
        }
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        homeViewModel.onVpnPermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(homeViewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            homeViewModel.vpnPrepareIntent.collect { intent ->
                vpnPermissionLauncher.launch(intent)
            }
        }
    }

    val handlePageChange: (Int) -> Unit = remember(mainPagerState) {
        { targetPage -> mainPagerState.animateToPage(targetPage) }
    }

    BackHandler(enabled = mainPagerState.selectedPage != 0) {
        handlePageChange(0)
    }

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalPagerState provides mainPagerState.pagerState,
        LocalMainPagerState provides mainPagerState,
        LocalHandlePageChange provides handlePageChange,
        LocalBottomBarScrollBehavior provides bottomBarScrollBehavior,
        LocalBottomBarUseLegacyStyle provides bottomBarUseLegacyStyle,
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
        ) { innerPadding ->
            Box(Modifier.fillMaxSize()) {
                val layoutDirection = LocalLayoutDirection.current
                val visibleBottomBarReservedHeight = rememberBottomBarReservedHeight(
                    useLegacyStyle = bottomBarUseLegacyStyle,
                )
                val mainStartPadding = WindowInsets.systemBars.asPaddingValues().calculateStartPadding(layoutDirection)
                val mainEndPadding = WindowInsets.systemBars.asPaddingValues().calculateEndPadding(layoutDirection)
                fun mainInnerPaddingForPage(page: Int): PaddingValues {
                    val pageBottomBarReservedHeight = if (acgMainUiEnabled && page == 0) {
                        UiDp.dp0
                    } else {
                        visibleBottomBarReservedHeight
                    }
                    return PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding() + pageBottomBarReservedHeight,
                        start = mainStartPadding,
                        end = mainEndPadding,
                    )
                }
                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = mainPagerState.pagerState,
                    beyondViewportPageCount = 3,
                    flingBehavior = pagerFlingBehavior,
                    userScrollEnabled = true,
                    overscrollEffect = null,
                    pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
                        state = mainPagerState.pagerState,
                        orientation = Orientation.Horizontal,
                    ),
                ) { page ->
                    MainRootPageContent(
                        page = page,
                        mainInnerPadding = mainInnerPaddingForPage(page),
                        acgMainUiEnabled = acgMainUiEnabled,
                        acgWallpaperUri = acgWallpaperUri,
                        acgWallpaperZoom = acgWallpaperZoom,
                        acgWallpaperBiasX = acgWallpaperBiasX,
                        acgWallpaperBiasY = acgWallpaperBiasY,
                        navigator = navigator,
                        homePageProgress = homeVisibility,
                        selectedPage = settledMainPage,
                        isProxyRunning = isProxyRunning,
                        onProxyStartRequested = homeViewModel::startCurrentOrRecommendedProxy,
                        profilesListState = profilesListState,
                        settingsListState = settingsListState,
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    BottomBarContent(
                        isVisible = acgBottomBarVisible,
                        useLegacyStyle = bottomBarUseLegacyStyle,
                    )
                }
            }
        }
    }
}

@Composable
private fun MainRootPageContent(
    page: Int,
    mainInnerPadding: PaddingValues,
    acgMainUiEnabled: Boolean,
    acgWallpaperUri: String,
    acgWallpaperZoom: Float,
    acgWallpaperBiasX: Float,
    acgWallpaperBiasY: Float,
    navigator: DestinationsNavigator,
    homePageProgress: Float,
    selectedPage: Int,
    isProxyRunning: Boolean,
    onProxyStartRequested: () -> Unit,
    profilesListState: LazyListState,
    settingsListState: LazyListState,
) {
    when (page) {
        0 -> {
            if (acgMainUiEnabled) {
                AcgHomePage(
                    mainInnerPadding = mainInnerPadding,
                    wallpaperUri = acgWallpaperUri,
                    wallpaperZoom = acgWallpaperZoom,
                    wallpaperBiasX = acgWallpaperBiasX,
                    wallpaperBiasY = acgWallpaperBiasY,
                    isActive = selectedPage == 0,
                    pageProgress = if (selectedPage == 0 || homePageProgress < 0.999f) homePageProgress else 1f,
                )
            } else {
                HomePager(
                    mainInnerPadding = mainInnerPadding,
                    isActive = selectedPage == 0,
                )
            }
        }

        1 -> ProxyPager(
            mainInnerPadding = mainInnerPadding,
            onNavigateToProviders = {
                navigator.navigate(ProvidersScreenDestination) {
                    launchSingleTop = true
                }
            },
            isPageActive = selectedPage == 1,
            isProxyRunning = isProxyRunning,
            onProxyStartRequested = onProxyStartRequested,
        )

        2 -> ProfilesPager(
            mainInnerPadding = mainInnerPadding,
            lazyListState = profilesListState,
        )
        3 -> SettingPager(
            mainInnerPadding = mainInnerPadding,
            lazyListState = settingsListState,
        )
    }
}
