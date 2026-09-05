package com.github.yumelira.yumebox.common.util

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ToastDialogBridgeTest {
    @Before @After fun clearQueue() {
        while (true) {
            val event = ToastDialogBridge.event.value ?: return
            ToastDialogBridge.dismiss(event.id)
        }
    }

    @Test fun ordinaryMessagesDoNotOfferCopy() {
        showToastDialog("Download complete: 4/4")
        val event = requireNotNull(ToastDialogBridge.event.value)
        assertFalse(event.copyable)
        assertEquals("Download complete: 4/4", event.message)
    }

    @Test fun copyRequiresExplicitOptInAndIsNotInferredFromText() {
        showToastDialog("Error message")
        assertFalse(requireNotNull(ToastDialogBridge.event.value).copyable)
        clearQueue()
        showToastDialog("Diagnostic detail", copyable = true)
        assertTrue(requireNotNull(ToastDialogBridge.event.value).copyable)
    }

    @Test fun queuedMessagesKeepTheirOwnActionType() {
        ToastDialogBridge.show("Failure", copyable = true)
        val first = requireNotNull(ToastDialogBridge.event.value)
        ToastDialogBridge.show("Success")
        ToastDialogBridge.dismiss(first.id)
        val second = requireNotNull(ToastDialogBridge.event.value)
        assertEquals("Success", second.message)
        assertFalse(second.copyable)
        ToastDialogBridge.dismiss(first.id)
        assertEquals(second, ToastDialogBridge.event.value)
        ToastDialogBridge.dismiss(second.id)
        assertNull(ToastDialogBridge.event.value)
    }

    @Test fun blankMessagesDoNotShowADialog() {
        ToastDialogBridge.show("  ")
        assertNull(ToastDialogBridge.event.value)
    }
}
