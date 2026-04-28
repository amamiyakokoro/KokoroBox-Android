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

import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import top.yukonga.miuix.kmp.theme.MiuixTheme

private class NativeConfigEditText(context: android.content.Context) : AppCompatEditText(context) {
    var suppressChanges: Boolean = false
}

@Composable
fun NativeTextEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
) {
    val density = LocalDensity.current
    val textColor = MiuixTheme.colorScheme.onBackground.toArgb()
    val cursorColor = MiuixTheme.colorScheme.primary.toArgb()
    val backgroundColor = MiuixTheme.colorScheme.background.toArgb()
    val paddingPx = remember(density) { with(density) { 16.dp.roundToPx() } }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            NativeConfigEditText(context).apply {
                setText(value)
                setTextColor(textColor)
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13f)
                typeface = Typeface.MONOSPACE
                gravity = Gravity.START or Gravity.TOP
                setHorizontallyScrolling(true)
                isHorizontalScrollBarEnabled = true
                isVerticalScrollBarEnabled = true
                setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                setBackgroundColor(backgroundColor)
                includeFontPadding = false
                isSingleLine = false
                overScrollMode = AppCompatEditText.OVER_SCROLL_IF_CONTENT_SCROLLS
                isLongClickable = true
                setTextIsSelectable(true)
                isFocusable = !readOnly
                isFocusableInTouchMode = !readOnly
                isCursorVisible = !readOnly
                inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
                    InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
                tintCursor(cursorColor)
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (!suppressChanges) {
                            onValueChange(s?.toString().orEmpty())
                        }
                    }

                    override fun afterTextChanged(s: Editable?) = Unit
                })
            }
        },
        update = { editText ->
            editText.setTextColor(textColor)
            editText.setBackgroundColor(backgroundColor)
            editText.isLongClickable = true
            editText.setTextIsSelectable(true)
            editText.isFocusable = !readOnly
            editText.isFocusableInTouchMode = !readOnly
            editText.isCursorVisible = !readOnly
            editText.tintCursor(cursorColor)
            if (editText.text.toString() != value) {
                val selectionStart = editText.selectionStart.coerceAtLeast(0)
                val selectionEnd = editText.selectionEnd.coerceAtLeast(0)
                editText.suppressChanges = true
                editText.setText(value)
                val resolvedStart = selectionStart.coerceAtMost(value.length)
                val resolvedEnd = selectionEnd.coerceAtMost(value.length).coerceAtLeast(resolvedStart)
                editText.setSelection(resolvedStart, resolvedEnd)
                editText.suppressChanges = false
            }
        },
    )
}

private fun AppCompatEditText.tintCursor(cursorColor: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        runCatching {
            textCursorDrawable?.setTint(cursorColor)
        }
    }
}
