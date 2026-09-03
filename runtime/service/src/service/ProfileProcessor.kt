/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  YumeLira 2025 - Present
 *
 */



package com.github.yumelira.yumebox.service

import android.content.Context
import android.net.Uri
import com.github.yumelira.yumebox.core.Clash
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroApi
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroAuthenticationRequiredException
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroSession
import com.github.yumelira.yumebox.service.common.log.Log
import com.github.yumelira.yumebox.service.remote.IFetchObserver
import com.github.yumelira.yumebox.service.runtime.config.ServiceStore
import com.github.yumelira.yumebox.service.runtime.entity.Imported
import com.github.yumelira.yumebox.service.runtime.entity.Profile
import com.github.yumelira.yumebox.service.runtime.records.ImportedDao
import com.github.yumelira.yumebox.service.runtime.records.SelectionDao
import com.github.yumelira.yumebox.service.runtime.util.importedDir
import com.github.yumelira.yumebox.service.runtime.util.sendProfileChanged
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URLDecoder
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

object ProfileProcessor {
    private const val DEFAULT_USER_AGENT = "ClashMetaForAndroid"

    private val profileLock = Mutex()
    private val processLock = Mutex()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private fun resolveUserAgent(): String {
        val settings = MMKV.mmkvWithID("settings", MMKV.MULTI_PROCESS_MODE)
        val custom = settings.decodeString("customUserAgent")?.trim().orEmpty()
        return custom.ifBlank { DEFAULT_USER_AGENT }
    }

    private data class SubscriptionInfo(
        val upload: Long = 0,
        val download: Long = 0,
        val total: Long = 0,
        val expire: Long = 0,
        val title: String? = null,
        val filename: String? = null,
        val interval: Int = 24
    )

    private data class UpdateSnapshot(
        val imported: Imported,
        val hasCommittedConfig: Boolean
    )

    private class SubscriptionDownloadException(message: String) : IOException(message)

    private suspend fun downloadWithSubscriptionInfo(
        context: Context,
        url: String,
        targetFile: File,
        onProgress: ((Int) -> Unit)? = null
    ): SubscriptionInfo? = withContext(Dispatchers.IO + NonCancellable) {
        try {
            targetFile.parentFile?.mkdirs()
            if (targetFile.exists()) targetFile.delete()

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", resolveUserAgent())
                .build()

            val isKokoroSubscription = KokoroApi.isAuthenticatedSubscriptionUrl(url)
            val response = if (isKokoroSubscription) {
                KokoroSession(context).executeSubscriptionConfig(request)
            } else {
                httpClient.newCall(request).execute()
            }

            response.use {
                if (isKokoroSubscription) {
                    val mediaType = response.body.contentType()
                    val responseFormat = mediaType?.let { "${it.type}/${it.subtype}" } ?: "unknown"
                    Log.i(
                        "Kokoro subscription response: HTTP ${response.code}, " +
                            "format=$responseFormat, bytes=${response.body.contentLength()}"
                    )
                }
                if (!response.isSuccessful) {
                    val message = when {
                        isKokoroSubscription && response.code in listOf(401, 403) ->
                            "Kokoro session expired. Sign in again."
                        isKokoroSubscription && response.code in listOf(400, 422) ->
                            "Kokoro rejected the subscription options (HTTP ${response.code})."
                        else -> "Subscription server returned HTTP ${response.code}."
                    }
                    throw SubscriptionDownloadException(message)
                }
                if (isKokoroSubscription && response.body.contentType()?.let {
                        "${it.type}/${it.subtype}"
                    } != "text/yaml"
                ) {
                    throw SubscriptionDownloadException(
                        "Kokoro returned an unexpected subscription format."
                    )
                }

                val parsedInfo = parseSubscriptionInfo(
                    response.headers["Subscription-Userinfo"] ?: response.headers["subscription-userinfo"],
                    response.headers
                )
                val subscriptionUri = Uri.parse(url)
                val profileAutoUpdate = subscriptionUri.getQueryParameter("profile_update")
                    ?.toBooleanStrictOrNull()
                    ?: (subscriptionUri.getQueryParameter("update") != "off")
                val subInfo = if (isKokoroSubscription && !profileAutoUpdate) {
                    parsedInfo.copy(interval = 0)
                } else {
                    parsedInfo
                }

                val body = response.body
                val contentLength = body.contentLength()
                val inputStream = body.byteStream()

                var totalBytesRead = 0L
                val lastUpdate = AtomicLong(0)

                targetFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate.get() >= 500) {
                            val progress = if (contentLength > 0) {
                                ((totalBytesRead * 100) / contentLength).toInt()
                            } else 0
                            onProgress?.invoke(progress)
                            lastUpdate.set(now)
                        }
                    }
                }

                subInfo
            }
        } catch (e: Exception) {
            val safeError = when (e) {
                is SubscriptionDownloadException -> e
                is KokoroAuthenticationRequiredException ->
                    SubscriptionDownloadException("Kokoro session expired. Sign in again.")
                is UnknownHostException ->
                    SubscriptionDownloadException("Unable to resolve the subscription server.")
                is SocketTimeoutException ->
                    SubscriptionDownloadException("The subscription server timed out.")
                else -> SubscriptionDownloadException("Unable to download subscription configuration.")
            }
            Log.w("Subscription download failed (${e::class.java.simpleName})")
            if (targetFile.exists()) targetFile.delete()
            throw safeError
        }
    }

    private fun parseSubscriptionInfo(
        userinfo: String?,
        headers: okhttp3.Headers
    ): SubscriptionInfo {
        var upload = 0L
        var download = 0L
        var total = 0L
        var expire = 0L

        fun parseLikeJsParseInt(value: String): Long {
            val trimmed = value.trim()
            val integerPart = trimmed.takeWhile { it.isDigit() }
            if (integerPart.isNotEmpty()) return integerPart.toLongOrNull() ?: 0L
            return trimmed.substringBefore('.').toLongOrNull() ?: 0L
        }

        fun findHeaderBySuffix(suffix: String): String? {
            val target = suffix.lowercase(Locale.getDefault())
            val key = headers.names().firstOrNull {
                it.lowercase(Locale.getDefault()).endsWith(target)
            } ?: return null
            return headers[key]
        }

        fun parseExpireDate(value: String): Long? = runCatching {
            when {
                value.matches(Regex("\\d+")) -> value.toLong() * 1000L
                value.contains("-") -> {
                    val parts = value.split("-")
                    if (parts.size < 3) return@runCatching null

                    val year = parts[0].toIntOrNull() ?: return@runCatching null
                    val month = parts[1].toIntOrNull() ?: return@runCatching null
                    val day = parts[2].toIntOrNull() ?: return@runCatching null

                    val calendar = Calendar.getInstance()
                    calendar.set(year, month - 1, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                else -> null
            }
        }.getOrNull()

        val resolvedUserinfo = userinfo ?: findHeaderBySuffix("subscription-userinfo")

        if (!resolvedUserinfo.isNullOrBlank()) {
            val flags = resolvedUserinfo.split(";")
            for (flag in flags) {
                val info = flag.trim().split("=", limit = 2)
                if (info.size >= 2) {
                    val key = info[0].trim().lowercase(Locale.getDefault())
                    val value = info[1].trim()

                    when {
                        key.contains("upload") && value.isNotEmpty() -> {
                            upload = parseLikeJsParseInt(value)
                        }
                        key.contains("download") && value.isNotEmpty() -> {
                            download = parseLikeJsParseInt(value)
                        }
                        key.contains("total") && value.isNotEmpty() -> {
                            total = parseLikeJsParseInt(value)
                        }
                        key.contains("expire") && value.isNotEmpty() -> {
                            expire = parseLikeJsParseInt(value) * 1000L
                        }
                    }
                }
            }
        }

        if (expire == 0L) {
            expire = (headers["Expires"] ?: findHeaderBySuffix("expires"))?.let { parseExpireDate(it) } ?: 0L
        }

        val title = decodeSubscriptionTitle(
            headers["Profile-Title"]
                ?: headers["Subscription-Title"]
                ?: findHeaderBySuffix("profile-title")
                ?: findHeaderBySuffix("subscription-title")
        )

        val filename = parseFilenameFromHeaders(headers)

        val interval = headers["Profile-Update-Interval"]?.toIntOrNull()
            ?: headers["Subscription-Update-Interval"]?.toIntOrNull()
            ?: findHeaderBySuffix("profile-update-interval")?.toIntOrNull()
            ?: findHeaderBySuffix("subscription-update-interval")?.toIntOrNull()
            ?: 24

        return SubscriptionInfo(upload, download, total, expire, title, filename, interval)
    }

    private fun decodeSubscriptionTitle(raw: String?): String? {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null

        fun decodeBase64(encoded: String): String? {
            val candidate = encoded.trim().trim('"', '\'')
            if (candidate.isBlank()) return null
            if (!candidate.matches(Regex("^[A-Za-z0-9+/=]+$"))) return null
            return runCatching {
                String(Base64.getDecoder().decode(candidate), StandardCharsets.UTF_8).trim()
            }.getOrNull()
        }

        fun decodeRfc5987(value: String): String? {
            val match = Regex("""^([^']*)'[^']*'(.*)$""").find(value.trim()) ?: return null
            val charset = match.groupValues[1].ifBlank { "UTF-8" }
            val encoded = match.groupValues[2]

            return runCatching {
                URLDecoder.decode(encoded, charset).trim()
            }.getOrNull()
        }

        return runCatching {
            val normalized = value.trim().trim('"', '\'')
            when {
                normalized.startsWith("base64:", ignoreCase = true) -> {
                    decodeBase64(normalized.substringAfter(':', "")) ?: value
                }
                else -> {
                    decodeRfc5987(normalized)
                        ?: runCatching {
                            URLDecoder.decode(normalized, StandardCharsets.UTF_8.name()).trim()
                        }.getOrNull()
                        ?: decodeBase64(normalized)
                        ?: value
                }
            }
        }.getOrElse { value }.takeIf { it.isNotBlank() }
    }

    private fun parseFilenameFromHeaders(headers: okhttp3.Headers): String? {
        val contentDisposition = headers["Content-Disposition"]
            ?: headers.names()
                .firstOrNull { it.lowercase(Locale.getDefault()).endsWith("content-disposition") }
                ?.let { headers[it] }
            ?: return null

        return runCatching {
            if (contentDisposition.contains("filename*=", ignoreCase = true)) {
                val regex = """filename\*=([^']*)'([^']*)'([^;]+)""".toRegex(RegexOption.IGNORE_CASE)
                regex.find(contentDisposition)?.let { match ->
                    val charset = match.groupValues[1].ifBlank { "UTF-8" }
                    val encodedFilename = match.groupValues[3].trim().trim('"', '\'')
                    val safeCharset = runCatching { Charset.forName(charset).name() }.getOrDefault("UTF-8")
                    URLDecoder.decode(encodedFilename, safeCharset).trim()
                }
            } else {
                val regex = """filename=([^;]+)""".toRegex(RegexOption.IGNORE_CASE)
                regex.find(contentDisposition)?.groupValues?.getOrNull(1)?.trim()?.trim('"', '\'')
            }?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private suspend fun fetchUrlSubscription(
        context: Context,
        stagingDir: File,
        source: String,
        onProgress: (Int) -> Unit
    ): SubscriptionInfo? {
        onProgress(5)
        val tempFile = stagingDir.resolve("config.download.yaml")
        val info = downloadWithSubscriptionInfo(context, source, tempFile) { progress ->
            onProgress(5 + (progress * 0.4).toInt())
        }
        tempFile.copyTo(stagingDir.resolve("config.yaml"), overwrite = true)
        tempFile.delete()
        return info
    }

    private fun resolveSubscriptionName(
        snapshotName: String,
        snapshotSource: String,
        subInfo: SubscriptionInfo?
    ): String {
        if (!ProfileNameUtils.isAutoGeneratedProfileName(snapshotName)) return snapshotName

        val headerTitle = subInfo?.title?.takeIf { it.isNotBlank() }
        val filename = subInfo?.filename?.substringBeforeLast(".")?.takeIf { it.isNotBlank() }
        val sourceName = ProfileNameUtils.extractSourceBaseName(snapshotSource)

        if (headerTitle != null) return headerTitle
        if (filename != null) return filename
        if (sourceName != null) return sourceName
        return snapshotName
    }

    suspend fun update(context: Context, uuid: UUID, callback: IFetchObserver?) {
        withContext(Dispatchers.IO + NonCancellable) {
            processLock.withLock {
                val targetDir = context.importedDir.resolve(uuid.toString())
                val stagingDir = context.cacheDir.resolve("profile-staging").resolve(uuid.toString())
                val snapshot = profileLock.withLock {
                    val imported = ImportedDao.queryByUUID(uuid)
                        ?: throw IllegalArgumentException("profile $uuid not found")

                    stagingDir.deleteRecursively()
                    stagingDir.mkdirs()

                    if (targetDir.exists()) {
                        targetDir.copyRecursively(stagingDir, overwrite = true)
                    }

                    UpdateSnapshot(
                        imported = imported,
                        hasCommittedConfig = targetDir.resolve("runtime.yaml").isFile
                    )
                }

                var cb = callback
                var subInfo: SubscriptionInfo? = null

                try {
                    if (snapshot.imported.type == Profile.Type.Url) {
                        subInfo = fetchUrlSubscription(context, stagingDir, snapshot.imported.source) { progress ->
                            try {
                                cb?.updateStatus(
                                    com.github.yumelira.yumebox.core.model.FetchStatus(
                                        action = com.github.yumelira.yumebox.core.model.FetchStatus.Action.FetchConfiguration,
                                        args = emptyList(),
                                        progress = progress,
                                        max = 100
                                    )
                                )
                            } catch (_: Exception) {
                                cb = null
                            }
                        }
                    }

                    Clash.fetchAndValid(stagingDir, snapshot.imported.source, true) {
                        try {
                            cb?.updateStatus(
                                it
                            )
                        } catch (e: Exception) {
                            cb = null
                            Log.w("Report fetch status: $e", e)
                        }
                    }.await()

                    profileLock.withLock {
                        if (ImportedDao.exists(snapshot.imported.uuid)) {
                            targetDir.deleteRecursively()
                            stagingDir.copyRecursively(targetDir, overwrite = true)

                            val finalName = if (snapshot.imported.type == Profile.Type.Url) {
                                resolveSubscriptionName(snapshot.imported.name, snapshot.imported.source, subInfo)
                            } else snapshot.imported.name

                            val updated = Imported(
                                snapshot.imported.uuid,
                                finalName,
                                snapshot.imported.type,
                                snapshot.imported.source,
                                if (snapshot.imported.type == Profile.Type.Url && subInfo != null) {
                                    subInfo.interval.toLong() * 60 * 60 * 1000
                                } else snapshot.imported.interval,
                                subInfo?.upload ?: snapshot.imported.upload,
                                subInfo?.download ?: snapshot.imported.download,
                                subInfo?.total ?: snapshot.imported.total,
                                subInfo?.expire ?: snapshot.imported.expire,
                                snapshot.imported.createdAt
                            )
                            ImportedDao.update(updated)

                            context.sendProfileChanged(snapshot.imported.uuid)
                        }
                    }
                } catch (e: Exception) {
                    profileLock.withLock {
                        if (!snapshot.hasCommittedConfig && ImportedDao.exists(snapshot.imported.uuid)) {
                            ImportedDao.remove(snapshot.imported.uuid)
                            SelectionDao.clear(snapshot.imported.uuid)
                            targetDir.deleteRecursively()
                            context.sendProfileChanged(snapshot.imported.uuid)
                        }
                    }
                    throw e
                } finally {
                    stagingDir.deleteRecursively()
                }
            }
        }
    }

    suspend fun delete(context: Context, uuid: UUID) {
        withContext(Dispatchers.IO + NonCancellable) {
            profileLock.withLock {
                ImportedDao.remove(uuid)
                SelectionDao.clear(uuid)

                val imported = context.importedDir.resolve(uuid.toString())
                imported.deleteRecursively()

                context.sendProfileChanged(uuid)
            }
        }
    }

    suspend fun active(context: Context, uuid: UUID) {
        withContext(Dispatchers.IO + NonCancellable) {
            profileLock.withLock {
                if (!ImportedDao.exists(uuid)) {
                    throw IllegalArgumentException("profile $uuid is not available")
                }
                val store = ServiceStore()
                store.activeProfile = uuid
                context.sendProfileChanged(uuid)
            }
        }
    }
}
