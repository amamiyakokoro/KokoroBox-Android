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

package com.amamiyakokoro.box.data.store

import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

/** Reads the recent valid entries once, then only complete appended lines. */
internal class IncrementalLogTail<T>(private val parse: (String) -> T?) {
    private var path: String? = null
    private var identity: Any? = null
    private var modified: FileTime? = null
    private var capacity = 0
    private var offset = 0L
    private var observedLength = -1L
    private val entries = ArrayDeque<T>()
    private var snapshot: List<T> = emptyList()

    @Synchronized
    fun read(file: File, maxEntries: Int): List<T> {
        if (maxEntries <= 0) return emptyList()
        val attributes = Files.readAttributes(file.toPath(), BasicFileAttributes::class.java)
        val size = attributes.size()
        val reset = path != file.absolutePath || identity != attributes.fileKey() || capacity != maxEntries ||
            size < offset || (size <= offset && modified != attributes.lastModifiedTime())
        if (!reset && size == observedLength && modified == attributes.lastModifiedTime()) return snapshot

        RandomAccessFile(file, "r").use { reader ->
            val limit = reader.length()
            if (reset) {
                path = file.absolutePath
                identity = attributes.fileKey()
                capacity = maxEntries
                entries.clear()
                snapshot = emptyList()
                offset = tailStart(reader, limit, maxEntries)
            }
            reader.seek(offset)
            var changed = reset
            while (reader.filePointer < limit) {
                val start = reader.filePointer
                val raw = reader.readLine() ?: break
                val end = reader.filePointer
                reader.seek(end - 1)
                val last = reader.read()
                reader.seek(end)
                if (end > limit || (last != 10 && last != 13)) {
                    offset = start
                    break
                }
                offset = end
                parse(String(raw.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8))?.let { entry ->
                    if (entries.size == maxEntries) entries.removeFirst()
                    entries.addLast(entry)
                    changed = true
                }
            }
            if (changed) snapshot = entries.toList()
            observedLength = limit
            modified = attributes.lastModifiedTime()
        }
        return snapshot
    }

    private fun tailStart(reader: RandomAccessFile, length: Long, maxEntries: Int): Long {
        if (length == 0L) return 0L
        reader.seek(length - 1)
        val last = reader.read()
        var skipPartial = last != 10 && last != 13
        var position = length
        var suffix = ByteArray(0)
        var validEntries = 0
        while (position > 0) {
            val start = (position - 32_768L).coerceAtLeast(0L)
            val chunk = ByteArray((position - start).toInt())
            reader.seek(start)
            reader.readFully(chunk)
            val bytes = chunk + suffix
            var lineEnd = bytes.size
            for (index in bytes.lastIndex downTo 0) {
                if (bytes[index] != 10.toByte()) continue
                if (skipPartial) {
                    skipPartial = false
                } else if (lineEnd > index + 1) {
                    val line = String(bytes, index + 1, lineEnd - index - 1, Charsets.UTF_8).removeSuffix("\r")
                    if (parse(line) != null && ++validEntries == maxEntries) return start + index + 1
                }
                lineEnd = index
            }
            suffix = bytes.copyOfRange(0, lineEnd)
            position = start
        }
        return 0L
    }
}
