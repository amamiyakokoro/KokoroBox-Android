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



package com.amamiyakokoro.box.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.amamiyakokoro.box.core.util.PollingTimerSpecs
import com.amamiyakokoro.box.core.util.PollingTimers
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.data.model.ProxyMode
import com.amamiyakokoro.box.runtime.service.R
import com.amamiyakokoro.box.service.common.constants.Components
import com.amamiyakokoro.box.service.common.constants.Intents
import com.amamiyakokoro.box.service.common.util.ServiceLanguageRuntime
import com.amamiyakokoro.box.service.common.util.appContextOrSelf
import com.amamiyakokoro.box.service.notification.NotificationPresentation
import com.amamiyakokoro.box.service.notification.NotificationPresentationFactory
import com.amamiyakokoro.box.service.notification.TodayTrafficNotificationReader
import com.amamiyakokoro.box.service.root.RootTunServiceBridge
import com.amamiyakokoro.box.service.root.RootTunState
import com.amamiyakokoro.box.service.root.RootTunStateStore
import com.amamiyakokoro.box.service.root.RootTunStatus
import com.amamiyakokoro.box.service.runtime.util.sendClashStarted
import com.amamiyakokoro.box.service.runtime.util.sendClashStopped
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class RootTunService : BaseService() {
    private val stateStore by lazy { RootTunStateStore(appContextOrSelf) }
    private val todayTrafficReader by lazy { TodayTrafficNotificationReader() }
    private val notificationManager by lazy { NotificationManagerCompat.from(this) }
    private var cachedTodayTrafficBytes: Long = 0L
    private var lastTodayTrafficRefreshAt: Long = 0L
    private var notificationJob: Job? = null
    private val settingsStore by lazy { MMKV.mmkvWithID("settings", MMKV.MULTI_PROCESS_MODE) }
    private val powerManager by lazy { getSystemService(PowerManager::class.java) }
    private var lastNotificationFingerprint: String? = null
    private var lastTrafficDisplayEnabled: Boolean? = null
    private var lastTrafficNotificationAt: Long = 0L
    private val languageChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intents.ACTION_APP_LANGUAGE_CHANGED) return
            launch {
                ServiceLanguageRuntime.applyAppLanguage(this@RootTunService)
                refreshNotificationLanguage()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        registerLanguageChangedReceiver()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                launch {
                    runCatching { RootTunServiceBridge.stop(appContextOrSelf) }
                }
                return START_NOT_STICKY
            }

            ACTION_START, null -> {
                val cachedStatus = stateStore.snapshot()
                val initialNotification = buildNotification(
                    NotificationPresentationFactory.createStatus(
                        profileName = cachedStatus.profileName
                            ?: getString(LocaleR.string.service_notification_unknown_profile),
                        status = describeStatus(cachedStatus),
                    ),
                )
                lastNotificationFingerprint = notificationFingerprint(initialNotification)
                startForeground(NOTIFICATION_ID, initialNotification)
                if (!cachedStatus.state.isActive && !cachedStatus.state.isRecovering) {
                    stopSelf()
                    return START_NOT_STICKY
                }

                if (notificationJob?.isActive != true) {
                    notificationJob = launch(Dispatchers.Default) {
                        var startedBroadcastSent = false
                        var unreachableCount = 0
                        var lastStatus = cachedStatus

                        PollingTimers.ticks(PollingTimerSpecs.RootTunStatusPolling).collect {
                            val snapshotResult = runCatching {
                                RootTunServiceBridge.queryStatus(appContextOrSelf)
                            }
                            val snapshot = snapshotResult.getOrNull()
                            if (snapshot == null) {
                                unreachableCount++
                                val error = snapshotResult.exceptionOrNull()
                                val fallbackStatus = stateStore.snapshot().takeIf {
                                    it.state != RootTunState.Idle || !it.profileName.isNullOrBlank() || !it.lastError.isNullOrBlank()
                                } ?: lastStatus
                                val title = fallbackStatus.profileName
                                    ?: getString(LocaleR.string.service_notification_unknown_profile)
                                val content = if (unreachableCount >= 3) {
                                    describeStatus(
                                        fallbackStatus.copy(
                                            lastError = fallbackStatus.lastError ?: error?.message
                                                ?: getString(LocaleR.string.service_notification_state_unavailable),
                                        ),
                                    )
                                } else {
                                    error?.message
                                        ?: getString(LocaleR.string.service_notification_waiting_for_reconnect)
                                }
                                notifyIfChanged(
                                    buildNotification(
                                        NotificationPresentationFactory.createStatus(
                                            profileName = title,
                                            status = content,
                                        ),
                                    ),
                                )
                                if (!fallbackStatus.state.isActive && !fallbackStatus.state.isRecovering) {
                                    stopSelf()
                                    return@collect
                                }
                                return@collect
                            }

                            unreachableCount = 0
                            val statusPresentationChanged =
                                snapshot.state != lastStatus.state || snapshot.profileName != lastStatus.profileName
                            lastStatus = snapshot
                            syncStatus(snapshot)

                            if (snapshot.state == RootTunState.Running && !startedBroadcastSent) {
                                sendClashStarted()
                                startedBroadcastSent = true
                            }

                            if (snapshot.state == RootTunState.Idle || snapshot.state == RootTunState.Failed) {
                                notifyIfChanged(
                                    buildNotification(
                                        NotificationPresentationFactory.createStatus(
                                            profileName = snapshot.profileName
                                                ?: getString(LocaleR.string.service_notification_unknown_profile),
                                            status = describeStatus(snapshot),
                                        ),
                                    ),
                                )
                                stopSelf()
                                return@collect
                            }

                            val profileName = snapshot.profileName
                                ?: getString(LocaleR.string.service_notification_unknown_profile)
                            val showTraffic = shouldShowTrafficNotification()
                            val now = SystemClock.elapsedRealtime()
                            val trafficDisplayChanged = showTraffic != lastTrafficDisplayEnabled
                            val trafficRefreshInterval = if (powerManager?.isInteractive != false) {
                                TRAFFIC_NOTIFICATION_REFRESH_INTERVAL_MS
                            } else {
                                TRAFFIC_NOTIFICATION_SCREEN_OFF_REFRESH_INTERVAL_MS
                            }
                            val trafficRefreshDue = showTraffic &&
                                now - lastTrafficNotificationAt >= trafficRefreshInterval
                            if (!statusPresentationChanged && !trafficDisplayChanged && !trafficRefreshDue) {
                                return@collect
                            }
                            val presentation = if (snapshot.state == RootTunState.Running) {
                                if (showTraffic) {
                                    lastTrafficNotificationAt = now
                                    buildTrafficPresentation(profileName)
                                } else {
                                    NotificationPresentationFactory.createStatus(
                                        profileName = profileName,
                                        status = getString(LocaleR.string.service_notification_running),
                                    )
                                }
                            } else {
                                NotificationPresentationFactory.createStatus(
                                    profileName = profileName,
                                    status = describeStatus(snapshot),
                                )
                            }
                            lastTrafficDisplayEnabled = showTraffic
                            notifyIfChanged(buildNotification(presentation))
                        }
                    }
                }

                return START_STICKY
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        runCatching { unregisterReceiver(languageChangedReceiver) }
        notificationJob?.cancel()
        notificationJob = null

        val snapshot = stateStore.snapshot()
        if (!snapshot.state.isActive) {
            StatusProvider.markRuntimeIdle(ProxyMode.RootTun)
            sendClashStopped(snapshot.lastError)
        }

        super.onDestroy()
    }

    private suspend fun buildTrafficPresentation(profileName: String): NotificationPresentation {
        val now = runCatching { RootTunServiceBridge.queryTrafficNow(appContextOrSelf) }.getOrDefault(0L)
        val total = runCatching { RootTunServiceBridge.queryTrafficTotal(appContextOrSelf) }.getOrDefault(0L)
        return NotificationPresentationFactory.createRunning(
            resources = resources,
            profileName = profileName,
            trafficNow = now,
            todayTrafficBytes = queryCachedTodayTrafficBytes(),
            fallbackTrafficTotal = total,
        )
    }

    private suspend fun refreshNotificationLanguage() {
        val status = stateStore.snapshot()
        val profileName = status.profileName
            ?: getString(LocaleR.string.service_notification_unknown_profile)
        val presentation = if (status.state == RootTunState.Running && shouldShowTrafficNotification()) {
            buildTrafficPresentation(profileName)
        } else {
            NotificationPresentationFactory.createStatus(
                profileName = profileName,
                status = describeStatus(status),
            )
        }
        lastNotificationFingerprint = null
        notifyIfChanged(buildNotification(presentation))
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerLanguageChangedReceiver() {
        val filter = IntentFilter(Intents.ACTION_APP_LANGUAGE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(languageChangedReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(languageChangedReceiver, filter)
        }
    }

    private fun buildNotification(presentation: NotificationPresentation): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent().apply {
                component = Components.PROXY_SHEET_ACTIVITY
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RootTunService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(presentation.title)
            .setContentText(presentation.content)
            .setSubText(presentation.subText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(presentation.expandedText)
                    .setSummaryText(presentation.subText)
            )
            .setSmallIcon(R.drawable.ic_notification_furin)
            .setColor(getColor(R.color.color_clash))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, getString(LocaleR.string.service_tile_click_to_stop_proxy), stopIntent)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun notifyIfChanged(notification: Notification) {
        val fingerprint = notificationFingerprint(notification)
        if (fingerprint == lastNotificationFingerprint) return
        lastNotificationFingerprint = fingerprint
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun notificationFingerprint(notification: Notification): String =
        "${notification.extras.getCharSequence(Notification.EXTRA_TITLE)}|" +
            "${notification.extras.getCharSequence(Notification.EXTRA_TEXT)}|" +
            "${notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT)}"

    private fun shouldShowTrafficNotification(): Boolean =
        settingsStore.decodeBool("showTrafficNotification", true)

    private fun createChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(CHANNEL_NAME)
                .build(),
        )
    }

    private fun syncStatus(status: RootTunStatus) {
        when (status.state) {
            RootTunState.Idle -> StatusProvider.markRuntimeIdle(ProxyMode.RootTun)
            RootTunState.Starting -> StatusProvider.markRuntimeStarting(ProxyMode.RootTun)
            RootTunState.Running -> StatusProvider.markRuntimeRunning(ProxyMode.RootTun)
            RootTunState.Stopping -> StatusProvider.markRuntimeStopping(ProxyMode.RootTun)
            RootTunState.Failed -> StatusProvider.markRuntimeFailed(ProxyMode.RootTun)
        }
    }

    private fun describeStatus(status: RootTunStatus): String {
        return when (status.state) {
            RootTunState.Starting -> getString(LocaleR.string.service_notification_starting)
            RootTunState.Running -> getString(LocaleR.string.service_notification_running)
            RootTunState.Stopping -> getString(LocaleR.string.service_notification_stopping)
            RootTunState.Failed -> getString(
                LocaleR.string.service_notification_failed_format,
                status.lastError ?: getString(LocaleR.string.util_error_unknown_error),
            )
            RootTunState.Idle -> getString(LocaleR.string.service_notification_stopped)
        }
    }

    private fun queryCachedTodayTrafficBytes(): Long {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastTodayTrafficRefreshAt < TODAY_TRAFFIC_REFRESH_INTERVAL_MS) {
            return cachedTodayTrafficBytes
        }
        cachedTodayTrafficBytes = runCatching { todayTrafficReader.readTodayTotalBytes() }.getOrDefault(0L)
        lastTodayTrafficRefreshAt = now
        return cachedTodayTrafficBytes
    }

    companion object {
        private const val ACTION_START = "com.amamiyakokoro.box.ROOT_TUN_SERVICE_START"
        private const val ACTION_STOP = "com.amamiyakokoro.box.ROOT_TUN_SERVICE_STOP"
        private const val NOTIFICATION_ID = 1003
        private const val CHANNEL_ID = "clash_root_tun_service"
        private const val CHANNEL_NAME = "Clash RootTun Service"
        private const val TRAFFIC_NOTIFICATION_REFRESH_INTERVAL_MS = 10_000L
        private const val TRAFFIC_NOTIFICATION_SCREEN_OFF_REFRESH_INTERVAL_MS = 30_000L
        private const val TODAY_TRAFFIC_REFRESH_INTERVAL_MS = 30_000L

        fun start(context: Context) {
            val intent = Intent(context, RootTunService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, RootTunService::class.java))
        }
    }
}
