package com.amamiyakokoro.box.service

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Native validation continues writing staging files until its callback completes. Wait for it
 * before releasing the staging directory/lock, but never commit a cancelled caller's result.
 */
internal suspend fun <T> awaitProfilePreparation(prepare: suspend () -> T): T {
    currentCoroutineContext().ensureActive()
    val result = withContext(NonCancellable) { prepare() }
    currentCoroutineContext().ensureActive()
    return result
}
