/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
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
 * Copyright (c)  YumeLira 2025 - Present
 *
 */

/*
 * This file is part of YumeBox.
 */

package com.github.yumelira.yumebox.screen.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.yumelira.yumebox.data.controller.GeoXCacheEntry
import com.github.yumelira.yumebox.data.controller.GeoXDataController
import com.github.yumelira.yumebox.data.store.LogStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppDataManagementUiState(
    val geoHistory: List<GeoXCacheEntry> = emptyList(),
    val logFiles: List<LogStore.LogFileInfo> = emptyList(),
    val selectedLogFileName: String? = null,
    val selectedLogEntries: List<LogStore.LogEntry> = emptyList(),
)

class AppDataManagementViewModel(
    private val geoXDataController: GeoXDataController,
    private val logStore: LogStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppDataManagementUiState())
    val uiState: StateFlow<AppDataManagementUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val history = withContext(Dispatchers.IO) { geoXDataController.listHistoryCache() }
            val logs = withContext(Dispatchers.IO) { logStore.listLogFiles() }
            _uiState.value = _uiState.value.copy(
                geoHistory = history,
                logFiles = logs,
            )
        }
    }

    fun deleteGeoHistory(paths: Collection<String>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val deleted = withContext(Dispatchers.IO) { geoXDataController.deleteHistoryCache(paths) }
            refresh()
            onComplete(deleted)
        }
    }

    fun openLogFile(fileName: String) {
        viewModelScope.launch {
            val entries = withContext(Dispatchers.IO) { logStore.readLogEntries(fileName, maxEntries = 500) }
            _uiState.value = _uiState.value.copy(
                selectedLogFileName = fileName,
                selectedLogEntries = entries,
            )
        }
    }

    fun closeLogFile() {
        _uiState.value = _uiState.value.copy(
            selectedLogFileName = null,
            selectedLogEntries = emptyList(),
        )
    }

    suspend fun exportLogFile(fileName: String, targetUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            logStore.exportLogFile(fileName, targetUri)
        }
    }

    fun deleteLogFiles(fileNames: Collection<String>, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val deleted = withContext(Dispatchers.IO) {
                fileNames.count { logStore.deleteLogFile(it) }
            }
            if (_uiState.value.selectedLogFileName in fileNames) {
                closeLogFile()
            }
            refresh()
            onComplete(deleted)
        }
    }
}
