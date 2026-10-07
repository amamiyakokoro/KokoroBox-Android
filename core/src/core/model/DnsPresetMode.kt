/*
 * This file is part of KokoroBox.
 * Licensed under the GNU Affero General Public License, version 3 or later.
 */
package com.amamiyakokoro.box.core.model

enum class DnsPresetMode {
    None, AntiPollution, Overseas;

    companion object {
        // Prefer the explicit overseas preset if older/imported flags conflict.
        fun fromFlags(antiPollution: Boolean, overseas: Boolean): DnsPresetMode = when {
            overseas -> Overseas
            antiPollution -> AntiPollution
            else -> None
        }
    }
}
