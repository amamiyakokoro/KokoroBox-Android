/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c)  YumeLira 2025 - Present
 *
 */



package com.github.yumelira.yumebox.common.util

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.atomic.AtomicLong

enum class NoticePresentation { Dialog, Snackbar }

data class ToastDialogEvent(
    val id: Long,
    val title: String,
    val message: String,
    val copyable: Boolean = false,
    val presentation: NoticePresentation = NoticePresentation.Dialog,
    val longDuration: Boolean = false,
)

object ToastDialogBridge {
    private val nextId = AtomicLong(1L)
    private val queue = ArrayDeque<ToastDialogEvent>()
    private val lock = Any()
    private val _event = MutableStateFlow<ToastDialogEvent?>(null)
    val event: StateFlow<ToastDialogEvent?> = _event.asStateFlow()

    fun show(
        message: String,
        title: String = "",
        copyable: Boolean = false,
        presentation: NoticePresentation = NoticePresentation.Dialog,
        longDuration: Boolean = false,
    ) {
        if (message.isBlank()) return

        val event = ToastDialogEvent(
            id = nextId.getAndIncrement(),
            title = title,
            message = message,
            copyable = copyable,
            presentation = if (copyable) NoticePresentation.Dialog else presentation,
            longDuration = longDuration,
        )

        synchronized(lock) {
            if (_event.value == null) {
                _event.value = event
            } else {
                queue.addLast(event)
            }
        }
    }

    fun dismiss(eventId: Long) {
        synchronized(lock) {
            if (_event.value?.id == eventId) {
                _event.value = if (queue.isEmpty()) null else queue.removeFirst()
            } else {
                queue.removeAll { it.id == eventId }
            }
        }
    }
}

fun showToastDialog(message: String, title: String = "", copyable: Boolean = false) {
    ToastDialogBridge.show(message = message, title = title, copyable = copyable)
}

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT, copyable: Boolean = false) {
    showTransientNotice(message, longDuration = duration == Toast.LENGTH_LONG, copyable = copyable)
}

/** Routine feedback is transient; diagnostics explicitly requesting Copy remain modal. */
fun showTransientNotice(message: String, longDuration: Boolean = false, copyable: Boolean = false) {
    ToastDialogBridge.show(message, copyable = copyable,
        presentation = NoticePresentation.Snackbar, longDuration = longDuration)
}

@Composable
fun ShowToast(message: String) {
    LaunchedEffect(message) {
        showTransientNotice(message)
    }
}
