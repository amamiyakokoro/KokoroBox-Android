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
import com.amamiyakokoro.box.data.model.ProxyMode
import com.amamiyakokoro.box.data.store.NetworkSettingsStore
import com.amamiyakokoro.box.data.store.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NetworkSettingsController(
    private val store: NetworkSettingsStore,
    private val isRunning: () -> Boolean,
    private val restartProxy: suspend (ProxyMode) -> Unit,
    private val beforeRestart: suspend (ProxyMode) -> Unit = {},
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var restartJob: Job? = null

    fun setProxyMode(mode: ProxyMode) {
        store.proxyMode.set(mode)
    }

    fun <T> setAndRestartIfNeeded(
        preference: Preference<T>,
        value: T,
    ) {
        if (preference.value == value) return
        preference.set(value)
        scheduleRestart()
    }

    fun <T> commitDraftAndRestart(
        preference: Preference<T>,
        value: T,
    ) {
        setAndRestartIfNeeded(preference, value)
    }

    suspend fun startService(mode: ProxyMode): Result<Unit> = runCatching {
        store.proxyMode.set(mode)
        beforeRestart(mode)
        withContext(Dispatchers.IO) {
            restartProxy(mode)
        }
    }

    fun requestRestartIfRunning() {
        scheduleRestart()
    }

    private fun scheduleRestart() {
        restartJob?.cancel()
        restartJob = scope.launch {
            PollingTimers.awaitTick(
                PollingTimerSpecs.dynamic(
                    name = "network_settings_restart_debounce",
                    intervalMillis = RESTART_DEBOUNCE_DELAY_MS,
                    initialDelayMillis = RESTART_DEBOUNCE_DELAY_MS,
                ),
            )
            if (!isRunning()) return@launch
            val targetMode = store.proxyMode.value
            beforeRestart(targetMode)
            startService(targetMode)
        }
    }

    companion object {
        private const val RESTART_DEBOUNCE_DELAY_MS = 300L
    }
}
