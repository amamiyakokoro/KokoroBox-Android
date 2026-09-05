/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.settings

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.yumelira.yumebox.MainActivity
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.PreferenceArrowItem
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.icon.AppMd3Icons
import com.github.yumelira.yumebox.screen.profiles.KokoroAccountCard
import com.github.yumelira.yumebox.screen.profiles.KokoroAuthState
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.KokoroCustomRulesScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
@Destination<RootGraph>
fun KokoroSettingsScreen(navigator: DestinationsNavigator) {
    val viewModel = koinViewModel<KokoroSettingsViewModel>()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val authResult by MainActivity.kokoroAuthResult.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadAccount() }
    LaunchedEffect(authResult) {
        when (authResult) {
            true -> viewModel.refreshAccount()
            false -> viewModel.reportLoginFailure()
            null -> Unit
        }
        if (authResult != null) MainActivity.clearKokoroAuthResult()
    }

    fun beginLogin() {
        scope.launch {
            var loginUrl: String? = null
            try {
                loginUrl = viewModel.beginLogin()
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, loginUrl.toUri()).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                    },
                )
            } catch (error: Exception) {
                loginUrl?.let { viewModel.cancelLogin(it) }
                if (error is CancellationException) throw error
                viewModel.reportLoginFailure()
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = MLang.Settings.Kokoro.Title,
                actions = {
                    IconButton(onClick = viewModel::refreshAccount) {
                        Icon(AppMd3Icons.Action.Refresh, MLang.MetaFeature.CustomRules.Refresh)
                    }
                },
            )
        },
    ) { innerPadding ->
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, rememberStandalonePageMainPadding()),
        ) {
            item("account-title") { Title(MLang.ProfilesPage.Kokoro.Account) }
            item("account") {
                KokoroAccountCard(
                    authState = authState,
                    onLogin = ::beginLogin,
                    onLogout = viewModel::logout,
                    onRetry = viewModel::refreshAccount,
                )
            }
            item("rules-title") { Title(MLang.MetaFeature.CustomRules.Rules) }
            item("custom-rules") {
                Card(modifier = Modifier.fillMaxWidth()) {
                    PreferenceArrowItem(
                        title = MLang.Settings.Kokoro.CustomRules,
                        summary = MLang.Settings.Kokoro.CustomRulesSummary,
                        enabled = authState is KokoroAuthState.Authenticated,
                        onClick = {
                            navigator.navigate(KokoroCustomRulesScreenDestination) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
        }
    }
}
