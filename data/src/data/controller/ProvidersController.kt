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



package com.amamiyakokoro.box.data.controller

import com.amamiyakokoro.box.core.Clash
import com.amamiyakokoro.box.core.model.Provider

class ProvidersController(
    private val queryProvidersAction: suspend () -> List<Provider>,
) {

    suspend fun queryProviders(): Result<List<Provider>> {
        return try {
            Result.success(queryProvidersAction())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProvider(provider: Provider): Result<Unit> {
        return updateProviderInternal(provider.type, provider.name)
    }

    suspend fun updateAllProviders(providers: List<Provider>): Result<UpdateProvidersResult> {
        if (providers.isEmpty()) return Result.success(UpdateProvidersResult(emptyList()))

        val failed = mutableListOf<String>()
        providers.forEach { provider ->
            val result = updateProviderInternal(provider.type, provider.name)
            if (result.isFailure) {
                failed.add(provider.name)
            }
        }
        return Result.success(UpdateProvidersResult(failed))
    }

    private suspend fun updateProviderInternal(type: Provider.Type, name: String): Result<Unit> {
        return try {
            Clash.updateProvider(type, name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    data class UpdateProvidersResult(
        val failedProviders: List<String>
    )
}
