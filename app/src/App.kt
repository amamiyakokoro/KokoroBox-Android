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



package com.amamiyakokoro.box

import android.app.Application
import android.content.res.Configuration
import com.amamiyakokoro.box.common.runtime.StartupGate
import com.amamiyakokoro.box.common.util.AppLanguageManager
import com.amamiyakokoro.box.common.util.PlatformIdentifier
import com.amamiyakokoro.box.core.Global
import com.amamiyakokoro.box.core.util.StartupTaskCoordinator
import com.amamiyakokoro.box.data.controller.AppTrafficStatisticsCollector
import com.amamiyakokoro.box.data.controller.GeoXDataController
import com.amamiyakokoro.box.data.integration.kokoro.KokoroPreloadCoordinator
import com.amamiyakokoro.box.data.store.AppSettingsStore
import com.amamiyakokoro.box.di.appModule
import com.amamiyakokoro.box.integration.update.AppUpdateWorkScheduler
import com.amamiyakokoro.box.runtime.client.ProxyFacade
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.Koin
import com.amamiyakokoro.box.service.ProfileUpdateJobService
import timber.log.Timber

class App : Application() {
    private val startupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
        if (BuildConfig.DEBUG && Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }

        StartupGate.verify(this)
        Global.init(this)
        MMKV.initialize(this)

        val koinApp = startKoin {
            androidContext(this@App)
            modules(appModule)
        }
        val appSettingsStorage: AppSettingsStore = koinApp.koin.get()
        AppLanguageManager.apply(appSettingsStorage.appLanguage.value)

        val geoXDataController = koinApp.koin.get<GeoXDataController>()
        StartupTaskCoordinator.startGeoInitialization(startupScope) {
            geoXDataController.ensureGeoFiles()
        }
        appSettingsStorage.syncAppVersion(BuildConfig.VERSION_CODE)
        AppUpdateWorkScheduler.sync(this, appSettingsStorage.automaticUpdateCheckEnabled.value)
        scheduleDeferredStartupTasks(koinApp.koin)
        ProfileUpdateJobService.ensureScheduled(this)
        koinApp.koin.get<KokoroPreloadCoordinator>().preloadIfAuthenticated()

        PlatformIdentifier.getPlatformIdentifier()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AppLanguageManager.refreshSystemLanguage()
    }

    private fun scheduleDeferredStartupTasks(koin: Koin) {
        StartupTaskCoordinator.startRuntimeWarmup(startupScope) {
            StartupTaskCoordinator.awaitGeoInitialization()
            runCatching { koin.get<AppTrafficStatisticsCollector>() }
                .onFailure { Timber.w(it, "App traffic collector init skipped") }

            runCatching { koin.get<ProxyFacade>().awaitProxyGroupWarmUp() }
                .onFailure { Timber.w(it, "Proxy preview warm-up skipped") }

        }
    }
}
