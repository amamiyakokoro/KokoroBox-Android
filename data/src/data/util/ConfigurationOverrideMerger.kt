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

import com.amamiyakokoro.box.core.model.ConfigurationOverride
import com.amamiyakokoro.box.core.model.dnsSection
import com.amamiyakokoro.box.core.model.proxyResourceSection
import com.amamiyakokoro.box.core.model.routingSection
import com.amamiyakokoro.box.core.model.snifferSection
import com.amamiyakokoro.box.core.model.supportSection
import com.amamiyakokoro.box.core.model.withDnsSection
import com.amamiyakokoro.box.core.model.withProxyResourceSection
import com.amamiyakokoro.box.core.model.withRoutingSection
import com.amamiyakokoro.box.core.model.withSnifferSection
import com.amamiyakokoro.box.core.model.withSupportSection

internal object ConfigurationOverrideMerger {

    fun merge(base: ConfigurationOverride, incoming: ConfigurationOverride): ConfigurationOverride {
        return ConfigurationOverrideCoreMerger.merge(base, incoming)
            .withRoutingSection(
                ConfigurationOverrideRoutingMerger.merge(
                    base = base.routingSection(),
                    incoming = incoming.routingSection(),
                ),
            )
            .withProxyResourceSection(
                ConfigurationOverrideProxyResourceMerger.merge(
                    base = base.proxyResourceSection(),
                    incoming = incoming.proxyResourceSection(),
                ),
            )
            .withDnsSection(
                ConfigurationOverrideDnsMerger.merge(
                    base = base.dnsSection(),
                    incoming = incoming.dnsSection(),
                ),
            )
            .withSnifferSection(
                ConfigurationOverrideSnifferMerger.merge(
                    base = base.snifferSection(),
                    incoming = incoming.snifferSection(),
                ),
            )
            .withSupportSection(
                ConfigurationOverrideSupportMerger.merge(
                    base = base.supportSection(),
                    incoming = incoming.supportSection(),
                ),
            )
    }
}
