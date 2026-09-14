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
import com.amamiyakokoro.box.presentation.component.SmallTopBar
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.UiDp
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun ConfigPreviewScreen(
    navigator: DestinationsNavigator,
    title: String? = null,
    initialContent: String = "",
    language: LanguageScope = LanguageScope.Yaml,
    onSave: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current

    val formattedContent = remember(initialContent, language) {
        if (language == LanguageScope.Json) {
            CodeFormatter.format(initialContent, language) ?: initialContent
        } else {
            initialContent
        }
    }
    var content by remember(formattedContent, language) { mutableStateOf(formattedContent) }
    val isModified = content != formattedContent
    val canSave = onSave != null && isModified
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SmallTopBar(
                title = title ?: stringResource(LocaleR.string.editor_common_config_preview_title),
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(AppMd3Icons.Navigation.Back, contentDescription = stringResource(LocaleR.string.component_navigation_back))
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.padding(end = UiDp.dp12),
                        onClick = {
                            val formatted = CodeFormatter.format(content, language)
                            if (formatted != null && formatted != content) {
                                content = formatted
                            }
                        }
                    ) {
                        Icon(AppMd3Icons.Editor.FormatStructured, contentDescription = stringResource(LocaleR.string.editor_action_format))
                    }
                    IconButton(
                        onClick = {
                            val save = onSave ?: return@IconButton
                            runCatching {
                                save(content)
                            }.onSuccess {
                                navigator.navigateUp()
                            }.onFailure {
                                context.toast(it.message ?: context.getString(LocaleR.string.editor_toast_save_failed))
                            }
                        },
                        enabled = canSave
                    ) {
                        Icon(AppMd3Icons.Editor.Save, contentDescription = stringResource(LocaleR.string.editor_action_save))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
        ) {
            NativeTextEditor(
                value = content,
                onValueChange = { newContent ->
                    if (newContent != content) {
                        content = newContent
                    }
                },
                modifier = Modifier.fillMaxSize(),
                readOnly = onSave == null,
                language = language,
            )
        }
    }
}
