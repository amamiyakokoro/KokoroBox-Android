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

plugins {
    id("com.android.library")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

val generateFYTxt by tasks.registering {
    val inputDir = layout.projectDirectory.dir("lang")
    val outputFile = layout.buildDirectory.file("generated/fytxt/kotlin/commonMain/kotlin/fytxt.kt")

    inputs.dir(inputDir)
    outputs.file(outputFile)

    doLast {
        fun MutableMap<String, Any>.merge(keys: List<String>, value: String) {
            var node = this
            keys.dropLast(1).forEach { key ->
                @Suppress("UNCHECKED_CAST")
                node = node.getOrPut(key) { linkedMapOf<String, Any>() } as MutableMap<String, Any>
            }
            node[keys.last()] = value
        }

        fun parseFvv(file: File): List<Pair<List<String>, String>> {
            val stack = mutableListOf<String>()
            val entries = mutableListOf<Pair<List<String>, String>>()
            val assignment = Regex("^([A-Za-z0-9_]+)\\s*=\\s*\"(.*)\"\\s*$")

            file.forEachLine(Charsets.UTF_8) { rawLine ->
                val line = rawLine.trim()
                when {
                    line.isBlank() || line.startsWith("<") || line.startsWith("//") -> Unit
                    line == "}" -> if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
                    line.endsWith("{") && line.contains("=") -> stack += line.substringBefore("=").trim()
                    else -> assignment.matchEntire(line)?.let { match ->
                        val value = match.groupValues[2].replace("\\\"", "\"")
                        entries += (stack + match.groupValues[1]) to value
                    }
                }
            }
            return entries
        }

        fun String.kotlinString() = buildString {
            append('"')
            this@kotlinString.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
            append('"')
        }

        val languageMaps = linkedMapOf<String, Map<String, String>>()
        val tree = linkedMapOf<String, Any>()

        inputDir.asFile.listFiles(File::isDirectory).orEmpty().sortedBy(File::getName).forEach { languageDir ->
            val map = linkedMapOf<String, String>()
            languageDir.listFiles { file -> file.extension == "fvv" }.orEmpty().sortedBy(File::getName).forEach { file ->
                parseFvv(file).forEach { (keys, value) ->
                    tree.merge(keys, value)
                    map[keys.joinToString(".")] = value
                }
            }
            languageMaps[languageDir.name] = map
        }

        val lines = mutableListOf(
            "package dev.oom_wg.purejoy.mlang",
            "",
            "import java.util.Locale",
            "",
            "object MLang {",
            "    private var activeLanguageTag: String = \"zh\"",
            "",
            "    fun updateLocale(locale: Locale) {",
            "        activeLanguageTag = normalize(locale.toLanguageTag())",
            "    }",
            "",
            "    fun updateLocale(languageTag: String?) {",
            "        activeLanguageTag = normalize(languageTag)",
            "    }",
            "",
            "    private fun normalize(languageTag: String?): String {",
            "        val tag = languageTag?.lowercase()?.replace('_', '-').orEmpty()",
            "        return when {",
        )

        languageMaps.keys.sortedByDescending { it.length }.forEach { language ->
            val lower = language.lowercase()
            val condition = if (lower == "zh-tw") {
                "tag == ${lower.kotlinString()} || tag.startsWith(${"$lower-".kotlinString()}) || tag == ${"zh-hant".kotlinString()} || tag.startsWith(${"zh-hant-".kotlinString()})"
            } else {
                "tag == ${lower.kotlinString()} || tag.startsWith(${"$lower-".kotlinString()})"
            }
            lines += "            $condition -> ${language.kotlinString()}"
        }
        lines += listOf(
            "            else -> \"zh\"",
            "        }",
            "    }",
            "",
            "    private fun text(key: String): String = when (activeLanguageTag) {",
        )
        languageMaps.keys.forEach { language ->
            val id = "TEXT_" + language.replace(Regex("[^A-Za-z0-9_]"), "_").uppercase()
            lines += "        ${language.kotlinString()} -> $id[key]"
        }
        lines += listOf(
            "        else -> null",
            "    } ?: TEXT_ZH[key] ?: TEXT_EN[key] ?: key",
            "",
        )

        languageMaps.forEach { (language, values) ->
            val id = "TEXT_" + language.replace(Regex("[^A-Za-z0-9_]"), "_").uppercase()
            lines += "    private val $id = mapOf("
            values.toSortedMap().forEach { (key, value) ->
                lines += "        ${key.kotlinString()} to ${value.kotlinString()},"
            }
            lines += "    )"
            lines += ""
        }

        fun emitObject(name: String, node: Map<String, Any>, indent: Int, prefix: List<String>) {
            val spaces = " ".repeat(indent)
            lines += "${spaces}object $name {"
            node.toSortedMap().forEach { (key, value) ->
                @Suppress("UNCHECKED_CAST")
                if (value is Map<*, *>) {
                    emitObject(key, value as Map<String, Any>, indent + 4, prefix + key)
                } else {
                    val textKey = (prefix + key).joinToString(".")
                    lines += "${" ".repeat(indent + 4)}val $key: String get() = text(${textKey.kotlinString()})"
                }
            }
            lines += "$spaces}"
        }

        tree.toSortedMap().forEach { (key, value) ->
            @Suppress("UNCHECKED_CAST")
            if (value is Map<*, *>) {
                emitObject(key, value as Map<String, Any>, 4, listOf(key))
            } else {
                lines += "    val $key: String get() = text(${key.kotlinString()})"
            }
        }
        lines += "}"

        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(lines.joinToString("\n") + "\n", Charsets.UTF_8)
        }
    }
}


android {
    namespace = "com.github.yumelira.yumebox.core.locale"

    sourceSets {
        getByName("main") {
            kotlin.srcDirs("src", "build/generated/fytxt/kotlin/commonMain/kotlin")
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:${gropify.dep.version.composeBom}")
    implementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
}

tasks.matching { it.name.startsWith("compile") && it.name.endsWith("Kotlin") }.configureEach {
    dependsOn(generateFYTxt)
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(generateFYTxt)
}
