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



package com.github.yumelira.yumebox.presentation.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.github.yumelira.yumebox.data.model.AppColorTheme
import com.github.yumelira.yumebox.data.model.ThemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val LocalPlatformSystemUiEffect = compositionLocalOf<@Composable () -> Unit> { {} }

@Composable
fun YumeTheme(
    themeMode: ThemeMode? = null,
    colorTheme: AppColorTheme = AppColorTheme.MonetDynamic,
    themeSeedColorArgb: Long = DEFAULT_THEME_SEED_ARGB,
    invertOnPrimaryColors: Boolean = false,
    spacing: Spacing = Spacing(),
    radii: Radii = Radii(),
    sizes: Sizes = Sizes(),
    opacity: Opacity = Opacity(),
    appColors: AppColors = AppColors(),
    content: @Composable () -> Unit,
) {
    LocalPlatformSystemUiEffect.current()
    val context = LocalContext.current
    val effectiveThemeMode = themeMode ?: ThemeMode.Auto
    val isDark = when (effectiveThemeMode) {
        ThemeMode.Auto -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val resolvedThemeSeedColorArgb = remember(context, colorTheme, themeSeedColorArgb, isDark) {
        when (colorTheme) {
            AppColorTheme.MonetDynamic -> context.resolveDynamicMonetSeedArgb(isDark) ?: themeSeedColorArgb
            AppColorTheme.Custom -> themeSeedColorArgb
        }
    }
    val colorScheme = remember(isDark, resolvedThemeSeedColorArgb, invertOnPrimaryColors) {
        colorSchemeFromSeed(
            seed = colorFromArgb(resolvedThemeSeedColorArgb),
            isDark = isDark,
            invertOnPrimaryColors = invertOnPrimaryColors,
        )
    }

    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalRadii provides radii,
        LocalSizes provides sizes,
        LocalOpacity provides opacity,
        LocalAppColors provides appColors,
    ) {
        MiuixTheme(
            colors = colorScheme,
        ) {
            content()
        }
    }
}

private fun Context.resolveDynamicMonetSeedArgb(isDark: Boolean): Long? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null

    val accentColorRes = if (isDark) {
        android.R.color.system_accent1_200
    } else {
        android.R.color.system_accent1_600
    }

    return runCatching {
        colorToArgbLong(Color(getColor(accentColorRes)))
    }.getOrNull()
}
