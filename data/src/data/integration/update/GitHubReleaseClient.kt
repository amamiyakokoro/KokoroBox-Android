package com.github.yumelira.yumebox.data.integration.update

import com.github.yumelira.yumebox.data.model.AppUpdateChannel
import com.github.yumelira.yumebox.data.gateway.SharedOkHttpClient
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
            val parts = Regex("v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)(?:-nightly)?")
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
        /** Present for nightly builds so builds sharing a version name remain ordered. */
        val versionCode: Int? = null,
    ) : ReleaseCheck
    enum class Failure : ReleaseCheck { NoRelease, RateLimited, Network, InvalidResponse }
}

/** Public metadata only: deliberately separate from authenticated Kokoro API clients. */
class GitHubReleaseClient(
    private val client: OkHttpClient = SharedOkHttpClient.newBuilder()
        .callTimeout(20, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .followRedirects(false)
        .build(),
    private val nowMillis: () -> Long = { System.nanoTime() / 1_000_000 },
) {
    private val mutex = Mutex()
    private data class CachedRelease(val result: ReleaseCheck, val cacheUntil: Long)

    private val cache = mutableMapOf<AppUpdateChannel, CachedRelease>()

    suspend fun check(channel: AppUpdateChannel = AppUpdateChannel.Stable): ReleaseCheck = mutex.withLock {
        cache[channel]?.takeIf { nowMillis() < it.cacheUntil }?.let { return@withLock it.result }
        val (result, cooldown) = fetch(channel)
        cache[channel] = CachedRelease(result, nowMillis() + cooldown)
        result
    }

    private suspend fun fetch(channel: AppUpdateChannel): Pair<ReleaseCheck, Long> = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder().url(channel.apiUrl)
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
                                } else parseRelease(it.body.string(), channel)
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
        const val NIGHTLY_API_URL = "https://api.github.com/repos/amamiyakokoro/KokoroBox-Android/releases/tags/nightly"
        private const val MAX_RESPONSE_BYTES = 1_048_576L

        internal fun parseRelease(
            body: String,
            channel: AppUpdateChannel = AppUpdateChannel.Stable,
        ): ReleaseCheck = try {
            parseReleaseObject(body, channel)
        } catch (_: IllegalArgumentException) {
            ReleaseCheck.Failure.InvalidResponse
        } catch (_: IllegalStateException) {
            ReleaseCheck.Failure.InvalidResponse
        }

        private fun parseReleaseObject(body: String, channel: AppUpdateChannel): ReleaseCheck {
            val obj = Json.parseToJsonElement(body) as? JsonObject
                ?: return ReleaseCheck.Failure.InvalidResponse
            if (obj["draft"]?.jsonPrimitive?.boolean != false ||
                obj["prerelease"]?.jsonPrimitive?.boolean != channel.isPrerelease) {
                return ReleaseCheck.Failure.NoRelease
            }
            val tag = obj["tag_name"]?.jsonPrimitive?.content ?: return ReleaseCheck.Failure.InvalidResponse
            val assets = obj["assets"] as? JsonArray ?: return ReleaseCheck.Failure.InvalidResponse
            return when (channel) {
                AppUpdateChannel.Stable -> parseStableRelease(tag, obj, assets)
                AppUpdateChannel.Nightly -> parseNightlyRelease(tag, obj, assets)
            }
        }

        private fun parseStableRelease(
            tag: String,
            obj: JsonObject,
            assets: JsonArray,
        ): ReleaseCheck {
            val version = ReleaseVersion.parse(tag) ?: return ReleaseCheck.Failure.InvalidResponse
            if (!tag.startsWith("v")) return ReleaseCheck.Failure.InvalidResponse
            return publishedRelease(tag, version, obj, assets, "KokoroBox-$tag-arm64-v8a-release.apk")
        }

        private fun parseNightlyRelease(
            tag: String,
            obj: JsonObject,
            assets: JsonArray,
        ): ReleaseCheck {
            if (tag != "nightly") return ReleaseCheck.Failure.InvalidResponse
            val filename = assets.filterIsInstance<JsonObject>()
                .mapNotNull { it["name"]?.jsonPrimitive?.content }
                .singleOrNull { NIGHTLY_APK_FILE.matchEntire(it) != null }
                ?: return ReleaseCheck.Failure.InvalidResponse
            val groups = NIGHTLY_APK_FILE.matchEntire(filename)?.groupValues ?: return ReleaseCheck.Failure.InvalidResponse
            val version = ReleaseVersion.parse(groups[1]) ?: return ReleaseCheck.Failure.InvalidResponse
            val versionCode = groups[2].toIntOrNull() ?: return ReleaseCheck.Failure.InvalidResponse
            return publishedRelease(tag, version, obj, assets, filename, versionCode)
        }

        private fun publishedRelease(
            tag: String,
            version: ReleaseVersion,
            obj: JsonObject,
            assets: JsonArray,
            filename: String,
            versionCode: Int? = null,
        ): ReleaseCheck {
            // Build links from validated release metadata; never open arbitrary URLs from it.
            val releaseUrl = "$REPOSITORY_URL/releases/tag/$tag"
            val expectedApk = "$REPOSITORY_URL/releases/download/$tag/$filename"
            val apk = assets.filterIsInstance<JsonObject>().singleOrNull {
                it["name"]?.jsonPrimitive?.content == filename &&
                    it["state"]?.jsonPrimitive?.content == "uploaded" &&
                    (it["size"]?.jsonPrimitive?.long ?: 0) > 0 &&
                    it["browser_download_url"]?.jsonPrimitive?.content == expectedApk
            }
            return ReleaseCheck.Published(
                tag = tag,
                version = version,
                notes = obj["body"]?.jsonPrimitive?.contentOrNull?.take(12_000).orEmpty(),
                releaseUrl = releaseUrl,
                apkUrl = expectedApk.takeIf { apk != null },
                versionCode = versionCode,
            )
        }

        private val NIGHTLY_APK_FILE = Regex(
            "KokoroBox-v((?:0|[1-9][0-9]*)\\.(?:0|[1-9][0-9]*)\\.(?:0|[1-9][0-9]*))-nightly-code([1-9][0-9]*)-arm64-v8a-release\\.apk",
        )
    }
}

private val AppUpdateChannel.apiUrl: String
    get() = when (this) {
        AppUpdateChannel.Stable -> GitHubReleaseClient.API_URL
        AppUpdateChannel.Nightly -> GitHubReleaseClient.NIGHTLY_API_URL
    }

private val AppUpdateChannel.isPrerelease: Boolean
    get() = this == AppUpdateChannel.Nightly

fun ReleaseCheck.Published.isNewerThan(currentVersionName: String, currentVersionCode: Int): Boolean {
    val currentVersion = ReleaseVersion.parse(currentVersionName) ?: return false
    return versionCode?.let { it > currentVersionCode } ?: (version > currentVersion)
}
