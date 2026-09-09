package com.github.yumelira.yumebox.integration.update

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.ParcelFileDescriptor
import com.github.yumelira.yumebox.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/** Installs a verified APK through Shizuku/Sui without exposing app-private paths to shell. */
class ShizukuUpdateInstaller(private val context: Context) {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun install(apk: File): String {
        require(apk.isFile && apk.length() > 0L) { "Verified update APK is unavailable" }
        awaitPermission()

        return suspendCoroutine { continuation ->
            val completed = AtomicBoolean(false)
            val started = AtomicBoolean(false)
            fun completeSuccess(value: String) {
                if (completed.compareAndSet(false, true)) continuation.resume(value)
            }
            fun completeFailure(error: Exception) {
                if (completed.compareAndSet(false, true)) continuation.resumeWithException(error)
            }
            val args = Shizuku.UserServiceArgs(
                ComponentName(context, ShizukuUpdateUserService::class.java),
            ).tag(USER_SERVICE_TAG)
                .version(USER_SERVICE_VERSION)
                .daemon(false)
                .debuggable(BuildConfig.DEBUG)
                .processNameSuffix("kokorobox-update")

            lateinit var connection: ServiceConnection
            fun unbind() {
                runCatching { Shizuku.unbindUserService(args, connection, true) }
            }

            connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, service: IBinder) {
                    if (!started.compareAndSet(false, true)) return
                    val installer = IShizukuUpdateInstaller.Stub.asInterface(service)
                    ioScope.launch {
                        try {
                            val result = ParcelFileDescriptor.open(
                                apk,
                                ParcelFileDescriptor.MODE_READ_ONLY,
                            ).use { descriptor ->
                                installer.install(descriptor, apk.length())
                            }
                            completeSuccess(result)
                        } catch (error: Exception) {
                            completeFailure(error)
                        } finally {
                            unbind()
                        }
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    completeFailure(IllegalStateException("Shizuku update service disconnected"))
                }
            }

            try {
                Shizuku.bindUserService(args, connection)
            } catch (error: Exception) {
                completeFailure(error)
            }
        }
    }

    private suspend fun awaitPermission() = suspendCoroutine<Unit> { continuation ->
        if (!Shizuku.pingBinder()) {
            continuation.resumeWithException(
                IllegalStateException("Shizuku or Sui is not running"),
            )
            return@suspendCoroutine
        }
        if (Shizuku.isPreV11()) {
            continuation.resumeWithException(
                IllegalStateException("Shizuku v11 or newer is required"),
            )
            return@suspendCoroutine
        }
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            continuation.resume(Unit)
            return@suspendCoroutine
        }
        if (Shizuku.shouldShowRequestPermissionRationale()) {
            continuation.resumeWithException(
                IllegalStateException("Grant KokoroBox permission in Shizuku before installing updates"),
            )
            return@suspendCoroutine
        }

        lateinit var listener: Shizuku.OnRequestPermissionResultListener
        listener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode != PERMISSION_REQUEST_CODE) return@OnRequestPermissionResultListener
            Shizuku.removeRequestPermissionResultListener(listener)
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(
                    IllegalStateException("Shizuku permission was denied"),
                )
            }
        }
        Shizuku.addRequestPermissionResultListener(listener)
        try {
            Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
        } catch (error: Exception) {
            Shizuku.removeRequestPermissionResultListener(listener)
            continuation.resumeWithException(error)
        }
    }

    private companion object {
        const val PERMISSION_REQUEST_CODE = 61942
        const val USER_SERVICE_TAG = "kokorobox-update-installer"
        const val USER_SERVICE_VERSION = 1
    }
}
