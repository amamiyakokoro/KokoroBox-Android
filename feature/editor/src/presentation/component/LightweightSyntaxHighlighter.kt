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

import android.text.Editable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.github.yumelira.yumebox.feature.editor.presentation.language.LanguageScope

internal data class SyntaxHighlightColors(
    val key: Int,
    val string: Int,
    val number: Int,
    val keyword: Int,
    val comment: Int,
)

internal object LightweightSyntaxHighlighter {
    private const val MAX_HIGHLIGHT_CHARS = 300_000

    private val quotedStringRegex = Regex("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'")
    private val yamlKeyRegex = Regex("(?m)^\\s*-?\\s*([A-Za-z0-9_.@/+$\\-]+|\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*')\\s*:")
    private val jsonKeyRegex = Regex("\"(?:\\\\.|[^\"\\\\])*\"(?=\\s*:)")
    private val numberRegex = Regex("(?<![A-Za-z0-9_.-])-?\\b\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?\\b")
    private val keywordRegex = Regex("\\b(?:true|false|null|yes|no|on|off)\\b", RegexOption.IGNORE_CASE)
    private val yamlCommentRegex = Regex("(?m)#.*$")

    fun apply(
        editable: Editable?,
        language: LanguageScope,
        colors: SyntaxHighlightColors,
        enabled: Boolean,
    ) {
        editable ?: return
        clear(editable)

        if (!enabled || language == LanguageScope.Text || editable.length > MAX_HIGHLIGHT_CHARS) {
            return
        }

        when (language) {
            LanguageScope.Json -> highlightJson(editable, colors)
            LanguageScope.Yaml -> highlightYaml(editable, colors)
            LanguageScope.Text -> Unit
        }
    }

    private fun highlightJson(editable: Editable, colors: SyntaxHighlightColors) {
        applyRegex(editable, quotedStringRegex, colors.string)
        applyRegex(editable, jsonKeyRegex, colors.key)
        applyRegex(editable, numberRegex, colors.number)
        applyRegex(editable, keywordRegex, colors.keyword)
    }

    private fun highlightYaml(editable: Editable, colors: SyntaxHighlightColors) {
        applyRegex(editable, quotedStringRegex, colors.string)
        applyRegex(editable, yamlKeyRegex, colors.key, group = 1)
        applyRegex(editable, numberRegex, colors.number)
        applyRegex(editable, keywordRegex, colors.keyword)
        applyRegex(editable, yamlCommentRegex, colors.comment)
    }

    private fun applyRegex(
        editable: Editable,
        regex: Regex,
        color: Int,
        group: Int = 0,
    ) {
        regex.findAll(editable).forEach { match ->
            val range = match.groups[group]?.range ?: return@forEach
            val start = range.first
            val end = range.last + 1
            if (start >= 0 && end <= editable.length && start < end) {
                editable.setSpan(
                    SyntaxSpan(color),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }
    }

    private fun clear(editable: Editable) {
        editable.getSpans(0, editable.length, SyntaxSpan::class.java).forEach(editable::removeSpan)
    }

    private class SyntaxSpan(color: Int) : ForegroundColorSpan(color)
}
