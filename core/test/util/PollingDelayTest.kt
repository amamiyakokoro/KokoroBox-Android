package com.amamiyakokoro.box.core.util

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class PollingDelayTest {
    @Test fun repeatedOneShotWaitsDoNotJoinARecurringTimer() = runBlocking {
        withTimeout(1_000L) {
            repeat(3) { PollingTimers.awaitTick(PollingTimerSpec("one_shot", 60_000L, 1L)) }
        }
    }
    @Test fun cancellingOneShotPreventsDeferredWork() = runBlocking {
        var executed = false
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            PollingTimers.awaitTick(PollingTimerSpec("cancelled", 60_000L))
            executed = true
        }
        job.cancelAndJoin()
        assertFalse(executed)
    }
}
