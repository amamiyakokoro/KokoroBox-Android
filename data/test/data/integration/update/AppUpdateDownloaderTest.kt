package com.github.yumelira.yumebox.data.integration.update

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest

class AppUpdateDownloaderTest {
    @Test
    fun downloadsOnlyWhenTheExpectedChecksumMatches() = runBlocking {
        val bytes = "verified update".encodeToByteArray()
        val release = release(bytes.size.toLong())
        val checksum = sha256(bytes)
        val client = responseClient(release, bytes, "$checksum  ${release.apkName}\n")
        val cacheDir = Files.createTempDirectory("kokorobox-update-test").toFile()

        try {
            val download = AppUpdateDownloader(cacheDir, client).download(release)
            assertArrayEquals(bytes, download.file.readBytes())
            assertTrue(download.sha256.equals(checksum, ignoreCase = true))
            assertFalse(File(cacheDir, "update.apk.part").exists())
        } finally {
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun removesPartialFilesWhenChecksumDoesNotMatch() = runBlocking {
        val bytes = "untrusted update".encodeToByteArray()
        val release = release(bytes.size.toLong())
        val client = responseClient(release, bytes, "${"0".repeat(64)}  ${release.apkName}\n")
        val cacheDir = Files.createTempDirectory("kokorobox-update-test").toFile()

        try {
            val error = runCatching { AppUpdateDownloader(cacheDir, client).download(release) }.exceptionOrNull()
            assertTrue(error is AppUpdateDownloadException)
            assertFalse(File(cacheDir, "update.apk").exists())
            assertFalse(File(cacheDir, "update.apk.part").exists())
        } finally {
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun reusesAnUnchangedVerifiedCachedApkWithoutMakingNetworkRequests() = runBlocking {
        val bytes = "cached verified update".encodeToByteArray()
        val release = release(bytes.size.toLong())
        val checksum = sha256(bytes)
        val cacheDir = Files.createTempDirectory("kokorobox-update-test").toFile()

        try {
            AppUpdateDownloader(cacheDir, responseClient(release, bytes, "$checksum  ${release.apkName}\n"))
                .download(release)
            var requests = 0
            val offlineClient = OkHttpClient.Builder().addInterceptor { chain ->
                requests += 1
                error("Cached APK should not request ${chain.request().url}")
            }.build()

            val cached = AppUpdateDownloader(cacheDir, offlineClient).download(release)

            assertArrayEquals(bytes, cached.file.readBytes())
            assertEquals(0, requests)
        } finally {
            cacheDir.deleteRecursively()
        }
    }

    @Test
    fun redownloadsWhenTheCachedApkChecksumNoLongerMatches() = runBlocking {
        val bytes = "original verified update".encodeToByteArray()
        val release = release(bytes.size.toLong())
        val checksum = sha256(bytes)
        val cacheDir = Files.createTempDirectory("kokorobox-update-test").toFile()

        try {
            AppUpdateDownloader(cacheDir, responseClient(release, bytes, "$checksum  ${release.apkName}\n"))
                .download(release)
            val tampered = bytes.copyOf().also { it[0] = 'x'.code.toByte() }
            File(cacheDir, "update.apk").writeBytes(tampered)
            var requests = 0
            val client = responseClient(release, bytes, "$checksum  ${release.apkName}\n", onRequest = { requests += 1 })

            val redownloaded = AppUpdateDownloader(cacheDir, client).download(release)

            assertArrayEquals(bytes, redownloaded.file.readBytes())
            assertEquals(2, requests)
        } finally {
            cacheDir.deleteRecursively()
        }
    }

    private fun release(size: Long): ReleaseCheck.Published {
        val tag = "v1.2.3"
        val apkName = "KokoroBox-$tag-arm64-v8a-release.apk"
        return ReleaseCheck.Published(
            tag = tag,
            version = ReleaseVersion(1, 2, 3),
            notes = "",
            releaseUrl = "${GitHubReleaseClient.REPOSITORY_URL}/releases/tag/$tag",
            apkName = apkName,
            apkUrl = "${GitHubReleaseClient.REPOSITORY_URL}/releases/download/$tag/$apkName",
            apkSizeBytes = size,
            checksumUrl = "${GitHubReleaseClient.REPOSITORY_URL}/releases/download/$tag/SHA256SUMS",
        )
    }

    private fun responseClient(
        release: ReleaseCheck.Published,
        apk: ByteArray,
        checksum: String,
        onRequest: () -> Unit = {},
    ): OkHttpClient = OkHttpClient.Builder().addInterceptor { chain ->
        onRequest()
        val body = when (chain.request().url.toString()) {
            release.apkUrl -> apk.toResponseBody()
            release.checksumUrl -> checksum.toResponseBody()
            else -> error("Unexpected update request: ${chain.request().url}")
        }
        Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body)
            .build()
    }.build()

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it.toInt() and 0xFF) }
}
