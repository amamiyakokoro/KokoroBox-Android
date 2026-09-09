package com.github.yumelira.yumebox.data.integration.update

import android.content.Context
import com.github.yumelira.yumebox.data.gateway.SharedOkHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

data class DownloadedUpdateApk(
    val file: File,
    val sha256: String,
)

class AppUpdateDownloadException(message: String, cause: Throwable? = null) : IOException(message, cause)

/** Downloads only a release APK already validated by [GitHubReleaseClient]. */
class AppUpdateDownloader(
    private val cacheDirectory: File,
    private val client: OkHttpClient = SharedOkHttpClient.newBuilder()
        // GitHub's release download endpoint redirects to its asset CDN.
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build(),
) {
    constructor(context: Context, client: OkHttpClient = defaultClient()) : this(
        cacheDirectory = File(context.cacheDir, CACHE_DIRECTORY_NAME),
        client = client,
    )

    suspend fun download(
        release: ReleaseCheck.Published,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> },
    ): DownloadedUpdateApk = withContext(Dispatchers.IO) {
        val apkUrl = requireNotNull(release.apkUrl) { "Release does not provide an APK" }
        val apkName = requireNotNull(release.apkName) { "Release does not provide an APK name" }
        val apkSize = requireNotNull(release.apkSizeBytes) { "Release does not provide an APK size" }
        val checksumUrl = requireNotNull(release.checksumUrl) { "Release does not provide SHA256SUMS" }
        require(apkSize in 1..MAX_APK_BYTES) { "APK size is outside the supported range" }
        require(apkUrl == "${GitHubReleaseClient.REPOSITORY_URL}/releases/download/${release.tag}/$apkName") {
            "Release APK URL is not trusted"
        }
        require(checksumUrl == "${GitHubReleaseClient.REPOSITORY_URL}/releases/download/${release.tag}/SHA256SUMS") {
            "Release checksum URL is not trusted"
        }

        cacheDirectory.mkdirs()
        if (!cacheDirectory.isDirectory) throw AppUpdateDownloadException("Cannot create update cache")
        val target = File(cacheDirectory, UPDATE_FILE_NAME)
        val partial = File(cacheDirectory, PARTIAL_FILE_NAME)
        partial.delete()

        try {
            val expectedSha256 = downloadExpectedSha256(checksumUrl, apkName)
            val actualSha256 = downloadApk(apkUrl, apkSize, partial, onProgress)
            if (!actualSha256.equals(expectedSha256, ignoreCase = true)) {
                throw AppUpdateDownloadException("Downloaded APK checksum does not match SHA256SUMS")
            }
            if (partial.length() != apkSize) {
                throw AppUpdateDownloadException("Downloaded APK size does not match release metadata")
            }

            if (target.exists() && !target.delete()) {
                throw AppUpdateDownloadException("Cannot replace cached update")
            }
            if (!partial.renameTo(target)) {
                throw AppUpdateDownloadException("Cannot finalize downloaded update")
            }
            DownloadedUpdateApk(file = target, sha256 = actualSha256)
        } catch (error: Exception) {
            partial.delete()
            if (error is AppUpdateDownloadException) throw error
            throw AppUpdateDownloadException("Unable to download update", error)
        }
    }

    private fun downloadExpectedSha256(url: String, apkName: String): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/plain")
            .header("User-Agent", USER_AGENT)
            .build()
        val content = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw AppUpdateDownloadException("Could not download SHA256SUMS (HTTP ${response.code})")
            }
            val body = response.body
            body.byteStream().use { input ->
                ByteArrayOutputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > MAX_CHECKSUM_BYTES) {
                            throw AppUpdateDownloadException("SHA256SUMS is too large")
                        }
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray().decodeToString()
                }
            }
        }
        val matches = content.lineSequence().mapNotNull { line ->
            SHA256_LINE.matchEntire(line.trim())?.takeIf { it.groupValues[2] == apkName }?.groupValues?.get(1)
        }.toList()
        return matches.singleOrNull()
            ?: throw AppUpdateDownloadException("SHA256SUMS does not contain the expected APK")
    }

    private suspend fun downloadApk(
        url: String,
        expectedSize: Long,
        partial: File,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): String {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.android.package-archive")
            .header("User-Agent", USER_AGENT)
            .build()
        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw AppUpdateDownloadException("Could not download APK (HTTP ${response.code})")
            }
            val body = response.body
            val contentLength = body.contentLength()
            if (contentLength >= 0L && contentLength != expectedSize) {
                throw AppUpdateDownloadException("APK response size does not match release metadata")
            }
            val digest = MessageDigest.getInstance("SHA-256")
            var downloaded = 0L
            body.byteStream().use { input ->
                partial.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)
                        downloaded += count
                        if (downloaded > expectedSize) {
                            throw AppUpdateDownloadException("APK exceeds release metadata size")
                        }
                        onProgress(downloaded, expectedSize)
                    }
                }
            }
            digest.digest().toHexString()
        }
    }

    companion object {
        private const val CACHE_DIRECTORY_NAME = "app-update"
        private const val UPDATE_FILE_NAME = "update.apk"
        private const val PARTIAL_FILE_NAME = "update.apk.part"
        private const val USER_AGENT = "KokoroBox-Android-Updater"
        private const val MAX_APK_BYTES = 512L * 1024L * 1024L
        private const val MAX_CHECKSUM_BYTES = 64 * 1024
        private const val CONNECT_TIMEOUT_SECONDS = 15L
        private const val READ_TIMEOUT_SECONDS = 60L
        private const val CALL_TIMEOUT_SECONDS = 10L * 60L
        private val SHA256_LINE = Regex("([A-Fa-f0-9]{64})\\s+\\*?(.+)")

        private fun defaultClient(): OkHttpClient = SharedOkHttpClient.newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
}

private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
    "%02x".format(byte.toInt() and 0xFF)
}
