/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */


package com.amamiyakokoro.box.feature.editor.presentation.screen

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
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.common.util.toast
import com.amamiyakokoro.box.feature.editor.presentation.component.NativeTextEditor
import com.amamiyakokoro.box.feature.editor.presentation.format.CodeFormatter
import com.amamiyakokoro.box.feature.editor.presentation.language.LanguageScope
import com.amamiyakokoro.box.presentation.component.AppDialog
import com.amamiyakokoro.box.presentation.component.DialogButtonRow
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.UiDp
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Suppress("unused")
@Composable
fun FullscreenEditorScreen(
    navigator: DestinationsNavigator,
    title: String? = null,
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
                title = title ?: stringResource(LocaleR.string.editor_common_edit_config_title),
                actions = {
                    IconButton(
                        modifier = Modifier.padding(end = UiDp.dp12),
                        onClick = {
                            val formatted = CodeFormatter.format(content, language)
                            if (formatted != null && formatted != content) {
                                content = formatted
                                context.toast(context.getString(LocaleR.string.editor_toast_format_success))
                            } else {
                                context.toast(context.getString(LocaleR.string.editor_toast_format_failed_or_unchanged))
                            }
                        },
                    ) {
                        Icon(
                            imageVector = AppMd3Icons.Editor.Format,
                            contentDescription = stringResource(LocaleR.string.editor_action_format)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!CodeFormatter.validate(content, language)) {
                                context.toast(context.getString(LocaleR.string.editor_toast_syntax_error))
                                return@IconButton
                            }
                            runCatching {
                                onSave(content)
                            }.onSuccess {
                                navigator.navigateUp()
                            }.onFailure {
                                context.toast(it.message ?: context.getString(LocaleR.string.editor_toast_save_failed))
                            }
                        },
                    ) {
                        Icon(
                            imageVector = AppMd3Icons.Editor.Save,
                            contentDescription = stringResource(LocaleR.string.editor_action_save)
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
        title = stringResource(LocaleR.string.editor_dialog_unsaved_changes_title),
        summary = stringResource(LocaleR.string.editor_dialog_unsaved_changes_message),
        onDismissRequest = { showDiscardDialog.value = false }
    ) {
        DialogButtonRow(
            onCancel = { showDiscardDialog.value = false },
            onConfirm = {
                showDiscardDialog.value = false
                navigator.navigateUp()
            },
            cancelText = stringResource(LocaleR.string.component_button_cancel),
            confirmText = stringResource(LocaleR.string.editor_action_discard),
        )
    }
}
