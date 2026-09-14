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

package com.amamiyakokoro.box.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.model.ConfigurationOverride

@Composable
fun InboundEditor(
    config: ConfigurationOverride,
    onConfigChange: (ConfigurationOverride) -> Unit,
    onEditStringList: OpenStringListModifiersEditor,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(OverrideSectionSpacing),
    ) {
        OverrideFormSection(stringResource(LocaleR.string.override_form_proxy_ports)) {
            OverridePortInputContent(
                title = stringResource(LocaleR.string.override_general_http_port),
                value = config.httpPort,
                onValueChange = { onConfigChange(config.copy(httpPort = it)) },
            )
            OverridePortInputContent(
                title = stringResource(LocaleR.string.override_general_socks_port),
                value = config.socksPort,
                onValueChange = { onConfigChange(config.copy(socksPort = it)) },
            )
            OverridePortInputContent(
                title = stringResource(LocaleR.string.override_general_mixed_port),
                value = config.mixedPort,
                onValueChange = { onConfigChange(config.copy(mixedPort = it)) },
            )
            OverridePortInputContent(
                title = stringResource(LocaleR.string.override_general_redirect_port),
                value = config.redirectPort,
                onValueChange = { onConfigChange(config.copy(redirectPort = it)) },
            )
            OverridePortInputContent(
                title = stringResource(LocaleR.string.override_general_tproxy_port),
                value = config.tproxyPort,
                onValueChange = { onConfigChange(config.copy(tproxyPort = it)) },
            )
        }
    }
}
