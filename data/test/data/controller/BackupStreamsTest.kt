package com.github.yumelira.yumebox.data.controller

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class BackupStreamsTest {
    @Test
    fun exactLimitAllowsEofAndEmptyReads() {
        val source = SizeLimitedInputStream(ByteArrayInputStream(byteArrayOf(1, 2, 3)), 3, "Backup")
        assertEquals(1, source.read())
        val buffer = ByteArray(2)
        assertEquals(2, source.read(buffer))
        assertArrayEquals(byteArrayOf(2, 3), buffer)
        assertEquals(0, source.read(buffer, 0, 0))
        assertEquals(-1, source.read())
    }

    @Test
    fun oversizedUnknownLengthInputIsRejectedBeforeBufferingWholeFile() {
        val bytes = ByteArrayInputStream(ByteArray(1000))
        val source = SizeLimitedInputStream(bytes, 8, "Backup")
        assertThrows(IOException::class.java) { source.read(ByteArray(1000)) }
        assertEquals(991, bytes.available())
    }

    @Test
    fun skipCannotBypassInputLimit() {
        val source = SizeLimitedInputStream(ByteArrayInputStream(ByteArray(100)), 8, "Backup")
        assertThrows(IOException::class.java) { source.skip(50) }
    }

    @Test
    fun outputLimitCountsBothSingleAndBulkWrites() {
        val target = ByteArrayOutputStream()
        val output = SizeLimitedOutputStream(target, 3)
        output.write(1)
        output.write(byteArrayOf(2, 3))
        assertThrows(IOException::class.java) { output.write(4) }
        assertArrayEquals(byteArrayOf(1, 2, 3), target.toByteArray())
    }

    @Test
    fun streamingJsonPreservesExistingBackupShapeAndUnicode() {
        val json = Json { prettyPrint = true }
        val backup = json.parseToJsonElement("""{"format":"YumeBoxUserSettingsBackup","version":3,"stores":{"app":{"customUserAgent":"測試"}},"assets":{"acgWallpaper":{"encoding":"base64","data":"AQID"}}}""")
        val destination = ByteArrayOutputStream()
        json.encodeToStream(JsonElement.serializer(), backup, SizeLimitedOutputStream(destination, 1024))
        val restored = json.decodeFromStream(
            JsonElement.serializer(),
            SizeLimitedInputStream(ByteArrayInputStream(destination.toByteArray()), 1024, "Backup"),
        )
        assertEquals(backup, restored)
    }

    @Test
    fun jsonDecoderRejectsOversizedPayload() {
        val content = """{"data":"${"a".repeat(200)}"}""".toByteArray()
        assertThrows(IOException::class.java) {
            Json.decodeFromStream(JsonElement.serializer(), SizeLimitedInputStream(ByteArrayInputStream(content), 32, "Backup"))
        }
    }
}
