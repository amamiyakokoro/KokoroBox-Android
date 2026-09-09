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

package com.github.yumelira.yumebox.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import com.github.yumelira.yumebox.core.util.StartupTaskCoordinator
import com.github.yumelira.yumebox.service.runtime.records.ImportedDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/** A persisted periodic job survives both failed downloads and process restarts. */
class ProfileUpdateJobService : JobService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var updateJob: Job? = null

    override fun onStartJob(params: JobParameters): Boolean {
        updateJob = scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    StartupTaskCoordinator.awaitRuntimeWarmup()
                    for (uuid in ImportedDao.queryAllUUIDs()) {
                        ensureActive()
                        try {
                            ProfileProcessor.update(applicationContext, uuid, null, onlyIfDue = true)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            // One failed subscription must not abort the remaining updates.
                            Timber.w(e, "Automatic profile update failed: $uuid")
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Automatic profile update job failed")
            }
            ensureActive()
            // Completing this run retains the periodic job, even after failures.
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        updateJob?.cancel()
        updateJob = null
        return true
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val JOB_ID = 0x505255
        private const val CHECK_INTERVAL_MILLIS = 15 * 60 * 1000L

        fun ensureScheduled(context: Context) {
            val scheduler = context.getSystemService(JobScheduler::class.java)
            // Do not reset the pending run every time the app process starts, but replace the
            // pre-optimization job once so installed users also receive the battery constraint.
            if (scheduler.getPendingJob(JOB_ID)?.isRequireBatteryNotLow == true) return
            val job = JobInfo.Builder(JOB_ID, ComponentName(context, ProfileUpdateJobService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setRequiresBatteryNotLow(true)
                .setPeriodic(CHECK_INTERVAL_MILLIS)
                .setPersisted(true)
                .build()
            if (scheduler.schedule(job) != JobScheduler.RESULT_SUCCESS) {
                Timber.w("Could not schedule automatic profile updates")
            }
        }
    }
}
