package com.github.yumelira.yumebox.integration.update

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.github.yumelira.yumebox.BuildConfig
import com.github.yumelira.yumebox.data.integration.update.AutomaticUpdateCheckPolicy
import com.github.yumelira.yumebox.data.integration.update.GitHubReleaseClient
import com.github.yumelira.yumebox.data.integration.update.ReleaseCheck
import com.github.yumelira.yumebox.data.integration.update.isNewerThan
import com.github.yumelira.yumebox.data.store.AppSettingsStore
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit

/** Schedules metadata-only checks; APK download and installation always remain user initiated. */
object AppUpdateWorkScheduler {
    fun sync(context: Context, enabled: Boolean) {
        val workManager = WorkManager.getInstance(context)
        if (!enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<AppUpdateCheckWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private const val WORK_NAME = "automatic-app-update-check"
}

class AppUpdateCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val koin = GlobalContext.get()
        val settings = koin.get<AppSettingsStore>()
        if (!settings.initialSetupCompleted.value || !settings.automaticUpdateCheckEnabled.value) {
            return Result.success()
        }
        val now = System.currentTimeMillis()
        if (!AutomaticUpdateCheckPolicy.isDue(settings.lastAutomaticUpdateCheckAtMillis, now)) {
            return Result.success()
        }
        settings.lastAutomaticUpdateCheckAtMillis = now
        val release = runCatching {
            koin.get<GitHubReleaseClient>().check(settings.appUpdateChannel.value) as? ReleaseCheck.Published
        }.getOrNull() ?: return Result.success()
        if (release.isNewerThan(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)) {
            koin.get<AppUpdateInstallNotifier>().showAvailableUpdate(release.tag)
        }
        return Result.success()
    }
}
