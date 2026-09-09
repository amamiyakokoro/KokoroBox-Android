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



package com.amamiyakokoro.box.data.store

import com.amamiyakokoro.box.core.model.TunnelState
import com.amamiyakokoro.box.data.model.PROXY_SHEET_HEIGHT_FRACTION_DEFAULT
import com.amamiyakokoro.box.data.model.ProxyDisplayMode
import com.amamiyakokoro.box.data.model.ProxySortMode
import com.tencent.mmkv.MMKV

class ProxyDisplaySettingsStore(externalMmkv: MMKV) : MMKVPreference(externalMmkv = externalMmkv) {

    val sortMode by enumFlow(ProxySortMode.DEFAULT)
    val displayMode by enumFlow(ProxyDisplayMode.SINGLE_DETAILED)
    val proxyMode by enumFlow(TunnelState.Mode.Rule)
    var ruleProfileUuid by str("")
    var globalProfileUuid by str("")
    var directProfileUuid by str("")
    val sheetHeightFraction by floatFlow(PROXY_SHEET_HEIGHT_FRACTION_DEFAULT)
}
