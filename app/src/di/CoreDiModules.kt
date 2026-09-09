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

import com.amamiyakokoro.box.data.controller.AccessControlController
import com.amamiyakokoro.box.data.controller.AcgWallpaperStorage
import com.amamiyakokoro.box.data.controller.AppSettingsController
import com.amamiyakokoro.box.data.controller.NetworkSettingsController
import com.amamiyakokoro.box.data.controller.RuntimeOverrideController
import com.amamiyakokoro.box.data.controller.UserSettingsBackupController
import com.amamiyakokoro.box.data.controller.ActiveProfileOverrideReloader
import com.amamiyakokoro.box.data.controller.AppIdentityResolver
import com.amamiyakokoro.box.data.controller.AppTrafficStatisticsCollector
import com.amamiyakokoro.box.data.controller.GeoXDataController
import com.amamiyakokoro.box.data.store.LogStore
import com.amamiyakokoro.box.data.gateway.LogRecordGateway
import com.amamiyakokoro.box.data.gateway.NetworkInfoService
import com.amamiyakokoro.box.data.store.OverrideConfigProvider
import com.amamiyakokoro.box.data.store.OverrideConfigStore
import com.amamiyakokoro.box.data.controller.OverrideResolver
import com.amamiyakokoro.box.data.controller.OverrideService
import com.amamiyakokoro.box.data.store.ProfileBindingProvider
import com.amamiyakokoro.box.data.store.ProfileBindingStore
import com.amamiyakokoro.box.data.controller.ProvidersController
import com.amamiyakokoro.box.data.store.AppSettingsStore
import com.amamiyakokoro.box.data.store.MMKVProvider
import com.amamiyakokoro.box.data.store.NetworkSettingsStore
import com.amamiyakokoro.box.data.store.ProfileLinksStore
import com.amamiyakokoro.box.data.store.ProxyDisplaySettingsStore
import com.amamiyakokoro.box.data.store.TrafficStatisticsStore
import com.amamiyakokoro.box.runtime.client.ProfilesRepository
import com.amamiyakokoro.box.runtime.client.ProxyFacade
import com.amamiyakokoro.box.runtime.client.RuntimeStateMapper
import com.amamiyakokoro.box.runtime.client.root.RootTunReloadScheduler
import com.amamiyakokoro.box.service.ServicePowerController
import com.amamiyakokoro.box.domain.model.TrafficData
import com.amamiyakokoro.box.common.util.AppLanguageManager
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val APPLICATION_SCOPE_NAME = "applicationScope"

val appFoundationModule = module {
    single<CoroutineScope>(named(APPLICATION_SCOPE_NAME)) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    single { MMKVProvider() }
    single<MMKV>(named("profiles")) { get<MMKVProvider>().getMMKV("profiles") }
    single<MMKV>(named("settings")) { get<MMKVProvider>().getMMKV("settings") }
    single<MMKV>(named("network_settings")) { get<MMKVProvider>().getMMKV("network_settings") }
    single<MMKV>(named("proxy_display")) { get<MMKVProvider>().getMMKV("proxy_display") }
    single<MMKV>(named("traffic_statistics")) { get<MMKVProvider>().getMMKV("traffic_statistics") }
    single<MMKV>(named("profile_links")) { get<MMKVProvider>().getMMKV("profile_links") }
    single<MMKV>(named("service_cache")) { get<MMKVProvider>().getMMKV("service_cache") }
    single<MMKV>(named("override_bindings")) { get<MMKVProvider>().getMMKV("override_bindings") }

    single { AppSettingsStore(get<MMKV>(named("settings"))) }
    single { NetworkSettingsStore(get(named("network_settings"))) }
    single { ProfileLinksStore(get(named("profile_links"))) }
    single { ProxyDisplaySettingsStore(get(named("proxy_display"))) }
    single { TrafficStatisticsStore(get(named("traffic_statistics"))) }
    single { AcgWallpaperStorage(androidContext()) }
    single { OverrideConfigStore(androidContext()) }
    single { UserSettingsBackupController(get(), get(), get(), get(), get(), get()) }
    single { GeoXDataController(androidContext()) }
}

val appDataRuntimeModule = module {
    single { AppSettingsController(get(), applyLanguage = AppLanguageManager::apply) }
    single {
        val proxyFacade = get<ProxyFacade>()
        NetworkSettingsController(
            store = get(),
            isRunning = { RuntimeStateMapper.isActuallyRunning(proxyFacade.runtimeSnapshot.value) },
            restartProxy = { mode -> proxyFacade.startProxy(mode) },
        )
    }
    single {
        val proxyFacade = get<ProxyFacade>()
        AccessControlController(
            store = get(),
            isRunning = { proxyFacade.isRunning.value },
            resolveActiveMode = { RuntimeStateMapper.modeForOwner(proxyFacade.runtimeSnapshot.value.owner) },
            restartProxy = { mode -> proxyFacade.startProxy(mode) },
        )
    }
    single { LogStore(androidApplication(), get()) }
    single { NetworkInfoService(context = androidContext()) }
    single {
        val profilesRepository = get<ProfilesRepository>()
        RuntimeOverrideController(
            configStore = get(),
            queryActiveProfile = { profilesRepository.queryActiveProfile() },
        )
    }
    single {
        val appContext = androidContext()
        ProvidersController(appContext) {
            com.amamiyakokoro.box.remote.ServiceClient.connect(appContext)
            com.amamiyakokoro.box.remote.ServiceClient.clash().queryProviders()
        }
    }

    single<OverrideConfigProvider> { get<OverrideConfigStore>() }

    single { ProfileBindingStore(androidContext()) }
    single<ProfileBindingProvider> { get<ProfileBindingStore>() }

    single { OverrideResolver(get(), get()) }
    single {
        val appContext = androidContext()
        OverrideService(appContext, get()) {
            RootTunReloadScheduler.schedule(
                appContext,
                RootTunReloadScheduler.Reason.PROFILE_OVERRIDE_CHANGED,
            )
        }
    }
    single {
        val profilesRepository = get<ProfilesRepository>()
        ActiveProfileOverrideReloader(
            queryActiveProfile = { profilesRepository.queryActiveProfile() },
            bindingProvider = get(),
            overrideService = get(),
        )
    }

    single { ServicePowerController(androidContext()).also(ServicePowerController::start) }
    single { ProxyFacade(androidContext(), get<ServicePowerController>().screenOn) }
    single { AppIdentityResolver(androidContext()) }
    single { ProfilesRepository(androidContext()) }
    single {
        val proxyFacade = get<ProxyFacade>()
        AppTrafficStatisticsCollector(
            isRunningFlow = proxyFacade.isRunning,
            currentProfileId = { proxyFacade.currentProfile.value?.uuid?.toString() },
            trafficStatisticsStore = get(),
            appIdentityResolver = get(),
            queryTrafficTotal = {
                TrafficData.from(proxyFacade.queryTrafficTotal())
            },
            queryConnections = {
                proxyFacade.queryConnections()
            },
            queryActiveProfileId = {
                proxyFacade.refreshCurrentProfile()
                proxyFacade.currentProfile.value?.uuid?.toString()
            },
            screenOnFlow = get<ServicePowerController>().screenOn,
        )
    }
}

val coreDiModules: List<Module> = listOf(
    appFoundationModule,
    appDataRuntimeModule,
)
