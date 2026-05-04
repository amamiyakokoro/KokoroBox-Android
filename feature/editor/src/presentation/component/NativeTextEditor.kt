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

package com.github.yumelira.yumebox.feature.editor.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.yumelira.yumebox.feature.editor.presentation.editor.CodeEditor
import com.github.yumelira.yumebox.feature.editor.presentation.editor.CodeEditorState
import com.github.yumelira.yumebox.feature.editor.presentation.language.LanguageScope

@Composable
fun NativeTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    language: LanguageScope = LanguageScope.Text,
    syntaxHighlightingEnabled: Boolean = true,
) {
    val editorLanguage = if (syntaxHighlightingEnabled) language else LanguageScope.Text
    val editorState = remember(editorLanguage, readOnly) {
        CodeEditorState(
            initialContent = value,
            language = editorLanguage,
            readOnly = readOnly,
        )
    }

    LaunchedEffect(value) {
        if (value != editorState.content) {
            editorState.loadContent(value)
        }
    }

    CodeEditor(
        state = editorState,
        modifier = modifier,
        onTextChange = onValueChange,
    )
}
