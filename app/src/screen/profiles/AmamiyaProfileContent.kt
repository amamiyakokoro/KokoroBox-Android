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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3DropdownPreference
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3OutlinedTextField
import com.github.yumelira.yumebox.presentation.theme.UiDp
import dev.oom_wg.purejoy.mlang.MLang

@Composable
internal fun AmamiyaProfileContent(
    authState: AmamiyaAuthState,
    name: String,
    selectedSubscriptionIndex: Int,
    options: AmamiyaConfigOptions,
    updateModeIndex: Int,
    customUpdateHours: String,
    error: String,
    onNameChange: (String) -> Unit,
    onSubscriptionSelected: (Int) -> Unit,
    onOptionsChange: (AmamiyaConfigOptions) -> Unit,
    onUpdateModeChange: (Int) -> Unit,
    onCustomUpdateHoursChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onRetry: () -> Unit,
) {
    var showOptions by remember { mutableStateOf(false) }
    val authenticated = authState as? AmamiyaAuthState.Authenticated
    val subscriptions = authenticated?.account?.subscriptions.orEmpty()
    val selectedSubscription = subscriptions.getOrNull(
        selectedSubscriptionIndex.coerceIn(0, (subscriptions.size - 1).coerceAtLeast(0)),
    )

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
                    AmamiyaAuthState.Checking -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(UiDp.dp12),
                        ) {
                            Md3EIndeterminateCircularWavyProgressIndicator()
                            Text(MLang.ProfilesPage.Amamiya.Checking)
                        }
                    }

                    AmamiyaAuthState.LoggedOut -> {
                        StatusText(
                            title = MLang.ProfilesPage.Amamiya.LoggedOut,
                            detail = MLang.ProfilesPage.Amamiya.LoginHint,
                        )
                        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                            Text(MLang.ProfilesPage.Amamiya.Login)
                        }
                    }

                    is AmamiyaAuthState.Error -> {
                        StatusText(
                            title = MLang.ProfilesPage.Amamiya.CheckFailed,
                            detail = authState.message,
                            error = true,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(UiDp.dp8),
                        ) {
                            OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f)) {
                                Text(MLang.ProfilesPage.Amamiya.Retry)
                            }
                            Button(onClick = onLogin, modifier = Modifier.weight(1f)) {
                                Text(MLang.ProfilesPage.Amamiya.Login)
                            }
                        }
                    }

                    is AmamiyaAuthState.Authenticated -> {
                        StatusText(
                            title = authState.account.displayName?.let {
                                MLang.ProfilesPage.Amamiya.LoggedInAs.format(it)
                            } ?: MLang.ProfilesPage.Amamiya.LoggedIn,
                            detail = if (subscriptions.isEmpty()) {
                                MLang.ProfilesPage.Amamiya.NoSubscription
                            } else {
                                MLang.ProfilesPage.Amamiya.SecureTokenSession
                            },
                        )
                        TextButton(onClick = onLogout) {
                            Text(MLang.ProfilesPage.Amamiya.Logout)
                        }
                    }
                }
            }
        }

        if (authenticated != null && subscriptions.isNotEmpty()) {
            if (subscriptions.size > 1) {
                Card {
                    YumeMd3DropdownPreference(
                        title = MLang.ProfilesPage.Amamiya.Subscription,
                        items = subscriptions.mapIndexed { index, subscription ->
                            subscription.plan?.takeIf(String::isNotBlank)
                                ?: MLang.ProfilesPage.Amamiya.SubscriptionNumber.format(index + 1)
                        },
                        selectedIndex = selectedSubscriptionIndex,
                        onSelectedIndexChange = onSubscriptionSelected,
                        showDivider = false,
                    )
                }
            }

            selectedSubscription?.let { subscription ->
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(UiDp.dp16),
                        verticalArrangement = Arrangement.spacedBy(UiDp.dp6),
                    ) {
                        SubscriptionLine(MLang.ProfilesPage.Amamiya.Plan, subscription.plan.orEmpty())
                        SubscriptionLine(
                            MLang.ProfilesPage.Amamiya.Isp,
                            subscription.supportedIsps.joinToString(", "),
                        )
                        val traffic = trafficText(subscription.usedBytes, subscription.totalBytes)
                        if (traffic.isNotBlank()) {
                            SubscriptionLine(MLang.ProfilesPage.Amamiya.Traffic, traffic)
                        }
                        if (!subscription.expiresAt.isNullOrBlank()) {
                            SubscriptionLine(MLang.ProfilesPage.Amamiya.Expires, subscription.expiresAt)
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
                        MLang.ProfilesPage.Amamiya.HideOptions
                    } else {
                        MLang.ProfilesPage.Amamiya.ShowOptions
                    },
                )
            }

            AnimatedVisibility(visible = showOptions) {
                Column(verticalArrangement = Arrangement.spacedBy(UiDp.dp12)) {
                    Card {
                        YumeMd3DropdownPreference(
                            title = MLang.ProfilesPage.Amamiya.Protocol,
                            items = listOf("VMess", "AnyTLS", "Hysteria 2"),
                            selectedIndex = listOf("vmess", "anytls", "hysteria2")
                                .indexOf(options.protocol).coerceAtLeast(0),
                            onSelectedIndexChange = { index ->
                                val protocol = listOf("vmess", "anytls", "hysteria2")[index]
                                onOptionsChange(
                                    options.copy(
                                        protocol = protocol,
                                        mode = if (protocol == "vmess") "relay" else options.mode,
                                    ),
                                )
                            },
                            showDivider = true,
                        )
                        YumeMd3DropdownPreference(
                            title = MLang.ProfilesPage.Amamiya.Isp,
                            items = listOf(
                                MLang.ProfilesPage.Amamiya.IspAuto,
                                MLang.ProfilesPage.Amamiya.IspCt,
                                MLang.ProfilesPage.Amamiya.IspCu,
                                MLang.ProfilesPage.Amamiya.IspCm,
                                MLang.ProfilesPage.Amamiya.IspOther,
                            ),
                            selectedIndex = listOf(null, "ct", "cu", "cm", "other").indexOf(options.isp)
                                .coerceAtLeast(0),
                            onSelectedIndexChange = { index ->
                                onOptionsChange(options.copy(isp = listOf(null, "ct", "cu", "cm", "other")[index]))
                            },
                            showDivider = true,
                        )
                        YumeMd3DropdownPreference(
                            title = MLang.ProfilesPage.Amamiya.Mode,
                            items = listOf(
                                MLang.ProfilesPage.Amamiya.Relay,
                                MLang.ProfilesPage.Amamiya.Direct,
                            ),
                            selectedIndex = if (options.mode == "direct") 1 else 0,
                            onSelectedIndexChange = { index ->
                                onOptionsChange(options.copy(mode = if (index == 1) "direct" else "relay"))
                            },
                            enabled = options.protocol != "vmess",
                            summary = if (options.protocol == "vmess") {
                                MLang.ProfilesPage.Amamiya.VmessRelayOnly
                            } else {
                                null
                            },
                            showDivider = false,
                        )
                    }

                    Card {
                        YumeMd3DropdownPreference(
                            title = MLang.ProfilesPage.Amamiya.RuleSource,
                            items = listOf(
                                MLang.ProfilesPage.Amamiya.Origin,
                                MLang.ProfilesPage.Amamiya.Mirror,
                            ),
                            selectedIndex = if (options.rule == "mirror") 1 else 0,
                            onSelectedIndexChange = { index ->
                                onOptionsChange(options.copy(rule = if (index == 1) "mirror" else "origin"))
                            },
                            showDivider = true,
                        )
                        YumeMd3DropdownPreference(
                            title = MLang.ProfilesPage.Amamiya.Fallback,
                            items = listOf(
                                MLang.ProfilesPage.Amamiya.KeepFallback,
                                MLang.ProfilesPage.Amamiya.Direct,
                            ),
                            selectedIndex = if (options.match == "direct") 1 else 0,
                            onSelectedIndexChange = { index ->
                                onOptionsChange(options.copy(match = if (index == 1) "direct" else "none"))
                            },
                            showDivider = true,
                        )
                        YumeMd3DropdownPreference(
                            title = MLang.ProfilesPage.Amamiya.RuleUpdate,
                            items = listOf(
                                MLang.ProfilesPage.Amamiya.Enabled,
                                MLang.ProfilesPage.Amamiya.Disabled,
                            ),
                            selectedIndex = if (options.ruleUpdate == "disable") 1 else 0,
                            onSelectedIndexChange = { index ->
                                onOptionsChange(options.copy(ruleUpdate = if (index == 1) "disable" else "enable"))
                            },
                            showDivider = false,
                        )
                    }

                    Card {
                        YumeMd3DropdownPreference(
                            title = MLang.ProfilesPage.Amamiya.ProfileUpdate,
                            items = listOf(
                                MLang.ProfilesPage.Amamiya.UpdateOn,
                                MLang.ProfilesPage.Amamiya.UpdateOff,
                                MLang.ProfilesPage.Amamiya.UpdateCustom,
                            ),
                            selectedIndex = updateModeIndex.coerceIn(0, 2),
                            onSelectedIndexChange = onUpdateModeChange,
                            showDivider = updateModeIndex == 2,
                        )
                        if (updateModeIndex == 2) {
                            YumeMd3OutlinedTextField(
                                value = customUpdateHours,
                                onValueChange = { value ->
                                    if (value.all(Char::isDigit) && value.length <= 6) {
                                        onCustomUpdateHoursChange(value)
                                    }
                                },
                                label = MLang.ProfilesPage.Amamiya.UpdateHours,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(UiDp.dp16),
                            )
                        }
                    }
                }
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
