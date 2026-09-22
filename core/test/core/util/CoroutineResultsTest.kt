/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.core.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoroutineResultsTest {
    @Test(expected = CancellationException::class)
    fun cancellationIsNeverConvertedToFailure() = runBlocking {
        runCatchingCancellable<Unit> { throw CancellationException("stopped") }
        Unit
    }

    @Test
    fun ordinaryFailureIsCaptured() = runBlocking {
        val result = runCatchingCancellable<Unit> { error("failed") }

        assertTrue(result.isFailure)
        assertEquals("failed", result.exceptionOrNull()?.message)
    }
}
