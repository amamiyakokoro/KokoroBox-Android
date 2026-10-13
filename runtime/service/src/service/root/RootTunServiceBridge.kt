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



package com.amamiyakokoro.box.service.root

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.amamiyakokoro.box.core.model.ConnectionSnapshot
import com.amamiyakokoro.box.core.model.ProxyGroup
import com.amamiyakokoro.box.core.model.ProxySort
import com.amamiyakokoro.box.service.RootTunService
import com.amamiyakokoro.box.service.common.util.appContextOrSelf
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal object RootTunServiceBridge {
    private val mutex = Mutex()

    @Volatile
    private var binder: IRootTunService? = null

    @Volatile
    private var connection: ServiceConnection? = null
    @Volatile
    private var deathRecipient: IBinder.DeathRecipient? = null

    private fun clearDeathRecipient() {
        deathRecipient?.let { recipient -> runCatching { binder?.asBinder()?.unlinkToDeath(recipient, 0) } }
        deathRecipient = null
    }

    fun statusChanges(context: Context): Flow<Unit> = callbackFlow {
        val observer = object : IRootTunStatusObserver.Stub() {
            override fun onStatusChanged() { trySend(Unit) }
        }
        var remote: IRootTunService? = null
        try {
            withContext(Dispatchers.IO) {
                remote = bind(context)
                remote!!.registerStatusObserver(observer)
            }
            awaitCancellation()
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                runCatching { remote?.unregisterStatusObserver(observer) }
            }
        }
    }.catch { error ->
        Timber.w(error, "Root status observer unavailable; periodic verification remains active")
        emit(Unit)
    }

    private suspend fun <T> remoteCall(
        context: Context,
        onBinderFailure: (() -> T)? = null,
        block: (IRootTunService) -> T,
    ): T {
        val appContext = context.appContextOrSelf
        return withContext(Dispatchers.IO) {
            try {
                block(bind(appContext))
            } catch (error: Throwable) {
                if (RootTunRuntimeRecovery.isBinderConnectionFailure(error)) {
                    invalidateConnection(appContext, RootTunRuntimeRecovery.binderFailureReason(error))
                    onBinderFailure?.let { return@withContext it() }
                }
                throw error
            }
        }
    }

    suspend fun start(context: Context): RootTunOperationResult {
        val appContext = context.appContextOrSelf
        val request = RootTunStartRequest(source = "service.bridge.start")
        val result = withContext(Dispatchers.IO) {
            val service = bind(context)
            val resultJson = service.startRootTun(
                RootTunJson.Default.encodeToString(RootTunStartRequest.serializer(), request)
            )
            RootTunJson.Default.decodeFromString(RootTunOperationResult.serializer(), resultJson)
        }
        if (result.success) {
            RootTunService.start(appContext)
        }
        return result
    }

    suspend fun stop(context: Context): RootTunOperationResult {
        val result = remoteCall(
            context = context,
            onBinderFailure = { RootTunOperationResult(success = true) },
        ) { service ->
            val resultJson = service.stopRootTun()
            RootTunJson.Default.decodeFromString(RootTunOperationResult.serializer(), resultJson)
        }
        disconnect()
        return result
    }

    suspend fun queryStatus(context: Context): RootTunStatus {
        return remoteCall(context) { service ->
            val statusJson = service.queryStatus()
            RootTunJson.Default.decodeFromString(RootTunStatus.serializer(), statusJson)
        }
    }

    suspend fun queryTrafficNow(context: Context): Long {
        return remoteCall(context) { service -> service.queryTrafficNow() }
    }

    suspend fun queryTrafficTotal(context: Context): Long {
        return remoteCall(context) { service -> service.queryTrafficTotal() }
    }

    suspend fun queryProxyGroupNames(context: Context, excludeNotSelectable: Boolean = false): List<String> {
        return remoteCall(context) { service ->
            RootTunJson.Default.decodeFromString(
                ListSerializer(String.serializer()),
                service.queryProxyGroupNamesJson(excludeNotSelectable),
            )
        }
    }

    suspend fun queryProxyGroup(
        context: Context,
        name: String,
        sort: ProxySort = ProxySort.Default,
    ): ProxyGroup? {
        return remoteCall(context) { service ->
            service.queryProxyGroupJson(name, sort.name)?.let {
                RootTunJson.Default.decodeFromString(ProxyGroup.serializer(), it)
            }
        }
    }

    suspend fun queryConnections(context: Context): ConnectionSnapshot {
        return remoteCall(context) { service ->
            RootTunJson.Default.decodeFromString(
                ConnectionSnapshot.serializer(),
                service.queryConnectionsJson(),
            )
        }
    }

    suspend fun closeConnection(context: Context, id: String): Boolean {
        return remoteCall(context) { service -> service.closeConnection(id) }
    }

    suspend fun closeAllConnections(context: Context) {
        remoteCall(context) { service -> service.closeAllConnections() }
    }

    private suspend fun bind(context: Context): IRootTunService {
        cachedBinder(context)?.let { return it }

        return mutex.withLock {
            cachedBinder(context)?.let { return it }

            suspendCancellableCoroutine { continuation ->
                val appContext = context.appContextOrSelf
                val intent = createIntent(appContext)
                val mainHandler = Handler(Looper.getMainLooper())

                val newConnection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        val remote = IRootTunService.Stub.asInterface(service)
                        if (remote == null) {
                            invalidateConnection(appContext, "root tun binder is null")
                            if (continuation.isActive) {
                                continuation.resumeWithException(IllegalStateException("root tun binder is null"))
                            }
                            return
                        }

                        clearDeathRecipient()
                        binder = remote
                        connection = this
                        val recipient = IBinder.DeathRecipient {
                            if (binder === remote) invalidateConnection(appContext, "RootTun binder died")
                        }
                        deathRecipient = recipient
                        runCatching { remote.asBinder().linkToDeath(recipient, 0) }.onFailure {
                            invalidateConnection(appContext, "RootTun binder died while connecting")
                            if (continuation.isActive) continuation.resumeWithException(it)
                            return
                        }
                        if (continuation.isActive) {
                            continuation.resume(remote)
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        if (connection === this) invalidateConnection(appContext, null)
                    }

                    override fun onNullBinding(name: ComponentName?) {
                        invalidateConnection(appContext, "root tun service returned null binding")
                        if (continuation.isActive) {
                            continuation.resumeWithException(IllegalStateException("root tun service returned null binding"))
                        }
                    }

                    override fun onBindingDied(name: ComponentName?) {
                        if (connection === this) invalidateConnection(appContext, "RootTun binding died")
                    }
                }

                connection = newConnection
                continuation.invokeOnCancellation {

                    mainHandler.post {
                        runCatching { RootService.unbind(newConnection) }
                    }
                    if (connection === newConnection) {
                        connection = null
                    }
                    if (binder != null && connection == null) {
                        binder = null
                    }
                }

                mainHandler.post {
                    runCatching {
                        RootService.bind(intent, newConnection)
                    }.onFailure { error ->
                        connection = null
                        binder = null
                        if (continuation.isActive) {
                            continuation.resumeWithException(error)
                        }
                    }
                }
            }
        }
    }

    private suspend fun disconnect() {
        mutex.withLock {
            val current = connection ?: return

            withContext(Dispatchers.Main) {
                runCatching { RootService.unbind(current) }
            }
            clearDeathRecipient()
            connection = null
            binder = null
        }
    }

    private fun createIntent(context: Context): Intent {
        return Intent(context, RootTunRootService::class.java)
    }

    private fun cachedBinder(context: Context): IRootTunService? {
        val current = binder ?: return null
        if (RootTunRuntimeRecovery.isBinderAlive(current)) {
            return current
        }
        invalidateConnection(context.appContextOrSelf, "RootTun binder cache is dead")
        return null
    }

    private fun invalidateConnection(context: Context, reason: String?) {
        clearDeathRecipient()
        binder = null
        connection = null
        RootTunRuntimeRecovery.handleBinderGone(context, reason)
    }
}
