package com.amamiyakokoro.box.data.store

import com.amamiyakokoro.box.core.model.DnsPresetMode
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

class DnsPresetSelectionTest {
    private fun preference(initial: Boolean, onChange: () -> Unit = {}): Preference<Boolean> {
        val flow = MutableStateFlow(initial)
        return Preference(state = flow, update = { flow.value = it; onChange() }, get = { flow.value })
    }

    @Test fun everyPresetTransitionMaintainsExclusivityAtEveryWrite() {
        for (from in DnsPresetMode.entries) for (to in DnsPresetMode.entries) {
            lateinit var anti: Preference<Boolean>
            lateinit var overseas: Preference<Boolean>
            val checkExclusive = { assertFalse("$from → $to", anti.value && overseas.value) }
            anti = preference(from == DnsPresetMode.AntiPollution, checkExclusive)
            overseas = preference(from == DnsPresetMode.Overseas, checkExclusive)
            val changed = setDnsPresetPreferences(anti, overseas, to)
            assertEquals(to == DnsPresetMode.AntiPollution, anti.value)
            assertEquals(to == DnsPresetMode.Overseas, overseas.value)
            assertEquals(from != to, changed)
        }
    }

    @Test fun selectingActivePresetDoesNotWriteOrRequestAnotherRestart() {
        var writes = 0
        val anti = preference(false) { writes++ }
        val overseas = preference(true) { writes++ }
        assertFalse(setDnsPresetPreferences(anti, overseas, DnsPresetMode.Overseas))
        assertEquals(0, writes)
    }

    @Test fun closingBothRestoresSubscriptionDnsSelection() {
        val anti = preference(false)
        val overseas = preference(true)
        assertTrue(setDnsPresetPreferences(anti, overseas, DnsPresetMode.None))
        assertFalse(anti.value)
        assertFalse(overseas.value)
        assertEquals(DnsPresetMode.None, DnsPresetMode.fromFlags(anti.value, overseas.value))
    }

    @Test fun conflictingImportedFlagsAreNormalizedToOnePreset() {
        val anti = preference(true)
        val overseas = preference(true)
        val mode = DnsPresetMode.fromFlags(anti.value, overseas.value)
        assertEquals(DnsPresetMode.Overseas, mode)
        assertTrue(setDnsPresetPreferences(anti, overseas, mode))
        assertFalse(anti.value)
        assertTrue(overseas.value)
    }

    @Test fun oldBackupWithoutOverseasFlagKeepsItsOriginalAntiPollutionChoice() {
        assertEquals(DnsPresetMode.AntiPollution, DnsPresetMode.fromFlags(true, false))
        assertEquals(DnsPresetMode.None, DnsPresetMode.fromFlags(false, false))
    }
}
