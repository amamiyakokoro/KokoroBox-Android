/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.amamiyakokoro.box.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amamiyakokoro.box.data.integration.speedtest.CloudflareSpeedTestClient
import com.amamiyakokoro.box.data.integration.speedtest.CloudflareSpeedTestResult
import com.amamiyakokoro.box.data.integration.speedtest.CloudflareSpeedTestStage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CloudflareSpeedTestUiState(
    val stage: CloudflareSpeedTestStage? = null,
    val result: CloudflareSpeedTestResult? = null,
    val failed: Boolean = false,
) {
    val running: Boolean get() = stage != null
}

class CloudflareSpeedTestViewModel(
    private val speedTestClient: CloudflareSpeedTestClient,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CloudflareSpeedTestUiState())
    val state = mutableState.asStateFlow()

    private var runningJob: Job? = null

    fun start() {
        if (runningJob?.isActive == true) return
        mutableState.value = CloudflareSpeedTestUiState(stage = CloudflareSpeedTestStage.Preparing)
        runningJob = viewModelScope.launch {
            try {
                val result = speedTestClient.run { stage ->
                    mutableState.value = mutableState.value.copy(stage = stage, failed = false)
                }
                mutableState.value = CloudflareSpeedTestUiState(result = result)
            } catch (error: CancellationException) {
                mutableState.value = CloudflareSpeedTestUiState()
                throw error
            } catch (_: Exception) {
                mutableState.value = CloudflareSpeedTestUiState(failed = true)
            }
        }
    }

    fun cancel() {
        runningJob?.cancel()
    }
}
