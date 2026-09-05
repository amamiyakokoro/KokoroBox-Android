package com.github.yumelira.yumebox.data.integration.update

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

data class ReleaseVersion(val major: Long, val minor: Long, val patch: Long) : Comparable<ReleaseVersion> {
    override fun compareTo(other: ReleaseVersion): Int =
        compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch })

    companion object {
        fun parse(value: String): ReleaseVersion? {
            val parts = Regex("v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)")
                .matchEntire(value)?.groupValues?.drop(1)?.map { it.toLongOrNull() ?: return null }
                ?: return null
            return ReleaseVersion(parts[0], parts[1], parts[2])
        }
    }
}

sealed interface ReleaseCheck {
    data class Published(
        val tag: String,
        val version: ReleaseVersion,
        val notes: String,
        val releaseUrl: String,
        val apkUrl: String?,
    ) : ReleaseCheck
    enum class Failure : ReleaseCheck { NoRelease, RateLimited, Network, InvalidResponse }
}

/** Public metadata only: deliberately separate from authenticated Kokoro API clients. */
class GitHubReleaseClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .followRedirects(false)
        .build(),
    private val nowMillis: () -> Long = { System.nanoTime() / 1_000_000 },
) {
    private val mutex = Mutex()
    private var cached: ReleaseCheck? = null
    private var cacheUntil = 0L

    suspend fun check(): ReleaseCheck = mutex.withLock {
        cached?.takeIf { nowMillis() < cacheUntil }?.let { return@withLock it }
        val (result, cooldown) = fetch()
        cached = result
        cacheUntil = nowMillis() + cooldown
        result
    }

    private suspend fun fetch(): Pair<ReleaseCheck, Long> = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder().url(API_URL)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "KokoroBox-Android-UpdateCheck")
            .build()
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resume(ReleaseCheck.Failure.Network to 5_000L)
            }

            override fun onResponse(call: Call, response: Response) {
                var cooldown = 60_000L
                val result = response.use {
                    try {
                        when {
                            it.code == 404 -> ReleaseCheck.Failure.NoRelease
                            it.code == 429 || it.code == 403 -> {
                                val retrySeconds = it.header("Retry-After")?.toLongOrNull()
                                    ?: it.header("X-RateLimit-Reset")?.toLongOrNull()?.let { reset ->
                                        reset - System.currentTimeMillis() / 1000
                                    } ?: 60L
                                cooldown = retrySeconds.coerceIn(60, 86_400) * 1000
                                ReleaseCheck.Failure.RateLimited
                            }
                            !it.isSuccessful -> ReleaseCheck.Failure.Network
                            else -> {
                                val source = it.body.source()
                                source.request(MAX_RESPONSE_BYTES + 1)
                                if (source.buffer.size > MAX_RESPONSE_BYTES) {
                                    ReleaseCheck.Failure.InvalidResponse
                                } else parseRelease(it.body.string())
                            }
                        }
                    } catch (_: IOException) {
                        ReleaseCheck.Failure.Network
                    } catch (_: IllegalArgumentException) {
                        ReleaseCheck.Failure.InvalidResponse
                    }
                }
                continuation.resume(result to cooldown)
            }
        })
    }

    companion object {
        const val REPOSITORY_URL = "https://github.com/amamiyakokoro/KokoroBox-Android"
        const val API_URL = "https://api.github.com/repos/amamiyakokoro/KokoroBox-Android/releases/latest"
        private const val MAX_RESPONSE_BYTES = 1_048_576L

        internal fun parseRelease(body: String): ReleaseCheck = try {
            parseReleaseObject(body)
        } catch (_: IllegalArgumentException) {
            ReleaseCheck.Failure.InvalidResponse
        } catch (_: IllegalStateException) {
            ReleaseCheck.Failure.InvalidResponse
        }

        private fun parseReleaseObject(body: String): ReleaseCheck {
            val obj = Json.parseToJsonElement(body) as? JsonObject
                ?: return ReleaseCheck.Failure.InvalidResponse
            if (obj["draft"]?.jsonPrimitive?.boolean != false ||
                obj["prerelease"]?.jsonPrimitive?.boolean != false) return ReleaseCheck.Failure.NoRelease
            val tag = obj["tag_name"]?.jsonPrimitive?.content ?: return ReleaseCheck.Failure.InvalidResponse
            val version = ReleaseVersion.parse(tag) ?: return ReleaseCheck.Failure.InvalidResponse
            if (!tag.startsWith("v")) return ReleaseCheck.Failure.InvalidResponse
            // Build links from a validated tag; never open arbitrary URLs supplied by metadata.
            val releaseUrl = "$REPOSITORY_URL/releases/tag/$tag"
            val filename = "KokoroBox-$tag-arm64-v8a-release.apk"
            val expectedApk = "$REPOSITORY_URL/releases/download/$tag/$filename"
            val assets = obj["assets"] as? JsonArray ?: return ReleaseCheck.Failure.InvalidResponse
            val apk = assets.filterIsInstance<JsonObject>().singleOrNull {
                it["name"]?.jsonPrimitive?.content == filename &&
                    it["state"]?.jsonPrimitive?.content == "uploaded" &&
                    (it["size"]?.jsonPrimitive?.long ?: 0) > 0 &&
                    it["browser_download_url"]?.jsonPrimitive?.content == expectedApk
            }
            return ReleaseCheck.Published(tag, version,
                obj["body"]?.jsonPrimitive?.contentOrNull?.take(12_000).orEmpty(),
                releaseUrl, expectedApk.takeIf { apk != null })
        }
    }
}
