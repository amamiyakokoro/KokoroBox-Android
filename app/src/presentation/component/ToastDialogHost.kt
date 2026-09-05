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



package com.github.yumelira.yumebox.presentation.component

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.yumelira.yumebox.common.util.ToastDialogBridge
import com.github.yumelira.yumebox.common.util.ToastDialogEvent
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import dev.oom_wg.purejoy.mlang.MLang

@Composable
fun ToastDialogHost() {
    val opacity = AppTheme.opacity
    val radii = AppTheme.radii
    val spacing = AppTheme.spacing

    val event by ToastDialogBridge.event.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var eventSnapshot by remember { mutableStateOf<ToastDialogEvent?>(null) }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        if (event != null) {
            eventSnapshot = event
            showDialog.value = true
        }
    }

    eventSnapshot?.let { snapshot ->
        val localizedTitle = snapshot.title.ifBlank {
            if (snapshot.copyable) MLang.Component.Message.Error else MLang.Component.Message.Hint
        }
        AppDialog(
            show = showDialog.value,
            modifier = Modifier,
            title = localizedTitle,
            summary = snapshot.message,
            enableWindowDim = true,
            onDismissRequest = { showDialog.value = false },
            onDismissFinished = {
                ToastDialogBridge.dismiss(snapshot.id)
                eventSnapshot = null
                showDialog.value = false
            },
            outsideMargin = AppDialogDefaults.outsideMargin,
            insideMargin = AppDialogDefaults.insideMargin,
            defaultWindowInsetsPadding = true,
            content = {
                Column {
                    if (snapshot.copyable) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText(localizedTitle, snapshot.message))
                            },
                        ) { Text(MLang.Component.Button.Copy) }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = opacity.subtleStrong),
                                shape = RoundedCornerShape(radii.radius16),
                            )
                            .clickable { showDialog.value = false }
                            .padding(vertical = spacing.space14),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = MLang.Component.Button.Ok,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            })
    }
}
