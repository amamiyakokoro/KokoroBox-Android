/*
 * This file is part of KokoroBox.
 * Licensed under the GNU Affero General Public License, version 3 or later.
 */
package com.amamiyakokoro.box.data.integration.kokoro

import kotlinx.coroutines.CancellationException
import java.util.UUID

/** Retries subscription downloads without repeating the already successful rule save. */
class KokoroRulesSubscriptionUpdater(
    private val querySubscriptions: suspend () -> List<UUID>,
    private val updateSubscription: suspend (UUID) -> Unit,
) {
    private var pending: MutableSet<UUID>? = null

    suspend fun refresh(newRevision: Boolean = false) {
        if (newRevision) pending = null
        val subscriptions = querySubscriptions()
        val remaining = if (pending == null) subscriptions.toMutableSet()
            else checkNotNull(pending).apply { retainAll(subscriptions.toSet()) }
        pending = remaining
        var failure: Exception? = null
        for (uuid in remaining.toList()) {
            try {
                updateSubscription(uuid)
                remaining.remove(uuid)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (failure == null) failure = error
            }
        }
        failure?.let { throw it }
        pending = null
    }
}
