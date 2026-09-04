/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.core

import android.os.SystemClock

/**
 * Coalesces concurrent and near-simultaneous JNI queries from UI, notifications, and telemetry.
 */
internal class TimedQueryCache<T>(
    private val clock: () -> Long = SystemClock::elapsedRealtime,
) {
    private var cachedValue: T? = null
    private var cachedAtMillis = Long.MIN_VALUE

    fun getOrQuery(maxAgeMillis: Long, query: () -> T): T {
        require(maxAgeMillis >= 0L)
        return synchronized(this) {
            val now = clock()
            cachedValue?.takeIf {
                cachedAtMillis != Long.MIN_VALUE && now - cachedAtMillis <= maxAgeMillis
            } ?: query().also { value ->
                cachedValue = value
                cachedAtMillis = clock()
            }
        }
    }

    fun clear() {
        synchronized(this) {
            cachedValue = null
            cachedAtMillis = Long.MIN_VALUE
        }
    }
}
