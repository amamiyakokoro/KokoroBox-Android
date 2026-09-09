package com.github.yumelira.yumebox.integration.update

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.oom_wg.purejoy.mlang.MLang

/** Delivers a deferred PackageInstaller confirmation through an explicit user notification. */
class AppUpdateInstallNotifier(
    private val context: Context,
) {
    fun showConfirmation(confirmationIntent: Intent): Boolean {
        if (!canPostNotifications()) return false
        createChannel()
        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            confirmationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(MLang.About.License.CheckUpdate)
            .setContentText(MLang.About.Update.WaitingForInstallConfirmation)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(MLang.About.Update.WaitingForInstallConfirmation),
            )
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        return true
    }

    fun showAvailableUpdate(tag: String, releaseUrl: String): Boolean {
        if (!canPostNotifications()) return false
        createChannel()
        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID + 1,
            Intent(Intent.ACTION_VIEW, android.net.Uri.parse(releaseUrl)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        NotificationManagerCompat.from(context).notify(
            NOTIFICATION_ID + 1,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(context.applicationInfo.icon)
                .setContentTitle("${MLang.About.Update.Available}: $tag")
                .setContentText(MLang.About.License.CheckUpdateSummary)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build(),
        )
        return true
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                MLang.About.License.CheckUpdate,
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }

    private companion object {
        const val CHANNEL_ID = "app_updates"
        const val NOTIFICATION_ID = 11_804
    }
}
