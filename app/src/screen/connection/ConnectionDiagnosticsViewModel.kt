/* This file is part of KokoroBox. Licensed under the GNU AGPL, version 3 or later. */
package com.amamiyakokoro.box.screen.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amamiyakokoro.box.core.model.ConnectionDiagnostic
import com.amamiyakokoro.box.core.model.ConnectionInfo
import com.amamiyakokoro.box.core.model.diagnosticHost
import com.amamiyakokoro.box.core.model.diagnosticProxyName
import com.amamiyakokoro.box.core.util.RuntimePowerState
import com.amamiyakokoro.box.remote.ServiceClient
import com.amamiyakokoro.box.runtime.client.ProxyFacade
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class DiagnosticFailure { Stopped, Failed, Unsupported, Timeout }

internal data class ConnectionDiagnosticsState(
    val running: Boolean = false,
    val result: ConnectionDiagnostic? = null,
    val failure: DiagnosticFailure? = null,
)

class ConnectionDiagnosticsViewModel internal constructor(
    private val isRunning: StateFlow<Boolean>,
    private val powerState: StateFlow<RuntimePowerState>,
    private val diagnose: suspend (String, String) -> ConnectionDiagnostic,
    private val diagnosticDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    constructor(proxyFacade: ProxyFacade) : this(proxyFacade.isRunning, proxyFacade.powerState,
        { host, node -> ServiceClient.clash().diagnoseConnection(host, node) })
    private val mutableState = MutableStateFlow(ConnectionDiagnosticsState())
    internal val state = mutableState.asStateFlow()
    private var job: Job? = null

    init {
        viewModelScope.launch {
            powerState.collect { if (!it.uiVisible) this@ConnectionDiagnosticsViewModel.cancel() }
        }
    }

    fun run(connection: ConnectionInfo) {
        if (job?.isActive == true || !powerState.value.uiVisible) return
        if (!isRunning.value) {
            mutableState.value = ConnectionDiagnosticsState(failure = DiagnosticFailure.Stopped)
            return
        }
        mutableState.value = ConnectionDiagnosticsState(running = true)
        job = viewModelScope.launch {
            try {
                val result = withTimeout(10_000L) {
                    withContext(diagnosticDispatcher) {
                        diagnose(connection.diagnosticHost(), connection.diagnosticProxyName())
                    }
                }
                ensureActive()
                mutableState.value = ConnectionDiagnosticsState(result = result)
            } catch (_: TimeoutCancellationException) {
                mutableState.value = ConnectionDiagnosticsState(failure = DiagnosticFailure.Timeout)
            } catch (error: CancellationException) {
                throw error
            } catch (_: UnsupportedOperationException) {
                mutableState.value = ConnectionDiagnosticsState(failure = DiagnosticFailure.Unsupported)
            } catch (_: Exception) {
                mutableState.value = ConnectionDiagnosticsState(failure = DiagnosticFailure.Failed)
            }
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
        if (mutableState.value.running) mutableState.value = ConnectionDiagnosticsState()
    }
}
