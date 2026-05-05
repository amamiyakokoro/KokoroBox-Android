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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.github.yumelira.yumebox.common.util.formatBytes
import com.github.yumelira.yumebox.data.controller.GeoXCacheEntry
import com.github.yumelira.yumebox.data.store.LogStore
import com.github.yumelira.yumebox.feature.editor.presentation.editor.CodeEditor
import com.github.yumelira.yumebox.feature.editor.presentation.editor.rememberConfiguredCodeEditorState
import com.github.yumelira.yumebox.feature.editor.presentation.language.LanguageScope
import com.github.yumelira.yumebox.presentation.component.AppBottomSheetCloseAction
import com.github.yumelira.yumebox.presentation.component.AppActionBottomSheet
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.PreferenceArrowItem
import com.github.yumelira.yumebox.presentation.component.PreferenceListItem
import com.github.yumelira.yumebox.presentation.component.ScreenLazyColumn
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.TopBar
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.github.yumelira.yumebox.presentation.icon.Yume
import com.github.yumelira.yumebox.presentation.icon.yume.ArrowLeft
import com.github.yumelira.yumebox.presentation.icon.yume.Delete
import com.github.yumelira.yumebox.presentation.icon.yume.Share
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date

@Composable
@Destination<RootGraph>
fun AppDataManagementScreen() {
    val viewModel = koinViewModel<AppDataManagementViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val showGeoHistorySheet = remember { androidx.compose.runtime.mutableStateOf(false) }
    val showLogFilesSheet = remember { androidx.compose.runtime.mutableStateOf(false) }

    if (uiState.selectedLogFileName != null) {
        AppDataLogViewerScreen(
            fileName = uiState.selectedLogFileName.orEmpty(),
            entries = uiState.selectedLogEntries,
            onBack = viewModel::closeLogFile,
            onExport = { targetUri -> viewModel.exportLogFile(uiState.selectedLogFileName.orEmpty(), targetUri) },
        )
        return
    }

    Scaffold(
        topBar = { TopBar(title = MLang.AppDataManagement.Title) },
    ) { innerPadding ->
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, rememberStandalonePageMainPadding()),
        ) {
            item {
                Title(MLang.AppDataManagement.Section.GeoFiles)
                Card {
                    PreferenceArrowItem(
                        title = MLang.AppDataManagement.GeoFiles.HistoryTitle,
                        summary = MLang.AppDataManagement.GeoFiles.HistorySummary.format(uiState.geoHistory.size),
                        onClick = {
                            viewModel.refresh()
                            showGeoHistorySheet.value = true
                        },
                    )
                }
            }
            item {
                Title(MLang.AppDataManagement.Section.Logs)
                Card {
                    PreferenceArrowItem(
                        title = MLang.AppDataManagement.Logs.ManagementTitle,
                        summary = MLang.AppDataManagement.Logs.ManagementSummary.format(uiState.logFiles.size),
                        onClick = {
                            viewModel.refresh()
                            showLogFilesSheet.value = true
                        },
                    )
                }
            }
        }
    }

    GeoHistorySheet(
        show = showGeoHistorySheet.value,
        entries = uiState.geoHistory,
        onDismiss = { showGeoHistorySheet.value = false },
        onDelete = { selectedPaths ->
            viewModel.deleteGeoHistory(selectedPaths) { deleted ->
                showGeoHistorySheet.value = false
                android.widget.Toast.makeText(
                    context,
                    MLang.AppDataManagement.GeoFiles.DeleteComplete.format(deleted),
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
            }
        },
    )

    LogFilesSheet(
        show = showLogFilesSheet.value,
        entries = uiState.logFiles,
        onDismiss = { showLogFilesSheet.value = false },
        onOpen = { fileName ->
            showLogFilesSheet.value = false
            viewModel.openLogFile(fileName)
        },
        onDelete = { selectedNames ->
            viewModel.deleteLogFiles(selectedNames) { deleted ->
                android.widget.Toast.makeText(
                    context,
                    MLang.AppDataManagement.Logs.DeleteComplete.format(deleted),
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
            }
        },
    )
}

@Composable
private fun AppDataLogViewerScreen(
    fileName: String,
    entries: List<LogStore.LogEntry>,
    onBack: () -> Unit,
    onExport: suspend (Uri) -> Boolean,
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            val success = onExport(uri)
            if (!success) {
                launch(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        MLang.Util.Error.UnknownError,
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    val content = remember(entries, fileName) {
        if (entries.isEmpty()) {
            MLang.AppDataManagement.Logs.EmptyLogContent
        } else {
            entries.joinToString(separator = "\n") { entry ->
                "${entry.time} [${entry.level.name}] ${entry.message}"
            }
        }
    }
    val editorState = rememberConfiguredCodeEditorState(
        initialContent = content,
        language = LanguageScope.Text,
        readOnly = true,
    )

    Scaffold(
        topBar = {
            TopBar(
                title = MLang.AppDataManagement.Logs.ViewerTitle.format(fileName),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Yume.ArrowLeft,
                            contentDescription = MLang.Component.Navigation.Back,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { exportLauncher.launch(fileName) }) {
                        Icon(
                            imageVector = Yume.Share,
                            contentDescription = "Export",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            CodeEditor(
                state = editorState,
                modifier = Modifier.fillMaxSize(),
                onTextChange = {},
            )
        }
    }
}

@Composable
private fun GeoHistorySheet(
    show: Boolean,
    entries: List<GeoXCacheEntry>,
    onDismiss: () -> Unit,
    onDelete: (Set<String>) -> Unit,
) {
    val selected = remember(entries) { mutableStateMapOf<String, Boolean>() }
    AppActionBottomSheet(
        show = show,
        title = MLang.AppDataManagement.GeoFiles.HistoryTitle,
        onDismissRequest = onDismiss,
        startAction = { AppBottomSheetCloseAction(onClick = onDismiss) },
        endAction = {
            IconButton(
                enabled = selected.values.any { it },
                onClick = { onDelete(selected.filterValues { it }.keys) },
            ) {
                Icon(
                    imageVector = Yume.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
        content = {
            if (entries.isEmpty()) {
                PreferenceListItem(
                    title = MLang.AppDataManagement.GeoFiles.EmptyHistory,
                    summary = MLang.AppDataManagement.GeoFiles.EmptyHistorySummary,
                )
            } else {
                entries.forEach { entry ->
                    PreferenceListItem(
                        title = entry.name,
                        summary = MLang.AppDataManagement.GeoFiles.CacheItemSummary.format(
                            formatBytes(entry.sizeBytes),
                            formatDateTime(entry.lastModified),
                        ),
                        endActions = checkboxAction(
                            checked = selected[entry.path] == true,
                            onCheckedChange = { checked -> selected[entry.path] = checked },
                        ),
                        onClick = { selected[entry.path] = !(selected[entry.path] ?: false) },
                    )
                }
            }
        },
    )
}

@Composable
private fun LogFilesSheet(
    show: Boolean,
    entries: List<LogStore.LogFileInfo>,
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (Set<String>) -> Unit,
) {
    val selected = remember(entries) { mutableStateMapOf<String, Boolean>() }
    AppActionBottomSheet(
        show = show,
        title = MLang.AppDataManagement.Logs.ManagementTitle,
        onDismissRequest = onDismiss,
        startAction = { AppBottomSheetCloseAction(onClick = onDismiss) },
        endAction = {
            IconButton(
                enabled = selected.values.any { it },
                onClick = { onDelete(selected.filterValues { it }.keys) },
            ) {
                Icon(
                    imageVector = Yume.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
            )
            }
        },
        content = {
            if (entries.isEmpty()) {
                PreferenceListItem(
                    title = MLang.AppDataManagement.Logs.EmptyLogs,
                    summary = MLang.AppDataManagement.Logs.EmptyLogsSummary,
                )
            } else {
                entries.forEach { entry ->
                    PreferenceListItem(
                        title = if (entry.isRecording) {
                            MLang.AppDataManagement.Logs.RecordingFileTitle.format(entry.name)
                        } else {
                            entry.name
                        },
                        summary = MLang.AppDataManagement.Logs.LogItemSummary.format(
                            formatBytes(entry.size),
                            formatDateTime(entry.createdAt),
                        ),
                        endActions = checkboxAction(
                            checked = selected[entry.name] == true,
                            onCheckedChange = { checked -> selected[entry.name] = checked },
                        ),
                        onClick = { onOpen(entry.name) },
                    )
                }
            }
        },
    )
}

private fun formatDateTime(timestamp: Long): String {
    return DateFormat.getDateTimeInstance().format(Date(timestamp))
}

private fun checkboxAction(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
): @Composable RowScope.() -> Unit = {
    Checkbox(checked = checked, onCheckedChange = onCheckedChange)
}
