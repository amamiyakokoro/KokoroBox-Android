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



package com.github.yumelira.yumebox.data.store

import com.github.yumelira.yumebox.data.model.AppColorTheme
import com.github.yumelira.yumebox.data.model.AppLanguage
import com.github.yumelira.yumebox.data.model.MonetContrast
import com.github.yumelira.yumebox.data.model.MonetStyle
import com.github.yumelira.yumebox.data.model.ThemeMode
import com.tencent.mmkv.MMKV

class AppSettingsStore(externalMmkv: MMKV) : MMKVPreference(externalMmkv = externalMmkv) {

    private companion object {
        const val KEY_LAST_APP_VERSION_CODE = "last_app_version_code"
        const val KEY_POST_UPDATE_COLD_START_PENDING = "post_update_cold_start_pending"
    }

    val initialSetupCompleted by boolFlow(false)
    val privacyPolicyAccepted by boolFlow(false)

    val themeMode by enumFlow(ThemeMode.Auto)
    val appLanguage by enumFlow(AppLanguage.System)
    val colorTheme by enumFlow(AppColorTheme.MonetDynamic)
    val monetStyle by enumFlow(MonetStyle.TonalSpot)
    val monetContrast by enumFlow(MonetContrast.Standard)
    val monetColorIntensity by floatFlow(0.45f)
    val themeAccentColorArgb by longFlow(0xFFDA98ABL)
    val acgWallpaperSeedColorArgb by longFlow(0xFFDA98ABL)
    val invertOnPrimaryColors by boolFlow(false)
    val automaticRestart by boolFlow(false)
    val autoUpdateCurrentProfileOnStart by boolFlow(true)
    val automaticUpdateCheckEnabled by boolFlow(true)
    var lastAutomaticUpdateCheckAtMillis by long(0L)
    val hideAppIcon by boolFlow(false)
    val excludeFromRecents by boolFlow(false)
    val showTrafficNotification by boolFlow(true)
    val bottomBarAutoHide by boolFlow(true)
    val bottomBarUseLegacyStyle by boolFlow(false)
    val acgMainUiEnabled by boolFlow(false)
    val acgWallpaperUri by strFlow("")
    val acgWallpaperZoom by floatFlow(1.0f)
    val acgWallpaperBiasX by floatFlow(0.0f)
    val acgWallpaperBiasY by floatFlow(0.0f)
    val acgSidebarExpanded by boolFlow(true)
    val pageScale by floatFlow(1.0f)
    val singleNodeTest by boolFlow(true)
    val healthCheckConcurrency by intFlow(DEFAULT_HEALTH_CHECK_CONCURRENCY)
    val screenshotProtectionEnabled by boolFlow(false)
    val biometricUnlockEnabled by boolFlow(false)
    val exitUiWhenBackground by boolFlow(false)

    val customUserAgent by strFlow("")

    fun syncAppVersion(versionCode: Int): Boolean {
        val previousVersionCode = mmkv.decodeInt(KEY_LAST_APP_VERSION_CODE, Int.MIN_VALUE)
        val isUpdated = previousVersionCode != Int.MIN_VALUE && previousVersionCode != versionCode
        if (isUpdated) {
            mmkv.encode(KEY_POST_UPDATE_COLD_START_PENDING, true)
        }
        mmkv.encode(KEY_LAST_APP_VERSION_CODE, versionCode)
        return isUpdated
    }

    fun consumePostUpdateColdStartPending(): Boolean {
        val pending = mmkv.decodeBool(KEY_POST_UPDATE_COLD_START_PENDING, false)
        if (pending) {
            mmkv.removeValueForKey(KEY_POST_UPDATE_COLD_START_PENDING)
        }
        return pending
    }

}

const val DEFAULT_HEALTH_CHECK_CONCURRENCY = 8
val SUPPORTED_HEALTH_CHECK_CONCURRENCY = listOf(8, 16, 24, 32)
