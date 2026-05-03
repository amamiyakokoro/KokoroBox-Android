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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.github.yumelira.yumebox.presentation.component.SmallTopBar
import com.github.yumelira.yumebox.presentation.icon.Yume
import com.github.yumelira.yumebox.presentation.icon.yume.ArrowLeft
import com.github.yumelira.yumebox.presentation.icon.yume.ListCollapse
import com.github.yumelira.yumebox.presentation.icon.yume.Save
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ConfigPreviewScreen(
    navigator: DestinationsNavigator,
    title: String = MLang.Editor.Common.ConfigPreviewTitle,
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
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        topBar = {
            SmallTopBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(Yume.ArrowLeft, contentDescription = MLang.Component.Navigation.Back)
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
                        Icon(Yume.ListCollapse, contentDescription = MLang.Editor.Action.Format)
                    }
                    IconButton(
                        onClick = {
                            val save = onSave ?: return@IconButton
                            runCatching {
                                save(content)
                            }.onSuccess {
                                navigator.navigateUp()
                            }.onFailure {
                                context.toast(it.message ?: MLang.Editor.Toast.SaveFailed)
                            }
                        },
                        enabled = canSave
                    ) {
                        Icon(Yume.Save, contentDescription = MLang.Editor.Action.Save)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
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
