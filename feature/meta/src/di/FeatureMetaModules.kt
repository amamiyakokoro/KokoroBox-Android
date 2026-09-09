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



package com.amamiyakokoro.box.di

import com.amamiyakokoro.box.data.store.TrafficStatisticsStore
import com.amamiyakokoro.box.feature.meta.presentation.viewmodel.ConnectionViewModel
import com.amamiyakokoro.box.feature.meta.presentation.viewmodel.TrafficStatisticsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureMetaViewModelModule = module {
    viewModel { ConnectionViewModel() }
    viewModel { TrafficStatisticsViewModel(get<TrafficStatisticsStore>()) }
}

val featureMetaModules = listOf(
    featureMetaViewModelModule,
)
