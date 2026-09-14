/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.screen.profiles

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
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.amamiyakokoro.box.common.util.ByteFormatter
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.locale.resolve
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.Md3EIndeterminateCircularWavyProgressIndicator
import com.amamiyakokoro.box.presentation.component.PreferenceSwitchItem
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.theme.AppTheme
import com.amamiyakokoro.box.presentation.theme.UiDp
import com.github.panpf.sketch.rememberAsyncImagePainter
import com.github.panpf.sketch.request.ImageRequest

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
                text = stringResource(LocaleR.string.profiles_page_kokoro_no_subscription),
            )

            authState == KokoroAuthState.Checking -> KokoroSubscriptionNotice(
                text = stringResource(LocaleR.string.profiles_page_kokoro_checking),
                loading = true,
            )

            else -> KokoroSubscriptionNotice(
                text = stringResource(LocaleR.string.profiles_page_kokoro_sign_in_from_settings),
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
                    Text(stringResource(LocaleR.string.profiles_page_kokoro_checking))
                }

                KokoroAuthState.LoggedOut -> {
                    StatusText(
                        title = stringResource(LocaleR.string.profiles_page_kokoro_logged_out),
                        detail = stringResource(LocaleR.string.profiles_page_kokoro_login_hint),
                    )
                    Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(LocaleR.string.profiles_page_kokoro_login))
                    }
                }

                is KokoroAuthState.Error -> {
                    StatusText(
                        title = stringResource(LocaleR.string.profiles_page_kokoro_check_failed),
                        detail = authState.message.resolve(LocalResources.current),
                        error = true,
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(UiDp.dp8),
                    ) {
                        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(LocaleR.string.profiles_page_kokoro_retry))
                        }
                        OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(LocaleR.string.profiles_page_kokoro_login))
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
                                    ?: stringResource(LocaleR.string.profiles_page_kokoro_logged_in),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = if (subscriptions.isEmpty()) {
                                    stringResource(LocaleR.string.profiles_page_kokoro_no_subscription)
                                } else {
                                    subscriptions.joinToString(", ") { it.plan }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = onLogout) {
                            Text(stringResource(LocaleR.string.profiles_page_kokoro_logout))
                        }
                    }

                    selectedSubscription?.let { subscription ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        subscription.usedBytes?.let {
                            SubscriptionLine(
                                stringResource(LocaleR.string.profiles_page_kokoro_traffic_used),
                                ByteFormatter.format(it),
                            )
                        }
                        subscription.totalBytes?.let {
                            SubscriptionLine(
                                stringResource(LocaleR.string.profiles_page_kokoro_bandwidth_limit),
                                if (it > 0) ByteFormatter.format(it)
                                else stringResource(LocaleR.string.profiles_page_kokoro_unlimited),
                            )
                        }
                        if (!subscription.expiresAt.isNullOrBlank()) {
                            SubscriptionLine(
                                stringResource(LocaleR.string.profiles_page_kokoro_expires),
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
        SectionLabel(stringResource(LocaleR.string.profiles_page_kokoro_subscription))

        Card {
            Column {
                YumeMd3DropdownPreference(
                    title = stringResource(LocaleR.string.profiles_page_kokoro_protocol),
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
                    title = stringResource(LocaleR.string.profiles_page_kokoro_plan),
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
                    title = stringResource(LocaleR.string.profiles_page_kokoro_isp),
                    items = selectableIsps.map { localizedIspLabel(it) },
                    selectedIndex = selectableIsps.indexOfFirst { it.value == normalized.isp }.coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        onSettingsChange(normalized.copy(isp = selectableIsps[index].value))
                    },
                    showDivider = supportsDirect,
                )
                AnimatedVisibility(visible = supportsDirect) {
                    YumeMd3DropdownPreference(
                        title = stringResource(LocaleR.string.profiles_page_kokoro_mode),
                        items = listOf(
                            stringResource(LocaleR.string.profiles_page_kokoro_relay),
                            stringResource(LocaleR.string.profiles_page_kokoro_direct),
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

        SectionLabel(stringResource(LocaleR.string.profiles_page_kokoro_routing))

        Card {
            Column {
                YumeMd3DropdownPreference(
                    title = stringResource(LocaleR.string.profiles_page_kokoro_rule_source),
                    items = availableOptions.ruleSources.map {
                        if (it == "mirror") stringResource(LocaleR.string.profiles_page_kokoro_mirror)
                        else stringResource(LocaleR.string.profiles_page_kokoro_origin)
                    },
                    selectedIndex = availableOptions.ruleSources.indexOf(normalized.ruleSource).coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        onSettingsChange(normalized.copy(ruleSource = availableOptions.ruleSources[index]))
                    },
                    showDivider = true,
                )
                YumeMd3DropdownPreference(
                    title = stringResource(LocaleR.string.profiles_page_kokoro_final_route),
                    items = availableOptions.finalRoutes.map {
                        if (it == "direct") stringResource(LocaleR.string.profiles_page_kokoro_direct)
                        else stringResource(LocaleR.string.profiles_page_kokoro_proxy)
                    },
                    selectedIndex = availableOptions.finalRoutes.indexOf(normalized.finalRoute).coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        onSettingsChange(normalized.copy(finalRoute = availableOptions.finalRoutes[index]))
                    },
                    showDivider = false,
                )
            }
        }

        SectionLabel(stringResource(LocaleR.string.profiles_page_kokoro_updates))

        Card {
            Column {
                PreferenceSwitchItem(
                    title = stringResource(LocaleR.string.profiles_page_kokoro_rule_provider_auto_update),
                    checked = normalized.ruleProviderAutoUpdate,
                    onCheckedChange = {
                        onSettingsChange(normalized.copy(ruleProviderAutoUpdate = it))
                    },
                )
                SettingsDivider()
                PreferenceSwitchItem(
                    title = stringResource(LocaleR.string.profiles_page_kokoro_subscription_auto_update),
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
    val decreaseDescription = stringResource(LocaleR.string.profiles_page_kokoro_decrease_update_hours)
    val increaseDescription = stringResource(LocaleR.string.profiles_page_kokoro_increase_update_hours)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = UiDp.dp16, vertical = UiDp.dp8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UiDp.dp8),
    ) {
        Text(
            text = stringResource(LocaleR.string.profiles_page_kokoro_profile_update),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(LocaleR.string.profiles_page_kokoro_update_hours_value).format(hours),
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
                        contentDescription = decreaseDescription
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
                        contentDescription = increaseDescription
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
    val loggedIn = stringResource(LocaleR.string.profiles_page_kokoro_logged_in)
    val avatarDescription = stringResource(LocaleR.string.profiles_page_kokoro_avatar_description)
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
                contentDescription = avatarDescription.format(
                    displayName ?: loggedIn,
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
    "" -> stringResource(LocaleR.string.profiles_page_kokoro_isp_auto)
    "ct" -> stringResource(LocaleR.string.profiles_page_kokoro_isp_ct)
    "cu" -> stringResource(LocaleR.string.profiles_page_kokoro_isp_cu)
    "cm" -> stringResource(LocaleR.string.profiles_page_kokoro_isp_cm)
    "other" -> stringResource(LocaleR.string.profiles_page_kokoro_isp_other)
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
