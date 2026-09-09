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



package com.amamiyakokoro.box.runtime.client

import com.amamiyakokoro.box.data.model.ProxyMode
import com.amamiyakokoro.box.service.runtime.state.RuntimeOwner
import com.amamiyakokoro.box.service.runtime.state.RuntimePhase
import com.amamiyakokoro.box.service.runtime.state.RuntimeSnapshot

object RuntimeStateMapper {
    fun isRunningOrStarting(snapshot: RuntimeSnapshot): Boolean {
        return snapshot.phase == RuntimePhase.Starting || snapshot.phase == RuntimePhase.Running
    }

    fun isActuallyRunning(snapshot: RuntimeSnapshot): Boolean {
        return snapshot.phase == RuntimePhase.Running
    }

    fun modeForOwner(owner: RuntimeOwner): ProxyMode? {
        return when (owner) {
            RuntimeOwner.LocalTun -> ProxyMode.Tun
            RuntimeOwner.LocalHttp -> ProxyMode.Http
            RuntimeOwner.RootTun -> ProxyMode.RootTun
            RuntimeOwner.None -> null
        }
    }

    fun resolveDisplayMode(snapshot: RuntimeSnapshot, configuredMode: ProxyMode): ProxyMode {
        return if (isRunningOrStarting(snapshot)) {
            modeForOwner(snapshot.owner) ?: configuredMode
        } else {
            configuredMode
        }
    }

    fun idleSnapshot(
        configuredMode: ProxyMode,
        generation: Long = 0L,
        lastError: String? = null,
    ): RuntimeSnapshot {
        return RuntimeSnapshot(
            owner = RuntimeOwner.None,
            phase = if (lastError.isNullOrBlank()) RuntimePhase.Idle else RuntimePhase.Failed,
            targetMode = configuredMode,
            lastError = lastError,
            generation = generation,
        )
    }
}
