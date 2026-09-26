package com.amamiyakokoro.box.data.store

import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class OverrideConfigBackupTest {
    @Test fun rejectsUnsafeIdsBeforeWritingAnyEntries() = runBlocking {
        val root = Files.createTempDirectory("override-backup").toFile()
        try {
            val store = OverrideConfigStore(root)
            val victim = File(root, "victim.json").apply { writeText("original") }
            for (id in listOf("../../victim", victim.absolutePath.removeSuffix(".json"), "..\\victim", "", "./__runtime__-profile")) {
                try {
                    store.importUserConfigBackup(listOf(entry("cfg-valid"), entry(id)))
                    fail("Accepted unsafe ID: $id")
                } catch (_: IllegalArgumentException) {
                    assertEquals("original", victim.readText())
                    assertFalse(store.getConfigFilePath("cfg-valid").exists())
                    assertFalse(File(root, "overrides/metadata.json").exists())
                }
            }
        } finally { root.deleteRecursively() }
    }

    @Test fun rejectsSymlinkOutsideConfigDirectory() = runBlocking {
        val root = Files.createTempDirectory("override-backup").toFile()
        try {
            val store = OverrideConfigStore(root)
            store.getConfigsDirectory().mkdirs()
            val victim = File(root, "victim.json").apply { writeText("original") }
            Files.createSymbolicLink(store.getConfigFilePath("cfg-link").toPath(), victim.toPath())
            try {
                store.importUserConfigBackup(listOf(entry("cfg-link")))
                fail("Accepted escaping symlink")
            } catch (_: IllegalArgumentException) {
                assertEquals("original", victim.readText())
            }
        } finally { root.deleteRecursively() }
    }

    @Test fun importsValidBackupButKeepsReservedConfigsUntouched() = runBlocking {
        val root = Files.createTempDirectory("override-backup").toFile()
        try {
            val store = OverrideConfigStore(root)
            store.importUserConfigBackup(listOf(entry("cfg-123-4567"), entry("__runtime__-profile"), entry("preset-default")))
            assertNotNull(store.getById("cfg-123-4567"))
            assertFalse(store.getConfigFilePath("__runtime__-profile").exists())
            assertFalse(store.getConfigFilePath("preset-default").exists())
        } finally { root.deleteRecursively() }
    }

    private fun entry(id: String) = OverrideConfigBackupEntry(
        id = id, name = "Imported config", createdAt = 1L, updatedAt = 1L, content = "{}",
    )
}
