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

package com.amamiyakokoro.box.data.controller

import com.amamiyakokoro.box.core.util.PollingTimerSpecs
import com.amamiyakokoro.box.core.util.PollingTimers
import com.amamiyakokoro.box.data.model.AccessControlMode
import com.amamiyakokoro.box.data.model.ProxyMode
import com.amamiyakokoro.box.data.store.NetworkSettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AccessControlController(
    private val store: NetworkSettingsStore,
    private val isRunning: () -> Boolean,
    private val resolveActiveMode: () -> ProxyMode?,
    private val restartProxy: suspend (ProxyMode) -> Unit,
    private val beforeRestart: suspend (ProxyMode) -> Unit = {},
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var applyJob: Job? = null

    fun setAccessControlMode(mode: AccessControlMode) {
        if (store.accessControlMode.value == mode) return
        store.accessControlMode.set(mode)
        scheduleApply()
    }

    fun applyPackages(packages: Set<String>) {
        if (store.accessControlPackages.value == packages) return
        store.accessControlPackages.set(packages)
        scheduleApply()
    }

    private fun scheduleApply() {
        applyJob?.cancel()
        applyJob = scope.launch {
            PollingTimers.awaitTick(
                PollingTimerSpecs.dynamic(
                    name = "access_control_apply_packages",
                    intervalMillis = 350L,
                    initialDelayMillis = 350L,
                ),
            )

            if (!isRunning()) return@launch
            val activeMode = resolveActiveMode()
            val targetMode = activeMode ?: store.proxyMode.value
            if (targetMode == ProxyMode.Http) return@launch
            if (store.accessControlMode.value == AccessControlMode.ALLOW_ALL) return@launch

            try {
                beforeRestart(targetMode)
                restartProxy(targetMode)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
            }
        }
    }
}
