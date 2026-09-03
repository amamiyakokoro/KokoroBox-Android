/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.settings

import android.app.Application
import com.github.yumelira.yumebox.common.util.ByteFormatter.formatSpeed
import com.github.yumelira.yumebox.data.store.AppSettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit

data class ResourceDownloadProgress(
    val progress: Int,
    val currentSize: Long,
    val totalSize: Long,
    val speed: String,
)

class ResourceDownloadClient(
    private val application: Application,
    private val appSettings: AppSettingsStore,
) {
    private companion object {
        const val DEFAULT_USER_AGENT = "ClashMetaForAndroid"
        const val UPDATE_INTERVAL_MS = 500L
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    suspend fun download(
        url: String,
        targetFile: File,
        onProgress: ((ResourceDownloadProgress) -> Unit)? = null,
        validator: ((File) -> Boolean)? = null,
    ): Boolean = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        try {
            targetFile.parentFile?.mkdirs()
            val downloadFile = createDownloadTempFile(targetFile)
            tempFile = downloadFile
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", appSettings.customUserAgent.value.ifEmpty { DEFAULT_USER_AGENT })
                .build()
            val call = client.newCall(request)
            val cancellationHandle = currentCoroutineContext().job.invokeOnCompletion { cause ->
                if (cause is CancellationException) call.cancel()
            }
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) return@withContext false
                    val body = response.body
                    val contentLength = response.header("Content-Length")?.toLongOrNull()
                        ?: body.contentLength()
                    var lastUpdateTime = System.currentTimeMillis()
                    var lastBytesRead = 0L
                    var totalBytesRead = 0L
                    var lastProgress = -1
                    var lastSpeed = 0L

                    onProgress?.invoke(ResourceDownloadProgress(0, 0L, contentLength, formatSpeed(0L)))
                    body.byteStream().use { input ->
                        downloadFile.sink().buffer().use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (true) {
                                val bytesRead = input.read(buffer)
                                if (bytesRead == -1) break
                                currentCoroutineContext().ensureActive()
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                val now = System.currentTimeMillis()
                                val progress = if (contentLength > 0) {
                                    ((totalBytesRead * 100) / contentLength).toInt().coerceIn(0, 100)
                                } else 0
                                if (now - lastUpdateTime >= UPDATE_INTERVAL_MS || progress != lastProgress) {
                                    val elapsedSeconds = (now - lastUpdateTime) / 1000.0
                                    if (elapsedSeconds > 0) {
                                        lastSpeed = ((totalBytesRead - lastBytesRead) / elapsedSeconds).toLong()
                                    }
                                    onProgress?.invoke(
                                        ResourceDownloadProgress(
                                            progress,
                                            totalBytesRead,
                                            contentLength,
                                            formatSpeed(lastSpeed),
                                        ),
                                    )
                                    lastUpdateTime = now
                                    lastBytesRead = totalBytesRead
                                    lastProgress = progress
                                }
                            }
                            output.flush()
                        }
                    }
                    if (totalBytesRead <= 0L) throw IOException("Downloaded file is empty")
                    if (validator != null && !validator(downloadFile)) {
                        throw IOException("Downloaded file failed validation: ${targetFile.name}")
                    }
                    replaceDownloadedFile(downloadFile, targetFile)
                    tempFile = null
                    onProgress?.invoke(
                        ResourceDownloadProgress(100, totalBytesRead, contentLength, formatSpeed(lastSpeed)),
                    )
                    true
                }
            } finally {
                cancellationHandle.dispose()
            }
        } catch (error: CancellationException) {
            tempFile?.delete()
            throw error
        } catch (error: Exception) {
            Timber.e(error, "Resource download failed: %s", url)
            tempFile?.delete()
            false
        }
    }

    private fun createDownloadTempFile(targetFile: File): File {
        val parent = targetFile.parentFile ?: application.cacheDir
        return File(parent, ".${targetFile.name}.${System.currentTimeMillis()}.download").apply {
            if (exists()) delete()
        }
    }

    private fun replaceDownloadedFile(tempFile: File, targetFile: File) {
        try {
            Files.move(
                tempFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
