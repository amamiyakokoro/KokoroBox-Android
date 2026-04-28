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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.github.yumelira.yumebox.data.model.AccessControlMode
import com.github.yumelira.yumebox.data.model.AppColorTheme
import com.github.yumelira.yumebox.data.model.AppLanguage
import com.github.yumelira.yumebox.data.model.ThemeMode
import com.github.yumelira.yumebox.data.model.TunStack
import com.github.yumelira.yumebox.presentation.component.AppActionBottomSheet
import com.github.yumelira.yumebox.presentation.component.AppBottomSheetCloseAction
import com.github.yumelira.yumebox.presentation.component.AppBottomSheetConfirmAction
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.PreferenceArrowItem
import com.github.yumelira.yumebox.presentation.component.PreferenceEnumItem
import com.github.yumelira.yumebox.presentation.component.PreferenceSwitchItem
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.github.yumelira.yumebox.presentation.theme.colorFromArgb
import com.github.yumelira.yumebox.presentation.theme.colorToArgbLong
import com.github.yumelira.yumebox.presentation.icon.Yume
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AccessControlScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import org.koin.androidx.compose.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ColorPicker
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TextField

private const val TUN_STACK_TITLE = "协议栈"
private val TUN_STACK_ITEMS = listOf("System", "GVisor", "Mixed")

@Destination<RootGraph>
@Composable
fun VpnSettingsScreen(navigator: DestinationsNavigator) {
    val vpnViewModel = koinViewModel<VpnSettingsViewModel>()
    val accessViewModel = koinViewModel<AccessControlViewModel>()

    val themeMode by vpnViewModel.themeMode.state.collectAsState()
    val colorTheme by vpnViewModel.colorTheme.state.collectAsState()
    val themeSeedColorArgb by vpnViewModel.themeSeedColorArgb.state.collectAsState()
    val invertOnPrimaryColors by vpnViewModel.invertOnPrimaryColors.state.collectAsState()
    val appLanguage by vpnViewModel.appLanguage.state.collectAsState()
    val dnsHijack by vpnViewModel.dnsHijack.state.collectAsState()
    val allowBypass by vpnViewModel.allowBypass.state.collectAsState()
    val enableIPv6 by vpnViewModel.enableIPv6.state.collectAsState()
    val systemProxy by vpnViewModel.systemProxy.state.collectAsState()
    val tunStack by vpnViewModel.tunStack.state.collectAsState()

    val accessUiState by accessViewModel.uiState.collectAsState()
    val accessControlMode by accessViewModel.accessControlMode.state.collectAsState()

    val scrollBehavior = MiuixScrollBehavior()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                accessViewModel.onPermissionResult()
            }
        },
    )

    LaunchedEffect(accessUiState.needsMiuiPermission) {
        if (accessUiState.needsMiuiPermission) {
            permissionLauncher.launch("com.android.permission.GET_INSTALLED_APPS")
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = MLang.AppSettings.Title, scrollBehavior = scrollBehavior)
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(
            scrollBehavior = scrollBehavior,
            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
        ) {
            item {
                Title(MLang.AppSettings.Section.Interface)
                Card {
                    PreferenceEnumItem(
                        title = MLang.AppSettings.Interface.LanguageTitle,
                        currentValue = appLanguage,
                        items = listOf(
                            MLang.AppSettings.Interface.LanguageSystem,
                            MLang.AppSettings.Interface.LanguageChinese,
                            MLang.AppSettings.Interface.LanguageEnglish,
                        ),
                        values = AppLanguage.entries,
                        onValueChange = vpnViewModel::onAppLanguageChange,
                    )
                }
                Title(MLang.AppSettings.Interface.ColorThemeTitle)
                Card {
                    PreferenceEnumItem(
                        title = MLang.AppSettings.Interface.ThemeModeTitle,
                        currentValue = themeMode,
                        items = listOf(
                            MLang.AppSettings.Interface.ThemeModeSystem,
                            MLang.AppSettings.Interface.ThemeModeLight,
                            MLang.AppSettings.Interface.ThemeModeDark,
                        ),
                        values = ThemeMode.entries,
                        onValueChange = vpnViewModel::onThemeModeChange,
                    )
                    PreferenceEnumItem(
                        title = MLang.AppSettings.Interface.ColorThemeModeTitle,
                        summary = MLang.AppSettings.Interface.ColorThemeModeSummary,
                        currentValue = colorTheme,
                        items = listOf(
                            MLang.AppSettings.Interface.ColorThemeModeMonet,
                            MLang.AppSettings.Interface.ColorThemeModeCustom,
                        ),
                        values = listOf(
                            AppColorTheme.MonetDynamic,
                            AppColorTheme.Custom,
                        ),
                        onValueChange = vpnViewModel::onColorThemeChange,
                    )
                    if (colorTheme == AppColorTheme.Custom) {
                        LiteThemeColorPickerItem(
                            themeSeedColorArgb = themeSeedColorArgb,
                            onThemeSeedColorChange = vpnViewModel::onThemeSeedColorChange,
                        )
                    } else {
                        PreferenceArrowItem(
                            title = MLang.AppSettings.Interface.ColorThemePickerTitle,
                            summary = MLang.AppSettings.Interface.ColorThemeDynamicSummary,
                            onClick = { },
                        )
                    }
                    PreferenceSwitchItem(
                        title = MLang.AppSettings.Interface.ThemeColorPolarityInvertTitle,
                        summary = MLang.AppSettings.Interface.ThemeColorPolarityInvertSummary,
                        checked = invertOnPrimaryColors,
                        onCheckedChange = vpnViewModel::onInvertOnPrimaryColorsChange,
                    )
                }

                Title("VPN 鏈嶅姟")
                Card {
                    PreferenceSwitchItem(
                        title = "DNS 鍔寔",
                        checked = dnsHijack,
                        onCheckedChange = vpnViewModel::onDnsHijackChange,
                    )
                    PreferenceSwitchItem(
                        title = "鍏佽搴旂敤缁曡繃 VPN Service",
                        checked = allowBypass,
                        onCheckedChange = vpnViewModel::onAllowBypassChange,
                    )
                    PreferenceSwitchItem(
                        title = "鍚敤 IPv6",
                        checked = enableIPv6,
                        onCheckedChange = vpnViewModel::onEnableIPv6Change,
                    )
                    PreferenceSwitchItem(
                        title = "VPN 绯荤粺浠ｇ悊",
                        checked = systemProxy,
                        onCheckedChange = vpnViewModel::onSystemProxyChange,
                    )
                    PreferenceEnumItem(
                        title = TUN_STACK_TITLE,
                        currentValue = tunStack,
                        items = TUN_STACK_ITEMS,
                        values = TunStack.entries,
                        onValueChange = vpnViewModel::onTunStackChange,
                    )
                }

                Title("璁块棶鎺у埗")
                Card {
                    PreferenceEnumItem(
                        title = "璁块棶妯″紡",
                        currentValue = accessControlMode,
                        items = listOf("鍏佽鍏ㄩ儴", "浠呭厑璁搁€変腑", "鎷掔粷閫変腑"),
                        values = AccessControlMode.entries,
                        onValueChange = accessViewModel::onAccessControlModeChange,
                    )
                    PreferenceArrowItem(
                        title = "绠＄悊璁块棶鎺у埗",
                        summary = "已选 ${accessUiState.selectedPackages.size} 个应用",
                        onClick = { navigator.navigate(AccessControlScreenDestination) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LiteThemeColorPickerItem(
    themeSeedColorArgb: Long,
    onThemeSeedColorChange: (Long) -> Unit,
) {
    val showThemeColorPicker = remember { mutableStateOf(false) }
    val editingThemeSeedColor = remember(themeSeedColorArgb) {
        mutableStateOf(runCatching { colorFromArgb(themeSeedColorArgb) }.getOrDefault(Color.White))
    }
    val editingThemeSeedHex = remember(themeSeedColorArgb) {
        mutableStateOf(liteThemeSeedHexFieldValue(themeSeedColorArgb))
    }

    BasicComponent(
        title = MLang.AppSettings.Interface.ColorThemePickerTitle,
        summary = MLang.AppSettings.Interface.ColorThemeCustomSummary.format(
            liteFormatThemeSeedHex(themeSeedColorArgb),
        ),
        onClick = {
            editingThemeSeedColor.value = runCatching { colorFromArgb(themeSeedColorArgb) }
                .getOrDefault(Color.White)
            editingThemeSeedHex.value = liteThemeSeedHexFieldValue(themeSeedColorArgb)
            showThemeColorPicker.value = true
        },

    )

    AppActionBottomSheet(
        show = showThemeColorPicker.value,
        title = MLang.AppSettings.Interface.ColorThemePickerTitle,
        onDismissRequest = { showThemeColorPicker.value = false },
        startAction = {
            AppBottomSheetCloseAction(onClick = { showThemeColorPicker.value = false })
        },
        endAction = {
            AppBottomSheetConfirmAction(
                onClick = {
                    onThemeSeedColorChange(colorToArgbLong(editingThemeSeedColor.value))
                    showThemeColorPicker.value = false
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            ColorPicker(
                color = editingThemeSeedColor.value,
                onColorChanged = {
                    editingThemeSeedColor.value = it
                    editingThemeSeedHex.value = liteThemeSeedHexFieldValue(colorToArgbLong(it))
                },
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = editingThemeSeedHex.value,
                onValueChange = { value ->
                    editingThemeSeedHex.value = normalizeLiteThemeHexInput(value)
                    liteParseThemeHexColorOrNull(editingThemeSeedHex.value.text)?.let {
                        editingThemeSeedColor.value = it
                    }
                },
                label = MLang.AppSettings.Interface.ColorThemeCodeLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = UiDp.dp8),
            )
        }
    }
}

private fun liteFormatThemeSeedHex(argb: Long): String {
    val rgb = (argb and 0x00FFFFFFL).toString(16).uppercase().padStart(6, '0')
    return "#$rgb"
}

private fun liteThemeSeedHexFieldValue(argb: Long): TextFieldValue {
    val text = liteFormatThemeSeedHex(argb)
    return TextFieldValue(text = text, selection = TextRange(text.length))
}

private fun normalizeLiteThemeHexInput(input: TextFieldValue): TextFieldValue {
    val normalizedBody = input.text
        .removePrefix("#")
        .removePrefix("0x")
        .uppercase()
        .filter { it in '0'..'9' || it in 'A'..'F' }
        .take(6)
    val normalizedBeforeCursor = input.text
        .take(input.selection.start)
        .removePrefix("#")
        .removePrefix("0x")
        .uppercase()
        .filter { it in '0'..'9' || it in 'A'..'F' }
        .take(6)
    val normalizedText = "#$normalizedBody"
    val cursor = (normalizedBeforeCursor.length + 1).coerceIn(1, normalizedText.length)
    return TextFieldValue(
        text = normalizedText,
        selection = TextRange(cursor),
    )
}

private fun liteParseThemeHexColorOrNull(input: String): Color? {
    val body = input.removePrefix("#").removePrefix("0x").uppercase()
    if (body.length != 6) return null
    val rgb = body.toLongOrNull(16) ?: return null
    return colorFromArgb(0xFF000000L or rgb)
}

