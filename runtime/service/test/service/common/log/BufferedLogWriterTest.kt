package com.amamiyakokoro.box.service.common.log

import java.io.BufferedWriter
import java.io.StringWriter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test

class BufferedLogWriterTest {
    private class Sink : StringWriter() {
        var flushes = 0
        override fun flush() { flushes++ }
    }
    @Test fun smallEntriesAreFlushedAsABatch() {
        val sink = Sink()
        val writer = BufferedLogWriter(BufferedWriter(sink))
        repeat(10) { n -> writer.append { "entry-$n\n" } }
        assertEquals(0, sink.flushes)
        writer.flushPending()
        assertEquals(1, sink.flushes)
        writer.flushPending()
        assertEquals(1, sink.flushes)
        assertEquals(10, sink.toString().lines().count { it.isNotEmpty() })
        writer.close()
    }
    @Test fun sizeThresholdFlushesWithoutWaitingForTimer() {
        val sink = Sink()
        val writer = BufferedLogWriter(BufferedWriter(sink), flushThreshold = 8)
        writer.append { "12345678" }
        assertEquals("12345678", sink.toString())
        assertEquals(1, sink.flushes)
        writer.close()
    }
    @Test fun closingPersistsTheFinalEntriesAndRejectsLateCallbacks() {
        val sink = Sink()
        val writer = BufferedLogWriter(BufferedWriter(sink))
        writer.append { "最後一筆\n" }
        writer.close()
        assertEquals("最後一筆\n", sink.toString())
        assertFalse(writer.append { error("late callbacks must not format or write") })
        writer.close()
    }
    @Test fun concurrentProducersDoNotInterleaveEntries() {
        val sink = Sink()
        val writer = BufferedLogWriter(BufferedWriter(sink))
        val executor = Executors.newFixedThreadPool(4)
        try {
            val jobs = (0..3).map { producer -> executor.submit {
                repeat(100) { n -> writer.append { "$producer:$n\n" } }
            } }
            jobs.forEach { it.get(5, TimeUnit.SECONDS) }
            writer.close()
            val entries = sink.toString().lineSequence().filter { it.isNotBlank() }.toList()
            assertEquals(400, entries.size)
            assertEquals(400, entries.toSet().size)
            assertTrue(entries.all { it.matches(Regex("[0-3]:[0-9]+")) })
        } finally { executor.shutdownNow() }
    }
}
