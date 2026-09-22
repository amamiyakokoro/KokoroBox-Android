/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */



package com.amamiyakokoro.box.remote

import android.content.Context
import android.os.Build
import com.amamiyakokoro.box.service.ClashManager
import com.amamiyakokoro.box.service.ProfileManager
import com.amamiyakokoro.box.service.common.util.appContextOrSelf
import com.amamiyakokoro.box.service.common.util.initializeServiceGlobal
import com.amamiyakokoro.box.service.remote.IClashManager
import com.amamiyakokoro.box.service.remote.IProfileManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

object ServiceClient {
    private val mutex = Mutex()
    private var initialized = false
    private var localClashManager: ClashManager? = null
    private var runtimeClashManager: RuntimeClashManager? = null
    private var clashManager: IClashManager? = null
    private var profileManager: IProfileManager? = null

    suspend fun connect(ctx: Context) {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val appContext = ctx.appContextOrSelf
                if (initialized && clashManager != null && profileManager != null) {
                    return@withLock
                }

                val startedAt = System.currentTimeMillis()

                try {
                    initializeServiceGlobal(appContext)
                    val localManager = ClashManager(appContext)
                    val runtimeManager = RuntimeClashManager(appContext, localManager)
                    localClashManager = localManager
                    runtimeClashManager = runtimeManager
                    clashManager = runtimeManager
                    profileManager = ProfileManager(appContext)
                    initialized = true
                    Timber.d(
                        "ServiceClient gateway initialized in pid=${android.os.Process.myPid()}, process=${currentProcessName(appContext)}, cost=${System.currentTimeMillis() - startedAt}ms"
                    )
                } catch (e: Exception) {
                    initialized = false
                    runtimeClashManager?.close()
                    runtimeClashManager = null
                    localClashManager = null
                    clashManager = null
                    profileManager = null
                    if (e is CancellationException) throw e
                    Timber.e(e, "Failed to initialize local service gateway")
                    throw e
                }
            }
        }
    }

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                runtimeClashManager?.close()
                runtimeClashManager = null
                localClashManager = null
                clashManager = null
                profileManager = null
                initialized = false
            }
        }
    }

    suspend fun clash(): IClashManager {
        return clashManager ?: throw IllegalStateException("ServiceClient not connected")
    }

    suspend fun profile(): IProfileManager {
        return profileManager ?: throw IllegalStateException("ServiceClient not connected")
    }

    fun isConnected(): Boolean = initialized && clashManager != null && profileManager != null

    private fun currentProcessName(context: Context): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            android.app.Application.getProcessName()
        } else {
            runCatching {
                java.io.File("/proc/self/cmdline")
                    .readText()
                    .trimEnd('\u0000')
                    .takeIf(String::isNotBlank)
            }.getOrNull() ?: context.packageName
        }
}
