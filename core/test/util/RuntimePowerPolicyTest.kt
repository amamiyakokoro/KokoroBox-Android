package com.amamiyakokoro.box.core.util

import org.junit.Assert.*
import org.junit.Test

class RuntimePowerPolicyTest {
    private val visible = RuntimePowerState(appVisible = true)

    @Test fun visibleHomeRefreshesImmediately() {
        val timer = RuntimePowerPolicy.trafficTimer(visible, true)
        assertEquals(1_000L, timer.intervalMillis)
        assertEquals(0L, timer.initialDelayMillis)
    }
    @Test fun backgroundIgnoresStaleLiveDemand() {
        assertEquals(30_000L, RuntimePowerPolicy.trafficTimer(visible.copy(appVisible = false), true).intervalMillis)
        assertNull(RuntimePowerPolicy.proxyGroupTimer(visible.copy(appVisible = false)))
    }
    @Test fun screenOffAndDozeOverrideLiveDemand() {
        assertEquals(60_000L, RuntimePowerPolicy.trafficTimer(visible.copy(screenOn = false), true).intervalMillis)
        assertEquals(120_000L, RuntimePowerPolicy.trafficTimer(visible.copy(deviceIdle = true), true).intervalMillis)
        assertNull(RuntimePowerPolicy.proxyGroupTimer(visible.copy(screenOn = false)))
    }
    @Test fun batterySaverKeepsVisibleDataResponsive() {
        val saver = visible.copy(powerSaveMode = true)
        assertEquals(2_000L, RuntimePowerPolicy.trafficTimer(saver, true).intervalMillis)
        assertEquals(3_000L, RuntimePowerPolicy.proxyGroupTimer(saver)!!.intervalMillis)
        assertFalse(RuntimePowerPolicy.animateDecorations(saver))
    }
    @Test fun attributionAccuracyUsesExistingIntervals() {
        assertEquals(30_000L, RuntimePowerPolicy.statisticsTimer(visible.copy(appVisible = false)).intervalMillis)
        assertEquals(120_000L, RuntimePowerPolicy.statisticsTimer(visible.copy(screenOn = false)).intervalMillis)
    }
    @Test fun notificationsUseDeviceStateWithoutUIVisibility() {
        assertEquals(10_000L, RuntimePowerPolicy.notificationTimer(RuntimePowerState()).intervalMillis)
        assertEquals(60_000L, RuntimePowerPolicy.notificationTimer(visible.copy(screenOn = false)).intervalMillis)
        assertEquals(120_000L, RuntimePowerPolicy.notificationTimer(visible.copy(deviceIdle = true)).intervalMillis)
    }
    @Test fun connectionPageStopsWithoutVisibleUI() {
        assertNull(RuntimePowerPolicy.connectionsTimer(visible.copy(appVisible = false)))
        assertEquals(2_000L, RuntimePowerPolicy.connectionsTimer(visible.copy(powerSaveMode = true))!!.intervalMillis)
        assertEquals(0L, RuntimePowerPolicy.connectionsTimer(visible)!!.initialDelayMillis)
    }
    @Test fun rootRecoveryIsFastEvenInDoze() {
        val idle = visible.copy(screenOn = false, deviceIdle = true)
        assertEquals(2_000L, RuntimePowerPolicy.rootStatusTimer(idle, true).intervalMillis)
        assertEquals(120_000L, RuntimePowerPolicy.rootStatusTimer(idle, false).intervalMillis)
    }
}
