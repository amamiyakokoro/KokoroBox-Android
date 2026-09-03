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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.yumelira.yumebox.common.util.ByteFormatter
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.Md3EIndeterminateCircularWavyProgressIndicator
import com.github.yumelira.yumebox.presentation.component.PreferenceSwitchItem
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DropdownPreference
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3OutlinedTextField
import com.github.yumelira.yumebox.presentation.theme.AppTheme
import com.github.yumelira.yumebox.presentation.theme.UiDp
import dev.oom_wg.purejoy.mlang.MLang

@Composable
internal fun KokoroProfileContent(
    authState: KokoroAuthState,
    name: String,
    settings: MihomoSubscriptionSettings,
    availableOptions: KokoroSubscriptionOptions,
    error: String,
    onNameChange: (String) -> Unit,
    onSettingsChange: (MihomoSubscriptionSettings) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
) {
    var showOptions by remember { mutableStateOf(false) }
    val authenticated = authState as? KokoroAuthState.Authenticated
    val subscriptions = authenticated?.account?.subscriptions.orEmpty()
    val effectiveOptions = if (availableOptions.plans.isEmpty()) {
        KokoroSubscriptionOptions.fallback(authenticated?.account)
    } else {
        availableOptions
    }
    val normalizedSettings = effectiveOptions.normalize(settings)
    val selectedSubscription = subscriptions.firstOrNull { it.plan == normalizedSettings.plan }
        ?: subscriptions.firstOrNull()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(UiDp.dp16),
    ) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(UiDp.dp16),
                verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
            ) {
                when (authState) {
                    KokoroAuthState.Checking -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
                        ) {
                            Md3EIndeterminateCircularWavyProgressIndicator()
                            Text(MLang.ProfilesPage.Kokoro.Checking)
                        }
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
                        StatusText(
                            title = authState.account.displayName?.let {
                                MLang.ProfilesPage.Kokoro.LoggedInAs.format(it)
                            } ?: MLang.ProfilesPage.Kokoro.LoggedIn,
                            detail = if (subscriptions.isEmpty()) {
                                MLang.ProfilesPage.Kokoro.NoSubscription
                            } else {
                                MLang.ProfilesPage.Kokoro.SecureTokenSession
                            },
                        )
                        TextButton(onClick = onLogout) {
                            Text(MLang.ProfilesPage.Kokoro.Logout)
                        }
                    }
                }
            }
        }

        if (authenticated != null && subscriptions.isNotEmpty()) {
            selectedSubscription?.let { subscription ->
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(UiDp.dp16),
                        verticalArrangement = Arrangement.spacedBy(UiDp.dp6),
                    ) {
                        SubscriptionLine(MLang.ProfilesPage.Kokoro.Plan, subscription.plan)
                        SubscriptionLine(
                            MLang.ProfilesPage.Kokoro.Isp,
                            subscription.supportedIsps.joinToString(", "),
                        )
                        val traffic = trafficText(subscription.usedBytes, subscription.totalBytes)
                        if (traffic.isNotBlank()) {
                            SubscriptionLine(MLang.ProfilesPage.Kokoro.Traffic, traffic)
                        }
                        if (!subscription.expiresAt.isNullOrBlank()) {
                            SubscriptionLine(MLang.ProfilesPage.Kokoro.Expires, subscription.expiresAt)
                        }
                    }
                }
            }

            YumeMd3OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = MLang.ProfilesPage.Input.ProfileName,
                modifier = Modifier.fillMaxWidth(),
            )

            TextButton(
                onClick = { showOptions = !showOptions },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (showOptions) {
                        MLang.ProfilesPage.Kokoro.HideOptions
                    } else {
                        MLang.ProfilesPage.Kokoro.ShowOptions
                    },
                )
            }

            AnimatedVisibility(visible = showOptions) {
                MihomoSubscriptionSettingsContent(
                    settings = normalizedSettings,
                    availableOptions = effectiveOptions,
                    onSettingsChange = onSettingsChange,
                )
            }
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
internal fun MihomoSubscriptionSettingsContent(
    settings: MihomoSubscriptionSettings,
    availableOptions: KokoroSubscriptionOptions,
    onSettingsChange: (MihomoSubscriptionSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    val opacity = AppTheme.opacity
    val sizes = AppTheme.sizes
    val normalized = availableOptions.normalize(settings)
    val protocol = availableOptions.protocols.firstOrNull { it.value == normalized.protocol }
    val supportsDirect = protocol?.supportsDirect == true
    val plan = availableOptions.plans.firstOrNull { it.name == normalized.plan }
    val supportedPlanIsps = plan?.supportedIsps.orEmpty().filterNot { it == "all" }
    val selectableIsps = availableOptions.isps.filter {
        it.value.isBlank() || it.value in supportedPlanIsps
    }.ifEmpty { availableOptions.isps }
    var updateHoursText by remember(normalized.updateIntervalHours) {
        mutableStateOf(normalized.updateIntervalHours.toString())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
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
                    showDivider = true,
                )
                PreferenceSwitchItem(
                    title = MLang.ProfilesPage.Kokoro.RuleProviderAutoUpdate,
                    summary = MLang.ProfilesPage.Kokoro.RuleProviderAutoUpdateSummary,
                    checked = normalized.ruleProviderAutoUpdate,
                    onCheckedChange = {
                        onSettingsChange(normalized.copy(ruleProviderAutoUpdate = it))
                    },
                )
            }
        }

        Card {
            Column {
                PreferenceSwitchItem(
                    title = MLang.ProfilesPage.Kokoro.SubscriptionAutoUpdate,
                    summary = MLang.ProfilesPage.Kokoro.SubscriptionAutoUpdateSummary,
                    checked = normalized.subscriptionAutoUpdate,
                    onCheckedChange = {
                        onSettingsChange(normalized.copy(subscriptionAutoUpdate = it))
                    },
                )
                AnimatedVisibility(visible = normalized.subscriptionAutoUpdate) {
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = spacing.space16),
                            thickness = sizes.thinDividerThickness,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = opacity.outline),
                        )
                        YumeMd3OutlinedTextField(
                            value = updateHoursText,
                            onValueChange = { value ->
                                if (value.all(Char::isDigit) && value.length <= 3) {
                                    updateHoursText = value
                                    value.toIntOrNull()?.let { hours ->
                                        if (hours in availableOptions.minUpdateHours..availableOptions.maxUpdateHours) {
                                            onSettingsChange(normalized.copy(updateIntervalHours = hours))
                                        }
                                    }
                                }
                            },
                            label = MLang.ProfilesPage.Kokoro.UpdateHoursRange.format(
                                availableOptions.minUpdateHours,
                                availableOptions.maxUpdateHours,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.space16),
                            singleLine = true,
                        )
                    }
                }
            }
        }
    }
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

private fun trafficText(used: Long?, total: Long?): String = when {
    used != null && total != null && total > 0 -> "${ByteFormatter.format(used)} / ${ByteFormatter.format(total)}"
    used != null -> ByteFormatter.format(used)
    total != null && total > 0 -> ByteFormatter.format(total)
    else -> ""
}
