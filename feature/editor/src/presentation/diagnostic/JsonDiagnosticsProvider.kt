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



package com.amamiyakokoro.box.feature.editor.presentation.diagnostic

import android.content.res.Resources
import com.amamiyakokoro.box.core.locale.R as LocaleR
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticDetail
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticRegion
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

object JsonDiagnosticsProvider {

    fun analyze(content: String, resources: Resources): DiagnosticsContainer {
        val container = DiagnosticsContainer()

        if (content.isBlank()) {
            return container
        }

        val trimmed = content.trim()

        try {

            when {
                trimmed.startsWith("{") -> JSONObject(trimmed)
                trimmed.startsWith("[") -> JSONArray(trimmed)
                else -> {

                    container.addDiagnostic(
                        DiagnosticRegion(
                            0,
                            content.length.coerceAtMost(1),
                            DiagnosticRegion.SEVERITY_ERROR,
                            0,
                            DiagnosticDetail(
                                briefMessage = resources.getString(LocaleR.string.editor_diagnostic_json_format_error),
                                detailedMessage = resources.getString(LocaleR.string.editor_diagnostic_json_must_start_with_object_or_array),
                            )
                        )
                    )
                }
            }
        } catch (e: JSONException) {

            val diagnostic = parseJsonException(e, content, resources)
            if (diagnostic != null) {
                container.addDiagnostic(diagnostic)
            }
        } catch (e: Exception) {
            Timber.w(e, "JSON analysis failed")
        }

        return container
    }

    private fun parseJsonException(e: JSONException, content: String, resources: Resources): DiagnosticRegion? {
        val message = e.message ?: return null

        val indexPattern = "character (\\d+)".toRegex()
        val match = indexPattern.find(message)

        val errorIndex = match?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val safeIndex = errorIndex.coerceIn(0, content.length - 1)
        val endIndex = (safeIndex + 1).coerceAtMost(content.length)

        return DiagnosticRegion(
            safeIndex,
            endIndex,
            DiagnosticRegion.SEVERITY_ERROR,
            0,
            DiagnosticDetail(
                briefMessage = resources.getString(LocaleR.string.editor_diagnostic_json_syntax_error),
                detailedMessage = formatErrorMessage(message, resources),
            )
        )
    }

    private fun formatErrorMessage(message: String, resources: Resources): String {
        return when {
            message.contains("Unterminated") -> resources.getString(LocaleR.string.editor_diagnostic_unterminated)
            message.contains("Expected") -> {

                val expectedPattern = "Expected (\\S+)".toRegex()
                val expected = expectedPattern.find(message)?.groupValues?.get(1)
                    ?: resources.getString(LocaleR.string.editor_diagnostic_unknown)
                resources.getString(LocaleR.string.editor_diagnostic_expected).format(expected)
            }
            message.contains("No value") -> resources.getString(LocaleR.string.editor_diagnostic_no_value)
            message.contains("Duplicate") -> resources.getString(LocaleR.string.editor_diagnostic_duplicate_key)
            else -> message
        }
    }
}
