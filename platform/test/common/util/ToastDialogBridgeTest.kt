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

    @Test fun routineFeedbackUsesSnackbarAndPreservesDuration() {
        showTransientNotice("Saved")
        val short = requireNotNull(ToastDialogBridge.event.value)
        assertEquals(NoticePresentation.Snackbar, short.presentation)
        assertFalse(short.longDuration)
        ToastDialogBridge.dismiss(short.id)
        showTransientNotice("Longer feedback", longDuration = true)
        assertTrue(requireNotNull(ToastDialogBridge.event.value).longDuration)
    }

    @Test fun explicitDialogsAndCopyableDiagnosticsRemainModal() {
        showToastDialog("Please read this")
        assertEquals(NoticePresentation.Dialog, requireNotNull(ToastDialogBridge.event.value).presentation)
        clearQueue()
        showTransientNotice("Failure detail", copyable = true)
        val error = requireNotNull(ToastDialogBridge.event.value)
        assertEquals(NoticePresentation.Dialog, error.presentation)
        assertTrue(error.copyable)
    }

    @Test fun mixedNotificationQueuePreservesMessagesAndPresentation() {
        showTransientNotice("Saved")
        val first = requireNotNull(ToastDialogBridge.event.value)
        showToastDialog("Failure detail", copyable = true)
        showTransientNotice("Copied")
        ToastDialogBridge.dismiss(first.id)
        val second = requireNotNull(ToastDialogBridge.event.value)
        assertEquals(NoticePresentation.Dialog, second.presentation)
        ToastDialogBridge.dismiss(second.id)
        val third = requireNotNull(ToastDialogBridge.event.value)
        assertEquals("Copied", third.message)
        assertEquals(NoticePresentation.Snackbar, third.presentation)
    }
}
