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

object ProfileUpdatePolicy {
    const val RETRY_INTERVAL_MILLIS = 60 * 60 * 1000L

    fun isDue(
        now: Long,
        interval: Long,
        lastAttemptAt: Long,
        lastAttemptFailed: Boolean,
        updatedAt: Long,
    ): Boolean {
        if (interval <= 0L) return false
        val baseline = if (lastAttemptAt > 0L) lastAttemptAt else updatedAt
        if (baseline <= 0L) return true
        val wait = if (lastAttemptFailed) RETRY_INTERVAL_MILLIS else interval
        // Recover if the wall clock was moved backwards.
        return now < baseline || now - baseline >= wait
    }
}
