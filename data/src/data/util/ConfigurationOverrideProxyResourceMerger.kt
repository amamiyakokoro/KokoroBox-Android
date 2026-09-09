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

package com.amamiyakokoro.box.data.util

import com.amamiyakokoro.box.core.model.ConfigurationOverrideProxyResourceSection

internal object ConfigurationOverrideProxyResourceMerger {

    fun merge(
        base: ConfigurationOverrideProxyResourceSection,
        incoming: ConfigurationOverrideProxyResourceSection,
    ): ConfigurationOverrideProxyResourceSection {
        return base.copy(
            proxies = MergeHelper.mergeProxyList(base.proxies, incoming.proxies),
            proxiesStart = MergeHelper.mergeProxyList(base.proxiesStart, incoming.proxiesStart),
            proxiesEnd = MergeHelper.mergeProxyList(base.proxiesEnd, incoming.proxiesEnd),
            proxyProviders = MergeHelper.mergeProviderMap(
                base = base.proxyProviders,
                replace = incoming.proxyProviders,
                merge = incoming.proxyProvidersMerge,
            ),
            proxyProvidersMerge = MergeHelper.mergeProviderMap(
                base = base.proxyProvidersMerge,
                replace = incoming.proxyProvidersMerge,
                merge = null,
            ),
        )
    }
}
