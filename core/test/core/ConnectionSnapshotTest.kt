package com.github.yumelira.yumebox.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionSnapshotTest {
    @Test
    fun nullConnectionsAreDecodedAsAnEmptyList() {
        val snapshot = Clash.decodeConnectionSnapshot(
            """{"downloadTotal":30020,"uploadTotal":10816,"connections":null,"memory":0}""",
        )

        assertEquals(30020L, snapshot.downloadTotal)
        assertEquals(10816L, snapshot.uploadTotal)
        assertTrue(snapshot.connections.isEmpty())
    }

    @Test
    fun connectionArraysContinueToDecodeNormally() {
        val snapshot = Clash.decodeConnectionSnapshot(
            """{"connections":[{"id":"connection-id","upload":12,"download":34}]}""",
        )

        assertEquals(1, snapshot.connections.size)
        assertEquals("connection-id", snapshot.connections.single().id)
        assertEquals(12L, snapshot.connections.single().upload)
        assertEquals(34L, snapshot.connections.single().download)
    }
}
