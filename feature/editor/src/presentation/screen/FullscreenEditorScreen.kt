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
import com.github.yumelira.yumebox.presentation.icon.Yume
import com.github.yumelira.yumebox.presentation.icon.yume.Atom
import com.github.yumelira.yumebox.presentation.icon.yume.Save
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Suppress("unused")
@Composable
fun FullscreenEditorScreen(
    navigator: DestinationsNavigator,
    title: String = "编辑配置",
    initialContent: String = "",
    language: LanguageScope = LanguageScope.Yaml,
    onSave: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val showDiscardDialog = remember { mutableStateOf(false) }
    var content by remember(initialContent, language) { mutableStateOf(initialContent) }
    val isModified = content != initialContent
    val scrollBehavior = MiuixScrollBehavior()

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
        topBar = {
            TopBar(
                title = title,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        modifier = Modifier.padding(end = UiDp.dp12),
                        onClick = {
                            val formatted = CodeFormatter.format(content, language)
                            if (formatted != null && formatted != content) {
                                content = formatted
                                context.toast("格式化成功")
                            } else {
                                context.toast("格式化失败或无需格式化")
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Yume.Atom,
                            contentDescription = "Format"
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!CodeFormatter.validate(content, language)) {
                                context.toast("语法错误，请检查内容")
                                return@IconButton
                            }
                            runCatching {
                                onSave(content)
                            }.onSuccess {
                                navigator.navigateUp()
                            }.onFailure {
                                context.toast(it.message ?: "保存失败")
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Yume.Save,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            NativeTextEditor(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    AppDialog(
        show = showDiscardDialog.value,
        title = "未保存的修改",
        summary = "当前有未保存的修改，确定要放弃吗？",
        onDismissRequest = { showDiscardDialog.value = false }
    ) {
        DialogButtonRow(
            onCancel = { showDiscardDialog.value = false },
            onConfirm = {
                showDiscardDialog.value = false
                navigator.navigateUp()
            },
            cancelText = "取消",
            confirmText = "放弃"
        )
    }
}
