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

package com.amamiyakokoro.box.core.util

/** Device/UI state used only to schedule telemetry; it never suspends packet forwarding. */
data class RuntimePowerState(
    val screenOn: Boolean = true,
    val appVisible: Boolean = false,
    val deviceIdle: Boolean = false,
    val powerSaveMode: Boolean = false,
) {
    val uiVisible: Boolean get() = screenOn && appVisible && !deviceIdle
}

object RuntimePowerPolicy {
    private fun timer(name: String, interval: Long, immediate: Boolean = false) =
        PollingTimerSpec("power_$name", interval, if (immediate) 0L else interval)

    fun trafficTimer(state: RuntimePowerState, liveRequested: Boolean): PollingTimerSpec = when {
        state.deviceIdle -> timer("traffic_idle", 120_000L)
        !state.screenOn -> timer("traffic_screen_off", 60_000L)
        !state.appVisible -> timer("traffic_background", if (state.powerSaveMode) 60_000L else 30_000L)
        liveRequested -> timer("traffic_live", if (state.powerSaveMode) 2_000L else 1_000L, immediate = true)
        else -> timer("traffic_foreground", if (state.powerSaveMode) 10_000L else 3_000L, immediate = true)
    }

    // Keep the existing attribution sampling intervals: lowering them would miss more short flows.
    fun statisticsTimer(state: RuntimePowerState): PollingTimerSpec =
        if (state.screenOn && !state.deviceIdle) PollingTimerSpecs.TrafficStatsCollection
        else PollingTimerSpecs.TrafficStatsCollectionScreenOff

    fun notificationTimer(state: RuntimePowerState): PollingTimerSpec = when {
        state.deviceIdle -> timer("notification_idle", 120_000L)
        !state.screenOn -> PollingTimerSpecs.ForegroundNotificationRefreshScreenOff
        state.powerSaveMode -> timer("notification_power_save", 30_000L)
        else -> PollingTimerSpecs.ForegroundNotificationRefresh
    }

    fun proxyGroupTimer(state: RuntimePowerState): PollingTimerSpec? = when {
        !state.uiVisible -> null
        state.powerSaveMode -> timer("proxy_groups_power_save", 3_000L, immediate = true)
        else -> PollingTimerSpecs.RuntimeProxyGroupSyncFast
    }

    fun connectionsTimer(state: RuntimePowerState): PollingTimerSpec? = when {
        !state.uiVisible -> null
        state.powerSaveMode -> timer("connections_power_save", 2_000L, immediate = true)
        else -> PollingTimerSpecs.ConnectionsPolling
    }

    fun rootStatusTimer(state: RuntimePowerState, transitioning: Boolean): PollingTimerSpec = when {
        transitioning -> PollingTimerSpecs.RootTunStatusPolling
        state.deviceIdle -> timer("root_status_idle", 120_000L)
        !state.screenOn -> timer("root_status_screen_off", 60_000L)
        else -> timer("root_status_stable", if (state.powerSaveMode) 30_000L else 10_000L)
    }

    fun animateDecorations(state: RuntimePowerState): Boolean = state.uiVisible && !state.powerSaveMode
}
