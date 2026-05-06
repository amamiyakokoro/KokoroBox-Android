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



package com.github.yumelira.yumebox.feature.editor.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.github.yumelira.yumebox.feature.editor.presentation.language.TextMateInitializer
import com.github.yumelira.yumebox.presentation.theme.LocalAppColors
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

object EditorThemeManager {

    @Composable
    fun rememberEditorTheme(): EditorThemeState {
        val colorScheme = MaterialTheme.colorScheme
        val appColors = LocalAppColors.current
        val background = colorScheme.background
        val lineNumber = colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        val currentLineBase = colorScheme.surfaceContainerHighest
        val isDark = background.luminance() < 0.5f

        return remember(isDark, background, lineNumber, currentLineBase, colorScheme.primary, colorScheme.onSurfaceVariant, appColors) {
            EditorThemeState(
                isDark = isDark,
                themeName = if (isDark) "dark-high-contrast" else "light",
                backgroundColor = background,
                lineNumberBackgroundColor = background,
                currentLineColor = currentLineBase.copy(alpha = if (isDark) 0.50f else 0.58f).compositeOver(background),
                lineNumberColor = lineNumber,
                currentLineNumberColor = colorScheme.primary,
                selectionHandleColor = appColors.editor.accent,
                selectionBackgroundColor = if (isDark) appColors.editor.darkSelectionBackground else appColors.editor.lightSelectionBackground,
                textActionBackgroundColor = background,
                textActionIconColor = colorScheme.onSurfaceVariant,
                highlightedDelimiterForegroundColor = if (isDark) appColors.editor.delimiterDark else appColors.editor.delimiterLight,
                highlightedDelimiterBackgroundColor = appColors.editor.delimiterBackground,
            )
        }
    }

    fun applyTheme(editor: CodeEditor, theme: EditorThemeState) {
        try {
            TextMateInitializer.setTheme(theme.isDark)
            val scheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            editor.colorScheme = scheme
            editor.applyProjectEditorChrome(theme)
        } catch (_: Exception) {
            val scheme = EditorColorSynchronizer.createColorScheme(theme.isDark)
            editor.colorScheme = scheme
            editor.applyProjectEditorChrome(theme)
        }
    }

    fun updateTheme(editor: CodeEditor, theme: EditorThemeState) {
        try {
            TextMateInitializer.setTheme(theme.isDark)
            val scheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            editor.colorScheme = scheme
            editor.applyProjectEditorChrome(theme)
        } catch (_: Exception) {
            val scheme = EditorColorSynchronizer.createColorScheme(theme.isDark)
            editor.colorScheme = scheme
            editor.applyProjectEditorChrome(theme)
        }
    }

    private fun CodeEditor.applyProjectEditorChrome(theme: EditorThemeState) {
        colorScheme.applyProjectEditorChrome(theme)
        setBackgroundColor(theme.backgroundColor.toArgb())
    }

    private fun EditorColorScheme.applyProjectEditorChrome(theme: EditorThemeState): EditorColorScheme {
        val background = theme.backgroundColor.toArgb()
        this.setColor(EditorColorScheme.WHOLE_BACKGROUND, background)
        this.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, theme.lineNumberBackgroundColor.toArgb())
        this.setColor(EditorColorScheme.CURRENT_LINE, theme.currentLineColor.toArgb())
        this.setColor(EditorColorScheme.LINE_NUMBER, theme.lineNumberColor.toArgb())
        this.setColor(EditorColorScheme.LINE_NUMBER_CURRENT, theme.currentLineNumberColor.toArgb())
        this.setColor(EditorColorScheme.SELECTION_INSERT, theme.selectionHandleColor.toArgb())
        this.setColor(EditorColorScheme.SELECTION_HANDLE, theme.selectionHandleColor.toArgb())
        this.setColor(EditorColorScheme.SELECTED_TEXT_BACKGROUND, theme.selectionBackgroundColor.toArgb())
        this.setColor(EditorColorScheme.TEXT_ACTION_WINDOW_BACKGROUND, theme.textActionBackgroundColor.toArgb())
        this.setColor(EditorColorScheme.TEXT_ACTION_WINDOW_ICON_COLOR, theme.textActionIconColor.toArgb())
        this.setColor(EditorColorScheme.DIAGNOSTIC_TOOLTIP_BACKGROUND, theme.textActionBackgroundColor.toArgb())
        this.setColor(EditorColorScheme.HIGHLIGHTED_DELIMITERS_FOREGROUND, theme.highlightedDelimiterForegroundColor.toArgb())
        this.setColor(EditorColorScheme.HIGHLIGHTED_DELIMITERS_BACKGROUND, theme.highlightedDelimiterBackgroundColor.toArgb())
        return this
    }
}

data class EditorThemeState(
    val isDark: Boolean,
    val themeName: String,
    val backgroundColor: Color,
    val lineNumberBackgroundColor: Color,
    val currentLineColor: Color,
    val lineNumberColor: Color,
    val currentLineNumberColor: Color,
    val selectionHandleColor: Color,
    val selectionBackgroundColor: Color,
    val textActionBackgroundColor: Color,
    val textActionIconColor: Color,
    val highlightedDelimiterForegroundColor: Color,
    val highlightedDelimiterBackgroundColor: Color,
)
