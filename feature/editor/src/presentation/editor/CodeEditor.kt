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



package com.github.yumelira.yumebox.feature.editor.presentation.editor

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.yumelira.yumebox.feature.editor.presentation.language.LanguageScope
import com.github.yumelira.yumebox.feature.editor.presentation.language.TextMateInitializer
import com.github.yumelira.yumebox.feature.editor.presentation.theme.EditorThemeManager
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.subscribeAlways
import kotlinx.coroutines.delay
import timber.log.Timber

@Composable
fun CodeEditor(
    state: CodeEditorState,
    modifier: Modifier = Modifier,
    onTextChange: ((String) -> Unit)? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val editorRef = remember { mutableStateOf<CodeEditor?>(null) }
    val latestOnTextChange by rememberUpdatedState(onTextChange)
    var pendingTextChange by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Timber.d("CodeEditor: onPause")
                }
                Lifecycle.Event.ON_RESUME -> {
                    Timber.d("CodeEditor: onResume")
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Timber.d("CodeEditor: Disposing...")
            lifecycleOwner.lifecycle.removeObserver(observer)
            state.clearDiagnostics()
            state.editor = null
            editorRef.value = null
        }
    }

    val editorThemeState = EditorThemeManager.rememberEditorTheme()
    LaunchedEffect(editorThemeState.isDark) {
        editorRef.value?.let { editor ->
            EditorThemeManager.updateTheme(editor, editorThemeState.isDark)
        }
    }

    LaunchedEffect(state.content, state.language) {
        if (state.language == LanguageScope.Json) {
            delay(180)
        }
        state.updateDiagnostics()
    }

    LaunchedEffect(pendingTextChange) {
        val text = pendingTextChange ?: return@LaunchedEffect
        delay(250)
        if (pendingTextChange == text) {
            latestOnTextChange?.invoke(text)
        }
    }

    AndroidView(
        factory = { ctx ->
            createCodeEditor(ctx, state, editorThemeState.isDark) { text ->
                pendingTextChange = text
            }.also { editor ->
                state.editor = editor
                state.refreshHistoryState()
                editorRef.value = editor
            }
        },
        modifier = modifier,
        onRelease = { editor ->
            try {
                editor.release()
            } catch (e: Exception) {
                Timber.e(e, "Failed to release editor in onRelease")
            }
        }
    )
}

private fun createCodeEditor(
    context: android.content.Context,
    state: CodeEditorState,
    isDark: Boolean,
    onTextChange: ((String) -> Unit)?
): CodeEditor {

    TextMateInitializer.initialize(context)

    return CodeEditor(context).apply {

        isEditable = !state.readOnly

        val font = EditorFontManager.getEditorTypeface(context)
        typefaceText = font
        typefaceLineNumber = font

        setScrollBarEnabled(false)

        nonPrintablePaintingFlags = CodeEditor.FLAG_DRAW_LINE_SEPARATOR

        setText(state.content)

        EditorThemeManager.applyTheme(this, isDark)
        TextMateInitializer.setLanguage(this, state.language)

        subscribeAlways<ContentChangeEvent> {
            if (state.syncContentFromEditor()) {
                onTextChange?.invoke(state.content)
            }
        }

        Timber.d("CodeEditor created: language=${state.language}, readOnly=${state.readOnly}")
    }
}
