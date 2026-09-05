package com.github.yumelira.yumebox.data.integration.kokoro

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class MemorySingleFlightCacheTest {
    @Test
    fun cachesForTtlAndSupportsForcedRefreshAndInvalidation() = runBlocking {
        var time = 1_000L
        var loads = 0
        val cache = MemorySingleFlightCache<Int>(ttlMillis = 300_000L, now = { time })
        suspend fun load() = ++loads

        assertEquals(1, cache.get(loader = ::load))
        assertEquals(1, cache.get(loader = ::load))
        assertEquals(2, cache.get(forceRefresh = true, loader = ::load))

        cache.invalidate()
        assertEquals(3, cache.get(loader = ::load))

        time += 300_000L
        assertEquals(4, cache.get(loader = ::load))
    }

    @Test
    fun concurrentMissesShareOneLoad() = runBlocking {
        val loads = AtomicInteger()
        val cache = MemorySingleFlightCache<Int>(ttlMillis = 300_000L)

        val results = List(12) {
            async(Dispatchers.Default) {
                cache.get {
                    delay(25)
                    loads.incrementAndGet()
                }
            }
        }.awaitAll()

        assertEquals(List(12) { 1 }, results)
        assertEquals(1, loads.get())
    }
}
