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

package com.github.yumelira.yumebox.data.controller

import android.content.Context
import android.net.Uri
import java.io.File

class AcgWallpaperStorage(
    private val context: Context,
) {
    private val wallpaperDir: File
        get() = File(context.filesDir, WALLPAPER_DIR_NAME)

    private fun newWallpaperFile(): File = File(
        wallpaperDir,
        "$WALLPAPER_FILE_PREFIX${System.currentTimeMillis()}_${System.nanoTime()}",
    )

    fun copyFromUri(sourceUri: String): String {
        val uri = Uri.parse(sourceUri)
        wallpaperDir.mkdirs()
        val targetFile = newWallpaperFile()
        val tempFile = File(wallpaperDir, "${targetFile.name}.tmp")
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open ACG wallpaper")
        if (targetFile.exists()) {
            targetFile.delete()
        }
        check(tempFile.renameTo(targetFile)) { "Unable to save ACG wallpaper" }
        clearExcept(targetFile)
        return Uri.fromFile(targetFile).toString()
    }

    fun saveBackupBytes(bytes: ByteArray): String {
        wallpaperDir.mkdirs()
        val targetFile = newWallpaperFile()
        val tempFile = File(wallpaperDir, "${targetFile.name}.tmp")
        tempFile.writeBytes(bytes)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        check(tempFile.renameTo(targetFile)) { "Unable to restore ACG wallpaper" }
        clearExcept(targetFile)
        return Uri.fromFile(targetFile).toString()
    }

    fun readBytes(wallpaperUri: String): ByteArray? = runCatching {
        if (wallpaperUri.isBlank()) return@runCatching null
        context.contentResolver.openInputStream(Uri.parse(wallpaperUri))?.use { input ->
            input.readBytes()
        }
    }.getOrNull()

    fun clear() {
        runCatching { wallpaperDir.deleteRecursively() }
    }

    private fun clearExcept(keptFile: File) {
        wallpaperDir.listFiles()?.forEach { file ->
            if (file.absolutePath != keptFile.absolutePath) {
                runCatching { file.delete() }
            }
        }
    }

    companion object {
        private const val WALLPAPER_DIR_NAME = "acg_wallpaper"
        private const val WALLPAPER_FILE_PREFIX = "wallpaper_"
    }
}
