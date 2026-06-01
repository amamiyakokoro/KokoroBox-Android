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

package com.github.yumelira.yumebox.service.notification

import com.github.yumelira.yumebox.data.model.DailyAppTrafficSummary
import com.tencent.mmkv.MMKV
import kotlinx.serialization.json.Json
import java.util.Calendar

internal class TodayTrafficNotificationReader {
    private val mmkv by lazy { MMKV.mmkvWithID(MMKV_ID, MMKV.MULTI_PROCESS_MODE) }
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun readTodayTotalBytes(): Long {
        val raw = mmkv.decodeString(KEY_DAILY_APP_SUMMARIES) ?: return 0L
        val summaries = runCatching {
            json.decodeFromString<Map<Long, DailyAppTrafficSummary>>(raw)
        }.getOrNull() ?: return 0L
        return summaries[getTodayKey()]?.total ?: 0L
    }

    private fun getTodayKey(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private companion object {
        const val MMKV_ID = "traffic_statistics"
        const val KEY_DAILY_APP_SUMMARIES = "daily_app_summaries_v2"
    }
}
