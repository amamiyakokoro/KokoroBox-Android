package com.amamiyakokoro.box.screen.connection

import androidx.lifecycle.ViewModelStore
import com.amamiyakokoro.box.core.model.*
import com.amamiyakokoro.box.core.util.RuntimePowerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionDiagnosticsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val power = MutableStateFlow(RuntimePowerState(appVisible = true))
    private val running = MutableStateFlow(true)
    private val store = ViewModelStore()
    private val connection = ConnectionInfo(chains = listOf("recorded-node", "group"))

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun viewModel(diagnose: suspend (String, String) -> ConnectionDiagnostic) =
        ConnectionDiagnosticsViewModel(running, power, diagnose, dispatcher).also { store.put("diagnostics", it) }

    @Test fun stoppedOrHiddenRuntimeMakesNoNetworkRequests() = runTest(dispatcher) {
        var calls = 0
        val vm = viewModel { _, _ -> calls++; ConnectionDiagnostic() }
        running.value = false
        vm.run(connection)
        assertEquals(DiagnosticFailure.Stopped, vm.state.value.failure)
        running.value = true
        power.value = power.value.copy(appVisible = false)
        runCurrent()
        vm.run(connection)
        runCurrent()
        assertEquals(0, calls)
        assertFalse(vm.state.value.running)
    }

    @Test fun backgroundCancelsRequestButKeepsPowerObserverForRetry() = runTest(dispatcher) {
        var calls = 0
        val gate = CompletableDeferred<ConnectionDiagnostic>()
        val vm = viewModel { _, node -> assertEquals("recorded-node", node); calls++; gate.await() }
        vm.run(connection); vm.run(connection)
        runCurrent()
        assertEquals(1, calls)
        assertTrue(vm.state.value.running)
        power.value = power.value.copy(appVisible = false)
        runCurrent()
        assertFalse(vm.state.value.running)
        gate.complete(ConnectionDiagnostic())
        runCurrent()
        assertNull(vm.state.value.result)
        power.value = power.value.copy(appVisible = true)
        runCurrent()
        vm.run(connection)
        runCurrent()
        assertEquals(2, calls)
        assertNotNull(vm.state.value.result)
        // The observer must still cancel subsequent requests after returning to the foreground.
        val nextGate = CompletableDeferred<ConnectionDiagnostic>()
        val next = viewModel { _, _ -> nextGate.await() }
        next.run(connection); runCurrent()
        power.value = power.value.copy(screenOn = false); runCurrent()
        assertFalse(next.state.value.running)
    }

    @Test fun timeoutReleasesRunButton() = runTest(dispatcher) {
        val vm = viewModel { _, _ -> awaitCancellation() }
        vm.run(connection); runCurrent()
        advanceTimeBy(10_001); runCurrent()
        assertEquals(DiagnosticFailure.Timeout, vm.state.value.failure)
        assertFalse(vm.state.value.running)
    }

    @Test fun oldRootDaemonIsReportedAsUnsupported() = runTest(dispatcher) {
        val vm = viewModel { _, _ -> throw UnsupportedOperationException("older daemon") }
        vm.run(connection); runCurrent()
        assertEquals(DiagnosticFailure.Unsupported, vm.state.value.failure)
        assertFalse(vm.state.value.running)
    }
}
