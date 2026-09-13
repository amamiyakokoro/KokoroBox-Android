/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.amamiyakokoro.box.runtime.client.ProxyFacade
import com.amamiyakokoro.box.service.runtime.state.RuntimePhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class LabViewModel(
    application: Application,
    private val proxyFacade: ProxyFacade,
) : AndroidViewModel(application) {

    suspend fun loadRunningConfiguration(): RuntimeConfigLoadResult {
        val initialSnapshot = proxyFacade.runtimeSnapshot.value
        if (initialSnapshot.phase != RuntimePhase.Running) {
            return RuntimeConfigLoadResult.NotRunning
        }
        if (!initialSnapshot.configReady) {
            return RuntimeConfigLoadResult.NotReady
        }

        val profileUuid = initialSnapshot.profileUuid
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }
            ?: return RuntimeConfigLoadResult.NotReady
        val runtimeFile = getApplication<Application>().filesDir
            .resolve("imported")
            .resolve(profileUuid.toString())
            .resolve("runtime.yaml")

        val content = withContext(Dispatchers.IO) {
            if (!runtimeFile.isFile) {
                return@withContext null
            }
            runCatching(runtimeFile::readText).getOrNull()
        } ?: return RuntimeConfigLoadResult.Unavailable

        if (content.isBlank()) {
            return RuntimeConfigLoadResult.Empty
        }

        val currentSnapshot = proxyFacade.runtimeSnapshot.value
        if (
            currentSnapshot.phase != RuntimePhase.Running ||
            !currentSnapshot.configReady ||
            currentSnapshot.profileUuid != initialSnapshot.profileUuid ||
            currentSnapshot.generation != initialSnapshot.generation ||
            currentSnapshot.effectiveFingerprint != initialSnapshot.effectiveFingerprint
        ) {
            return RuntimeConfigLoadResult.RuntimeChanged
        }

        return RuntimeConfigLoadResult.Loaded(
            profileName = initialSnapshot.profileName.orEmpty(),
            content = content,
        )
    }
}

sealed interface RuntimeConfigLoadResult {
    data class Loaded(
        val profileName: String,
        val content: String,
    ) : RuntimeConfigLoadResult

    data object NotRunning : RuntimeConfigLoadResult
    data object NotReady : RuntimeConfigLoadResult
    data object Unavailable : RuntimeConfigLoadResult
    data object Empty : RuntimeConfigLoadResult
    data object RuntimeChanged : RuntimeConfigLoadResult
}
