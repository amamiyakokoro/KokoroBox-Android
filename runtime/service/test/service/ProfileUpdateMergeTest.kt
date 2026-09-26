package com.amamiyakokoro.box.service

import com.amamiyakokoro.box.service.runtime.entity.Imported
import com.amamiyakokoro.box.service.runtime.entity.Profile
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test

class ProfileUpdateMergeTest {
    private val before = Imported(
        uuid = UUID.randomUUID(), name = "New Profile", type = Profile.Type.Url,
        source = "https://example.com/subscription", interval = 1L,
        upload = 0L, download = 0L, total = 0L, expire = 0L, createdAt = 1L,
    )
    private val downloaded = before.copy(
        name = "Server title", interval = 24L, upload = 2L, download = 3L,
        total = 100L, expire = 200L, lastUpdateAttemptAt = 50L, lastUpdateFailed = false,
    )

    @Test fun preservesConcurrentNameAndIntervalEditsWhileUpdatingUsage() {
        val current = before.copy(name = "My profile", interval = 0L, lastUpdateFailed = true)
        val merged = requireNotNull(mergeProfileUpdate(before, current, downloaded))
        assertEquals("My profile", merged.name)
        assertEquals(0L, merged.interval)
        assertEquals(2L, merged.upload)
        assertEquals(3L, merged.download)
        assertEquals(100L, merged.total)
        assertEquals(200L, merged.expire)
        assertEquals(50L, merged.lastUpdateAttemptAt)
        assertFalse(merged.lastUpdateFailed)
    }

    @Test fun appliesServerDefaultsWhenUserDidNotEditFields() {
        assertEquals(downloaded, mergeProfileUpdate(before, before, downloaded))
    }

    @Test fun rejectsResultsForChangedDownloadSettings() {
        for (current in listOf(
            before.copy(source = "https://example.com/new"),
            before.copy(userAgent = "NewAgent"),
            before.copy(type = Profile.Type.File),
        )) {
            assertNull(mergeProfileUpdate(before, current, downloaded))
        }
    }

    @Test fun metadataEditsDoNotInvalidateAnOtherwiseCurrentDownload() {
        assertTrue(hasSameProfileDownloadSettings(before, before.copy(name = "renamed", interval = 0L)))
    }
}
