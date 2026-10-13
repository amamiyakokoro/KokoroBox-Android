package com.amamiyakokoro.box.core.util

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class ScheduledFlushTest {
    @Test fun continuousChangesDoNotStarvePersistence() = runBlocking {
        val written = CompletableDeferred<Unit>()
        val scheduler = ScheduledFlush(this, 20L) { written.complete(Unit) }
        val producer = launch {
            repeat(100) { scheduler.request(); delay(2L) }
        }
        withTimeout(2_000L) { written.await() }
        assertTrue(producer.isActive)
        producer.cancelAndJoin()
        scheduler.cancelPending()
    }
    @Test fun aChangeDuringWriteSchedulesAnotherFlush() = runBlocking {
        val secondWrite = CompletableDeferred<Unit>()
        var writes = 0
        lateinit var scheduler: ScheduledFlush
        scheduler = ScheduledFlush(this, 1L) {
            writes++
            if (writes == 1) scheduler.request() else secondWrite.complete(Unit)
        }
        scheduler.request()
        withTimeout(1_000L) { secondWrite.await() }
        assertEquals(2, writes)
    }
    @Test fun clearCancelsThePendingWrite() = runBlocking {
        var writes = 0
        val scheduler = ScheduledFlush(this, 60_000L) { writes++ }
        scheduler.request()
        scheduler.cancelPending()
        assertEquals(0, writes)
    }
}
