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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.yumelira.yumebox.feature.editor.presentation.component.NativeTextEditor
import com.github.yumelira.yumebox.feature.editor.presentation.language.LanguageScope
import com.github.yumelira.yumebox.feature.editor.presentation.viewmodel.ConfigType
import com.github.yumelira.yumebox.feature.editor.presentation.viewmodel.ConfigEditorViewModel
import com.github.yumelira.yumebox.presentation.component.AppDialog
import com.github.yumelira.yumebox.presentation.component.DialogButtonRow
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.theme.ProvideAndroidPlatformTheme
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Suppress("unused")
@Composable
fun ConfigEditorScreen(
    navigator: DestinationsNavigator,
    configId: String,
    configType: ConfigType = ConfigType.Override,
    initialContent: String = "",
    language: LanguageScope = LanguageScope.Yaml,
) {
    val viewModel: ConfigEditorViewModel = koinViewModel()
    val session by viewModel.session.collectAsState()
    val showDiscardDialog = remember { mutableStateOf(false) }
    val scrollBehavior = MiuixScrollBehavior()
    var content by remember(configId, configType, initialContent, language) { mutableStateOf(initialContent) }

    LaunchedEffect(configId, configType, initialContent) {
        viewModel.loadConfig(
            configId = configId,
            configType = configType,
            initialContent = initialContent,
        )
    }

    LaunchedEffect(session.configId, session.draftContent) {
        if (session.configId == configId && session.draftContent != content) {
            content = session.draftContent
        }
    }

    LaunchedEffect(content, session.configId, session.savedContent) {
        if (session.configId != configId) {
            return@LaunchedEffect
        }
        delay(250)
        if (content != session.draftContent) {
            viewModel.updateDraft(content)
        }
    }

    val isDirty = content != session.savedContent

    BackHandler {
        if (isDirty) {
            showDiscardDialog.value = true
        } else {
            navigator.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = when (configType) {
                    ConfigType.Override -> MLang.Editor.Common.EditOverrideConfigTitle
                    ConfigType.Profile -> MLang.Editor.Common.EditProfileConfigTitle
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { paddingValues ->
        ProvideAndroidPlatformTheme {
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
                    language = language,
                )
            }
        }

        AppDialog(
            show = showDiscardDialog.value,
            title = MLang.Editor.Dialog.DiscardTitle,
            summary = MLang.Editor.Dialog.UnsavedChangesMessage,
            onDismissRequest = { showDiscardDialog.value = false }
        ) {
            DialogButtonRow(
                onCancel = { showDiscardDialog.value = false },
                onConfirm = {
                    showDiscardDialog.value = false
                    viewModel.discardDraft()
                    navigator.navigateUp()
                },
                cancelText = MLang.Component.Button.Cancel,
                confirmText = MLang.Editor.Action.Discard
            )
        }
    }
}
