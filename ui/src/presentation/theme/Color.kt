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
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.annotation.ColorRes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme as materialDarkColorScheme
import androidx.compose.material3.lightColorScheme as materialLightColorScheme
import com.github.yumelira.yumebox.data.model.MonetContrast
import com.github.yumelira.yumebox.data.model.MonetStyle
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

private data class ThemeColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryVariant: Color,
    val onPrimaryVariant: Color,
    val disabledPrimary: Color,
    val disabledOnPrimary: Color,
    val disabledPrimaryButton: Color,
    val disabledOnPrimaryButton: Color,
    val disabledPrimarySlider: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val tertiaryContainerVariant: Color,
    val onBackgroundVariant: Color,
)

private data class ThemePalette(
    val light: ThemeColors,
    val dark: ThemeColors,
)

const val DEFAULT_THEME_SEED_ARGB: Long = 0xFF138A74L
const val DEFAULT_ACG_WALLPAPER_THEME_SEED_ARGB: Long = 0xFFDA98ABL
const val DEFAULT_CUSTOM_THEME_SEED_ARGB: Long = 0xFFDA98ABL

data class TrafficColors(
    val download: Color = Color(0xFF5B8FF9),
    val upload: Color = Color(0xFF5AD8A6),
    val unattributed: Color = Color(0xFFD97706),
    val other: Color = Color(0xFF94A3B8),
    val unknown: Color = Color(0xFF64748B),
    val donutTrackForeground: Color = Color(0x1F8A94A6),
    val donutTrackBackground: Color = Color(0x148A94A6),
)

data class LogLevelColors(
    val debug: Color = Color(0xFF9E9E9E),
    val warning: Color = Color(0xFFFF9800),
    val error: Color = Color(0xFFF44336),
    val neutral: Color = Color(0xFF9E9E9E),
)

data class LatencyColors(
    val fast: Color = Color(0xFF007906),
    val moderate: Color = Color(0xFFFFB300),
    val slow: Color = Color(0xFFE53935),
    val timeout: Color = Color(0xFF9E9E9E),
)

data class ProtocolColors(
    val tcp: Color = Color(0xFF2196F3),
    val udp: Color = Color(0xFF4CAF50),
    val http: Color = Color(0xFF9E9E9E),
    val https: Color = Color(0xFF00BCD4),
    val unknown: Color = Color(0xFF9E9E9E),
)

data class ConnectionColors(
    val chainArrow: Color = Color(0xFF6B7280),
    val chainInactiveText: Color = Color(0xFF6B7280),
    val chainActive: Color = Color(0xFF00BFA5),
) {
    val chainInactive: Color
        get() = chainInactiveText
}

data class StatusColors(
    val destructive: Color = Color(0xFFFF3B30),
    val destructiveContainer: Color = Color(0x1AFF3B30),
)

data class StateColors(
    val danger: Color = Color(0xFFFF3B30),
    val subtleDivider: Color = Color(0xFFC7C7CC),
    val neutralPlaceholderBackground: Color = Color(0xFFE0E0E0),
)

data class AcgColors(
    val pingExcellent: Color = Color(0xFF0E7A34),
    val pingWarning: Color = Color(0xFFB87900),
)

data class EditorColors(
    val darkBackground: Color = Color(0xFF1E1E1E),
    val darkText: Color = Color(0xFFD4D4D4),
    val darkLineNumber: Color = Color(0xFF858585),
    val darkLineNumberBackground: Color = Color(0xFF1E1E1E),
    val darkCurrentLine: Color = Color(0xFF2D2D2D),
    val darkSelectionBackground: Color = Color(0xFF264F78),
    val darkTextActionBackground: Color = Color(0xFF2D2D2D),
    val darkTextActionIcon: Color = Color(0xFFD4D4D4),
    val lightBackground: Color = Color.White,
    val lightText: Color = Color(0xFF1E1E1E),
    val lightLineNumber: Color = Color(0xFF6E6E6E),
    val lightLineNumberBackground: Color = Color(0xFFF0F0F0),
    val lightCurrentLine: Color = Color(0xFFF5F5F5),
    val lightSelectionBackground: Color = Color(0xFFADD6FF),
    val lightTextActionBackground: Color = Color(0xFFF0F0F0),
    val lightTextActionIcon: Color = Color(0xFF333333),
    val accent: Color = Color(0xFF007ACC),
    val delimiterDark: Color = Color(0xFF569CD6),
    val delimiterLight: Color = Color(0xFF0000FF),
    val delimiterBackground: Color = Color(0x2646A2D4),
)

data class AppColors(
    val traffic: TrafficColors = TrafficColors(),
    val logLevel: LogLevelColors = LogLevelColors(),
    val latency: LatencyColors = LatencyColors(),
    val protocol: ProtocolColors = ProtocolColors(),
    val connection: ConnectionColors = ConnectionColors(),
    val status: StatusColors = StatusColors(),
    val state: StateColors = StateColors(),
    val acg: AcgColors = AcgColors(),
    val editor: EditorColors = EditorColors(),
) {
    val neutralPlaceholderBackground: Color
        get() = state.neutralPlaceholderBackground
}

val LocalAppColors = staticCompositionLocalOf { AppColors() }

internal fun appColorsFromMaterial(
    colorScheme: ColorScheme,
    isDark: Boolean,
): AppColors {
    val primary = colorScheme.primary
    val secondary = colorScheme.secondary
    val tertiary = colorScheme.tertiary
    val neutral = colorScheme.onSurfaceVariant
    val success = if (isDark) {
        primary.mix(Color(0xFF8FF5B2), 0.42f).boostContrast(dark = true, amount = 0.06f)
    } else {
        primary.mix(Color(0xFF006D36), 0.46f).boostContrast(dark = false, amount = 0.05f)
    }
    val warning = if (isDark) {
        tertiary.mix(Color(0xFFFFD166), 0.52f).boostContrast(dark = true, amount = 0.03f)
    } else {
        tertiary.mix(Color(0xFF8A5A00), 0.42f).boostContrast(dark = false, amount = 0.04f)
    }
    val danger = if (isDark) colorScheme.error.mix(Color.White, 0.12f) else colorScheme.error
    val neutralAccent = if (isDark) neutral.mix(Color.White, 0.12f) else neutral.mix(Color.Black, 0.08f)
    val download = primary.mix(Color(0xFF4F8DFF), 0.35f)
    val upload = secondary.mix(success, 0.42f)
    val other = tertiary.mix(neutralAccent, 0.24f)
    val unknown = neutralAccent
    val trackBase = colorScheme.onSurfaceVariant

    return AppColors(
        traffic = TrafficColors(
            download = download,
            upload = upload,
            unattributed = warning,
            other = other,
            unknown = unknown,
            donutTrackForeground = trackBase.copy(alpha = if (isDark) 0.26f else 0.16f),
            donutTrackBackground = trackBase.copy(alpha = if (isDark) 0.18f else 0.10f),
        ),
        logLevel = LogLevelColors(
            debug = unknown,
            warning = warning,
            error = danger,
            neutral = unknown,
        ),
        latency = LatencyColors(
            fast = success,
            moderate = warning,
            slow = danger,
            timeout = unknown,
        ),
        protocol = ProtocolColors(
            tcp = download,
            udp = upload,
            http = unknown,
            https = tertiary.mix(primary, 0.36f),
            unknown = unknown,
        ),
        connection = ConnectionColors(
            chainArrow = neutralAccent,
            chainInactiveText = neutralAccent,
            chainActive = primary.mix(success, 0.28f),
        ),
        status = StatusColors(
            destructive = danger,
            destructiveContainer = danger.copy(alpha = if (isDark) 0.24f else 0.12f),
        ),
        state = StateColors(
            danger = danger,
            subtleDivider = colorScheme.outlineVariant,
            neutralPlaceholderBackground = colorScheme.surfaceVariant,
        ),
        acg = AcgColors(
            pingExcellent = success,
            pingWarning = warning,
        ),
        editor = EditorColors(
            accent = primary,
            delimiterDark = primary.mix(Color.White, 0.24f),
            delimiterLight = primary.mix(Color.Black, 0.12f),
            delimiterBackground = primary.copy(alpha = if (isDark) 0.22f else 0.15f),
        ),
    )
}

private fun ThemeColors.toLightScheme() = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryVariant = primaryVariant,
    onPrimaryVariant = onPrimaryVariant,
    disabledPrimary = disabledPrimary,
    disabledOnPrimary = disabledOnPrimary,
    disabledPrimaryButton = disabledPrimaryButton,
    disabledOnPrimaryButton = disabledOnPrimaryButton,
    disabledPrimarySlider = disabledPrimarySlider,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    tertiaryContainerVariant = tertiaryContainerVariant,
    onBackgroundVariant = onBackgroundVariant,
)

private fun ThemeColors.toDarkScheme() = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryVariant = primaryVariant,
    onPrimaryVariant = onPrimaryVariant,
    disabledPrimary = disabledPrimary,
    disabledOnPrimary = disabledOnPrimary,
    disabledPrimaryButton = disabledPrimaryButton,
    disabledOnPrimaryButton = disabledOnPrimaryButton,
    disabledPrimarySlider = disabledPrimarySlider,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    tertiaryContainerVariant = tertiaryContainerVariant,
    onBackgroundVariant = onBackgroundVariant,
)

private fun ThemePalette.toColorScheme(isDark: Boolean) =
    if (isDark) dark.toDarkScheme() else light.toLightScheme()

private val basePalette = ThemePalette(
    light = ThemeColors(
        primary = Color(0xFF006C5C),
        onPrimary = Color.White,
        primaryVariant = Color(0xFF2A7D70),
        onPrimaryVariant = Color(0xFF4F635D),
        disabledPrimary = Color(0xFFC7D0CC),
        disabledOnPrimary = Color(0xFFE8ECEA),
        disabledPrimaryButton = Color(0xFFC7D0CC),
        disabledOnPrimaryButton = Color(0xFFF4F7F5),
        disabledPrimarySlider = Color(0xFFDCE4E1),
        primaryContainer = Color(0xFFE0F2EC),
        onPrimaryContainer = Color(0xFF0B1F1A),
        tertiaryContainer = Color(0xFFF4F7F5),
        onTertiaryContainer = Color(0xFF161D1B),
        tertiaryContainerVariant = Color(0xFFECF2EF),
        onBackgroundVariant = Color(0xFF3F4946),
    ),
    dark = ThemeColors(
        primary = Color(0xFF7FD7C3),
        onPrimary = Color(0xFF00382E),
        primaryVariant = Color(0xFFA4EBDD),
        onPrimaryVariant = Color(0xFFAEB9B5),
        disabledPrimary = Color(0xFF2E3835),
        disabledOnPrimary = Color(0xFF6F7B76),
        disabledPrimaryButton = Color(0xFF2E3835),
        disabledOnPrimaryButton = Color(0xFF7E8A85),
        disabledPrimarySlider = Color(0xFF3B4642),
        primaryContainer = Color(0xFF1A2B27),
        onPrimaryContainer = Color(0xFFE0F2EC),
        tertiaryContainer = Color(0xFF161D1B),
        onTertiaryContainer = Color(0xFFE2E3E1),
        tertiaryContainerVariant = Color(0xFF27302D),
        onBackgroundVariant = Color(0xFFC0CAC6),
    ),
)

fun colorSchemeFromSeed(
    seed: Color,
    isDark: Boolean,
    invertOnPrimaryColors: Boolean = false,
    monetStyle: MonetStyle = MonetStyle.TonalSpot,
    monetContrast: MonetContrast = MonetContrast.Standard,
    monetIntensity: Float = 0.45f,
) = derivePaletteFromSeed(
    seed = seed,
    invertOnPrimaryColors = invertOnPrimaryColors,
    monetStyle = monetStyle,
    monetContrast = monetContrast,
    monetIntensity = monetIntensity,
).toColorScheme(isDark)

fun materialColorSchemeFromSeed(
    seed: Color,
    isDark: Boolean,
    invertOnPrimaryColors: Boolean = false,
    monetStyle: MonetStyle = MonetStyle.TonalSpot,
    monetContrast: MonetContrast = MonetContrast.Standard,
    monetIntensity: Float = 0.45f,
) = derivePaletteFromSeed(
    seed = seed,
    invertOnPrimaryColors = invertOnPrimaryColors,
    monetStyle = monetStyle,
    monetContrast = monetContrast,
    monetIntensity = monetIntensity,
).let { palette ->
    val colors = if (isDark) palette.dark else palette.light
    val surface = if (isDark) {
        colors.tertiaryContainer.mix(Color.Black, 0.40f)
    } else {
        colors.tertiaryContainer.mix(Color.White, 0.62f)
    }
    val surfaceContainerLowest = if (isDark) {
        surface.mix(Color.Black, 0.18f)
    } else {
        surface.mix(Color.White, 0.46f)
    }
    val surfaceContainerLow = if (isDark) {
        colors.tertiaryContainer.mix(Color.Black, 0.28f)
    } else {
        colors.tertiaryContainer.mix(Color.White, 0.48f)
    }
    val surfaceContainer = colors.tertiaryContainerVariant
    val surfaceContainerHigh = if (isDark) {
        colors.tertiaryContainerVariant.mix(Color.White, 0.05f)
    } else {
        colors.tertiaryContainerVariant.mix(colors.primaryContainer, 0.06f)
    }
    val surfaceContainerHighest = if (isDark) {
        colors.tertiaryContainerVariant.mix(Color.White, 0.10f)
    } else {
        colors.tertiaryContainerVariant.mix(colors.primaryContainer, 0.10f)
    }
    val outline = if (isDark) {
        colors.onBackgroundVariant.mix(Color.Black, 0.22f)
    } else {
        colors.onBackgroundVariant.mix(Color.White, 0.22f)
    }
    val outlineVariant = if (isDark) {
        colors.tertiaryContainerVariant.mix(Color.White, 0.18f)
    } else {
        colors.tertiaryContainerVariant.mix(Color.Black, 0.10f)
    }
    if (isDark) {
        materialDarkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.primaryVariant,
            onSecondary = colors.onPrimaryVariant,
            secondaryContainer = colors.primaryContainer,
            onSecondaryContainer = colors.onPrimaryContainer,
            tertiary = colors.primaryVariant,
            onTertiary = colors.onPrimaryVariant,
            tertiaryContainer = colors.tertiaryContainerVariant,
            onTertiaryContainer = colors.onTertiaryContainer,
            background = surface,
            onBackground = Color(0xFFE1E4E1),
            surface = surface,
            onSurface = Color(0xFFE1E4E1),
            surfaceDim = surfaceContainerLowest,
            surfaceBright = surfaceContainerHigh,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceVariant = colors.tertiaryContainerVariant,
            onSurfaceVariant = colors.onBackgroundVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
        )
    } else {
        materialLightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.primaryVariant,
            onSecondary = colors.onPrimaryVariant,
            secondaryContainer = colors.primaryContainer,
            onSecondaryContainer = colors.onPrimaryContainer,
            tertiary = colors.primaryVariant,
            onTertiary = colors.onPrimaryVariant,
            tertiaryContainer = colors.tertiaryContainerVariant,
            onTertiaryContainer = colors.onTertiaryContainer,
            background = surface,
            onBackground = Color(0xFF191C1A),
            surface = surface,
            onSurface = Color(0xFF191C1A),
            surfaceDim = surfaceContainerLow,
            surfaceBright = surfaceContainerLowest,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceVariant = colors.tertiaryContainerVariant,
            onSurfaceVariant = colors.onBackgroundVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            error = Color(0xFFBA1A1A),
            onError = Color.White,
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002),
        )
    }
}

fun miuixColorSchemeFromMaterial(
    colorScheme: ColorScheme,
    isDark: Boolean,
    invertOnPrimaryColors: Boolean = false,
) = if (isDark) {
    darkColorScheme(
        primary = colorScheme.primary,
        onPrimary = colorScheme.primary.readableOnColor(invertOnPrimaryColors),
        primaryVariant = colorScheme.secondary,
        onPrimaryVariant = colorScheme.onSecondary,
        background = colorScheme.background,
        onBackground = colorScheme.onBackground,
        surface = colorScheme.surface,
        onSurface = colorScheme.onSurface,
        outline = colorScheme.outline,
        error = colorScheme.error,
        onError = colorScheme.onError,
        disabledPrimary = colorScheme.surfaceVariant,
        disabledOnPrimary = colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
        disabledPrimaryButton = colorScheme.surfaceVariant,
        disabledOnPrimaryButton = colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
        disabledPrimarySlider = colorScheme.surfaceVariant,
        primaryContainer = colorScheme.primaryContainer,
        onPrimaryContainer = colorScheme.onPrimaryContainer,
        tertiaryContainer = colorScheme.tertiaryContainer,
        onTertiaryContainer = colorScheme.onTertiaryContainer,
        tertiaryContainerVariant = colorScheme.surfaceVariant,
        onBackgroundVariant = colorScheme.onSurfaceVariant,
    )
} else {
    lightColorScheme(
        primary = colorScheme.primary,
        onPrimary = colorScheme.primary.readableOnColor(invertOnPrimaryColors),
        primaryVariant = colorScheme.secondary,
        onPrimaryVariant = colorScheme.onSecondary,
        background = colorScheme.background,
        onBackground = colorScheme.onBackground,
        surface = colorScheme.surface,
        onSurface = colorScheme.onSurface,
        outline = colorScheme.outline,
        error = colorScheme.error,
        onError = colorScheme.onError,
        disabledPrimary = colorScheme.surfaceVariant,
        disabledOnPrimary = colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
        disabledPrimaryButton = colorScheme.surfaceVariant,
        disabledOnPrimaryButton = colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
        disabledPrimarySlider = colorScheme.surfaceVariant,
        primaryContainer = colorScheme.primaryContainer,
        onPrimaryContainer = colorScheme.onPrimaryContainer,
        tertiaryContainer = colorScheme.tertiaryContainer,
        onTertiaryContainer = colorScheme.onTertiaryContainer,
        tertiaryContainerVariant = colorScheme.surfaceVariant,
        onBackgroundVariant = colorScheme.onSurfaceVariant,
    )
}

fun colorFromArgb(argb: Long): Color = Color(argb.toInt())

fun colorToArgbLong(color: Color): Long = color.toArgb().toLong()

fun systemMonetColorScheme(
    context: Context,
    isDark: Boolean,
    invertOnPrimaryColors: Boolean = false,
) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    if (isDark) {
        darkColorScheme(
            primary = context.systemColor(android.R.color.system_accent1_200),
            onPrimary = context.systemColor(android.R.color.system_accent1_200)
                .readableOnColor(invertOnPrimaryColors),
            primaryVariant = context.systemColor(android.R.color.system_accent1_100),
            onPrimaryVariant = context.systemColor(android.R.color.system_neutral2_200),
            disabledPrimary = context.systemColor(android.R.color.system_neutral2_700),
            disabledOnPrimary = context.systemColor(android.R.color.system_neutral2_300),
            disabledPrimaryButton = context.systemColor(android.R.color.system_neutral1_700),
            disabledOnPrimaryButton = context.systemColor(android.R.color.system_neutral1_300),
            disabledPrimarySlider = context.systemColor(android.R.color.system_neutral2_600),
            primaryContainer = context.systemColor(android.R.color.system_accent1_700),
            onPrimaryContainer = context.systemColor(android.R.color.system_accent1_700)
                .readableOnColor(invertOnPrimaryColors),
            tertiaryContainer = context.systemColor(android.R.color.system_accent3_700),
            onTertiaryContainer = context.systemColor(android.R.color.system_accent3_700)
                .readableOnColor(invertOnPrimaryColors),
            tertiaryContainerVariant = context.systemColor(android.R.color.system_accent3_800),
            onBackgroundVariant = context.systemColor(android.R.color.system_neutral2_200),
        )
    } else {
        lightColorScheme(
            primary = context.systemColor(android.R.color.system_accent1_600),
            onPrimary = context.systemColor(android.R.color.system_accent1_600)
                .readableOnColor(invertOnPrimaryColors),
            primaryVariant = context.systemColor(android.R.color.system_accent1_500),
            onPrimaryVariant = context.systemColor(android.R.color.system_neutral2_700),
            disabledPrimary = context.systemColor(android.R.color.system_neutral2_200),
            disabledOnPrimary = context.systemColor(android.R.color.system_neutral2_600),
            disabledPrimaryButton = context.systemColor(android.R.color.system_neutral1_200),
            disabledOnPrimaryButton = context.systemColor(android.R.color.system_neutral1_600),
            disabledPrimarySlider = context.systemColor(android.R.color.system_neutral2_300),
            primaryContainer = context.systemColor(android.R.color.system_accent1_100),
            onPrimaryContainer = context.systemColor(android.R.color.system_accent1_100)
                .readableOnColor(invertOnPrimaryColors),
            tertiaryContainer = context.systemColor(android.R.color.system_accent3_100),
            onTertiaryContainer = context.systemColor(android.R.color.system_accent3_100)
                .readableOnColor(invertOnPrimaryColors),
            tertiaryContainerVariant = context.systemColor(android.R.color.system_accent3_200),
            onBackgroundVariant = context.systemColor(android.R.color.system_neutral2_700),
        )
    }
} else {
    colorSchemeFromSeed(
        seed = colorFromArgb(DEFAULT_THEME_SEED_ARGB),
        isDark = isDark,
        invertOnPrimaryColors = invertOnPrimaryColors,
    )
}

private fun Context.systemColor(@ColorRes colorRes: Int): Color = Color(getColor(colorRes))

private fun Color.readableOnColor(invert: Boolean): Color {
    val readable = readableOn(this, contrastBoost = 0f)
    return if (!invert) readable else if (readable.luminance() > 0.5f) Color.Black else Color.White
}

private fun derivePaletteFromSeed(

    seed: Color,
    invertOnPrimaryColors: Boolean,
    monetStyle: MonetStyle,
    monetContrast: MonetContrast,
    monetIntensity: Float,
): ThemePalette {
    val normalizedSeed = seed.normalizeForMonet(monetStyle = monetStyle, intensity = monetIntensity)
    return ThemePalette(
        light = deriveThemeColors(
            base = basePalette.light,
            seed = normalizedSeed,
            dark = false,
            invertOnPrimaryColors = invertOnPrimaryColors,
            monetStyle = monetStyle,
            monetContrast = monetContrast,
            monetIntensity = monetIntensity,
        ),
        dark = deriveThemeColors(
            base = basePalette.dark,
            seed = normalizedSeed,
            dark = true,
            invertOnPrimaryColors = invertOnPrimaryColors,
            monetStyle = monetStyle,
            monetContrast = monetContrast,
            monetIntensity = monetIntensity,
        ),
    )
}

private fun deriveThemeColors(
    base: ThemeColors,
    seed: Color,
    dark: Boolean,
    invertOnPrimaryColors: Boolean,
    monetStyle: MonetStyle,
    monetContrast: MonetContrast,
    monetIntensity: Float,
): ThemeColors {
    val intensity = monetIntensity.coerceIn(0f, 1f)
    val surfaceTintWeight = when (monetStyle) {
        MonetStyle.TonalSpot -> 0.18f
        MonetStyle.Vibrant -> 0.24f
        MonetStyle.Expressive -> 0.22f
        MonetStyle.Neutral -> 0.10f
        MonetStyle.Monochrome -> 0.04f
    } * (0.55f + intensity)
    val primaryStrength = when (monetStyle) {
        MonetStyle.TonalSpot -> 0.90f
        MonetStyle.Vibrant -> 1.00f
        MonetStyle.Expressive -> 0.95f
        MonetStyle.Neutral -> 0.62f
        MonetStyle.Monochrome -> 0.35f
    }
    val contrastBoost = when (monetContrast) {
        MonetContrast.Standard -> 0f
        MonetContrast.Medium -> 0.08f
        MonetContrast.High -> 0.16f
    }

    val primarySeed = seed.mix(if (dark) Color.White else Color.Black, if (dark) 0.10f else 0.05f)
    val neutralSeed = seed.normalizeForMonet(
        monetStyle = if (monetStyle == MonetStyle.Monochrome) MonetStyle.Monochrome else MonetStyle.Neutral,
        intensity = intensity * 0.55f,
    )
    val primary = primarySeed.mix(neutralSeed, (1f - primaryStrength) * 0.35f)
    val primaryVariant = if (dark) {
        seed.mix(Color.White, 0.28f + intensity * 0.16f)
    } else {
        seed.mix(Color.White, 0.18f + intensity * 0.16f)
    }
    val primaryContainerSeed = if (monetStyle == MonetStyle.Expressive) {
        seed.rotateHue(32f)
    } else {
        seed
    }

    val disabledPrimary = base.disabledPrimary.mix(neutralSeed, surfaceTintWeight * if (dark) 0.70f else 0.58f)
    val disabledOnPrimary = base.disabledOnPrimary.mix(neutralSeed, surfaceTintWeight * 0.28f)
    val disabledPrimaryButton = base.disabledPrimaryButton.mix(neutralSeed, surfaceTintWeight * 0.62f)
    val disabledOnPrimaryButton = base.disabledOnPrimaryButton.mix(neutralSeed, surfaceTintWeight * 0.24f)
    val disabledPrimarySlider = base.disabledPrimarySlider.mix(neutralSeed, surfaceTintWeight * 0.54f)

    val primaryContainer = if (dark) {
        primaryContainerSeed.mix(Color.Black, 0.58f).mix(Color.White, 0.06f)
    } else {
        primaryContainerSeed.mix(Color.White, 0.78f)
    }

    val tertiarySeed = when (monetStyle) {
        MonetStyle.Expressive -> neutralSeed.rotateHue(18f)
        MonetStyle.Vibrant -> neutralSeed.mix(seed.rotateHue(18f).normalizeForMonet(MonetStyle.Neutral, intensity * 0.45f), 0.16f)
        else -> neutralSeed
    }
    val tertiaryContainer = if (dark) {
        tertiarySeed.mix(Color.Black, 0.82f).mix(Color.White, 0.03f)
    } else {
        tertiarySeed.mix(Color.White, 0.90f)
    }
    val tertiaryContainerVariant = if (dark) {
        tertiarySeed.mix(Color.Black, 0.68f).mix(Color.White, 0.04f)
    } else {
        tertiarySeed.mix(Color.White, 0.80f)
    }

    val fallbackPalette = if (invertOnPrimaryColors) basePalette.dark else basePalette.light
    val onPrimary = if (invertOnPrimaryColors) fallbackPalette.onPrimary else readableOn(primary, contrastBoost)
    val onPrimaryVariant = if (invertOnPrimaryColors) fallbackPalette.onPrimaryVariant else readableOn(primaryVariant, contrastBoost * 0.5f)
    val onPrimaryContainer = if (invertOnPrimaryColors) fallbackPalette.onPrimaryContainer else readableOn(primaryContainer, contrastBoost)
    val onTertiaryContainer = if (invertOnPrimaryColors) fallbackPalette.onTertiaryContainer else readableOn(tertiaryContainer, contrastBoost)

    return base.copy(
        primary = primary.boostContrast(dark = dark, amount = contrastBoost * 0.35f),
        onPrimary = onPrimary,
        primaryVariant = primaryVariant,
        onPrimaryVariant = onPrimaryVariant,
        disabledPrimary = disabledPrimary,
        disabledOnPrimary = disabledOnPrimary,
        disabledPrimaryButton = disabledPrimaryButton,
        disabledOnPrimaryButton = disabledOnPrimaryButton,
        disabledPrimarySlider = disabledPrimarySlider,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        tertiaryContainerVariant = tertiaryContainerVariant,
        onBackgroundVariant = if (dark) {
            neutralSeed.mix(Color.White, 0.24f + contrastBoost)
        } else {
            neutralSeed.mix(Color.Black, 0.18f + contrastBoost)
        },
    )
}

private fun Color.mix(other: Color, ratio: Float): Color {
    val t = ratio.coerceIn(0f, 1f)
    return Color(
        red = red + (other.red - red) * t,
        green = green + (other.green - green) * t,
        blue = blue + (other.blue - blue) * t,
        alpha = alpha + (other.alpha - alpha) * t,
    )
}

private fun Color.normalizeForMonet(monetStyle: MonetStyle, intensity: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(toArgb(), hsv)
    val clampedIntensity = intensity.coerceIn(0f, 1f)
    when (monetStyle) {
        MonetStyle.TonalSpot -> hsv[1] = (hsv[1] * (0.62f + clampedIntensity * 0.40f)).coerceIn(0.18f, 0.82f)
        MonetStyle.Vibrant -> hsv[1] = (hsv[1] * (0.92f + clampedIntensity * 0.50f)).coerceIn(0.36f, 1.0f)
        MonetStyle.Expressive -> {
            hsv[0] = (hsv[0] + 18f) % 360f
            hsv[1] = (hsv[1] * (0.74f + clampedIntensity * 0.44f)).coerceIn(0.28f, 0.92f)
        }
        MonetStyle.Neutral -> hsv[1] = (hsv[1] * (0.24f + clampedIntensity * 0.18f)).coerceIn(0.06f, 0.36f)
        MonetStyle.Monochrome -> hsv[1] = (hsv[1] * 0.06f).coerceIn(0f, 0.08f)
    }
    hsv[2] = hsv[2].coerceIn(0.34f, 0.86f)
    return Color(AndroidColor.HSVToColor((alpha * 255).toInt().coerceIn(0, 255), hsv))
}

private fun Color.rotateHue(degrees: Float): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(toArgb(), hsv)
    hsv[0] = (hsv[0] + degrees).mod(360f)
    return Color(AndroidColor.HSVToColor((alpha * 255).toInt().coerceIn(0, 255), hsv))
}

private fun Color.boostContrast(dark: Boolean, amount: Float): Color =
    mix(if (dark) Color.White else Color.Black, amount.coerceIn(0f, 0.35f))

private fun readableOn(background: Color, contrastBoost: Float): Color {
    val darkText = Color.Black.mix(background, (0.05f - contrastBoost * 0.18f).coerceAtLeast(0f))
    val lightText = Color.White.mix(background, (0.04f - contrastBoost * 0.12f).coerceAtLeast(0f))
    return if (background.luminance() > 0.52f) darkText else lightText
}

