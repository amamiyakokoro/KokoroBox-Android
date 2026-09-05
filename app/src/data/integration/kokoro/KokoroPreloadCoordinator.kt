/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.data.integration.kokoro

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex

internal class KokoroPreloadCoordinator(
    private val repository: KokoroRepository,
    private val applicationScope: CoroutineScope,
) {
    private val preloadMutex = Mutex()

    fun preloadIfAuthenticated() {
        applicationScope.launch {
            if (!preloadMutex.tryLock()) return@launch
            try {
                if (!repository.hasSession()) return@launch
                supervisorScope {
                    val account = async { runCatching { repository.getAccount() }.getOrNull() }
                    val rules = async { runCatching { repository.getRulesEditorData() } }
                    account.await()?.let { authenticatedAccount ->
                        runCatching { repository.getSubscriptionOptions(authenticatedAccount) }
                    }
                    rules.await()
                }
            } finally {
                preloadMutex.unlock()
            }
        }
    }
}
