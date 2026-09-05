/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.data.integration.kokoro

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

/** Process-memory-only TTL cache that coalesces concurrent loads. */
class MemorySingleFlightCache<T>(
    private val ttlMillis: Long,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private data class Entry<T>(val value: T, val storedAt: Long)

    private val loadMutex = Mutex()
    private val generation = AtomicLong()

    @Volatile
    private var entry: Entry<T>? = null

    init {
        require(ttlMillis > 0L) { "Cache TTL must be positive" }
    }

    suspend fun get(
        forceRefresh: Boolean = false,
        loader: suspend () -> T,
    ): T {
        val observedEntry = entry
        if (!forceRefresh && observedEntry.isFresh()) return requireNotNull(observedEntry).value
        return loadMutex.withLock {
            val currentEntry = entry
            // A concurrent caller already completed the same miss or forced refresh.
            if (currentEntry !== observedEntry && currentEntry.isFresh()) {
                return@withLock requireNotNull(currentEntry).value
            }
            if (!forceRefresh && currentEntry.isFresh()) {
                return@withLock requireNotNull(currentEntry).value
            }

            val loadGeneration = generation.get()
            val value = loader()
            // An invalidation that occurs during I/O must win over the stale result.
            if (generation.get() == loadGeneration) {
                entry = Entry(value, now())
            }
            value
        }
    }

    fun invalidate() {
        generation.incrementAndGet()
        entry = null
    }

    private fun Entry<T>?.isFresh(): Boolean =
        this != null && now() - storedAt in 0 until ttlMillis
}
