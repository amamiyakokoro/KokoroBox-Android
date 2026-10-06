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


package com.amamiyakokoro.box.presentation.screen.node

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.model.Proxy
import com.amamiyakokoro.box.presentation.component.CountryFlagCircle
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.AppMotion
import com.amamiyakokoro.box.presentation.theme.AppTheme
import com.amamiyakokoro.box.presentation.theme.appPressSink

@Composable
internal fun nodeLatencyLabel(delay: Int?): Pair<String, Color>? = when {
    delay == null -> null
    delay < 0 -> stringResource(LocaleR.string.proxy_node_timeout) to AppTheme.colors.latency.timeout
    delay == 0 -> null
    delay in 1..300 -> stringResource(LocaleR.string.home_node_info_delay_value).format(delay) to AppTheme.colors.latency.fast
    delay in 301..1000 -> stringResource(LocaleR.string.home_node_info_delay_value).format(delay) to AppTheme.colors.latency.moderate
    delay in 1001..3000 -> stringResource(LocaleR.string.home_node_info_delay_value).format(delay) to AppTheme.colors.latency.slow
    else -> null
}

internal fun Proxy.Type.displayName(): String = when (this) {
    Proxy.Type.Direct -> "Direct"
    Proxy.Type.Reject -> "Reject"
    Proxy.Type.RejectDrop -> "RejectDrop"
    Proxy.Type.Compatible -> "Compatible"
    Proxy.Type.Pass -> "Pass"
    Proxy.Type.Relay -> "Relay"
    Proxy.Type.Selector -> "Selector"
    Proxy.Type.Fallback -> "Fallback"
    Proxy.Type.URLTest -> "URLTest"
    Proxy.Type.LoadBalance -> "LoadBalance"
    Proxy.Type.Smart -> "Smart"
    Proxy.Type.Unknown -> "Unknown"
    Proxy.Type.Shadowsocks -> "SS"
    Proxy.Type.ShadowsocksR -> "SSR"
    Proxy.Type.Snell -> "Snell"
    Proxy.Type.Socks5 -> "SOCKS5"
    Proxy.Type.Http -> "HTTP"
    Proxy.Type.Vmess -> "VMess"
    Proxy.Type.Vless -> "VLESS"
    Proxy.Type.Trojan -> "Trojan"
    Proxy.Type.Hysteria -> "Hysteria"
    Proxy.Type.Hysteria2 -> "Hysteria2"
    Proxy.Type.Tuic -> "TUIC"
    Proxy.Type.WireGuard -> "WireGuard"
    Proxy.Type.Dns -> "DNS"
    Proxy.Type.Ssh -> "SSH"
    Proxy.Type.Mieru -> "Mieru"
    Proxy.Type.AnyTLS -> "AnyTLS"
    Proxy.Type.Sudoku -> "Sudoku"
    Proxy.Type.Masque -> "Masque"
    Proxy.Type.TrustTunnel -> "TrustTunnel"
}

internal fun Proxy.Type.iconLabel(): String = when (this) {
    Proxy.Type.Direct -> "DI"
    Proxy.Type.Reject -> "RJ"
    Proxy.Type.RejectDrop -> "RD"
    Proxy.Type.Compatible -> "CP"
    Proxy.Type.Pass -> "PS"
    Proxy.Type.Relay -> "RL"
    Proxy.Type.Selector -> "SE"
    Proxy.Type.Fallback -> "FB"
    Proxy.Type.URLTest -> "UT"
    Proxy.Type.LoadBalance -> "LB"
    Proxy.Type.Smart -> "SM"
    Proxy.Type.Unknown -> "UN"
    Proxy.Type.Shadowsocks -> "SS"
    Proxy.Type.ShadowsocksR -> "SR"
    Proxy.Type.Snell -> "SN"
    Proxy.Type.Socks5 -> "S5"
    Proxy.Type.Http -> "HT"
    Proxy.Type.Vmess -> "VM"
    Proxy.Type.Vless -> "VL"
    Proxy.Type.Trojan -> "TR"
    Proxy.Type.Hysteria -> "HY"
    Proxy.Type.Hysteria2 -> "H2"
    Proxy.Type.Tuic -> "TU"
    Proxy.Type.WireGuard -> "WG"
    Proxy.Type.Dns -> "DN"
    Proxy.Type.Ssh -> "SH"
    Proxy.Type.Mieru -> "MI"
    Proxy.Type.AnyTLS -> "AT"
    Proxy.Type.Sudoku -> "SU"
    Proxy.Type.Masque -> "MQ"
    Proxy.Type.TrustTunnel -> "TT"
}

internal data class ProxySelectionPalette(
    val containerColor: Color,
    val borderColor: Color,
    val contentColor: Color,
    val supportingColor: Color,
    val chipBackgroundColor: Color,
    val chipContentColor: Color,
    val trailingBadgeBackgroundColor: Color,
    val trailingBadgeContentColor: Color,
    val iconBackgroundColor: Color,
    val iconContentColor: Color,
)

@Composable
internal fun rememberProxySelectionPalette(
    selected: Boolean,
    defaultContainerColor: Color? = null,
): ProxySelectionPalette {
    val colorScheme = MaterialTheme.colorScheme
    val surface = colorScheme.surface
    val isDarkTheme = isSystemInDarkTheme()
    val fallbackContainer = defaultContainerColor
        ?: colorScheme.surfaceVariant.copy(alpha = 0.42f).compositeOver(surface)
    val selectedContainer = colorScheme.primaryContainer
        .copy(alpha = if (isDarkTheme) 0.62f else 0.78f)
        .compositeOver(surface)

    return if (selected) {
        ProxySelectionPalette(
            containerColor = selectedContainer,
            borderColor = Color.Transparent,
            contentColor = colorScheme.primary,
            supportingColor = colorScheme.onSurfaceVariant,
            chipBackgroundColor = colorScheme.primary.copy(alpha = 0.10f),
            chipContentColor = colorScheme.primary,
            trailingBadgeBackgroundColor = colorScheme.primary.copy(alpha = 0.10f),
            trailingBadgeContentColor = colorScheme.primary,
            iconBackgroundColor = colorScheme.primary.copy(alpha = 0.10f),
            iconContentColor = colorScheme.primary,
        )
    } else {
        ProxySelectionPalette(
            containerColor = fallbackContainer,
            borderColor = Color.Transparent,
            contentColor = colorScheme.onSurface,
            supportingColor = colorScheme.onSurface.copy(alpha = 0.72f),
            chipBackgroundColor = colorScheme.primary.copy(alpha = 0.10f),
            chipContentColor = colorScheme.primary,
            trailingBadgeBackgroundColor = Color.Transparent,
            trailingBadgeContentColor = colorScheme.primary,
            iconBackgroundColor = colorScheme.primary.copy(alpha = 0.10f),
            iconContentColor = colorScheme.primary,
        )
    }
}

@Composable
internal fun RotatingRefreshIcon(
    isRotating: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "node_delay_test_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
        ),
        label = "node_delay_test_rotation_value",
    )

    Icon(
        imageVector = AppMd3Icons.Action.Refresh,
        contentDescription = contentDescription ?: stringResource(LocaleR.string.proxy_action_test),
        tint = tint,
        modifier = if (isRotating) modifier.rotate(rotation) else modifier,
    )
}

@Composable
internal fun NodeSelectableCard(
    isSelected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    paddingVertical: Dp,
    paddingHorizontal: Dp? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val radii = AppTheme.radii
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current
    val shape = RoundedCornerShape(radii.radius18)
    val palette = rememberProxySelectionPalette(selected = isSelected)
    val fastEffectsSpec = AppMotion.fastEffects<Color>()
    val transition = updateTransition(targetState = palette, label = "node_card_selection")
    val backgroundColor by transition.animateColor(
        transitionSpec = { fastEffectsSpec },
        label = "node_card_background_color",
    ) { it.containerColor }
    val cardRipple = ripple(
        bounded = true,
        color = MaterialTheme.colorScheme.primary,
    )

    Box(
        modifier = modifier
            .appPressSink(
                interactionSource = interactionSource,
                enabled = onClick != null,
            )
            .fillMaxWidth()
            .semantics { selected = isSelected }
            .clip(shape)
            .background(backgroundColor)
            .let { cardModifier ->
                if (onClick != null) {
                    cardModifier.clickable(
                        interactionSource = interactionSource,
                        indication = cardRipple,
                        role = Role.RadioButton,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onClick()
                        },
                    )
                } else {
                    cardModifier
                }
            }
            .padding(horizontal = paddingHorizontal ?: AppTheme.sizes.nodeCardPaddingHorizontal, vertical = paddingVertical),
        content = content,
    )
}

@Composable
internal fun NodeCard(
    proxy: Proxy,
    isSelected: Boolean,
    onClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
    isDelayTesting: Boolean = false,
    isThisProxyTesting: Boolean = false,
    onSingleNodeTestClick: ((String) -> Unit)? = null,
    showCountryFlag: Boolean = true,
    singleNodeTestEnabled: Boolean = true,
) {
    val palette = rememberProxySelectionPalette(selected = isSelected)
    val onCardClick = remember(proxy.name, onClick) {
        onClick?.let { click -> { click(proxy.name) } }
    }
    val onNodeTestClick = remember(proxy.name, onSingleNodeTestClick) {
        onSingleNodeTestClick?.let { click -> { click(proxy.name) } }
    }
    val presentation = remember(proxy.name, proxy.title) {
        resolveProxyDisplayPresentation(name = proxy.name, title = proxy.title)
    }
    val delayLabel = nodeLatencyLabel(proxy.delay)
    NodeSelectableCard(
        isSelected = isSelected,
        onClick = onCardClick,
        modifier = modifier.heightIn(min = 104.dp),
        paddingVertical = 14.dp,
        paddingHorizontal = 14.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                presentation.countryCode?.takeIf { showCountryFlag }?.let { country ->
                    CountryFlagCircle(countryCode = country, size = 16.dp)
                }
                Text(
                    text = presentation.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = palette.contentColor,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val measurer = rememberTextMeasurer()
                val density = LocalDensity.current
                val typeStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
                val latencyStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp)
                val badgeWidth = measurer.measure(proxy.type.iconLabel(), style = typeStyle, maxLines = 1).size.width
                val latencyWidth = delayLabel?.let {
                    measurer.measure(it.first.replace(Regex("\\s*ms$"), " ms"), style = latencyStyle, maxLines = 1).size.width
                } ?: 0
                val minimumRowWidth = with(density) {
                    badgeWidth.toDp() + 12.dp + maxOf(latencyWidth.toDp(), 40.dp) + 6.dp
                }
                val onTestClick = onNodeTestClick.takeIf { singleNodeTestEnabled }
                if (maxWidth < minimumRowWidth) {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ProtocolBadge(proxy.type, modifier = Modifier.widthIn(max = 120.dp))
                        NodeLatency(delayLabel, onTestClick, isThisProxyTesting || isDelayTesting,
                            palette.supportingColor, Modifier.align(Alignment.End))
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProtocolBadge(proxy.type, modifier = Modifier.weight(1f))
                        NodeLatency(delayLabel, onTestClick, isThisProxyTesting || isDelayTesting, palette.supportingColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtocolBadge(type: Proxy.Type, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)
    val measurer = rememberTextMeasurer()
    val fullLabel = type.displayName()
    val fullWidth = remember(fullLabel, style, LocalDensity.current) {
        measurer.measure(fullLabel, style = style, maxLines = 1).size.width
    }
    // The weighted slot reserves room for latency; the Surface wraps only its label.
    BoxWithConstraints(modifier = modifier) {
        val label = if (with(LocalDensity.current) { (maxWidth - 12.dp).toPx() } >= fullWidth) {
            fullLabel
        } else type.iconLabel()
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.heightIn(min = 28.dp).semantics { contentDescription = fullLabel },
        ) {
            Text(label, style = style, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun NodeLatency(
    delayLabel: Pair<String, Color>?,
    onTestClick: (() -> Unit)?,
    testing: Boolean,
    supportingColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.widthIn(min = 40.dp).heightIn(min = 40.dp).let { base ->
            if (onTestClick != null) base.clickable(role = Role.Button,
                onClickLabel = stringResource(LocaleR.string.proxy_action_test), onClick = onTestClick) else base
        },
        contentAlignment = Alignment.CenterEnd,
    ) {
        when {
            delayLabel != null -> Text(delayLabel.first.replace(Regex("\\s*ms$"), " ms"),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = delayLabel.second, maxLines = 1)
            onTestClick != null -> if (testing) {
                RotatingRefreshIcon(true, modifier = Modifier.size(20.dp), tint = supportingColor)
            } else {
                Icon(AppMd3Icons.Proxy.CloudTest, stringResource(LocaleR.string.proxy_action_test),
                    tint = supportingColor, modifier = Modifier.size(20.dp))
            }
        }
    }
}
