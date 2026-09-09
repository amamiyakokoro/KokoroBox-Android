package com.github.yumelira.yumebox.common.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import java.io.File
import java.io.IOException

class PackageUpdateInstallException(message: String, cause: Throwable? = null) : IOException(message, cause)

/** Installs a verified APK through the system PackageInstaller confirmation flow. */
class PackageUpdateInstaller(
    private val context: Context,
) {
    fun install(apk: File, resultIntent: Intent) {
        if (!apk.isFile || apk.length() == 0L) {
            throw PackageUpdateInstallException("Verified update APK is missing")
        }

        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setSize(apk.length())
        }
        val sessionId = packageInstaller.createSession(params)
        try {
            packageInstaller.openSession(sessionId).use { session ->
                apk.inputStream().buffered().use { input ->
                    session.openWrite(APK_ENTRY_NAME, 0, apk.length()).use { output ->
                        input.copyTo(output)
                        session.fsync(output)
                    }
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
                )
                session.commit(pendingIntent.intentSender)
            }
        } catch (error: Exception) {
            packageInstaller.abandonSession(sessionId)
            throw PackageUpdateInstallException("Unable to start system update installation", error)
        }
    }

    fun canRequestInstallPackages(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    private companion object {
        const val APK_ENTRY_NAME = "KokoroBox-update.apk"
    }
}
