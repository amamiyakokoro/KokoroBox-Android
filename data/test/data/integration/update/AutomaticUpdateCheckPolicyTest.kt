package com.github.yumelira.yumebox.data.integration.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomaticUpdateCheckPolicyTest {
    @Test
    fun firstCheckIsDue() {
        assertTrue(AutomaticUpdateCheckPolicy.isDue(0L, 1_000L))
    }

    @Test
    fun recentCheckIsNotDue() {
        assertFalse(AutomaticUpdateCheckPolicy.isDue(1_000L, 1_000L + 60_000L))
    }

    @Test
    fun checkIsDueAtDailyBoundary() {
        assertTrue(
            AutomaticUpdateCheckPolicy.isDue(
                lastCheckAtMillis = 1_000L,
                nowMillis = 1_000L + AutomaticUpdateCheckPolicy.INTERVAL_MILLIS,
            ),
        )
    }

    @Test
    fun clockRollbackAllowsARecoveryCheck() {
        assertTrue(AutomaticUpdateCheckPolicy.isDue(2_000L, 1_000L))
    }
}
