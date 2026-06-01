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

package com.github.yumelira.yumebox.service.common.util

import android.content.res.Resources
import android.os.Build
import com.tencent.mmkv.MMKV
import dev.oom_wg.purejoy.mlang.MLang
import java.util.Locale

object ServiceLanguageRuntime {
    fun applyAppLanguage() {
        val language = runCatching {
            MMKV.mmkvWithID(SETTINGS_MMKV_ID, MMKV.MULTI_PROCESS_MODE)
                .decodeString(APP_LANGUAGE_KEY, APP_LANGUAGE_SYSTEM)
                ?: APP_LANGUAGE_SYSTEM
        }.getOrDefault(APP_LANGUAGE_SYSTEM)

        val locale = when (language) {
            APP_LANGUAGE_ZH -> Locale.SIMPLIFIED_CHINESE
            APP_LANGUAGE_ZH_TW -> Locale.TRADITIONAL_CHINESE
            APP_LANGUAGE_EN -> Locale.ENGLISH
            else -> systemLocale()
        }

        Locale.setDefault(locale)
        MLang.updateLocale(locale)
    }

    private fun systemLocale(): Locale {
        val resources = Resources.getSystem()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0] ?: Locale.getDefault()
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale ?: Locale.getDefault()
        }
    }

    private const val SETTINGS_MMKV_ID = "settings"
    private const val APP_LANGUAGE_KEY = "appLanguage"
    private const val APP_LANGUAGE_SYSTEM = "System"
    private const val APP_LANGUAGE_ZH = "Zh"
    private const val APP_LANGUAGE_ZH_TW = "ZhTw"
    private const val APP_LANGUAGE_EN = "En"
}
