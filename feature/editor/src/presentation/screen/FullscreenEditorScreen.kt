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


package com.github.yumelira.yumebox.feature.editor.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.yumelira.yumebox.common.util.toast
import com.github.yumelira.yumebox.feature.editor.presentation.component.NativeTextEditor
import com.github.yumelira.yumebox.feature.editor.presentation.format.CodeFormatter
import com.github.yumelira.yumebox.feature.editor.presentation.language.LanguageScope
import com.github.yumelira.yumebox.presentation.component.AppDialog
import com.github.yumelira.yumebox.presentation.component.DialogButtonRow
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang

@Suppress("unused")
@Composable
fun FullscreenEditorScreen(
    navigator: DestinationsNavigator,
    title: String = MLang.Editor.Common.EditConfigTitle,
    initialContent: String = "",
    language: LanguageScope = LanguageScope.Yaml,
    onSave: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val showDiscardDialog = remember { mutableStateOf(false) }
    var content by remember(initialContent, language) { mutableStateOf(initialContent) }
    val isModified = content != initialContent

    fun handleBack() {
        if (isModified) {
            showDiscardDialog.value = true
        } else {
            navigator.navigateUp()
        }
    }

    BackHandler {
        handleBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = title,
                actions = {
                    IconButton(
                        modifier = Modifier.padding(end = UiDp.dp12),
                        onClick = {
                            val formatted = CodeFormatter.format(content, language)
                            if (formatted != null && formatted != content) {
                                content = formatted
                                context.toast(MLang.Editor.Toast.FormatSuccess)
                            } else {
                                context.toast(MLang.Editor.Toast.FormatFailedOrUnchanged)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = AppMd3Icons.Editor.Format,
                            contentDescription = MLang.Editor.Action.Format
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!CodeFormatter.validate(content, language)) {
                                context.toast(MLang.Editor.Toast.SyntaxError)
                                return@IconButton
                            }
                            runCatching {
                                onSave(content)
                            }.onSuccess {
                                navigator.navigateUp()
                            }.onFailure {
                                context.toast(it.message ?: MLang.Editor.Toast.SaveFailed)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = AppMd3Icons.Editor.Save,
                            contentDescription = MLang.Editor.Action.Save
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            NativeTextEditor(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxSize(),
                language = language,
            )
        }
    }

    AppDialog(
        show = showDiscardDialog.value,
        title = MLang.Editor.Dialog.UnsavedChangesTitle,
        summary = MLang.Editor.Dialog.UnsavedChangesMessage,
        onDismissRequest = { showDiscardDialog.value = false }
    ) {
        DialogButtonRow(
            onCancel = { showDiscardDialog.value = false },
            onConfirm = {
                showDiscardDialog.value = false
                navigator.navigateUp()
            },
            cancelText = MLang.Component.Button.Cancel,
            confirmText = MLang.Editor.Action.Discard
        )
    }
}
