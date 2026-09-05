package com.github.yumelira.yumebox.data.controller

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Enforces limits while reading, including providers that do not report a file size. */
internal class SizeLimitedInputStream(
    private val source: InputStream,
    private val limit: Long,
    private val description: String,
) : InputStream() {
    private var consumed = 0L

    override fun read(): Int = source.read().also {
        if (it != -1) record(1)
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        // Read at most one byte past the limit to distinguish exact-size input from overflow.
        val count = source.read(buffer, offset, minOf(length.toLong(), limit - consumed + 1).toInt())
        if (count > 0) record(count)
        return count
    }

    private fun record(count: Int) {
        consumed += count
        if (consumed > limit) throw IOException("$description exceeds the ${limit / (1024 * 1024)} MiB limit")
    }

    override fun close() = source.close()
}

internal class SizeLimitedOutputStream(
    private val target: OutputStream,
    private val limit: Long,
) : OutputStream() {
    private var written = 0L

    override fun write(value: Int) {
        reserve(1)
        target.write(value)
    }

    override fun write(buffer: ByteArray, offset: Int, length: Int) {
        reserve(length)
        target.write(buffer, offset, length)
    }

    private fun reserve(count: Int) {
        if (count > limit - written) throw IOException("Backup exceeds the ${limit / (1024 * 1024)} MiB limit")
        written += count
    }

    override fun flush() = target.flush()
    override fun close() = target.close()
}
