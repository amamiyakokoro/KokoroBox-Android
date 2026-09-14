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


package com.amamiyakokoro.box.screen.settings
import com.amamiyakokoro.box.presentation.theme.UiDp
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.common.util.AppIconHelper
import com.amamiyakokoro.box.common.util.BiometricHelper
import com.amamiyakokoro.box.common.util.toast
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.data.model.AppColorTheme
import com.amamiyakokoro.box.data.model.AppLanguage
import com.amamiyakokoro.box.data.model.AppUpdateChannel
import com.amamiyakokoro.box.data.model.AppUpdateInstallMethod
import com.amamiyakokoro.box.data.model.ThemeMode
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.AppTextFieldDialog
import com.amamiyakokoro.box.presentation.component.PreferenceArrowItem
import com.amamiyakokoro.box.presentation.component.PreferenceEnumItem
import com.amamiyakokoro.box.presentation.component.PreferenceSwitchItem
import com.amamiyakokoro.box.presentation.component.PreferenceValueItem
import com.amamiyakokoro.box.presentation.component.ScreenLazyColumn
import com.amamiyakokoro.box.presentation.component.Title
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.presentation.component.WarningBottomSheet
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.component.rememberStandalonePageMainPadding
import com.amamiyakokoro.box.screen.settings.component.ThemeColorPickerItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AcgWallpaperCropScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri
import kotlin.math.abs

@Composable
@Destination<RootGraph>
fun AppSettingsScreen(
    navigator: DestinationsNavigator,
) {
    val viewModel = koinViewModel<AppSettingsViewModel>()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(title = stringResource(LocaleR.string.app_settings_title))
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
        ) {
            item { AppBehaviorSettingsSection(viewModel) }
            item { AppInterfaceSettingsSection(viewModel) }
            item { AppPrivacySettingsSection(viewModel) }
            item { AppServiceSettingsSection(viewModel) }
            item {
                AppExperimentalSettingsSection(
                    viewModel = viewModel,
                    navigator = navigator,
                )
            }
        }
    }
}

@Composable
private fun AppBehaviorSettingsSection(viewModel: AppSettingsViewModel) {
    val automaticRestart by viewModel.automaticRestart.state.collectAsStateWithLifecycle()
    val autoUpdateCurrentProfileOnStart by viewModel.autoUpdateCurrentProfileOnStart.state.collectAsStateWithLifecycle()
    val automaticUpdateCheckEnabled by viewModel.automaticUpdateCheckEnabled.state.collectAsStateWithLifecycle()
    val appUpdateChannel by viewModel.appUpdateChannel.state.collectAsStateWithLifecycle()

    Title(stringResource(LocaleR.string.app_settings_section_behavior))
    Card {
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_behavior_auto_start_title),
            summary = stringResource(LocaleR.string.app_settings_behavior_auto_start_summary),
            checked = automaticRestart,
            onCheckedChange = viewModel::onAutomaticRestartChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_behavior_auto_update_on_start_title),
            summary = stringResource(LocaleR.string.app_settings_behavior_auto_update_on_start_summary),
            checked = autoUpdateCurrentProfileOnStart,
            onCheckedChange = viewModel::onAutoUpdateCurrentProfileOnStartChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_behavior_automatic_update_check_title),
            summary = stringResource(LocaleR.string.app_settings_behavior_automatic_update_check_summary),
            checked = automaticUpdateCheckEnabled,
            onCheckedChange = viewModel::onAutomaticUpdateCheckChange,
        )
        PreferenceEnumItem(
            title = stringResource(LocaleR.string.app_settings_behavior_update_channel_title),
            summary = stringResource(LocaleR.string.app_settings_behavior_update_channel_summary),
            currentValue = appUpdateChannel,
            items = listOf(
                stringResource(LocaleR.string.app_settings_behavior_update_channel_stable),
                stringResource(LocaleR.string.app_settings_behavior_update_channel_nightly),
            ),
            values = AppUpdateChannel.entries,
            onValueChange = viewModel::onAppUpdateChannelChange,
        )
    }
}

@Composable
private fun AppInterfaceSettingsSection(viewModel: AppSettingsViewModel) {
    val themeMode by viewModel.themeMode.state.collectAsStateWithLifecycle()
    val colorTheme by viewModel.colorTheme.state.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.state.collectAsStateWithLifecycle()
    val themeSeedColorArgb by viewModel.themeSeedColorArgb.state.collectAsStateWithLifecycle()
    val invertOnPrimaryColors by viewModel.invertOnPrimaryColors.state.collectAsStateWithLifecycle()
    val bottomBarAutoHide by viewModel.bottomBarAutoHide.state.collectAsStateWithLifecycle()
    val bottomBarUseLegacyStyle by viewModel.bottomBarUseLegacyStyle.state.collectAsStateWithLifecycle()
    val homeUseFabProxyControl by viewModel.homeUseFabProxyControl.state.collectAsStateWithLifecycle()
    val pageScale by viewModel.pageScale.state.collectAsStateWithLifecycle()

    Title(stringResource(LocaleR.string.app_settings_interface_color_theme_title))
    Card {
        PreferenceEnumItem(
            title = stringResource(LocaleR.string.app_settings_interface_theme_mode_title),
            summary = stringResource(LocaleR.string.app_settings_interface_theme_mode_summary),
            currentValue = themeMode,
            items = listOf(
                stringResource(LocaleR.string.app_settings_interface_theme_mode_system),
                stringResource(LocaleR.string.app_settings_interface_theme_mode_light),
                stringResource(LocaleR.string.app_settings_interface_theme_mode_dark),
            ),
            values = ThemeMode.entries,
            onValueChange = viewModel::onThemeModeChange,
        )
        PreferenceEnumItem(
            title = stringResource(LocaleR.string.app_settings_interface_color_theme_mode_title),
            summary = stringResource(LocaleR.string.app_settings_interface_color_theme_mode_summary),
            currentValue = colorTheme,
            items = listOf(
                stringResource(LocaleR.string.app_settings_interface_color_theme_mode_monet),
                stringResource(LocaleR.string.app_settings_interface_color_theme_mode_custom),
                stringResource(LocaleR.string.app_settings_interface_color_theme_mode_acg_wallpaper),
            ),
            values = listOf(
                AppColorTheme.MonetDynamic,
                AppColorTheme.Custom,
                AppColorTheme.AcgWallpaper,
            ),
            onValueChange = viewModel::onColorThemeChange,
        )
        if (colorTheme == AppColorTheme.Custom) {
            ThemeColorPickerItem(
                themeSeedColorArgb = themeSeedColorArgb,
                onThemeSeedColorChange = viewModel::onThemeSeedColorChange,
            )
        } else if (colorTheme == AppColorTheme.AcgWallpaper) {
            PreferenceValueItem(
                title = stringResource(LocaleR.string.app_settings_interface_color_theme_picker_title),
                summary = stringResource(LocaleR.string.app_settings_interface_color_theme_acg_wallpaper_summary),
                onClick = { },
            )
        } else {
            PreferenceValueItem(
                title = stringResource(LocaleR.string.app_settings_interface_color_theme_picker_title),
                summary = stringResource(LocaleR.string.app_settings_interface_color_theme_dynamic_summary),
                onClick = { },
            )
        }
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_interface_theme_color_polarity_invert_title),
            summary = stringResource(LocaleR.string.app_settings_interface_theme_color_polarity_invert_summary),
            checked = invertOnPrimaryColors,
            onCheckedChange = viewModel::onInvertOnPrimaryColorsChange,
        )
    }
    Title(stringResource(LocaleR.string.app_settings_section_interface))
    Card {
        PreferenceEnumItem(
            title = stringResource(LocaleR.string.app_settings_interface_language_title),
            summary = stringResource(LocaleR.string.app_settings_interface_language_summary),
            currentValue = appLanguage,
            items = listOf(
                stringResource(LocaleR.string.app_settings_interface_language_system),
                stringResource(LocaleR.string.app_settings_interface_language_chinese),
                stringResource(LocaleR.string.app_settings_interface_language_traditional_chinese),
                stringResource(LocaleR.string.app_settings_interface_language_english),
            ),
            values = AppLanguage.entries,
            onValueChange = viewModel::onAppLanguageChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_interface_auto_hide_navbar_title),
            summary = stringResource(LocaleR.string.app_settings_interface_auto_hide_navbar_summary),
            checked = bottomBarAutoHide,
            onCheckedChange = viewModel::onBottomBarAutoHideChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_interface_legacy_navbar_style_title),
            summary = stringResource(LocaleR.string.app_settings_interface_legacy_navbar_style_summary),
            checked = bottomBarUseLegacyStyle,
            onCheckedChange = viewModel::onBottomBarUseLegacyStyleChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_interface_home_control_fab_title),
            summary = stringResource(LocaleR.string.app_settings_interface_home_control_fab_summary),
            checked = homeUseFabProxyControl,
            onCheckedChange = viewModel::onHomeUseFabProxyControlChange,
        )
        PageScalePreferenceItem(
            pageScale = pageScale,
            onApply = viewModel::onPageScaleChange,
        )
    }
}

@Composable
private fun AppPrivacySettingsSection(viewModel: AppSettingsViewModel) {
    val context = LocalContext.current
    val excludeFromRecents by viewModel.excludeFromRecents.state.collectAsStateWithLifecycle()

    Title(stringResource(LocaleR.string.app_settings_section_privacy))
    Card {
        BiometricProtectedPreferenceSwitch(
            checkedFlow = viewModel.biometricUnlockEnabled.state,
            title = stringResource(LocaleR.string.app_settings_privacy_biometric_unlock_title),
            summary = stringResource(LocaleR.string.app_settings_privacy_biometric_unlock_summary),
            enableTitle = stringResource(LocaleR.string.app_settings_privacy_biometric_dialog_title_enable),
            disableTitle = stringResource(LocaleR.string.app_settings_privacy_biometric_dialog_title_disable),
            onConfirmedChange = viewModel::onBiometricUnlockEnabledChange,
        )
        BiometricProtectedPreferenceSwitch(
            checkedFlow = viewModel.screenshotProtectionEnabled.state,
            title = stringResource(LocaleR.string.app_settings_privacy_screenshot_protection_title),
            summary = stringResource(LocaleR.string.app_settings_privacy_screenshot_protection_summary),
            enableTitle = stringResource(LocaleR.string.app_settings_privacy_screenshot_dialog_title_enable),
            disableTitle = stringResource(LocaleR.string.app_settings_privacy_screenshot_dialog_title_disable),
            onConfirmedChange = viewModel::onScreenshotProtectionEnabledChange,
        )
        HideAppIconPreferenceItem(
            hideAppIconFlow = viewModel.hideAppIcon.state,
            onHideAppIconChange = viewModel::onHideAppIconChange,
            context = context,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_privacy_hide_from_recents_title),
            summary = stringResource(LocaleR.string.app_settings_privacy_hide_from_recents_summary),
            checked = excludeFromRecents,
            onCheckedChange = viewModel::onExcludeFromRecentsChange,
        )
    }
}

@Composable
private fun AppServiceSettingsSection(viewModel: AppSettingsViewModel) {
    val context = LocalContext.current
    val showTrafficNotification by viewModel.showTrafficNotification.state.collectAsStateWithLifecycle()
    val singleNodeTest by viewModel.singleNodeTest.state.collectAsStateWithLifecycle()
    val exitUiWhenBackground by viewModel.exitUiWhenBackground.state.collectAsStateWithLifecycle()

    val unknownError = stringResource(LocaleR.string.util_error_unknown_error)
    Title(stringResource(LocaleR.string.app_settings_section_service))
    Card {
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_service_section_traffic_notification_title),
            summary = stringResource(LocaleR.string.app_settings_service_section_traffic_notification_summary),
            checked = showTrafficNotification,
            onCheckedChange = viewModel::onShowTrafficNotificationChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_service_section_single_node_test_title),
            summary = stringResource(LocaleR.string.app_settings_service_section_single_node_test_summary),
            checked = singleNodeTest,
            onCheckedChange = viewModel::onSingleNodeTestChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_service_section_exit_ui_when_background_title),
            summary = stringResource(LocaleR.string.app_settings_service_section_exit_ui_when_background_summary),
            checked = exitUiWhenBackground,
            onCheckedChange = viewModel::onExitUiWhenBackgroundChange,
        )
        PreferenceArrowItem(
            title = stringResource(LocaleR.string.app_settings_service_section_battery_optimization_title),
            onClick = {
                if (!openBatteryOptimizationSettings(context)) {
                    context.toast(unknownError)
                }
            },
        )
    }
}

@Composable
private fun AppExperimentalSettingsSection(
    viewModel: AppSettingsViewModel,
    navigator: DestinationsNavigator,
) {
    val acgMainUiEnabled by viewModel.acgMainUiEnabled.state.collectAsStateWithLifecycle()
    val acgSidebarExpanded by viewModel.acgSidebarExpanded.state.collectAsStateWithLifecycle()
    val acgWallpaperUri by viewModel.acgWallpaperUri.state.collectAsStateWithLifecycle()
    val acgWallpaperZoom by viewModel.acgWallpaperZoom.state.collectAsStateWithLifecycle()
    val acgWallpaperBiasX by viewModel.acgWallpaperBiasX.state.collectAsStateWithLifecycle()
    val acgWallpaperBiasY by viewModel.acgWallpaperBiasY.state.collectAsStateWithLifecycle()
    val appUpdateInstallMethod by viewModel.appUpdateInstallMethod.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resetWallpaperSuccess = stringResource(LocaleR.string.app_settings_experimental_reset_wallpaper_success)
    Title(stringResource(LocaleR.string.app_settings_section_experimental))
    Card {
        PreferenceEnumItem(
            title = stringResource(LocaleR.string.app_settings_behavior_update_install_method_title),
            summary = stringResource(LocaleR.string.app_settings_behavior_update_install_method_summary),
            currentValue = appUpdateInstallMethod,
            items = listOf(
                stringResource(LocaleR.string.app_settings_behavior_update_install_method_system),
                stringResource(LocaleR.string.app_settings_behavior_update_install_method_shizuku),
                stringResource(LocaleR.string.app_settings_behavior_update_install_method_root),
            ),
            values = AppUpdateInstallMethod.entries,
            onValueChange = viewModel::onAppUpdateInstallMethodChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_experimental_acg_home_title),
            summary = stringResource(LocaleR.string.app_settings_experimental_acg_home_summary),
            checked = acgMainUiEnabled,
            onCheckedChange = viewModel::onAcgMainUiEnabledChange,
        )
        PreferenceSwitchItem(
            title = stringResource(LocaleR.string.app_settings_experimental_acg_sidebar_expanded_title),
            summary = stringResource(LocaleR.string.app_settings_experimental_acg_sidebar_expanded_summary),
            checked = acgSidebarExpanded,
            onCheckedChange = viewModel::onAcgSidebarExpandedChange,
        )
        AcgWallpaperPreferenceItem(
            navigator = navigator,
            wallpaperZoom = acgWallpaperZoom,
            wallpaperBiasX = acgWallpaperBiasX,
            wallpaperBiasY = acgWallpaperBiasY,
        )
        PreferenceValueItem(
            title = stringResource(LocaleR.string.app_settings_experimental_reset_wallpaper_title),
            summary = stringResource(LocaleR.string.app_settings_experimental_reset_wallpaper_summary),
            onClick = {
                if (acgWallpaperUri.isNotBlank()) {
                    runCatching {
                        context.contentResolver.releasePersistableUriPermission(
                            acgWallpaperUri.toUri(),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION,
                        )
                    }
                }
                viewModel.clearAcgWallpaperUri()
                context.toast(resetWallpaperSuccess)
            },
        )
    }
}

@Composable
private fun BiometricProtectedPreferenceSwitch(
    checkedFlow: kotlinx.coroutines.flow.StateFlow<Boolean>,
    title: String,
    summary: String,
    enableTitle: String,
    disableTitle: String,
    onConfirmedChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val checked by checkedFlow.collectAsStateWithLifecycle()
    val showUnavailableDialogState = remember { mutableStateOf(false) }
    var unavailableMessage by remember { mutableStateOf("") }
    val biometricUnavailableMessage = stringResource(
        LocaleR.string.app_settings_privacy_biometric_unavailable_message,
    )

    PreferenceSwitchItem(
        title = title,
        summary = summary,
        checked = checked,
        onCheckedChange = { targetState ->
            requestBiometricConfirmation(
                context = context,
                title = if (targetState) enableTitle else disableTitle,
                allowBypassWhenUnavailable = !targetState,
                unavailableMessage = biometricUnavailableMessage,
                onUnavailable = { message ->
                    unavailableMessage = message
                    showUnavailableDialogState.value = true
                },
                onSuccess = { onConfirmedChange(targetState) },
            )
        },
    )

    WarningBottomSheet(
        show = showUnavailableDialogState,
        title = stringResource(LocaleR.string.app_settings_privacy_biometric_unavailable_title),
        messages = listOf(
            unavailableMessage.ifBlank {
                biometricUnavailableMessage
            },
        ),
        onConfirm = { showUnavailableDialogState.value = false },
    )
}

@Composable
private fun HideAppIconPreferenceItem(
    hideAppIconFlow: kotlinx.coroutines.flow.StateFlow<Boolean>,
    onHideAppIconChange: (Boolean) -> Unit,
    context: android.content.Context,
) {
    val hideAppIcon by hideAppIconFlow.collectAsStateWithLifecycle()
    val showHideIconDialogState = remember { mutableStateOf(false) }

    PreferenceSwitchItem(
        title = stringResource(LocaleR.string.app_settings_privacy_hide_icon_title),
        summary = stringResource(LocaleR.string.app_settings_privacy_hide_icon_summary),
        checked = hideAppIcon,
        onCheckedChange = { checked ->
            if (checked) {
                showHideIconDialogState.value = true
            } else {
                onHideAppIconChange(false)
                AppIconHelper.toggleIcon(context, false)
            }
        },
    )

    WarningBottomSheet(
        show = showHideIconDialogState,
        title = stringResource(LocaleR.string.app_settings_warning_dialog_title),
        messages = listOf(
            stringResource(LocaleR.string.app_settings_warning_dialog_hide_icon_msg1),
            stringResource(LocaleR.string.app_settings_warning_dialog_hide_icon_msg2),
        ),
        onConfirm = {
            onHideAppIconChange(true)
            AppIconHelper.toggleIcon(context, true)
        },
    )
}

@Composable
private fun AcgWallpaperPreferenceItem(
    navigator: DestinationsNavigator,
    wallpaperZoom: Float,
    wallpaperBiasX: Float,
    wallpaperBiasY: Float,
) {
    val context = LocalContext.current
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        navigator.navigate(
            AcgWallpaperCropScreenDestination(
                wallpaperUri = uri.toString(),
                initialZoom = wallpaperZoom,
                initialBiasX = wallpaperBiasX,
                initialBiasY = wallpaperBiasY,
            ),
        ) {
            launchSingleTop = true
        }
    }

    PreferenceArrowItem(
        title = stringResource(LocaleR.string.app_settings_experimental_wallpaper_title),
        summary = stringResource(LocaleR.string.app_settings_experimental_wallpaper_summary),
        onClick = {
            wallpaperPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
    )
}

private fun requestBiometricConfirmation(
    context: android.content.Context,
    title: String,
    allowBypassWhenUnavailable: Boolean = false,
    unavailableMessage: String,
    onUnavailable: (String) -> Unit,
    onSuccess: () -> Unit,
) {
    val activity = BiometricHelper.findFragmentActivity(context)
    if (activity == null) {
        if (allowBypassWhenUnavailable) {
            onSuccess()
        } else {
            onUnavailable(unavailableMessage)
        }
        return
    }

    if (!BiometricHelper.canAuthenticate(activity)) {
        if (allowBypassWhenUnavailable) {
            onSuccess()
        } else {
            onUnavailable(BiometricHelper.getAuthenticationStatusMessage(activity))
        }
        return
    }

    BiometricHelper.authenticate(
        activity = activity,
        title = title,
        onSuccess = onSuccess,
    )
}

private fun openBatteryOptimizationSettings(
    context: android.content.Context,
): Boolean {
    val packageName = context.packageName
    val packageUri = "package:$packageName".toUri()
    val appLabel = runCatching {
        context.applicationInfo.loadLabel(context.packageManager).toString()
    }.getOrDefault(packageName)

    val intents = buildList {
        add(
            Intent("android.settings.APP_BATTERY_USAGE_SETTINGS").apply {
                data = packageUri
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        add(
            Intent("android.settings.APP_BATTERY_SETTINGS").apply {
                data = packageUri
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        add(
            Intent("miui.intent.action.POWER_HIDE_MODE_APP_CONFIG").apply {
                putExtra("package_name", packageName)
                putExtra("package_label", appLabel)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        add(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        add(
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    intents.forEach { intent ->
        if (runCatching { context.startActivity(intent) }.isSuccess) {
            return true
        }
    }
    return false
}

@Composable
private fun PageScalePreferenceItem(
    pageScale: Float,
    onApply: (Float) -> Unit,
) {
    var pageScaleLocal by remember(pageScale) { mutableFloatStateOf(pageScale) }
    var lastHapticScale by remember(pageScale) { mutableFloatStateOf(pageScale) }
    var lastHapticTimeMs by remember { mutableStateOf(0L) }
    var lastChangeScale by remember(pageScale) { mutableFloatStateOf(pageScale) }
    var lastChangeTimeMs by remember { mutableStateOf(0L) }
    val hapticFeedback = LocalHapticFeedback.current
    val pageScalePercentText = remember(pageScaleLocal) { "${(pageScaleLocal * 100).toInt()}%" }
    val showPageScaleDialogState = remember { mutableStateOf(false) }

    fun performPageScaleSliderHaptic(targetScale: Float) {
        val now = SystemClock.uptimeMillis()
        val elapsedMs = (now - lastChangeTimeMs).coerceAtLeast(1L)
        val percentPerSecond = abs(targetScale - lastChangeScale) * 100_000f / elapsedMs
        val minIntervalMs = when {
            percentPerSecond >= 120f -> 28L
            percentPerSecond >= 60f -> 42L
            percentPerSecond >= 25f -> 60L
            else -> 90L
        }
        val crossedPercentStep = abs(targetScale - lastHapticScale) >= 0.01f
        if (crossedPercentStep && now - lastHapticTimeMs >= minIntervalMs) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
            lastHapticScale = targetScale
            lastHapticTimeMs = now
        }
        lastChangeScale = targetScale
        lastChangeTimeMs = now
    }

    PreferenceArrowItem(
        title = stringResource(LocaleR.string.app_settings_interface_page_scale_title),
        summary = stringResource(LocaleR.string.app_settings_interface_page_scale_summary),
        endActions = {
            Text(
                text = pageScalePercentText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        onClick = { showPageScaleDialogState.value = true },
        holdDownState = showPageScaleDialogState.value,
        bottomAction = {
            Slider(
                value = pageScaleLocal,
                onValueChange = { value ->
                    pageScaleLocal = value
                    performPageScaleSliderHaptic(value)
                },
                onValueChangeFinished = { onApply(pageScaleLocal) },
                valueRange = 0.8f..1.2f,
            )
        },
    )

    PageScaleDialog(
        show = showPageScaleDialogState.value,
        pageScale = pageScaleLocal,
        onPageScaleChange = { pageScaleLocal = it },
        onApply = onApply,
        onDismissRequest = { showPageScaleDialogState.value = false },
    )
}

@Composable
private fun PageScaleDialog(
    show: Boolean,
    pageScale: Float,
    onPageScaleChange: (Float) -> Unit,
    onApply: (Float) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var scaleText by remember(show, pageScale) {
        mutableStateOf((pageScale * 100).toInt().toString())
    }

    AppTextFieldDialog(
        show = show,
        title = stringResource(LocaleR.string.app_settings_interface_page_scale_title),
        value = scaleText,
        onValueChange = { value ->
            if (value.isEmpty() || value.all(Char::isDigit)) {
                scaleText = value
            }
        },
        onDismissRequest = onDismissRequest,
        onConfirm = {
            val parsedPercent = scaleText.toFloatOrNull() ?: (pageScale * 100)
            val clampedScale = parsedPercent.coerceIn(80f, 120f) / 100f
            onPageScaleChange(clampedScale)
            onApply(clampedScale)
            onDismissRequest()
        },
        summary = stringResource(LocaleR.string.app_settings_interface_page_scale_dialog_summary),
        renderInRootScaffold = true,
        singleLine = true,
        trailingIcon = {
            Text(
                text = "%",
                modifier = Modifier.padding(horizontal = UiDp.dp16),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}
