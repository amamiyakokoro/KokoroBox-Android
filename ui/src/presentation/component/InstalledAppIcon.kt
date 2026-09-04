/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.presentation.component

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads an installed application's icon away from the main thread and shares decoded
 * bitmaps between lists and screens. PackageManager returns a new Drawable for every call,
 * so loading directly from each lazy-list item causes repeated I/O and bitmap allocation.
 */
@Composable
fun rememberInstalledAppIcon(
    packageName: String?,
    bitmapSize: Int,
): ImageBitmap? {
    val appContext = LocalContext.current.applicationContext
    val normalizedPackageName = packageName?.trim().orEmpty()
    val icon by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = normalizedPackageName,
        key2 = bitmapSize,
    ) {
        value = InstalledAppIconCache.load(
            context = appContext,
            packageName = normalizedPackageName,
            bitmapSize = bitmapSize,
        )?.asImageBitmap()
    }
    return icon
}

private object InstalledAppIconCache {
    private const val MAX_CACHE_BYTES = 8 * 1024 * 1024

    private val loadLock = Any()
    private val missingKeys = HashSet<String>()
    private val bitmaps = object : LruCache<String, Bitmap>(MAX_CACHE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }

    suspend fun load(
        context: Context,
        packageName: String,
        bitmapSize: Int,
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (packageName.isBlank() || bitmapSize <= 0) return@withContext null
        val key = "$packageName@$bitmapSize"

        synchronized(loadLock) {
            bitmaps.get(key)?.let { return@synchronized it }
            if (key in missingKeys) return@synchronized null

            runCatching {
                context.packageManager
                    .getApplicationIcon(packageName)
                    .toBitmap(width = bitmapSize, height = bitmapSize)
            }.getOrNull()?.also { bitmap ->
                bitmaps.put(key, bitmap)
            } ?: run {
                missingKeys += key
                null
            }
        }
    }
}
