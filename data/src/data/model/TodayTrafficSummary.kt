package com.amamiyakokoro.box.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TodayTrafficSummary(val dateMillis: Long, val totalBytes: Long) {
    fun totalForDay(day: Long): Long = if (dateMillis == day) totalBytes.coerceAtLeast(0L) else 0L

    companion object {
        const val STORAGE_KEY = "today_traffic_summary_v1"
    }
}
