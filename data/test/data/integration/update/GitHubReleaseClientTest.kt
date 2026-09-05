package com.github.yumelira.yumebox.data.integration.update

import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class GitHubReleaseClientTest {
    private fun release(tag: String = "v0.5.7"): String = """
        {"tag_name":"$tag","draft":false,"prerelease":false,"body":"Release notes",
         "assets":[{"name":"KokoroBox-$tag-arm64-v8a-release.apk","state":"uploaded","size":1024,
         "browser_download_url":"${GitHubReleaseClient.REPOSITORY_URL}/releases/download/$tag/KokoroBox-$tag-arm64-v8a-release.apk"}]}
    """.trimIndent()

    @Test fun comparesNumericVersionsAndRejectsNonStableVersions() {
        assertTrue(ReleaseVersion.parse("v0.5.10")!! > ReleaseVersion.parse("0.5.9")!!)
        assertTrue(ReleaseVersion.parse("v1.0.0")!! > ReleaseVersion.parse("v0.99.99")!!)
        assertEquals(ReleaseVersion.parse("v0.5.6"), ReleaseVersion.parse("0.5.6"))
        listOf("v0.5.6-beta1", "dev", "v01.0.0", "v0.5.6/evil", "0.5", "999999999999999999999.0.0")
            .forEach { assertNull(ReleaseVersion.parse(it)) }
    }

    @Test fun parsesStableReleaseAndOnlyExactArm64Asset() {
        val result = GitHubReleaseClient.parseRelease(release()) as ReleaseCheck.Published
        assertEquals("v0.5.7", result.tag)
        assertEquals("Release notes", result.notes)
        assertTrue(result.apkUrl!!.endsWith("/KokoroBox-v0.5.7-arm64-v8a-release.apk"))
        assertEquals("${GitHubReleaseClient.REPOSITORY_URL}/releases/tag/v0.5.7", result.releaseUrl)
    }

    @Test fun missingWrongOrUnfinishedApkStillAllowsViewingRelease() {
        listOf(
            release().replace("arm64-v8a", "x86_64"),
            release().replace("https://github.com/", "https://evil.example/"),
            release().replace("\"uploaded\"", "\"new\""),
            release().replace("\"size\":1024", "\"size\":0"),
        ).forEach { assertNull((GitHubReleaseClient.parseRelease(it) as ReleaseCheck.Published).apkUrl) }
    }

    @Test fun rejectsDraftPrereleaseAndMalformedResponses() {
        listOf("draft", "prerelease").forEach { field ->
            assertEquals(ReleaseCheck.Failure.NoRelease,
                GitHubReleaseClient.parseRelease(release().replace("\"$field\":false", "\"$field\":true")))
        }
        listOf("<html>oops</html>", "[]", "null", release("v0.5.7-rc1"),
            release().replace("\"size\":1024", "\"size\":{}"),
            release().replace("\"body\":\"Release notes\"", "\"body\":[]"))
            .forEach { assertEquals(ReleaseCheck.Failure.InvalidResponse, GitHubReleaseClient.parseRelease(it)) }
        assertEquals("", (GitHubReleaseClient.parseRelease(
            release().replace("\"body\":\"Release notes\"", "\"body\":null")) as ReleaseCheck.Published).notes)
    }

    @Test fun requestIsAnonymousAndConcurrentChecksAreCoalesced() = runBlocking {
        val requests = AtomicInteger()
        var now = 0L
        val http = OkHttpClient.Builder().addInterceptor { chain ->
            requests.incrementAndGet()
            assertEquals(GitHubReleaseClient.API_URL, chain.request().url.toString())
            assertNull(chain.request().header("Authorization"))
            assertEquals("application/vnd.github+json", chain.request().header("Accept"))
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").body(release().toResponseBody("application/json".toMediaType())).build()
        }.build()
        val client = GitHubReleaseClient(http) { now }
        List(10) { async { client.check() } }.awaitAll().forEach { assertTrue(it is ReleaseCheck.Published) }
        assertEquals(1, requests.get())
        now = 60_001
        client.check()
        assertEquals(2, requests.get())
    }

    @Test fun reportsHttpFailuresAndHonorsRateLimitCooldown() = runBlocking {
        for ((status, expected) in listOf(404 to ReleaseCheck.Failure.NoRelease,
            429 to ReleaseCheck.Failure.RateLimited, 403 to ReleaseCheck.Failure.RateLimited,
            500 to ReleaseCheck.Failure.Network)) {
            val requests = AtomicInteger()
            var now = 0L
            val http = OkHttpClient.Builder().addInterceptor { chain ->
                requests.incrementAndGet()
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                    .code(status).message("Error").header("Retry-After", "120")
                    .body("{}".toResponseBody()).build()
            }.build()
            val client = GitHubReleaseClient(http) { now }
            assertEquals(expected, client.check())
            if (status == 429 || status == 403) {
                now = 90_000
                assertEquals(expected, client.check())
                assertEquals(1, requests.get())
                now = 120_001
                client.check()
                assertEquals(2, requests.get())
            }
        }
    }

    @Test fun rejectsOversizedMetadataAndHandlesNetworkFailure() = runBlocking {
        val oversized = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").body("x".repeat(1_048_577).toResponseBody()).build()
        }.build()
        assertEquals(ReleaseCheck.Failure.InvalidResponse, GitHubReleaseClient(oversized).check())
        val offline = OkHttpClient.Builder().addInterceptor { throw IOException("offline") }.build()
        assertEquals(ReleaseCheck.Failure.Network, GitHubReleaseClient(offline).check())
    }
}
