/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */

package com.amamiyakokoro.box.core.util

import kotlinx.coroutines.*

/** Batches writes without restarting the deadline when more data arrives. */
class ScheduledFlush(
    private val scope: CoroutineScope,
    private val intervalMillis: Long,
    private val flush: () -> Unit,
) {
    init { require(intervalMillis > 0L) }

    private val lock = Any()
    private var pending: Job? = null
    private var revision = 0L

    fun request() = synchronized(lock) {
        revision++
        if (pending?.isActive == true) return@synchronized
        pending = scope.launch {
            val ownJob = currentCoroutineContext().job
            while (isActive) {
                delay(intervalMillis)
                val writingRevision = synchronized(lock) { revision }
                flush()
                synchronized(lock) {
                    if (pending !== ownJob) return@launch
                    if (revision == writingRevision) {
                        pending = null
                        return@launch
                    }
                }
            }
        }
    }

    fun cancelPending() = synchronized(lock) {
        pending?.cancel()
        pending = null
    }
}
