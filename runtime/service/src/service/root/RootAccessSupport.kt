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

package com.amamiyakokoro.box.service.root

import android.content.Context
import com.amamiyakokoro.box.core.locale.R as LocaleR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RootAccessStatus(
    val rootAccessGranted: Boolean,
) {
    val canStartRootTun: Boolean
        get() = rootAccessGranted

    fun rootTunBlockedMessage(context: Context): String {
        return context.getString(LocaleR.string.network_settings_error_root_required)
    }
}

object RootAccessSupport {
    fun evaluate(@Suppress("UNUSED_PARAMETER") context: Context): RootAccessStatus {
        val rootAccessGranted = RootPackageShell.hasRootAccess()
        return RootAccessStatus(
            rootAccessGranted = rootAccessGranted,
        )
    }

    suspend fun evaluateAsync(context: Context): RootAccessStatus = withContext(Dispatchers.IO) {
        evaluate(context)
    }

    suspend fun requireRootTunAccess(context: Context): RootAccessStatus {
        return evaluateAsync(context).also { status ->
            check(status.canStartRootTun) { status.rootTunBlockedMessage(context) }
        }
    }
}
