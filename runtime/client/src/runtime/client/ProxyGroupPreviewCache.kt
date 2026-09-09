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

package com.amamiyakokoro.box.runtime.client

import com.amamiyakokoro.box.core.model.TunnelState
import com.amamiyakokoro.box.domain.model.ProxyGroupInfo
import com.amamiyakokoro.box.service.runtime.entity.Profile
import com.amamiyakokoro.box.service.runtime.state.RuntimePhase
import java.util.UUID

internal class ProxyGroupPreviewCache {
    private data class CacheKey(
        val profileId: UUID,
        val profileUpdatedAt: Long,
        val excludeNotSelectable: Boolean,
        val overrideSignature: String,
        val mode: TunnelState.Mode,
    )

    private data class CacheEntry(
        val key: CacheKey,
        val groups: List<ProxyGroupInfo>,
    )

    private val entries = linkedMapOf<CacheKey, CacheEntry>()

    fun store(
        profile: Profile,
        excludeNotSelectable: Boolean,
        overrideSignature: String,
        mode: TunnelState.Mode,
        groups: List<ProxyGroupInfo>,
    ) {
        val cacheKey = key(profile, excludeNotSelectable, overrideSignature, mode)
        entries[cacheKey] = CacheEntry(
            key = cacheKey,
            groups = groups,
        )
    }

    fun cached(
        profile: Profile?,
        excludeNotSelectable: Boolean,
        overrideSignature: String,
        mode: TunnelState.Mode,
    ): List<ProxyGroupInfo>? {
        val targetProfile = profile ?: return null
        val cacheKey = key(targetProfile, excludeNotSelectable, overrideSignature, mode)
        return entries[cacheKey]?.groups
            ?: entries.values.lastOrNull { entry ->
                entry.key.profileId == targetProfile.uuid &&
                    entry.key.excludeNotSelectable == excludeNotSelectable &&
                    entry.key.mode == mode
            }?.groups
    }

    fun fallback(
        phase: RuntimePhase,
        profile: Profile?,
        excludeNotSelectable: Boolean,
        overrideSignature: String,
        mode: TunnelState.Mode,
    ): List<ProxyGroupInfo>? {
        if (phase == RuntimePhase.Running) return null
        return cached(
            profile = profile,
            excludeNotSelectable = excludeNotSelectable,
            overrideSignature = overrideSignature,
            mode = mode,
        )
    }

    private fun key(
        profile: Profile,
        excludeNotSelectable: Boolean,
        overrideSignature: String,
        mode: TunnelState.Mode,
    ): CacheKey {
        return CacheKey(
            profileId = profile.uuid,
            profileUpdatedAt = profile.updatedAt,
            excludeNotSelectable = excludeNotSelectable,
            overrideSignature = overrideSignature,
            mode = mode,
        )
    }
}
