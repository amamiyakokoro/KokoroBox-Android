package com.github.yumelira.yumebox.common.util

import org.junit.Assert.*
import org.junit.Test

class NoticeHostRegistryTest {
    @Test fun modalTakesPriorityEvenWhenRootRegistersLater() {
        val registry = NoticeHostRegistry()
        val root = registry.newId()
        val sheet = registry.newId()
        registry.activate(sheet, 1)
        registry.activate(root, 0)
        assertEquals(sheet, registry.activeHost.value)
        registry.deactivate(sheet)
        assertEquals(root, registry.activeHost.value)
    }

    @Test fun nestedAndSiblingWindowsSelectOnlyTheTopHost() {
        val registry = NoticeHostRegistry()
        val sheet = registry.newId()
        val sibling = registry.newId()
        val dialog = registry.newId()
        registry.activate(sheet, 1)
        registry.activate(sibling, 1)
        assertEquals(sibling, registry.activeHost.value)
        registry.activate(dialog, 2)
        assertEquals(dialog, registry.activeHost.value)
        registry.deactivate(dialog)
        assertEquals(sibling, registry.activeHost.value)
        registry.deactivate(sibling)
        assertEquals(sheet, registry.activeHost.value)
    }

    @Test fun backgroundHostsCannotConsumeNotifications() {
        val registry = NoticeHostRegistry()
        val root = registry.newId()
        registry.activate(root, 0)
        registry.deactivate(root)
        registry.deactivate(root)
        assertNull(registry.activeHost.value)
        registry.activate(root, 0)
        assertEquals(root, registry.activeHost.value)
    }
}
