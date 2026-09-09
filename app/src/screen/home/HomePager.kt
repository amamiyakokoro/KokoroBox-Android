/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */


package com.amamiyakokoro.box.screen.home

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amamiyakokoro.box.presentation.theme.UiDp
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import com.amamiyakokoro.box.common.AppConstants
import com.amamiyakokoro.box.common.util.toast
import com.amamiyakokoro.box.data.store.AppSettingsStore
import com.amamiyakokoro.box.domain.model.TrafficData
import com.amamiyakokoro.box.presentation.component.LocalNavigator
import com.amamiyakokoro.box.presentation.component.ScreenLazyColumn
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.AppTheme
import com.amamiyakokoro.box.presentation.theme.yumeDestructiveActionColors
import com.ramcosta.composedestinations.generated.destinations.TrafficStatisticsScreenDestination
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun HomePager(
    mainInnerPadding: PaddingValues,
    isActive: Boolean,
) {
    val homeViewModel = koinViewModel<HomeViewModel>()
    val appSettings: AppSettingsStore = koinInject()
    val navigator = LocalNavigator.current

    val controlState by homeViewModel.controlState.collectAsStateWithLifecycle()
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val trafficNow by homeViewModel.trafficNow.collectAsStateWithLifecycle()
    val profilesLoaded by homeViewModel.profilesLoaded.collectAsStateWithLifecycle()
    val ipMonitoringState by homeViewModel.ipMonitoringState.collectAsStateWithLifecycle()
    val recommendedProfile by homeViewModel.recommendedProfile.collectAsStateWithLifecycle()
    val hasEnabledProfile by homeViewModel.hasEnabledProfile.collectAsStateWithLifecycle(initialValue = false)
    val currentProfile by homeViewModel.currentProfile.collectAsStateWithLifecycle()
    val selectedServerName by homeViewModel.selectedServerName.collectAsStateWithLifecycle()
    val selectedServerPing by homeViewModel.selectedServerPing.collectAsStateWithLifecycle()
    val speedHistory by homeViewModel.speedHistory.collectAsStateWithLifecycle()
    val proxyMode by homeViewModel.proxyMode.collectAsStateWithLifecycle()
    val tunnelMode by homeViewModel.tunnelMode.collectAsStateWithLifecycle()
    val useFabProxyControl by appSettings.homeUseFabProxyControl.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    LifecycleStartEffect(homeViewModel, isActive) {
        homeViewModel.setHomeScreenActive(isActive)
        onStopOrDispose { homeViewModel.setHomeScreenActive(false) }
    }

    LifecycleResumeEffect(homeViewModel, isActive) {
        if (isActive) {
            homeViewModel.reconcileRuntimeState()
            homeViewModel.refreshProxyMode()
        }
        onPauseOrDispose { }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            context.toast(it, Toast.LENGTH_LONG, copyable = true)
            homeViewModel.consumeError()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            context.toast(it, Toast.LENGTH_SHORT)
            homeViewModel.consumeMessage()
        }
    }


    val isRunning = controlState == HomeProxyControlState.Running
    val isProxyEnabled = profilesLoaded && hasEnabledProfile && recommendedProfile != null && controlState.canInteract
    val onProxyToggle: () -> Unit = {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
        if (isRunning) {
            coroutineScope.launch { homeViewModel.stopProxy() }
        } else {
            homeViewModel.startCurrentOrRecommendedProxy()
        }
    }
    val destructiveActionColors = yumeDestructiveActionColors()
    val spacing = AppTheme.spacing
    val componentSizes = AppTheme.sizes
    val fabContainerColor = when {
        !isProxyEnabled -> MaterialTheme.colorScheme.surfaceVariant
        isRunning -> destructiveActionColors.containerColor
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val fabContentColor = when {
        !isProxyEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
        isRunning -> destructiveActionColors.contentColor
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(title = MLang.Home.Title)
        },
        floatingActionButton = {
            if (useFabProxyControl) {
                FloatingActionButton(
                    modifier = Modifier.padding(
                        end = spacing.space20,
                        bottom = componentSizes.floatingActionButtonBottomInset,
                    ),
                    onClick = onProxyToggle,
                    containerColor = fabContainerColor,
                    contentColor = fabContentColor,
                ) {
                    Icon(
                        imageVector = if (isRunning) {
                            AppMd3Icons.Shell.StopProxy
                        } else {
                            AppMd3Icons.Shell.StartProxy
                        },
                        contentDescription = if (isRunning) {
                            MLang.Home.Control.Stop
                        } else {
                            MLang.Home.Control.Start
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, mainInnerPadding),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppConstants.UI.DEFAULT_HORIZONTAL_PADDING),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(AppConstants.UI.DEFAULT_VERTICAL_SPACING)
                ) {

                    TrafficDisplay(
                        trafficNow = if (isRunning) {
                            TrafficData.from(trafficNow)
                        } else {
                            TrafficData.ZERO
                        },
                        profileName = currentProfile?.name?.takeIf { isRunning },
                        tunnelMode = tunnelMode.takeIf { isRunning },
                        controlState = controlState,
                        proxyMode = proxyMode,
                        isEnabled = controlState.canInteract && !useFabProxyControl,
                        showIdleStatus = !useFabProxyControl,
                        onClick = onProxyToggle,
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(UiDp.dp16)) {
                        NodeInfoDisplay(
                            serverName = selectedServerName.takeIf { isRunning },
                            serverPing = selectedServerPing.takeIf { isRunning }
                        )
                        IpInfoDisplay(
                            state = ipMonitoringState
                        )
                    }

                    SpeedChart(
                        speedHistory = speedHistory,
                        isRunning = isRunning,
                        animateIdle = isActive,
                        onClick = {
                            navigator.navigate(TrafficStatisticsScreenDestination) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(UiDp.dp32)) }
        }
    }
}
