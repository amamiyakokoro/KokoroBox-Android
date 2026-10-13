/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */



package com.amamiyakokoro.box.screen.log

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amamiyakokoro.box.core.util.RuntimePowerState
import com.amamiyakokoro.box.data.store.LogStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class LogViewModel(
    private val repository: LogStore,
    val powerState: StateFlow<RuntimePowerState>,
) : ViewModel() {

    private val _isRecording = MutableStateFlow(repository.isRecording())
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _tempLogEntries = MutableStateFlow<List<LogStore.LogEntry>>(emptyList())
    val tempLogEntries: StateFlow<List<LogStore.LogEntry>> = _tempLogEntries.asStateFlow()

    private var refreshJob: Job? = null
    private var stopJob: Job? = null

    fun startRecording() {
        stopJob?.cancel()
        repository.startRecording()
        _isRecording.value = true
        _tempLogEntries.value = emptyList()
    }

    fun stopRecording() {
        val fileName = repository.currentRecordingFileName
        repository.stopRecording()
        _isRecording.value = false
        stopRefreshing()
        stopJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val entries = repository.readStoppedRecordingEntries(fileName)
                currentCoroutineContext().ensureActive()
                if (!_isRecording.value) _tempLogEntries.value = entries
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _isRecording.value = repository.isRecording()
                Timber.w(error, "Reading stopped log failed")
            }
        }
    }

    fun refreshTempLogEntries() {
        if (!_isRecording.value || refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch(Dispatchers.IO) {
            val entries = repository.readTempLogEntries()
            currentCoroutineContext().ensureActive()
            if (_isRecording.value) _tempLogEntries.value = entries
        }
    }

    fun stopRefreshing() {
        refreshJob?.cancel()
        refreshJob = null
    }

    fun clearTempLog() {
        _tempLogEntries.value = emptyList()
    }

    suspend fun saveTempLog(targetUri: Uri): Boolean = withContext(Dispatchers.IO) {
        stopJob?.join()
        val entries = _tempLogEntries.value
        if (entries.isEmpty()) return@withContext false
        try {
            repository.writeLogEntries(targetUri, entries)
            true
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            false
        }
    }
}
