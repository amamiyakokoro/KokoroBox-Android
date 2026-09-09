/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.github.yumelira.yumebox.core.util.StartupTaskCoordinator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import kotlin.coroutines.coroutineContext

/**
 * Defers non-essential provider downloads that follow a successful Kokoro import. The imported
 * profile is usable immediately; missing providers can also be fetched by a later manual update.
 */
class KokoroProviderPrefetchJobService : JobService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefetchJobs = mutableMapOf<Int, Job>()

    override fun onStartJob(params: JobParameters): Boolean {
        val uuid = params.extras.getString(EXTRA_PROFILE_ID)
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: return false
        prefetchJobs.remove(params.jobId)?.cancel()
        prefetchJobs[params.jobId] = scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    StartupTaskCoordinator.awaitRuntimeWarmup()
                    ProfileProcessor.prefetchKokoroProviders(applicationContext, uuid)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Prefetching is best-effort. A manual profile/provider update can retry it.
                Timber.w(e, "Deferred Kokoro provider prefetch failed: $uuid")
            } finally {
                val currentJob = coroutineContext[Job]
                if (prefetchJobs[params.jobId] === currentJob) {
                    prefetchJobs.remove(params.jobId)
                }
            }
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        prefetchJobs.remove(params.jobId)?.cancel()
        return true
    }

    override fun onDestroy() {
        prefetchJobs.values.forEach(Job::cancel)
        prefetchJobs.clear()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_PROFILE_ID = "profile_id"
        private const val JOB_ID_SALT = 0x4B505246
        private const val MINIMUM_LATENCY_MILLIS = 5 * 60 * 1000L

        fun schedule(context: Context, uuid: UUID) {
            val scheduler = context.getSystemService(JobScheduler::class.java) ?: return
            val extras = PersistableBundle().apply { putString(EXTRA_PROFILE_ID, uuid.toString()) }
            val job = JobInfo.Builder(jobId(uuid), ComponentName(context, KokoroProviderPrefetchJobService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .setMinimumLatency(MINIMUM_LATENCY_MILLIS)
                .setPersisted(true)
                .setExtras(extras)
                .build()
            if (scheduler.schedule(job) != JobScheduler.RESULT_SUCCESS) {
                Timber.w("Could not schedule Kokoro provider prefetch: $uuid")
            }
        }

        fun cancel(context: Context, uuid: UUID) {
            context.getSystemService(JobScheduler::class.java)?.cancel(jobId(uuid))
        }

        private fun jobId(uuid: UUID): Int = uuid.hashCode() xor JOB_ID_SALT
    }
}
