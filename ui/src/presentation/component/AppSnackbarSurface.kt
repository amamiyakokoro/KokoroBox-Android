package com.github.yumelira.yumebox.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.yumelira.yumebox.common.util.NoticeHostRegistry
import com.github.yumelira.yumebox.common.util.NoticePresentation
import com.github.yumelira.yumebox.common.util.ToastDialogBridge
import com.github.yumelira.yumebox.presentation.theme.UiDp

private val hosts = NoticeHostRegistry()
private val LocalNoticeDepth = staticCompositionLocalOf { -1 }

/** An in-window overlay, not a dialog or focusable popup. Does not affect content measurement. */
@Composable
fun AppSnackbarSurface(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val depth = LocalNoticeDepth.current + 1
    val id = remember { hosts.newId() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state = remember { SnackbarHostState() }
    val activeHost by hosts.activeHost.collectAsState()
    val event by ToastDialogBridge.event.collectAsState()

    DisposableEffect(lifecycle, enabled, depth) {
        fun update() {
            if (enabled && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                hosts.activate(id, depth)
            } else hosts.deactivate(id)
        }
        val observer = LifecycleEventObserver { _, _ -> update() }
        lifecycle.addObserver(observer)
        update()
        onDispose {
            lifecycle.removeObserver(observer)
            hosts.deactivate(id)
        }
    }

    LaunchedEffect(event?.id, activeHost == id, enabled) {
        val notice = event
        if (enabled && activeHost == id && notice?.presentation == NoticePresentation.Snackbar) {
            state.showSnackbar(
                message = notice.message,
                duration = if (notice.longDuration) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            // Cancellation (backgrounding or a new modal) leaves the message for the next host.
            ToastDialogBridge.dismiss(notice.id)
        }
    }

    CompositionLocalProvider(LocalNoticeDepth provides depth) {
        Box(modifier) {
            content()
            if (enabled && activeHost == id) {
                Box(Modifier.matchParentSize(), contentAlignment = Alignment.BottomCenter) {
                    SnackbarHost(
                        hostState = state,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.ime)
                                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                            .padding(UiDp.dp16)
                            .widthIn(max = UiDp.dp560),
                    )
                }
            }
        }
    }
}
