package com.github.yumelira.yumebox.data.integration.speedtest

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections
import java.util.concurrent.atomic.AtomicLong

class CloudflareSpeedTestMathTest {
    @Test
    fun percentileInterpolatesTheMiddleValue() {
        assertEquals(25.0, CloudflareSpeedTestMath.percentile(listOf(10.0, 20.0, 30.0, 40.0), 0.5), 0.001)
    }

    @Test
    fun jitterUsesConsecutiveLatencySamples() {
        assertEquals(12.5, CloudflareSpeedTestMath.jitter(listOf(10.0, 20.0, 5.0)), 0.001)
    }

    @Test
    fun bandwidthUsesAggregateBytesAndElapsedTime() {
        assertEquals(80_000_000L, CloudflareSpeedTestMath.bitsPerSecond(10_000_000L, 1_000_000_000L))
    }

    @Test
    fun runUsesBoundedNativeCloudflareRequestsWithoutResultsUpload() = runBlocking {
        val requests = Collections.synchronizedList(mutableListOf<RecordedRequest>())
        val client = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
            val request = chain.request()
            val bodyBytes = request.body?.let { body ->
                Buffer().use { buffer ->
                    body.writeTo(buffer)
                    buffer.size
                }
            } ?: 0L
            requests += RecordedRequest(request.url.encodedPath, request.url.queryParameter("bytes"), bodyBytes)

            val body = when (request.url.encodedPath) {
                "/meta" -> "colo=TPE\n".toResponseBody("text/plain".toMediaType())
                "/__down" -> ByteArray(request.url.queryParameter("bytes")!!.toInt()).toResponseBody()
                "/__up" -> ByteArray(0).toResponseBody()
                else -> error("Unexpected speed test endpoint")
            }
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body)
                .build()
        }).build()
        val clock = AtomicLong()
        val result = CloudflareSpeedTestClient(client) { clock.addAndGet(1_000_000_000L) }.run {}

        assertEquals("TPE", result.cloudflareColo)
        assertEquals(14, requests.count { it.path == "/__down" })
        assertEquals(
            32_000_000L,
            requests.filter { it.path == "/__down" }.sumOf { it.bytes?.toLong() ?: 0L },
        )
        assertEquals(4, requests.count { it.path == "/__up" })
        assertEquals(8_000_000L, requests.filter { it.path == "/__up" }.sumOf { it.bodyBytes })
        assertFalse(requests.any { it.path == "/__results" })
        assertTrue(result.downloadBitsPerSecond > 0)
        assertTrue(result.uploadBitsPerSecond > 0)
    }

    private data class RecordedRequest(val path: String, val bytes: String?, val bodyBytes: Long)
}
