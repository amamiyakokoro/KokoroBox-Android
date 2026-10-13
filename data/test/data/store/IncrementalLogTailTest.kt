package com.amamiyakokoro.box.data.store

import java.nio.file.Files
import org.junit.Assert.*
import org.junit.Test

class IncrementalLogTailTest {
    private fun withFile(block: (java.io.File) -> Unit) {
        val dir = Files.createTempDirectory("log-tail").toFile()
        try { block(dir.resolve("log.txt").apply { writeText("") }) } finally { dir.deleteRecursively() }
    }
    @Test fun repeatedReadsReuseTheSnapshotAndParseOnlyNewLines() = withFile { file ->
        var parsed = 0
        val tail = IncrementalLogTail { line: String -> parsed++; line }
        file.writeText("a\nb\n")
        val original = tail.read(file, 10)
        val calls = parsed
        assertSame(original, tail.read(file, 10))
        assertEquals(calls, parsed)
        file.appendText("c\n")
        assertEquals(listOf("a", "b", "c"), tail.read(file, 10))
        assertEquals(calls + 1, parsed)
    }
    @Test fun coldReadSeeksToRecentValidEntries() = withFile { file ->
        file.bufferedWriter().use { writer ->
            repeat(10_000) { writer.write("keep:$it\nignored\n") }
        }
        var calls = 0
        val tail = IncrementalLogTail { line: String -> calls++; line.takeIf { it.startsWith("keep:") } }
        assertEquals((9996..9999).map { "keep:$it" }, tail.read(file, 4))
        assertTrue("Cold read parsed $calls lines", calls < 40)
    }
    @Test fun incompleteUtf8LineWaitsForItsTerminator() = withFile { file ->
        val tail = IncrementalLogTail { line: String -> line }
        file.writeText("first\n東京")
        assertEquals(listOf("first"), tail.read(file, 10))
        file.appendText(" 香港\n")
        assertEquals(listOf("first", "東京 香港"), tail.read(file, 10))
    }
    @Test fun ringRetainsTheNewestEntriesAndCanIncreaseCapacity() = withFile { file ->
        val tail = IncrementalLogTail { line: String -> line }
        file.writeText("a\nb\nc\n")
        assertEquals(listOf("b", "c"), tail.read(file, 2))
        file.appendText("d\n")
        assertEquals(listOf("c", "d"), tail.read(file, 2))
        assertEquals(listOf("a", "b", "c", "d"), tail.read(file, 10))
    }
    @Test fun truncationAndFileRotationResetTheCache() = withFile { file ->
        val tail = IncrementalLogTail { line: String -> line }
        file.writeText("old-long-entry\n")
        tail.read(file, 10)
        file.writeText("new\n")
        assertEquals(listOf("new"), tail.read(file, 10))
        val replacement = file.parentFile.resolve("replacement.txt").apply { writeText("rotated\n") }
        assertEquals(listOf("rotated"), tail.read(replacement, 10))
    }
    @Test fun zeroCapacityIsEmpty() = withFile { file ->
        assertTrue(IncrementalLogTail { line: String -> line }.read(file, 0).isEmpty())
    }
}
