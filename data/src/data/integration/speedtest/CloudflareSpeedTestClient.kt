/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.data.integration.speedtest

import com.github.yumelira.yumebox.data.gateway.SharedOkHttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/** A bounded, native implementation of Cloudflare's public edge speed test. */
class CloudflareSpeedTestClient(
    private val httpClient: OkHttpClient = SharedOkHttpClient.newBuilder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build(),
    private val nanoTime: () -> Long = System::nanoTime,
) {
    suspend fun run(onProgress: (CloudflareSpeedTestStage) -> Unit): CloudflareSpeedTestResult =
        withContext(Dispatchers.IO) {
            onProgress(CloudflareSpeedTestStage.Preparing)
            val colo = runCatching { requestColo() }.getOrElse { error ->
                if (error is CancellationException) throw error
                null
            }

            onProgress(CloudflareSpeedTestStage.Latency)
            val latencySamples = List(LATENCY_SAMPLE_COUNT) { measureLatency() }

            onProgress(CloudflareSpeedTestStage.Download)
            val downloadBitsPerSecond = measureAggregateBandwidth { downloadChunk(DOWNLOAD_BYTES_PER_STREAM) }

            onProgress(CloudflareSpeedTestStage.Upload)
            val uploadBitsPerSecond = measureAggregateBandwidth { uploadChunk(UPLOAD_BYTES_PER_STREAM) }

            CloudflareSpeedTestResult(
                cloudflareColo = colo,
                latencyMs = CloudflareSpeedTestMath.percentile(latencySamples, 0.5),
                jitterMs = CloudflareSpeedTestMath.jitter(latencySamples),
                downloadBitsPerSecond = downloadBitsPerSecond,
                uploadBitsPerSecond = uploadBitsPerSecond,
            )
        }

    private suspend fun requestColo(): String? {
        val request = requestBuilder(CLOUDFLARE_META_URL).build()
        return execute(request) { response ->
            response.body.string()
                .lineSequence()
                .firstOrNull { it.startsWith("colo=") }
                ?.substringAfter('=')
                ?.trim()
                ?.takeIf(String::isNotEmpty)
        }
    }

    private suspend fun measureLatency(): Double {
        val startedAt = nanoTime()
        val request = requestBuilder("$CLOUDFLARE_DOWNLOAD_URL?bytes=0&nonce=${startedAt}").build()
        execute(request) { }
        return (nanoTime() - startedAt) / NANOS_PER_MILLISECOND
    }

    private suspend fun measureAggregateBandwidth(transfer: suspend () -> Long): Long = coroutineScope {
        val startedAt = nanoTime()
        val transferredBytes = List(PARALLEL_STREAM_COUNT) {
            async { transfer() }
        }.awaitAll().sum()
        CloudflareSpeedTestMath.bitsPerSecond(transferredBytes, nanoTime() - startedAt)
    }

    private suspend fun downloadChunk(byteCount: Long): Long {
        val request = requestBuilder("$CLOUDFLARE_DOWNLOAD_URL?bytes=$byteCount&nonce=${nanoTime()}").build()
        return execute(request) { response ->
            response.body.byteStream().use { input ->
                val buffer = ByteArray(BUFFER_SIZE)
                var total = 0L
                while (total < byteCount) {
                    currentCoroutineContext().ensureActive()
                    val read = input.read(buffer, 0, minOf(buffer.size.toLong(), byteCount - total).toInt())
                    if (read < 0) break
                    total += read
                }
                total
            }
        }
    }

    private suspend fun uploadChunk(byteCount: Long): Long {
        val request = requestBuilder("$CLOUDFLARE_UPLOAD_URL?bytes=$byteCount&nonce=${nanoTime()}")
            .post(ZeroRequestBody(byteCount))
            .build()
        return execute(request) { byteCount }
    }

    private suspend fun <T> execute(request: Request, block: suspend (Response) -> T): T {
        val call = httpClient.newCall(request)
        val cancellationHandle = currentCoroutineContext().job.invokeOnCompletion { cause ->
            if (cause is CancellationException) call.cancel()
        }
        try {
            return call.execute().use { response ->
                if (!response.isSuccessful) throw IOException("Cloudflare speed test request failed")
                block(response)
            }
        } finally {
            cancellationHandle.dispose()
        }
    }

    private fun requestBuilder(url: String): Request.Builder = Request.Builder()
        .url(url)
        .header("User-Agent", USER_AGENT)
        .cacheControl(CacheControl.FORCE_NETWORK)

    private class ZeroRequestBody(private val byteCount: Long) : RequestBody() {
        override fun contentType() = BINARY_CONTENT_TYPE

        override fun contentLength() = byteCount

        override fun writeTo(sink: BufferedSink) {
            val buffer = ByteArray(BUFFER_SIZE)
            var remaining = byteCount
            while (remaining > 0) {
                val count = minOf(remaining, buffer.size.toLong()).toInt()
                sink.write(buffer, 0, count)
                remaining -= count
            }
        }
    }

    private companion object {
        const val CLOUDFLARE_META_URL = "https://speed.cloudflare.com/meta"
        const val CLOUDFLARE_DOWNLOAD_URL = "https://speed.cloudflare.com/__down"
        const val CLOUDFLARE_UPLOAD_URL = "https://speed.cloudflare.com/__up"
        const val USER_AGENT = "KokoroBox-SpeedTest"

        const val CONNECT_TIMEOUT_SECONDS = 10L
        const val READ_TIMEOUT_SECONDS = 30L
        const val CALL_TIMEOUT_SECONDS = 45L
        const val LATENCY_SAMPLE_COUNT = 10
        const val PARALLEL_STREAM_COUNT = 4
        const val DOWNLOAD_BYTES_PER_STREAM = 8_000_000L
        const val UPLOAD_BYTES_PER_STREAM = 2_000_000L
        const val BUFFER_SIZE = 32 * 1024
        const val NANOS_PER_MILLISECOND = 1_000_000.0

        val BINARY_CONTENT_TYPE = "application/octet-stream".toMediaType()
    }
}

enum class CloudflareSpeedTestStage {
    Preparing,
    Latency,
    Download,
    Upload,
}

data class CloudflareSpeedTestResult(
    val cloudflareColo: String?,
    val latencyMs: Double,
    val jitterMs: Double,
    val downloadBitsPerSecond: Long,
    val uploadBitsPerSecond: Long,
)

internal object CloudflareSpeedTestMath {
    fun percentile(samples: List<Double>, percentile: Double): Double {
        require(samples.isNotEmpty())
        val sorted = samples.sorted()
        val position = (sorted.lastIndex * percentile.coerceIn(0.0, 1.0))
        val lower = position.toInt()
        val upper = (lower + 1).coerceAtMost(sorted.lastIndex)
        return sorted[lower] + (sorted[upper] - sorted[lower]) * (position - lower)
    }

    fun jitter(samples: List<Double>): Double {
        if (samples.size < 2) return 0.0
        return samples.zipWithNext { first, second -> abs(second - first) }.average()
    }

    fun bitsPerSecond(bytes: Long, durationNanos: Long): Long {
        if (bytes <= 0 || durationNanos <= 0) return 0L
        return ((bytes.toDouble() * 8 * NANOS_PER_SECOND) / durationNanos).toLong()
    }

    private const val NANOS_PER_SECOND = 1_000_000_000.0
}
