/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.profiles

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.github.yumelira.yumebox.common.util.ByteFormatter
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.Md3EIndeterminateCircularWavyProgressIndicator
import com.github.yumelira.yumebox.presentation.component.PreferenceSwitchItem
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DropdownPreference
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import com.github.yumelira.yumebox.presentation.theme.UiDp
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.request.ImageRequest
import dev.oom_wg.purejoy.mlang.MLang

@Composable
internal fun KokoroProfileContent(
    authState: KokoroAuthState,
    settings: MihomoSubscriptionSettings,
    availableOptions: KokoroSubscriptionOptions,
    error: String,
    onSettingsChange: (MihomoSubscriptionSettings) -> Unit,
) {
    val authenticated = authState as? KokoroAuthState.Authenticated
    val subscriptions = authenticated?.account?.subscriptions.orEmpty()
    val effectiveOptions = if (availableOptions.plans.isEmpty()) {
        KokoroSubscriptionOptions.fallback(authenticated?.account)
    } else {
        availableOptions
    }
    val normalizedSettings = effectiveOptions.normalize(settings)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UiDp.dp16),
    ) {
        when {
            authenticated != null && subscriptions.isNotEmpty() -> {
                MihomoSubscriptionSettingsContent(
                    settings = normalizedSettings,
                    availableOptions = effectiveOptions,
                    onSettingsChange = onSettingsChange,
                )
            }

            authenticated != null -> KokoroSubscriptionNotice(
                text = MLang.ProfilesPage.Kokoro.NoSubscription,
            )

            authState == KokoroAuthState.Checking -> KokoroSubscriptionNotice(
                text = MLang.ProfilesPage.Kokoro.Checking,
                loading = true,
            )

            else -> KokoroSubscriptionNotice(
                text = MLang.ProfilesPage.Kokoro.SignInFromSettings,
            )
        }

        if (error.isNotBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun KokoroSubscriptionNotice(
    text: String,
    loading: Boolean = false,
) {
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(UiDp.dp16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
        ) {
            if (loading) {
                Md3EIndeterminateCircularWavyProgressIndicator()
            }
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun KokoroAccountCard(
    authState: KokoroAuthState,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
    subscriptionPlan: String? = null,
) {
    val subscriptions = (authState as? KokoroAuthState.Authenticated)
        ?.account
        ?.subscriptions
        .orEmpty()
    val selectedSubscription = subscriptions.firstOrNull { it.plan == subscriptionPlan }
        ?: subscriptions.firstOrNull()

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiDp.dp16),
            verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
        ) {
            when (authState) {
                KokoroAuthState.Checking -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
                ) {
                    Md3EIndeterminateCircularWavyProgressIndicator()
                    Text(MLang.ProfilesPage.Kokoro.Checking)
                }

                KokoroAuthState.LoggedOut -> {
                    StatusText(
                        title = MLang.ProfilesPage.Kokoro.LoggedOut,
                        detail = MLang.ProfilesPage.Kokoro.LoginHint,
                    )
                    Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                        Text(MLang.ProfilesPage.Kokoro.Login)
                    }
                }

                is KokoroAuthState.Error -> {
                    StatusText(
                        title = MLang.ProfilesPage.Kokoro.CheckFailed,
                        detail = authState.message,
                        error = true,
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(UiDp.dp8),
                    ) {
                        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                            Text(MLang.ProfilesPage.Kokoro.Retry)
                        }
                        OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                            Text(MLang.ProfilesPage.Kokoro.Login)
                        }
                    }
                }

                is KokoroAuthState.Authenticated -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
                    ) {
                        OsuAvatar(
                            displayName = authState.account.displayName,
                            avatarUrl = authState.account.avatarUrl,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authState.account.displayName
                                    ?: MLang.ProfilesPage.Kokoro.LoggedIn,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (subscriptions.isEmpty()) {
                                    MLang.ProfilesPage.Kokoro.NoSubscription
                                } else {
                                    subscriptions.joinToString(", ") { it.plan }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = onLogout) {
                            Text(MLang.ProfilesPage.Kokoro.Logout)
                        }
                    }

                    selectedSubscription?.let { subscription ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        subscription.usedBytes?.let {
                            SubscriptionLine(
                                MLang.ProfilesPage.Kokoro.TrafficUsed,
                                ByteFormatter.format(it),
                            )
                        }
                        subscription.totalBytes?.let {
                            SubscriptionLine(
                                MLang.ProfilesPage.Kokoro.BandwidthLimit,
                                if (it > 0) ByteFormatter.format(it) else MLang.ProfilesPage.Kokoro.Unlimited,
                            )
                        }
                        if (!subscription.expiresAt.isNullOrBlank()) {
                            SubscriptionLine(
                                MLang.ProfilesPage.Kokoro.Expires,
                                displayExpiry(subscription.expiresAt),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MihomoSubscriptionSettingsContent(
    settings: MihomoSubscriptionSettings,
    availableOptions: KokoroSubscriptionOptions,
    onSettingsChange: (MihomoSubscriptionSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    val normalized = availableOptions.normalize(settings)
    val protocol = availableOptions.protocols.firstOrNull { it.value == normalized.protocol }
    val supportsDirect = protocol?.supportsDirect == true
    val plan = availableOptions.plans.firstOrNull { it.name == normalized.plan }
    val supportedPlanIsps = plan?.supportedIsps.orEmpty().filterNot { it == "all" }
    val selectableIsps = availableOptions.isps.filter {
        it.value.isBlank() || it.value in supportedPlanIsps
    }.ifEmpty { availableOptions.isps }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
        SectionLabel(MLang.ProfilesPage.Kokoro.Subscription)

        Card {
            Column {
                YumeMd3DropdownPreference(
                    title = MLang.ProfilesPage.Kokoro.Protocol,
                    items = availableOptions.protocols.map { it.label },
                    selectedIndex = availableOptions.protocols.indexOfFirst { it.value == normalized.protocol }
                        .coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        val selected = availableOptions.protocols[index]
                        onSettingsChange(
                            availableOptions.normalize(
                                normalized.copy(
                                    protocol = selected.value,
                                    mode = if (selected.supportsDirect) normalized.mode else "relay",
                                ),
                            ),
                        )
                    },
                    showDivider = true,
                )
                YumeMd3DropdownPreference(
                    title = MLang.ProfilesPage.Kokoro.Plan,
                    items = availableOptions.plans.map { it.name },
                    selectedIndex = availableOptions.plans.indexOfFirst { it.name == normalized.plan }
                        .coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        onSettingsChange(
                            availableOptions.normalize(normalized.copy(plan = availableOptions.plans[index].name)),
                        )
                    },
                    enabled = availableOptions.plans.isNotEmpty(),
                    showDivider = true,
                )
                YumeMd3DropdownPreference(
                    title = MLang.ProfilesPage.Kokoro.Isp,
                    items = selectableIsps.map { localizedIspLabel(it) },
                    selectedIndex = selectableIsps.indexOfFirst { it.value == normalized.isp }.coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        onSettingsChange(normalized.copy(isp = selectableIsps[index].value))
                    },
                    showDivider = supportsDirect,
                )
                AnimatedVisibility(visible = supportsDirect) {
                    YumeMd3DropdownPreference(
                        title = MLang.ProfilesPage.Kokoro.Mode,
                        items = listOf(
                            MLang.ProfilesPage.Kokoro.Relay,
                            MLang.ProfilesPage.Kokoro.Direct,
                        ),
                        selectedIndex = if (normalized.mode == "direct") 1 else 0,
                        onSelectedIndexChange = { index ->
                            onSettingsChange(normalized.copy(mode = if (index == 1) "direct" else "relay"))
                        },
                        showDivider = false,
                    )
                }
            }
        }

        SectionLabel(MLang.ProfilesPage.Kokoro.Routing)

        Card {
            Column {
                YumeMd3DropdownPreference(
                    title = MLang.ProfilesPage.Kokoro.RuleSource,
                    items = availableOptions.ruleSources.map {
                        if (it == "mirror") MLang.ProfilesPage.Kokoro.Mirror
                        else MLang.ProfilesPage.Kokoro.Origin
                    },
                    selectedIndex = availableOptions.ruleSources.indexOf(normalized.ruleSource).coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        onSettingsChange(normalized.copy(ruleSource = availableOptions.ruleSources[index]))
                    },
                    showDivider = true,
                )
                YumeMd3DropdownPreference(
                    title = MLang.ProfilesPage.Kokoro.FinalRoute,
                    items = availableOptions.finalRoutes.map {
                        if (it == "direct") MLang.ProfilesPage.Kokoro.Direct
                        else MLang.ProfilesPage.Kokoro.Proxy
                    },
                    selectedIndex = availableOptions.finalRoutes.indexOf(normalized.finalRoute).coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        onSettingsChange(normalized.copy(finalRoute = availableOptions.finalRoutes[index]))
                    },
                    showDivider = false,
                )
            }
        }

        SectionLabel(MLang.ProfilesPage.Kokoro.Updates)

        Card {
            Column {
                PreferenceSwitchItem(
                    title = MLang.ProfilesPage.Kokoro.RuleProviderAutoUpdate,
                    checked = normalized.ruleProviderAutoUpdate,
                    onCheckedChange = {
                        onSettingsChange(normalized.copy(ruleProviderAutoUpdate = it))
                    },
                )
                SettingsDivider()
                PreferenceSwitchItem(
                    title = MLang.ProfilesPage.Kokoro.SubscriptionAutoUpdate,
                    checked = normalized.subscriptionAutoUpdate,
                    onCheckedChange = {
                        onSettingsChange(normalized.copy(subscriptionAutoUpdate = it))
                    },
                )
                AnimatedVisibility(visible = normalized.subscriptionAutoUpdate) {
                    Column {
                        SettingsDivider()
                        UpdateIntervalPreference(
                            hours = normalized.updateIntervalHours,
                            minHours = availableOptions.minUpdateHours,
                            maxHours = availableOptions.maxUpdateHours,
                            onHoursChange = { hours ->
                                onSettingsChange(normalized.copy(updateIntervalHours = hours))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    val spacing = AppTheme.spacing
    val opacity = AppTheme.opacity
    val sizes = AppTheme.sizes
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = spacing.space16),
        thickness = sizes.thinDividerThickness,
        color = MaterialTheme.colorScheme.outline.copy(alpha = opacity.outline),
    )
}

@Composable
private fun UpdateIntervalPreference(
    hours: Int,
    minHours: Int,
    maxHours: Int,
    onHoursChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UiDp.dp16, vertical = UiDp.dp8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiDp.dp8),
    ) {
        Text(
            text = MLang.ProfilesPage.Kokoro.ProfileUpdate,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = MLang.ProfilesPage.Kokoro.UpdateHoursValue.format(hours),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onHoursChange(hours - 1) },
                    enabled = hours > minHours,
                    modifier = Modifier.semantics {
                        contentDescription = MLang.ProfilesPage.Kokoro.DecreaseUpdateHours
                    },
                ) {
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                VerticalDivider(
                    modifier = Modifier.height(UiDp.dp24),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                IconButton(
                    onClick = { onHoursChange(hours + 1) },
                    enabled = hours < maxHours,
                    modifier = Modifier.semantics {
                        contentDescription = MLang.ProfilesPage.Kokoro.IncreaseUpdateHours
                    },
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun OsuAvatar(displayName: String?, avatarUrl: String?) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(UiDp.dp48)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(
                    request = ImageRequest(context, avatarUrl),
                    contentScale = ContentScale.Crop,
                ),
                contentDescription = MLang.ProfilesPage.Kokoro.AvatarDescription.format(
                    displayName ?: MLang.ProfilesPage.Kokoro.LoggedIn,
                ),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = displayName?.trim()?.firstOrNull()?.uppercase() ?: "K",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = UiDp.dp8),
    )
}

@Composable
private fun localizedIspLabel(option: KokoroSubscriptionOptions.IspOption): String = when (option.value) {
    "" -> MLang.ProfilesPage.Kokoro.IspAuto
    "ct" -> MLang.ProfilesPage.Kokoro.IspCt
    "cu" -> MLang.ProfilesPage.Kokoro.IspCu
    "cm" -> MLang.ProfilesPage.Kokoro.IspCm
    "other" -> MLang.ProfilesPage.Kokoro.IspOther
    else -> option.label
}

@Composable
private fun StatusText(title: String, detail: String, error: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(UiDp.dp4)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SubscriptionLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun displayExpiry(value: String): String = value
    .removeSuffix("Z")
    .replace('T', ' ')
