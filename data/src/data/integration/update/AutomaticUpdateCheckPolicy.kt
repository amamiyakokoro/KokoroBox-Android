package com.github.yumelira.yumebox.data.integration.update

object AutomaticUpdateCheckPolicy {
    const val INTERVAL_MILLIS: Long = 24L * 60L * 60L * 1_000L

    fun isDue(
        lastCheckAtMillis: Long,
        nowMillis: Long,
    ): Boolean = lastCheckAtMillis <= 0L ||
        lastCheckAtMillis > nowMillis ||
        nowMillis - lastCheckAtMillis >= INTERVAL_MILLIS
}
