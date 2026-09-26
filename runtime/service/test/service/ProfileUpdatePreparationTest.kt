package com.amamiyakokoro.box.service

import java.nio.file.Files
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class ProfileUpdatePreparationTest {
    @Test fun cancellationWaitsForWriterAndPreservesCommittedConfig() = runBlocking {
        val root = Files.createTempDirectory("profile-update").toFile()
        try {
            val config = root.resolve("config.yaml").apply { writeText("old") }
            val staging = root.resolve("staging.yaml")
            val started = CompletableDeferred<Unit>()
            val finish = CompletableDeferred<Unit>()
            var cleaned = false
            val job = launch {
                try {
                    awaitProfilePreparation {
                        started.complete(Unit)
                        finish.await()
                        staging.writeText("new")
                    }
                    staging.copyTo(config, overwrite = true)
                } finally {
                    staging.delete()
                    cleaned = true
                }
            }
            started.await()
            job.cancel()
            yield()
            assertFalse(cleaned)
            finish.complete(Unit)
            job.join()
            assertEquals("old", config.readText())
            assertFalse(staging.exists())
            assertTrue(cleaned)
        } finally { root.deleteRecursively() }
    }

    @Test fun successfulPreparationReturnsCommitValue() = runBlocking {
        assertEquals("new", awaitProfilePreparation { "new" })
    }

    @Test fun alreadyCancelledUpdateNeverStartsPreparation() = runBlocking {
        var started = false
        val job = launch {
            currentCoroutineContext().cancel()
            awaitProfilePreparation { started = true }
        }
        job.join()
        assertFalse(started)
    }
}
