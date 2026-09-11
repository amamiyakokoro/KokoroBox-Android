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

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.amamiyakokoro.box.common.util.AppLanguageManager
import com.amamiyakokoro.box.common.util.ProxyAutoStartHelper
import com.amamiyakokoro.box.core.util.AutoStartSessionGate
import com.amamiyakokoro.box.core.util.StartupTaskCoordinator
import com.amamiyakokoro.box.di.APPLICATION_SCOPE_NAME
import com.amamiyakokoro.box.data.model.AppColorTheme
import com.amamiyakokoro.box.data.integration.kokoro.KokoroPreloadCoordinator
import com.amamiyakokoro.box.data.integration.kokoro.KokoroRepository
import com.amamiyakokoro.box.data.integration.update.AutomaticAppUpdateChecker
import com.amamiyakokoro.box.integration.update.AppUpdateManager
import com.amamiyakokoro.box.integration.update.AppUpdateWorkScheduler
import com.amamiyakokoro.box.presentation.component.StartupBiometricContent
import com.amamiyakokoro.box.presentation.component.ToastDialogHost
import com.amamiyakokoro.box.presentation.component.AppSnackbarSurface
import com.amamiyakokoro.box.presentation.component.rememberStartupBiometricGateState
import com.amamiyakokoro.box.presentation.theme.DEFAULT_ACG_WALLPAPER_THEME_SEED_ARGB
import com.amamiyakokoro.box.presentation.theme.DEFAULT_CUSTOM_THEME_SEED_ARGB
import com.amamiyakokoro.box.presentation.theme.NavigationTransitions
import com.amamiyakokoro.box.presentation.theme.ProvideAndroidPlatformTheme
import com.amamiyakokoro.box.presentation.theme.YumeTheme
import com.amamiyakokoro.box.screen.onboarding.OnboardingLauncher
import com.amamiyakokoro.box.screen.about.AppUpdateDialog
import com.amamiyakokoro.box.screen.settings.AppSettingsViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.qualifier.named

class MainActivity : FragmentActivity() {

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
        private const val EXTRA_EXIT_UI_WHEN_BACKGROUND = "exit_ui_when_background"
        private val _pendingImportUrl = MutableStateFlow<String?>(null)
        val pendingImportUrl: StateFlow<String?> = _pendingImportUrl.asStateFlow()
        private val _kokoroAuthResult = MutableStateFlow<Boolean?>(null)
        val kokoroAuthResult: StateFlow<Boolean?> = _kokoroAuthResult.asStateFlow()
        fun clearPendingImportUrl() {
            _pendingImportUrl.value = null
        }
        fun clearKokoroAuthResult() {
            _kokoroAuthResult.value = null
        }
    }

    private val appSettingsStorage: com.amamiyakokoro.box.data.store.AppSettingsStore by inject()
    private val networkSettingsStorage: com.amamiyakokoro.box.data.store.NetworkSettingsStore by inject()
    private val profilesRepository: com.amamiyakokoro.box.runtime.client.ProfilesRepository by inject()
    private val proxyFacade: com.amamiyakokoro.box.runtime.client.ProxyFacade by inject()
    private val serviceCache: MMKV by inject(qualifier = named("service_cache"))
    private val applicationScope: CoroutineScope by inject(qualifier = named(APPLICATION_SCOPE_NAME))
    private val kokoroRepository: KokoroRepository by inject()
    private val kokoroPreloadCoordinator: KokoroPreloadCoordinator by inject()
    private val automaticAppUpdateChecker: AutomaticAppUpdateChecker by inject()
    private val appUpdateManager: AppUpdateManager by inject()

    override fun onStart() {
        super.onStart()
        kokoroPreloadCoordinator.preloadIfAuthenticated()
        automaticAppUpdateChecker.checkIfDue()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguageManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)
        applyExcludeFromRecents(appSettingsStorage.excludeFromRecents.value)
        applyScreenshotProtection(appSettingsStorage.screenshotProtectionEnabled.value)

        handleIntent(intent)

        if (!appSettingsStorage.initialSetupCompleted.value) {
            OnboardingLauncher.start(this, previewMode = false)
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION,
                )
            }
        }

        setContent {
            val appSettingsViewModel = koinViewModel<AppSettingsViewModel>()
            val themeMode = appSettingsViewModel.themeMode.state.collectAsStateWithLifecycle().value
            val colorTheme = appSettingsViewModel.colorTheme.state.collectAsStateWithLifecycle().value
            val themeSeedColorArgb = appSettingsViewModel.themeSeedColorArgb.state.collectAsStateWithLifecycle().value
            val acgWallpaperSeedColorArgb = appSettingsViewModel.acgWallpaperSeedColorArgb.state.collectAsStateWithLifecycle().value
            val acgWallpaperUri = appSettingsViewModel.acgWallpaperUri.state.collectAsStateWithLifecycle().value
            val effectiveThemeSeedColorArgb = if (colorTheme == AppColorTheme.AcgWallpaper) {
                if (acgWallpaperUri.isBlank() && acgWallpaperSeedColorArgb == DEFAULT_CUSTOM_THEME_SEED_ARGB) {
                    DEFAULT_ACG_WALLPAPER_THEME_SEED_ARGB
                } else {
                    acgWallpaperSeedColorArgb
                }
            } else {
                themeSeedColorArgb
            }
            val invertOnPrimaryColors = appSettingsViewModel.invertOnPrimaryColors.state.collectAsStateWithLifecycle().value
            val excludeFromRecents = appSettingsViewModel.excludeFromRecents.state.collectAsStateWithLifecycle().value
            val pageScale = appSettingsViewModel.pageScale.state.collectAsStateWithLifecycle().value
            val screenshotProtectionEnabled = appSettingsViewModel.screenshotProtectionEnabled.state.collectAsStateWithLifecycle().value
            val biometricUnlockEnabled by appSettingsViewModel.biometricUnlockEnabled.state.collectAsStateWithLifecycle()
            val automaticUpdateCheckEnabled by appSettingsViewModel.automaticUpdateCheckEnabled.state.collectAsStateWithLifecycle()
            val appUpdateChannel by appSettingsViewModel.appUpdateChannel.state.collectAsStateWithLifecycle()
            val availableUpdate by automaticAppUpdateChecker.availableUpdate.collectAsStateWithLifecycle()
            val updateInstallState by appUpdateManager.state.collectAsStateWithLifecycle()

            val biometricGateState = rememberStartupBiometricGateState(
                activity = this@MainActivity,
                biometricUnlockEnabled = biometricUnlockEnabled,
            )

            LaunchedEffect(excludeFromRecents) {
                this@MainActivity.applyExcludeFromRecents(excludeFromRecents)
            }

            LaunchedEffect(screenshotProtectionEnabled) {
                this@MainActivity.applyScreenshotProtection(screenshotProtectionEnabled)
            }

            LaunchedEffect(automaticUpdateCheckEnabled, appUpdateChannel) {
                automaticAppUpdateChecker.onEnabledChanged(automaticUpdateCheckEnabled)
                AppUpdateWorkScheduler.sync(this@MainActivity, automaticUpdateCheckEnabled)
            }

            ProvideAndroidPlatformTheme {
                val systemDensity = LocalDensity.current
                val scaledDensity = remember(systemDensity, pageScale) {
                    Density(systemDensity.density * pageScale, systemDensity.fontScale)
                }
                CompositionLocalProvider(LocalDensity provides scaledDensity) {
                    YumeTheme(
                        themeMode = themeMode,
                        colorTheme = colorTheme,
                        themeSeedColorArgb = effectiveThemeSeedColorArgb,
                        invertOnPrimaryColors = invertOnPrimaryColors,
                    ) {
                        if (!biometricGateState.isAuthenticated) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surface,
                            ) {
                                StartupBiometricContent(
                                    isAuthenticating = biometricGateState.isAuthenticating,
                                    biometricErrorMessage = biometricGateState.biometricErrorMessage,
                                    onRetry = biometricGateState.retryAuthentication,
                                    onExit = { finishAndRemoveTask() },
                                )
                            }
                        } else {
                            val navController = rememberNavController()

                            Surface(
                                modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface
                            ) {
                                AppSnackbarSurface(Modifier.fillMaxSize()) {
                                    DestinationsNavHost(
                                        navGraph = NavGraphs.root,
                                        navController = navController,
                                        defaultTransitions = NavigationTransitions.defaultStyle,
                                    )
                                    ToastDialogHost()
                                }
                            }
                            availableUpdate?.let { release ->
                                AppUpdateDialog(
                                    result = release,
                                    installState = updateInstallState,
                                    onDownloadAndInstall = appUpdateManager::downloadAndPrepare,
                                    onContinueInstall = appUpdateManager::installPreparedUpdate,
                                    onDismiss = {
                                        appUpdateManager.dismiss()
                                        automaticAppUpdateChecker.dismiss()
                                    },
                                )
                            }
                        }
                    }
                }
            }

        }

        applicationScope.launch {
            if (!AutoStartSessionGate.tryBeginForegroundAutoActions()) {
                return@launch
            }
            var handled = false
            try {
                StartupTaskCoordinator.awaitRuntimeWarmup()
                ProxyAutoStartHelper.checkAndAutoStart(
                    context = this@MainActivity,
                    proxyFacade = proxyFacade,
                    profilesRepository = profilesRepository,
                    appSettingsStorage = appSettingsStorage,
                    networkSettingsStorage = networkSettingsStorage,
                    serviceCache = serviceCache,
                )
                handled = true
            } finally {
                AutoStartSessionGate.finishForegroundAutoActions(markHandled = handled)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level < TRIM_MEMORY_UI_HIDDEN || isFinishing) {
            return
        }
        if (!appSettingsStorage.exitUiWhenBackground.value) {
            return
        }
        if (proxyFacade.runtimeSnapshot.value.running) {
            finishAndRemoveTask()
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun handleIntent(intent: Intent?) {
        intent?.let { safeIntent ->
            if (safeIntent.getBooleanExtra(EXTRA_EXIT_UI_WHEN_BACKGROUND, false)) {
                finishAndRemoveTask()
                return
            }
            safeIntent.data?.let { uri ->
                val scheme = uri.scheme
                if (scheme.equals("kokoro", ignoreCase = true)) {
                    safeIntent.data = null
                    // Exchange survives Activity recreation. The session strictly validates and
                    // atomically consumes the persisted pending login before sending any code.
                    applicationScope.launch {
                        val authenticated = runCatching {
                            kokoroRepository.handleOAuthCallback(uri)
                        }.onFailure {
                            if (it is kotlinx.coroutines.CancellationException) throw it
                            Timber.w("Kokoro OAuth callback failed; sign in again")
                        }.isSuccess
                        _kokoroAuthResult.value = authenticated
                        if (authenticated) kokoroPreloadCoordinator.preloadIfAuthenticated()
                    }
                    return
                }
                if (scheme == "clash" || scheme == "clashmeta") {
                    val host = uri.host
                    if (host == "install-config") {
                        val configUrl = uri.getQueryParameter("url")
                        if (!configUrl.isNullOrBlank()) {
                            _pendingImportUrl.value = configUrl
                        }
                    }
                }
            }
        }
    }

    private fun applyScreenshotProtection(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    @Suppress("DEPRECATION")
    private fun applyExcludeFromRecents(exclude: Boolean) {

        runCatching {
            val am = getSystemService(ActivityManager::class.java) ?: return@runCatching
            val currentTaskId = taskId
            val task = am.appTasks.firstOrNull { appTask: ActivityManager.AppTask ->
                val taskInfo = appTask.taskInfo ?: return@firstOrNull false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    taskInfo.taskId == currentTaskId
                } else {
                    taskInfo.id == currentTaskId
                }
            }
            task?.setExcludeFromRecents(exclude)
        }
    }
}
