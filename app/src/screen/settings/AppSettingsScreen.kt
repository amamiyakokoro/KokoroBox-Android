/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  YumeLira 2025 - Present
 *
 */


package com.github.yumelira.yumebox.screen.settings
import com.github.yumelira.yumebox.presentation.theme.UiDp
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.yumelira.yumebox.common.util.AppIconHelper
import com.github.yumelira.yumebox.common.util.BiometricHelper
import com.github.yumelira.yumebox.common.util.toast
import com.github.yumelira.yumebox.data.model.AppColorTheme
import com.github.yumelira.yumebox.data.model.AppLanguage
import com.github.yumelira.yumebox.data.model.ThemeMode
import com.github.yumelira.yumebox.feature.editor.presentation.language.LanguageScope
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.AppTextFieldDialog
import com.github.yumelira.yumebox.presentation.component.PreferenceArrowItem
import com.github.yumelira.yumebox.presentation.component.PreferenceEnumItem
import com.github.yumelira.yumebox.presentation.component.PreferenceSwitchItem
import com.github.yumelira.yumebox.presentation.component.PreferenceValueItem
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.TextEditBottomSheet
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.WarningBottomSheet
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.util.OverrideStructuredEditorStore
import com.github.yumelira.yumebox.screen.settings.component.ThemeColorPickerItem
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AcgWallpaperCropScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OverrideConfigPreviewRouteDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri

@Composable
@Destination<RootGraph>
fun AppSettingsScreen(
    navigator: DestinationsNavigator,
) {
    val viewModel = koinViewModel<AppSettingsViewModel>()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(title = MLang.AppSettings.Title)
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
            item { AppNetworkSettingsSection(viewModel) }
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
    val automaticRestart by viewModel.automaticRestart.state.collectAsState()
    val autoUpdateCurrentProfileOnStart by viewModel.autoUpdateCurrentProfileOnStart.state.collectAsState()

    Title(MLang.AppSettings.Section.Behavior)
    Card {
        PreferenceSwitchItem(
            title = MLang.AppSettings.Behavior.AutoStartTitle,
            summary = MLang.AppSettings.Behavior.AutoStartSummary,
            checked = automaticRestart,
            onCheckedChange = viewModel::onAutomaticRestartChange,
        )
        PreferenceSwitchItem(
            title = MLang.AppSettings.Behavior.AutoUpdateOnStartTitle,
            summary = MLang.AppSettings.Behavior.AutoUpdateOnStartSummary,
            checked = autoUpdateCurrentProfileOnStart,
            onCheckedChange = viewModel::onAutoUpdateCurrentProfileOnStartChange,
        )
    }
}

@Composable
private fun AppInterfaceSettingsSection(viewModel: AppSettingsViewModel) {
    val themeMode by viewModel.themeMode.state.collectAsState()
    val colorTheme by viewModel.colorTheme.state.collectAsState()
    val appLanguage by viewModel.appLanguage.state.collectAsState()
    val themeSeedColorArgb by viewModel.themeSeedColorArgb.state.collectAsState()
    val invertOnPrimaryColors by viewModel.invertOnPrimaryColors.state.collectAsState()
    val bottomBarAutoHide by viewModel.bottomBarAutoHide.state.collectAsState()
    val bottomBarUseLegacyStyle by viewModel.bottomBarUseLegacyStyle.state.collectAsState()
    val topBarBlurEnabled by viewModel.topBarBlurEnabled.state.collectAsState()
    val pageScale by viewModel.pageScale.state.collectAsState()

    Title(MLang.AppSettings.Interface.ColorThemeTitle)
    Card {
        PreferenceEnumItem(
            title = MLang.AppSettings.Interface.ThemeModeTitle,
            summary = MLang.AppSettings.Interface.ThemeModeSummary,
            currentValue = themeMode,
            items = listOf(
                MLang.AppSettings.Interface.ThemeModeSystem,
                MLang.AppSettings.Interface.ThemeModeLight,
                MLang.AppSettings.Interface.ThemeModeDark,
            ),
            values = ThemeMode.entries,
            onValueChange = viewModel::onThemeModeChange,
        )
        PreferenceEnumItem(
            title = MLang.AppSettings.Interface.ColorThemeModeTitle,
            summary = MLang.AppSettings.Interface.ColorThemeModeSummary,
            currentValue = colorTheme,
            items = listOf(
                MLang.AppSettings.Interface.ColorThemeModeMonet,
                MLang.AppSettings.Interface.ColorThemeModeCustom,
                "根据 ACG 壁纸莫奈取色",
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
                title = MLang.AppSettings.Interface.ColorThemePickerTitle,
                summary = "将使用当前 ACG 首页壁纸提取的主色生成主题；更换并应用壁纸后会自动更新取色。",
                onClick = { },
            )
        } else {
            PreferenceValueItem(
                title = MLang.AppSettings.Interface.ColorThemePickerTitle,
                summary = MLang.AppSettings.Interface.ColorThemeDynamicSummary,
                onClick = { },
            )
        }
        PreferenceSwitchItem(
            title = MLang.AppSettings.Interface.ThemeColorPolarityInvertTitle,
            summary = MLang.AppSettings.Interface.ThemeColorPolarityInvertSummary,
            checked = invertOnPrimaryColors,
            onCheckedChange = viewModel::onInvertOnPrimaryColorsChange,
        )
    }
    Title(MLang.AppSettings.Section.Interface)
    Card {
        PreferenceEnumItem(
            title = MLang.AppSettings.Interface.LanguageTitle,
            summary = MLang.AppSettings.Interface.LanguageSummary,
            currentValue = appLanguage,
            items = listOf(
                MLang.AppSettings.Interface.LanguageSystem,
                MLang.AppSettings.Interface.LanguageChinese,
                MLang.AppSettings.Interface.LanguageEnglish,
            ),
            values = AppLanguage.entries,
            onValueChange = viewModel::onAppLanguageChange,
        )
        PreferenceSwitchItem(
            title = MLang.AppSettings.Interface.AutoHideNavbarTitle,
            summary = MLang.AppSettings.Interface.AutoHideNavbarSummary,
            checked = bottomBarAutoHide,
            onCheckedChange = viewModel::onBottomBarAutoHideChange,
        )
        PreferenceSwitchItem(
            title = MLang.AppSettings.Interface.LegacyNavbarStyleTitle,
            summary = MLang.AppSettings.Interface.LegacyNavbarStyleSummary,
            checked = bottomBarUseLegacyStyle,
            onCheckedChange = viewModel::onBottomBarUseLegacyStyleChange,
        )
        PreferenceSwitchItem(
            title = MLang.AppSettings.Interface.TopBarBlurTitle,
            summary = MLang.AppSettings.Interface.TopBarBlurSummary,
            checked = topBarBlurEnabled,
            onCheckedChange = viewModel::onTopBarBlurEnabledChange,
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
    val excludeFromRecents by viewModel.excludeFromRecents.state.collectAsState()

    Title(MLang.AppSettings.Section.Privacy)
    Card {
        BiometricProtectedPreferenceSwitch(
            checkedFlow = viewModel.biometricUnlockEnabled.state,
            title = MLang.AppSettings.Privacy.BiometricUnlockTitle,
            summary = MLang.AppSettings.Privacy.BiometricUnlockSummary,
            enableTitle = MLang.AppSettings.Privacy.BiometricDialogTitleEnable,
            disableTitle = MLang.AppSettings.Privacy.BiometricDialogTitleDisable,
            onConfirmedChange = viewModel::onBiometricUnlockEnabledChange,
        )
        BiometricProtectedPreferenceSwitch(
            checkedFlow = viewModel.screenshotProtectionEnabled.state,
            title = MLang.AppSettings.Privacy.ScreenshotProtectionTitle,
            summary = MLang.AppSettings.Privacy.ScreenshotProtectionSummary,
            enableTitle = MLang.AppSettings.Privacy.ScreenshotDialogTitleEnable,
            disableTitle = MLang.AppSettings.Privacy.ScreenshotDialogTitleDisable,
            onConfirmedChange = viewModel::onScreenshotProtectionEnabledChange,
        )
        HideAppIconPreferenceItem(
            hideAppIconFlow = viewModel.hideAppIcon.state,
            onHideAppIconChange = viewModel::onHideAppIconChange,
            context = context,
        )
        PreferenceSwitchItem(
            title = MLang.AppSettings.Privacy.HideFromRecentsTitle,
            summary = MLang.AppSettings.Privacy.HideFromRecentsSummary,
            checked = excludeFromRecents,
            onCheckedChange = viewModel::onExcludeFromRecentsChange,
        )
    }
}

@Composable
private fun AppServiceSettingsSection(viewModel: AppSettingsViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val showTrafficNotification by viewModel.showTrafficNotification.state.collectAsState()
    val singleNodeTest by viewModel.singleNodeTest.state.collectAsState()
    val exitUiWhenBackground by viewModel.exitUiWhenBackground.state.collectAsState()
    var batteryOptimizationIgnored by remember {
        mutableStateOf(isBatteryOptimizationIgnored(context))
    }
    val batteryOptimizationSummary = remember(batteryOptimizationIgnored) {
        if (batteryOptimizationIgnored) {
            MLang.AppSettings.ServiceSection.BatteryOptimizationSummaryEnabled
        } else {
            MLang.AppSettings.ServiceSection.BatteryOptimizationSummaryDisabled
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                batteryOptimizationIgnored = isBatteryOptimizationIgnored(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Title(MLang.AppSettings.Section.Service)
    Card {
        PreferenceSwitchItem(
            title = MLang.AppSettings.ServiceSection.TrafficNotificationTitle,
            summary = MLang.AppSettings.ServiceSection.TrafficNotificationSummary,
            checked = showTrafficNotification,
            onCheckedChange = viewModel::onShowTrafficNotificationChange,
        )
        PreferenceSwitchItem(
            title = MLang.AppSettings.ServiceSection.SingleNodeTestTitle,
            summary = MLang.AppSettings.ServiceSection.SingleNodeTestSummary,
            checked = singleNodeTest,
            onCheckedChange = viewModel::onSingleNodeTestChange,
        )
        PreferenceSwitchItem(
            title = MLang.AppSettings.ServiceSection.ExitUiWhenBackgroundTitle,
            summary = MLang.AppSettings.ServiceSection.ExitUiWhenBackgroundSummary,
            checked = exitUiWhenBackground,
            onCheckedChange = viewModel::onExitUiWhenBackgroundChange,
        )
        PreferenceSwitchItem(
            title = MLang.AppSettings.ServiceSection.BatteryOptimizationTitle,
            summary = batteryOptimizationSummary,
            checked = batteryOptimizationIgnored,
            onCheckedChange = {
                if (!openBatteryOptimizationSettings(context, batteryOptimizationIgnored)) {
                    context.toast(MLang.Util.Error.UnknownError)
                }
            },
        )
    }
}

@Composable
private fun AppNetworkSettingsSection(viewModel: AppSettingsViewModel) {
    val customUserAgent by viewModel.customUserAgent.state.collectAsState()

    Title(MLang.AppSettings.Section.Network)
    Card {
        CustomUserAgentPreferenceItem(
            customUserAgent = customUserAgent,
            onConfirm = viewModel::applyCustomUserAgent,
        )
    }
}

@Composable
private fun AppExperimentalSettingsSection(
    viewModel: AppSettingsViewModel,
    navigator: DestinationsNavigator,
) {
    val acgMainUiEnabled by viewModel.acgMainUiEnabled.state.collectAsState()
    val acgHomeQuote by viewModel.acgHomeQuote.state.collectAsState()
    val acgHomeQuoteAuthor by viewModel.acgHomeQuoteAuthor.state.collectAsState()
    val acgDailyQuoteEnabled by viewModel.acgDailyQuoteEnabled.state.collectAsState()
    val acgDailyQuoteApiUrl by viewModel.acgDailyQuoteApiUrl.state.collectAsState()
    val acgCustomQuoteListJson by viewModel.acgCustomQuoteListJson.state.collectAsState()
    val acgMergeCustomQuoteList by viewModel.acgMergeCustomQuoteList.state.collectAsState()
    val acgSidebarExpanded by viewModel.acgSidebarExpanded.state.collectAsState()
    val acgQuoteSummary = remember(acgHomeQuote) {
        acgHomeQuote.ifBlank { MLang.AppSettings.Experimental.AcgQuoteDefault }
    }
    val acgQuoteAuthorSummary = remember(acgHomeQuoteAuthor) {
        acgHomeQuoteAuthor.ifBlank { MLang.AppSettings.Experimental.AcgQuoteAuthorDefault }
    }
    Title(MLang.AppSettings.Section.Experimental)
    Card {
        PreferenceSwitchItem(
            title = MLang.AppSettings.Experimental.AcgHomeTitle,
            summary = MLang.AppSettings.Experimental.AcgHomeSummary,
            checked = acgMainUiEnabled,
            onCheckedChange = viewModel::onAcgMainUiEnabledChange,
        )
        PreferenceSwitchItem(
            title = MLang.AppSettings.Experimental.AcgSidebarExpandedTitle,
            summary = MLang.AppSettings.Experimental.AcgSidebarExpandedSummary,
            checked = acgSidebarExpanded,
            onCheckedChange = viewModel::onAcgSidebarExpandedChange,
        )
        PreferenceSwitchItem(
            title = "每日刷新 ACG 一言",
            summary = "启用后每天从默认或自定义 API 获取 JSON 一言；自定义 ACG 一言将暂时不可编辑。",
            checked = acgDailyQuoteEnabled,
            onCheckedChange = { enabled ->
                viewModel.onAcgDailyQuoteEnabledChange(enabled)
                if (enabled) {
                    viewModel.refreshDailyAcgQuoteIfNeeded(force = true)
                }
            },
        )
        AcgQuotePreferenceItem(
            title = "每日一言 API（仅支持 JSON）",
            summary = acgDailyQuoteApiUrl.ifBlank { "仅支持返回JSON的API，详细请看样例" },
            dialogTitle = "编辑每日一言 API（仅 JSON）",
            currentValue = acgDailyQuoteApiUrl,
            onConfirm = {
                viewModel.onAcgDailyQuoteApiUrlChange(it)
                if (acgDailyQuoteEnabled) viewModel.refreshDailyAcgQuoteIfNeeded(force = true)
            },
        )
        AcgTextEditorPreferenceItem(
            title = "每日一言 API 样例",
            summary = "查看 API 与返回格式样例",
            editorTitle = "每日一言 API 样例",
            content = DAILY_QUOTE_API_SAMPLE,
            navigator = navigator,
        )
        AcgTextEditorPreferenceItem(
            title = "自定义一言列表（JSON）",
            summary = acgCustomQuoteListJson.ifBlank { "点击编辑可带注释的 JSON 文本；样例已写在编辑内容中。" },
            editorTitle = "编辑自定义一言列表 JSON",
            content = acgCustomQuoteListJson.ifBlank { CUSTOM_QUOTE_LIST_TEMPLATE },
            navigator = navigator,
            onSave = {
                viewModel.onAcgCustomQuoteListJsonChange(it)
                if (acgDailyQuoteEnabled) viewModel.refreshDailyAcgQuoteIfNeeded(force = true)
            },
        )
        PreferenceSwitchItem(
            title = "合并自定义列表到每日一言",
            summary = "启用后每日刷新会随机从 API 与自定义 JSON 列表中取一条；API 不可用时也会回退到自定义列表。",
            checked = acgMergeCustomQuoteList,
            onCheckedChange = viewModel::onAcgMergeCustomQuoteListChange,
        )
        AcgQuotePreferenceItem(
            title = MLang.AppSettings.Experimental.AcgQuoteTitle,
            summary = if (acgDailyQuoteEnabled) "已启用每日刷新，暂不可编辑" else acgQuoteSummary,
            dialogTitle = MLang.AppSettings.Experimental.EditAcgQuoteTitle,
            currentValue = acgHomeQuote,
            enabled = !acgDailyQuoteEnabled,
            onConfirm = viewModel::onAcgHomeQuoteChange,
        )
        AcgQuotePreferenceItem(
            title = MLang.AppSettings.Experimental.AcgQuoteAuthorTitle,
            summary = if (acgDailyQuoteEnabled) "已启用每日刷新，暂不可编辑" else acgQuoteAuthorSummary,
            dialogTitle = MLang.AppSettings.Experimental.EditAcgQuoteAuthorTitle,
            currentValue = acgHomeQuoteAuthor,
            enabled = !acgDailyQuoteEnabled,
            onConfirm = viewModel::onAcgHomeQuoteAuthorChange,
        )
        AcgWallpaperPreferenceItem(
            navigator = navigator,
            wallpaperZoom = viewModel.acgWallpaperZoom.value,
            wallpaperBiasX = viewModel.acgWallpaperBiasX.value,
            wallpaperBiasY = viewModel.acgWallpaperBiasY.value,
        )
    }
}

private const val DAILY_QUOTE_API_SAMPLE = """API样例：https://v1.hitokoto.cn/?c=a&c=b&c=c
返回格式样例：{
"id": 1234,
"hitokoto": "所谓的成长，就是越来越能接受自己本来的样子。",
"from": "某作品",
"from_who": "某角色"
}"""

private const val CUSTOM_QUOTE_LIST_TEMPLATE = """// 自定义一言列表样例，保存前可以保留以 // 开头的注释
// 支持字符串数组：
// ["一句话", "另一句话"]
// 也支持对象数组，字段支持 text/quote/content/hitokoto 与 author/from/from_who/source：
[
  {
    "text": "所谓的成长，就是越来越能接受自己本来的样子。",
    "author": "某角色"
  },
  {
    "hitokoto": "愿你历尽千帆，归来仍是少年。",
    "from": "自定义"
  }
]
"""

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
    val checked by checkedFlow.collectAsState()
    val showUnavailableDialogState = remember { mutableStateOf(false) }
    var unavailableMessage by remember { mutableStateOf("") }

    PreferenceSwitchItem(
        title = title,
        summary = summary,
        checked = checked,
        onCheckedChange = { targetState ->
            requestBiometricConfirmation(
                context = context,
                title = if (targetState) enableTitle else disableTitle,
                allowBypassWhenUnavailable = !targetState,
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
        title = MLang.AppSettings.Privacy.BiometricUnavailableTitle,
        messages = listOf(
            unavailableMessage.ifBlank {
                MLang.AppSettings.Privacy.BiometricUnavailableMessage
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
    val hideAppIcon by hideAppIconFlow.collectAsState()
    val showHideIconDialogState = remember { mutableStateOf(false) }

    PreferenceSwitchItem(
        title = MLang.AppSettings.Privacy.HideIconTitle,
        summary = MLang.AppSettings.Privacy.HideIconSummary,
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
        title = MLang.AppSettings.WarningDialog.Title,
        messages = listOf(
            MLang.AppSettings.WarningDialog.HideIconMsg1,
            MLang.AppSettings.WarningDialog.HideIconMsg2,
        ),
        onConfirm = {
            onHideAppIconChange(true)
            AppIconHelper.toggleIcon(context, true)
        },
    )
}

@Composable
private fun AcgQuotePreferenceItem(
    title: String,
    summary: String,
    dialogTitle: String,
    currentValue: String,
    enabled: Boolean = true,
    onConfirm: (String) -> Unit,
) {
    val showEditDialogState = remember { mutableStateOf(false) }
    val textFieldState = remember { mutableStateOf(TextFieldValue()) }

    PreferenceValueItem(
        title = title,
        summary = summary,
        enabled = enabled,
        onClick = {
            textFieldState.value = TextFieldValue(currentValue)
            showEditDialogState.value = true
        },
    )

    TextEditBottomSheet(
        show = showEditDialogState,
        title = dialogTitle,
        textFieldValue = textFieldState,
        onConfirm = onConfirm,
    )
}

@Composable
private fun AcgTextEditorPreferenceItem(
    title: String,
    summary: String,
    editorTitle: String,
    content: String,
    navigator: DestinationsNavigator,
    onSave: ((String) -> Unit)? = null,
) {
    PreferenceValueItem(
        title = title,
        summary = summary,
        onClick = {
            OverrideStructuredEditorStore.setupConfigPreview(
                title = editorTitle,
                content = content,
                language = LanguageScope.Text,
                callback = onSave,
            )
            navigator.navigate(OverrideConfigPreviewRouteDestination)
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
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
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
        title = MLang.AppSettings.Experimental.WallpaperTitle,
        summary = MLang.AppSettings.Experimental.WallpaperSummary,
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
    onUnavailable: (String) -> Unit,
    onSuccess: () -> Unit,
) {
    val activity = BiometricHelper.findFragmentActivity(context)
    if (activity == null) {
        if (allowBypassWhenUnavailable) {
            onSuccess()
        } else {
            onUnavailable(MLang.AppSettings.Privacy.BiometricUnavailableMessage)
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

private fun isBatteryOptimizationIgnored(context: android.content.Context): Boolean {
    val powerManager = context.getSystemService(PowerManager::class.java) ?: return false
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun openBatteryOptimizationSettings(
    context: android.content.Context,
    alreadyIgnored: Boolean,
): Boolean {
    val intents = buildList {
        if (!alreadyIgnored) {
            add(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        }
        add(
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
        add(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
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
    val pageScalePercentText = remember(pageScaleLocal) { "${(pageScaleLocal * 100).toInt()}%" }
    val showPageScaleDialogState = remember { mutableStateOf(false) }

    PreferenceArrowItem(
        title = MLang.AppSettings.Interface.PageScaleTitle,
        summary = MLang.AppSettings.Interface.PageScaleSummary,
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
                onValueChange = { pageScaleLocal = it },
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
private fun CustomUserAgentPreferenceItem(
    customUserAgent: String,
    onConfirm: (String) -> Unit,
) {
    val customUserAgentSummary = remember(customUserAgent) {
        customUserAgent.ifEmpty {
            MLang.AppSettings.Network.CustomUserAgentSummaryDefault
        }
    }
    val showEditCustomUserAgentDialogState = remember { mutableStateOf(false) }
    val customUserAgentTextFieldState = remember { mutableStateOf(TextFieldValue()) }

    PreferenceValueItem(
        title = MLang.AppSettings.Network.CustomUserAgentTitle,
        summary = customUserAgentSummary,
        onClick = {
            customUserAgentTextFieldState.value = TextFieldValue(customUserAgent)
            showEditCustomUserAgentDialogState.value = true
        },
    )

    TextEditBottomSheet(
        show = showEditCustomUserAgentDialogState,
        title = MLang.AppSettings.EditDialog.UserAgentTitle,
        textFieldValue = customUserAgentTextFieldState,
        onConfirm = onConfirm,
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
        title = MLang.AppSettings.Interface.PageScaleTitle,
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
        summary = MLang.AppSettings.Interface.PageScaleDialogSummary,
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
