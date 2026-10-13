package com.amamiyakokoro.box.data.store

import com.amamiyakokoro.box.data.model.TodayTrafficSummary
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class TodayTrafficSummaryTest {
    @Test fun dayRolloverDoesNotShowYesterdayTraffic() {
        val summary = TodayTrafficSummary(100L, 9876L)
        assertEquals(9876L, summary.totalForDay(100L))
        assertEquals(0L, summary.totalForDay(200L))
    }
    @Test fun corruptNegativeTotalIsClamped() {
        assertEquals(0L, TodayTrafficSummary(100L, -2L).totalForDay(100L))
    }
    @Test fun compactSummaryRoundTripsAndIgnoresFutureFields() {
        val json = Json { ignoreUnknownKeys = true }
        val encoded = json.encodeToString(TodayTrafficSummary.serializer(), TodayTrafficSummary(100L, 42L))
        assertEquals(42L, json.decodeFromString(TodayTrafficSummary.serializer(), encoded).totalForDay(100L))
        assertEquals(42L, json.decodeFromString(TodayTrafficSummary.serializer(), "{\"dateMillis\":100,\"totalBytes\":42,\"extra\":true}").totalForDay(100L))
    }
}
