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



package com.amamiyakokoro.box.service

import android.content.Context
import com.amamiyakokoro.box.service.remote.IFetchObserver
import com.amamiyakokoro.box.service.remote.IProfileManager
import com.amamiyakokoro.box.service.runtime.config.ServiceStore
import com.amamiyakokoro.box.service.runtime.entity.Imported
import com.amamiyakokoro.box.service.runtime.entity.Profile
import com.amamiyakokoro.box.service.runtime.records.ImportedDao
import com.amamiyakokoro.box.service.runtime.records.ProfileStore
import com.amamiyakokoro.box.service.runtime.util.directoryLastModified
import com.amamiyakokoro.box.service.runtime.util.generateProfileUUID
import com.amamiyakokoro.box.service.runtime.util.importedDir
import com.amamiyakokoro.box.service.runtime.util.sendProfileChanged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.util.*

class ProfileManager(private val context: Context) : IProfileManager,
    CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val store = ServiceStore()

    init {
        launch {
            context.importedDir.mkdirs()
        }
    }

    override suspend fun create(type: Profile.Type, name: String, source: String, userAgent: String): UUID {
        val uuid = generateProfileUUID()
        val normalizedName = name.trim().ifBlank { "New Profile" }
        val now = System.currentTimeMillis()

        val imported = Imported(
            uuid = uuid,
            name = normalizedName,
            type = type,
            source = source,
            interval = 0,
            upload = 0,
            total = 0,
            download = 0,
            expire = 0,
            createdAt = now,
            userAgent = normalizeUserAgent(userAgent),
        )

        ImportedDao.insert(imported)
        appendProfileToOrder(uuid)

        return uuid
    }

    override suspend fun clone(uuid: UUID): UUID {
        val newUUID = generateProfileUUID()

        val imported = ImportedDao.queryByUUID(uuid)
            ?: throw FileNotFoundException("profile $uuid not found")

        val now = System.currentTimeMillis()
        val newImported = Imported(
            uuid = newUUID,
            name = imported.name,
            type = Profile.Type.File,
            source = imported.source,
            interval = imported.interval,
            upload = imported.upload,
            total = imported.total,
            download = imported.download,
            expire = imported.expire,
            createdAt = now,
            userAgent = imported.userAgent,
        )

        val sourceDir = context.importedDir.resolve(uuid.toString())
        val targetDir = context.importedDir.resolve(newUUID.toString())

        if (!sourceDir.exists())
            throw FileNotFoundException("profile $uuid not found")

        targetDir.deleteRecursively()
        sourceDir.copyRecursively(targetDir)

        ImportedDao.insert(newImported)
        appendProfileToOrder(newUUID)

        return newUUID
    }

    override suspend fun patch(
        uuid: UUID,
        name: String,
        source: String,
        interval: Long,
        userAgent: String,
    ) {
        val imported = ImportedDao.queryByUUID(uuid)
            ?: throw FileNotFoundException("profile $uuid not found")

        val updated = imported.copy(
            name = name,
            source = source,
            interval = interval,
            userAgent = normalizeUserAgent(userAgent),
        )

        ImportedDao.update(updated)
        context.sendProfileChanged(uuid)
    }

    override suspend fun update(uuid: UUID, callback: IFetchObserver?) {
        ProfileProcessor.update(context, uuid, callback)
    }

    override suspend fun delete(uuid: UUID) {
        ProfileProcessor.delete(context, uuid)
    }

    override suspend fun queryByUUID(uuid: UUID): Profile? {
        return resolveProfile(uuid)
    }

    override suspend fun queryAll(): List<Profile> {
        val uuids = withContext(Dispatchers.IO) {
            ImportedDao.queryAllUUIDs()
        }

        val order = normalizeProfileOrder(uuids)
        val orderIndex = order.withIndex().associate { it.value to it.index }

        return uuids.mapNotNull { resolveProfile(it) }
            .sortedBy { orderIndex[it.uuid] ?: Int.MAX_VALUE }
    }

    override suspend fun queryActive(): Profile? {
        val active = store.activeProfile ?: return null

        return if (ImportedDao.exists(active)) {
            resolveProfile(active)
        } else {
            store.activeProfile = null
            null
        }
    }

    override suspend fun setActive(profile: Profile) {
        store.activeProfile = profile.uuid
        StatusProvider.currentProfile = profile.toString()
        context.sendProfileChanged(profile.uuid)
    }

    override suspend fun clearActive(profile: Profile) {
        store.activeProfile = null
        StatusProvider.currentProfile = null
        context.sendProfileChanged(profile.uuid)
    }

    override suspend fun reorder(uuids: List<UUID>) {
        val existing = ImportedDao.queryAllUUIDs()
        val existingSet = existing.toSet()

        val normalized = buildList {
            uuids.forEach { uuid ->
                if (uuid in existingSet && uuid !in this) add(uuid)
            }
            existing.forEach { uuid ->
                if (uuid !in this) add(uuid)
            }
        }

        ProfileStore.saveProfileOrder(normalized)
    }

    private fun normalizeProfileOrder(existing: List<UUID>): List<UUID> {
        val existingSet = existing.toSet()
        val storedOrder = ProfileStore.loadProfileOrder()
        val normalized = buildList {
            storedOrder.forEach { uuid ->
                if (uuid in existingSet && uuid !in this) add(uuid)
            }
            existing.forEach { uuid ->
                if (uuid !in this) add(uuid)
            }
        }

        if (normalized != storedOrder) {
            ProfileStore.saveProfileOrder(normalized)
        }

        return normalized
    }

    private fun appendProfileToOrder(uuid: UUID) {
        val order = ProfileStore.loadProfileOrder()
        if (uuid !in order) {
            ProfileStore.saveProfileOrder(order + uuid)
        }
    }

    private suspend fun resolveProfile(uuid: UUID): Profile? {
        val imported = ImportedDao.queryByUUID(uuid) ?: return null

        val active = store.activeProfile
        val name = ProfileNameUtils.resolveDisplayName(imported.name, imported.source)

        return Profile(
            uuid = uuid,
            name = name,
            type = imported.type,
            source = imported.source,
            active = active != null && imported.uuid == active,
            interval = imported.interval,
            upload = imported.upload,
            download = imported.download,
            total = imported.total,
            expire = imported.expire,
            updatedAt = resolveUpdatedAt(uuid),
            userAgent = imported.userAgent,
        )
    }

    private fun resolveUpdatedAt(uuid: UUID): Long {
        return context.importedDir.resolve(uuid.toString()).directoryLastModified ?: -1
    }

    private fun normalizeUserAgent(userAgent: String): String {
        val normalized = userAgent.trim()
        require(normalized.length <= MAX_USER_AGENT_LENGTH) {
            "User-Agent must not exceed $MAX_USER_AGENT_LENGTH characters"
        }
        require(normalized.all { it.code in 0x20..0x7e }) {
            "User-Agent must contain printable ASCII characters only"
        }
        return normalized
    }

    private companion object {
        const val MAX_USER_AGENT_LENGTH = 512
    }

}
