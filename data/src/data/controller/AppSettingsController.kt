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

package com.amamiyakokoro.box.data.controller

import com.amamiyakokoro.box.core.Clash
import com.amamiyakokoro.box.data.model.AppLanguage
import com.amamiyakokoro.box.data.store.AppSettingsStore

class AppSettingsController(
    private val store: AppSettingsStore,
    private val applyLanguage: (AppLanguage) -> Unit = {},
    private val applyUserAgent: (String) -> Unit = Clash::setCustomUserAgent,
) {
    fun applyAppLanguage(language: AppLanguage) {
        store.appLanguage.set(language)
        applyLanguage(language)
    }

    fun applyCustomUserAgent(userAgent: String) {
        store.customUserAgent.set(userAgent)
        applyUserAgent(userAgent)
    }
}
