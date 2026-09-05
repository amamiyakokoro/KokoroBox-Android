package com.github.yumelira.yumebox.screen.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.yumelira.yumebox.data.integration.update.GitHubReleaseClient
import com.github.yumelira.yumebox.data.integration.update.ReleaseCheck
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppUpdateState(val checking: Boolean = false, val result: ReleaseCheck? = null)

class AppUpdateViewModel(private val client: GitHubReleaseClient) : ViewModel() {
    private val mutableState = MutableStateFlow(AppUpdateState())
    val state = mutableState.asStateFlow()

    fun check() {
        if (mutableState.value.checking) return
        mutableState.value = AppUpdateState(checking = true)
        viewModelScope.launch {
            try {
                mutableState.value = AppUpdateState(result = client.check())
            } catch (error: CancellationException) {
                throw error
            } finally {
                mutableState.value = mutableState.value.copy(checking = false)
            }
        }
    }

    fun dismiss() { mutableState.value = AppUpdateState() }
}
