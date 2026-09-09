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

import com.amamiyakokoro.box.data.gateway.LogRecordGateway
import com.amamiyakokoro.box.data.integration.kokoro.KokoroCustomRulesClient
import com.amamiyakokoro.box.data.integration.kokoro.KokoroPreloadCoordinator
import com.amamiyakokoro.box.data.integration.kokoro.KokoroRepository
import com.amamiyakokoro.box.data.integration.update.GitHubReleaseClient
import com.amamiyakokoro.box.data.integration.update.AutomaticAppUpdateChecker
import com.amamiyakokoro.box.data.integration.update.AppUpdateDownloader
import com.amamiyakokoro.box.data.integration.speedtest.CloudflareSpeedTestClient
import com.amamiyakokoro.box.BuildConfig
import com.amamiyakokoro.box.common.update.ApkUpdateVerifier
import com.amamiyakokoro.box.common.update.PackageUpdateInstaller
import com.amamiyakokoro.box.integration.update.AppUpdateManager
import com.amamiyakokoro.box.integration.update.AppForegroundTracker
import com.amamiyakokoro.box.integration.update.AppUpdateInstallNotifier
import com.amamiyakokoro.box.integration.update.RootUpdateInstaller
import com.amamiyakokoro.box.integration.update.ShizukuUpdateInstaller
import com.amamiyakokoro.box.screen.about.AppUpdateViewModel
import com.amamiyakokoro.box.screen.home.HomeViewModel
import com.amamiyakokoro.box.screen.log.LogViewModel
import com.amamiyakokoro.box.screen.profiles.ProfilesViewModel
import com.amamiyakokoro.box.screen.profiles.KokoroAccountClient
import com.amamiyakokoro.box.screen.settings.AccessControlViewModel
import com.amamiyakokoro.box.screen.settings.AppDataManagementViewModel
import com.amamiyakokoro.box.screen.settings.AppSettingsViewModel
import com.amamiyakokoro.box.screen.settings.KokoroCustomRulesViewModel
import com.amamiyakokoro.box.screen.settings.KokoroSettingsViewModel
import com.amamiyakokoro.box.screen.settings.NetworkSettingsViewModel
import com.amamiyakokoro.box.screen.settings.ResourceDownloadClient
import com.amamiyakokoro.box.screen.settings.CloudflareSpeedTestViewModel
import com.amamiyakokoro.box.service.LogRecordServiceGateway
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appIntegrationModule = module {
    single { GitHubReleaseClient() }
    single { AppUpdateDownloader(androidApplication()) }
    single { ApkUpdateVerifier(androidApplication()) }
    single { PackageUpdateInstaller(androidApplication()) }
    single { ShizukuUpdateInstaller(androidApplication()) }
    single { RootUpdateInstaller() }
    single { AppForegroundTracker(androidApplication()) }
    single { AppUpdateInstallNotifier(androidApplication()) }
    single {
        AppUpdateManager(
            context = androidApplication(),
            downloader = get(),
            verifier = get(),
            installer = get(),
            shizukuInstaller = get(),
            rootInstaller = get(),
            settings = get(),
            foregroundTracker = get(),
            installNotifier = get(),
            applicationScope = get(named(APPLICATION_SCOPE_NAME)),
        )
    }
    single {
        AutomaticAppUpdateChecker(
            client = get(),
            settings = get(),
            applicationScope = get(named(APPLICATION_SCOPE_NAME)),
            currentVersionName = BuildConfig.VERSION_NAME,
            currentVersionCode = BuildConfig.VERSION_CODE,
        )
    }
    single<LogRecordGateway> { LogRecordServiceGateway() }
    single { KokoroAccountClient(androidApplication()) }
    single { KokoroCustomRulesClient(androidApplication()) }
    single { KokoroRepository(get(), get()) }
    single { KokoroPreloadCoordinator(get(), get(named(APPLICATION_SCOPE_NAME))) }
    single { ResourceDownloadClient(androidApplication(), get()) }
    single { CloudflareSpeedTestClient() }
}

val appViewModelModule = module {
    viewModel { AppUpdateViewModel(get(), get(), get()) }
    viewModel { AppSettingsViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(androidApplication(), get(), get(), get(), get(), get()) }
    viewModel { ProfilesViewModel(androidApplication(), get(), get(), get()) }
    viewModel { NetworkSettingsViewModel(androidApplication(), get(), get(), get(), get(), get()) }
    viewModel { AccessControlViewModel(androidApplication(), get(), get()) }
    viewModel { AppDataManagementViewModel(get(), get()) }
    viewModel { LogViewModel(get()) }
    viewModel { KokoroCustomRulesViewModel(get()) }
    viewModel { KokoroSettingsViewModel(get()) }
    viewModel { CloudflareSpeedTestViewModel(get()) }
}

val appModule: List<Module> = coreDiModules + listOf(
    appIntegrationModule,
    appViewModelModule,
) + featureProxyModules + featureOverrideModules + featureMetaModules
