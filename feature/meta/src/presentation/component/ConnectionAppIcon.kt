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


package com.github.yumelira.yumebox.feature.meta.presentation.component
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import com.github.yumelira.yumebox.presentation.component.rememberInstalledAppIcon
import com.github.yumelira.yumebox.data.controller.AppIdentity
import com.github.yumelira.yumebox.data.controller.AppIdentityResolver
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import com.github.yumelira.yumebox.presentation.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.koin.compose.koinInject

private const val CONNECTION_APP_ICON_BITMAP_SIZE = 80

@Composable
internal fun ConnectionLeadingIcon(
    metadata: JsonObject,
    network: String,
    modifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
    bitmapSize: Int = CONNECTION_APP_ICON_BITMAP_SIZE,
) {
    val sizes = AppTheme.sizes
    val appIdentityResolver: AppIdentityResolver = koinInject()
    val identity by produceState<AppIdentity?>(
        initialValue = null,
        key1 = metadata,
        key2 = appIdentityResolver,
    ) {
        value = withContext(Dispatchers.IO) {
            appIdentityResolver.resolve(metadata)
        }
    }
    val resolvedSize = size.takeOrElse { sizes.connectionLeadingIconSize }
    val iconBitmap = rememberInstalledAppIcon(identity?.packageName, bitmapSize)

    val bitmap = iconBitmap
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = identity?.appName?.ifEmpty { network } ?: network,
            modifier = modifier
                .size(resolvedSize)
                .clip(RoundedCornerShape(sizes.connectionLeadingIconCornerRadius)),
        )
        return
    }

    ProtocolFallbackIcon(
        network = network,
        modifier = modifier,
        size = resolvedSize,
    )
}

@Composable
private fun ProtocolFallbackIcon(
    network: String,
    modifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
) {
    val sizes = AppTheme.sizes
    val neutral = MaterialTheme.colorScheme.onSurface
    val resolvedSize = size.takeOrElse { sizes.connectionLeadingIconSize }
    val protocolColor = getProtocolColor(network)

    Box(
        modifier = modifier
            .size(resolvedSize)
            .clip(RoundedCornerShape(sizes.connectionLeadingIconCornerRadius))
            .background(neutral.copy(alpha = AppTheme.opacity.ultraSubtle + AppTheme.opacity.ambientShadow)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = network.take(3).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
            color = protocolColor,
        )
    }
}

@Composable
internal fun getProtocolColor(network: String): androidx.compose.ui.graphics.Color =
    getProtocolColor(network, AppTheme.colors)

internal fun getProtocolColor(
    network: String,
    appColors: AppColors,
) = when (network.uppercase()) {
    "TCP" -> appColors.protocol.tcp
    "UDP" -> appColors.protocol.udp
    "HTTP" -> appColors.protocol.http
    "HTTPS" -> appColors.protocol.https
    else -> appColors.protocol.unknown
}
