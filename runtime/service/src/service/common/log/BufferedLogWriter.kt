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

package com.amamiyakokoro.box.service.common.log

import java.io.BufferedWriter
import java.io.Closeable

/** All formatting and file writes share one lock; normal flushing is batched. */
internal class BufferedLogWriter(
    private val writer: BufferedWriter,
    private val flushThreshold: Int = 16_384,
) : Closeable {
    private var pendingCharacters = 0
    private var closed = false

    @Synchronized
    fun append(buildLine: () -> String): Boolean {
        if (closed) return false
        val line = buildLine()
        writer.write(line)
        pendingCharacters += line.length
        if (pendingCharacters >= flushThreshold) flushDirty()
        return true
    }

    @Synchronized
    fun flushPending() {
        if (!closed && pendingCharacters > 0) flushDirty()
    }

    private fun flushDirty() {
        writer.flush()
        pendingCharacters = 0
    }

    @Synchronized
    override fun close() {
        if (closed) return
        closed = true
        try {
            if (pendingCharacters > 0) flushDirty()
        } finally {
            writer.close()
        }
    }
}
