/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.runtime.client

import com.amamiyakokoro.box.data.model.ProxyMode
import com.amamiyakokoro.box.service.LocalRuntimePhase
import com.amamiyakokoro.box.service.root.RootTunState
import com.amamiyakokoro.box.service.root.RootTunStatus
import com.amamiyakokoro.box.service.runtime.state.RuntimeOwner
import com.amamiyakokoro.box.service.runtime.state.RuntimePhase
import com.amamiyakokoro.box.service.runtime.state.RuntimeSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxyRuntimeOwnershipTest {
    @Test
    fun runningLocalRuntimeMarksCompiledConfigurationReady() {
        val snapshot = ProxyRuntimeOwnership.activeSnapshot(
            owner = RuntimeOwner.LocalTun,
            configuredMode = ProxyMode.Tun,
            rootStatus = RootTunStatus(),
            localPhase = LocalRuntimePhase.Running,
        )

        assertEquals(RuntimePhase.Running, snapshot.phase)
        assertTrue(snapshot.configReady)
    }

    @Test
    fun startingLocalRuntimeDoesNotMarkConfigurationReady() {
        val snapshot = ProxyRuntimeOwnership.activeSnapshot(
            owner = RuntimeOwner.LocalHttp,
            configuredMode = ProxyMode.Http,
            rootStatus = RootTunStatus(),
            localPhase = LocalRuntimePhase.Starting,
        )

        assertFalse(snapshot.configReady)
    }

    @Test
    fun rootRuntimeUsesRemoteReadinessAndFingerprint() {
        val snapshot = ProxyRuntimeOwnership.activeSnapshot(
            owner = RuntimeOwner.RootTun,
            configuredMode = ProxyMode.RootTun,
            rootStatus = RootTunStatus(
                state = RootTunState.Running,
                runtimeReady = true,
                overrideFingerprint = "compiled-fingerprint",
            ),
        )

        assertTrue(snapshot.configReady)
        assertEquals("compiled-fingerprint", snapshot.effectiveFingerprint)
    }

    @Test
    fun startedEventMarksConfigurationReady() {
        val snapshot = ProxyRuntimeOwnership.startedSnapshot(
            current = RuntimeSnapshot(
                owner = RuntimeOwner.LocalTun,
                phase = RuntimePhase.Starting,
            ),
            owner = RuntimeOwner.LocalTun,
            configuredMode = ProxyMode.Tun,
        )

        assertTrue(snapshot.configReady)
    }
}
