/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroRepository
import com.github.yumelira.yumebox.screen.profiles.KokoroAuthState
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class KokoroSettingsViewModel(
    private val repository: KokoroRepository,
) : ViewModel() {
    private val _authState = MutableStateFlow<KokoroAuthState>(KokoroAuthState.Checking)
    val authState = _authState.asStateFlow()

    fun loadAccount() = loadAccount(forceRefresh = false)

    fun refreshAccount() = loadAccount(forceRefresh = true)

    private fun loadAccount(forceRefresh: Boolean) {
        viewModelScope.launch {
            _authState.value = KokoroAuthState.Checking
            _authState.value = try {
                repository.getAccount(forceRefresh)?.let(KokoroAuthState::Authenticated)
                    ?: KokoroAuthState.LoggedOut
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                KokoroAuthState.Error(MLang.ProfilesPage.Kokoro.CheckFailedDetail)
            }
        }
    }

    suspend fun beginLogin(): String = repository.beginLogin()

    suspend fun cancelLogin(loginUrl: String) = repository.cancelLogin(loginUrl)

    fun reportLoginFailure() {
        _authState.value = KokoroAuthState.Error(MLang.ProfilesPage.Kokoro.LoginFailed)
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = KokoroAuthState.Checking
            try {
                repository.revoke()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
            } finally {
                _authState.value = KokoroAuthState.LoggedOut
            }
        }
    }
}
