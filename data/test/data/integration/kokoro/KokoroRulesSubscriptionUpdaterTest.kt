package com.amamiyakokoro.box.data.integration.kokoro

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.util.UUID

class KokoroRulesSubscriptionUpdaterTest {
    private val first = UUID(0, 1)
    private val second = UUID(0, 2)

    @Test fun updatesAllSubscriptionsAndRetriesOnlyFailures() = runBlocking {
        val attempts = mutableListOf<UUID>()
        var fail = true
        val updater = KokoroRulesSubscriptionUpdater({ listOf(first, second) }) {
            attempts += it
            if (it == first && fail) throw IOException("offline")
        }
        assertTrue(runCatching { updater.refresh(newRevision = true) }.exceptionOrNull() is IOException)
        assertEquals(listOf(first, second), attempts)
        fail = false
        updater.refresh()
        assertEquals(listOf(first, second, first), attempts)
        updater.refresh(newRevision = true)
        assertEquals(listOf(first, second, first, first, second), attempts)
    }

    @Test fun deletedSubscriptionsAreRemovedFromRetryQueue() = runBlocking {
        var subscriptions = listOf(first, second)
        val attempts = mutableListOf<UUID>()
        val updater = KokoroRulesSubscriptionUpdater({ subscriptions }) {
            attempts += it
            if (it == first) throw IOException("offline")
        }
        runCatching { updater.refresh(newRevision = true) }
        subscriptions = listOf(second)
        updater.refresh()
        assertEquals(listOf(first, second), attempts)
    }

    @Test fun queryFailureCanBeRetriedWithoutLosingWork() = runBlocking {
        var queryFails = true
        val attempts = mutableListOf<UUID>()
        val updater = KokoroRulesSubscriptionUpdater({
            if (queryFails) throw IOException("service unavailable")
            listOf(first)
        }) { attempts += it }
        assertTrue(runCatching { updater.refresh(newRevision = true) }.exceptionOrNull() is IOException)
        assertTrue(attempts.isEmpty())
        queryFails = false
        updater.refresh()
        assertEquals(listOf(first), attempts)
    }

    @Test fun cancellationIsPropagatedAndDoesNotStartOtherDownloads() = runBlocking {
        val attempts = mutableListOf<UUID>()
        val updater = KokoroRulesSubscriptionUpdater({ listOf(first, second) }) {
            attempts += it
            throw CancellationException("closed")
        }
        assertTrue(runCatching { updater.refresh(newRevision = true) }.exceptionOrNull() is CancellationException)
        assertEquals(listOf(first), attempts)
    }

    @Test fun newRevisionResetsRetryQueueEvenIfQueryFails() = runBlocking {
        var queryFails = false
        var downloadFails = true
        val attempts = mutableListOf<UUID>()
        val updater = KokoroRulesSubscriptionUpdater({
            if (queryFails) throw IOException("service unavailable")
            listOf(first, second)
        }) {
            attempts += it
            if (it == first && downloadFails) throw IOException("offline")
        }
        runCatching { updater.refresh(newRevision = true) }
        queryFails = true
        runCatching { updater.refresh(newRevision = true) }
        queryFails = false
        downloadFails = false
        updater.refresh()
        assertEquals(listOf(first, second, first, second), attempts)
    }

    @Test fun noKokoroSubscriptionsRequiresNoDownload() = runBlocking {
        val updater = KokoroRulesSubscriptionUpdater({ emptyList() }) { fail("unexpected download") }
        updater.refresh(newRevision = true)
    }
}
