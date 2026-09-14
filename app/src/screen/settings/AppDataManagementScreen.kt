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

/*
 * This file is part of KokoroBox.
 */

package com.amamiyakokoro.box.screen.settings

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.common.util.formatBytes
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.data.controller.GeoXCacheEntry
import com.amamiyakokoro.box.data.store.LogStore
import com.amamiyakokoro.box.feature.editor.presentation.editor.CodeEditor
import com.amamiyakokoro.box.feature.editor.presentation.editor.rememberConfiguredCodeEditorState
import com.amamiyakokoro.box.feature.editor.presentation.language.LanguageScope
import com.amamiyakokoro.box.presentation.component.AppActionBottomSheet
import com.amamiyakokoro.box.presentation.component.AppBottomSheetCloseAction
import com.amamiyakokoro.box.presentation.component.AppConfirmDialog
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.component.PreferenceArrowItem
import com.amamiyakokoro.box.presentation.component.PreferenceListItem
import com.amamiyakokoro.box.presentation.component.ScreenLazyColumn
import com.amamiyakokoro.box.presentation.component.Title
import com.amamiyakokoro.box.presentation.component.TopBar
import com.amamiyakokoro.box.presentation.component.combinePaddingValues
import com.amamiyakokoro.box.presentation.component.rememberStandalonePageMainPadding
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date

@Composable
@Destination<RootGraph>
fun AppDataManagementScreen() {
    val viewModel = koinViewModel<AppDataManagementViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val showGeoHistorySheet = remember { mutableStateOf(false) }
    val showLogFilesSheet = remember { mutableStateOf(false) }
    val geoDeleteComplete = stringResource(LocaleR.string.app_data_management_geo_files_delete_complete)
    val logDeleteComplete = stringResource(LocaleR.string.app_data_management_logs_delete_complete)

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
        topBar = { TopBar(title = stringResource(LocaleR.string.app_data_management_title)) },
    ) { innerPadding ->
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, rememberStandalonePageMainPadding()),
        ) {
            item {
                Title(stringResource(LocaleR.string.app_data_management_section_geo_files))
                Card {
                    PreferenceArrowItem(
                        title = stringResource(LocaleR.string.app_data_management_geo_files_history_title),
                        summary = stringResource(LocaleR.string.app_data_management_geo_files_history_summary)
                            .format(uiState.geoHistory.size),
                        onClick = {
                            viewModel.refresh()
                            showGeoHistorySheet.value = true
                        },
                    )
                }
            }
            item {
                Title(stringResource(LocaleR.string.app_data_management_section_logs))
                Card {
                    PreferenceArrowItem(
                        title = stringResource(LocaleR.string.app_data_management_logs_management_title),
                        summary = stringResource(LocaleR.string.app_data_management_logs_management_summary)
                            .format(uiState.logFiles.size),
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
                    geoDeleteComplete.format(deleted),
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
                    logDeleteComplete.format(deleted),
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
    val unknownError = stringResource(LocaleR.string.util_error_unknown_error)
    val emptyLogContent = stringResource(LocaleR.string.app_data_management_logs_empty_log_content)
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
                        unknownError,
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    val content = remember(entries, fileName, emptyLogContent) {
        if (entries.isEmpty()) {
            emptyLogContent
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
                title = stringResource(LocaleR.string.app_data_management_logs_viewer_title).format(fileName),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = AppMd3Icons.Navigation.Back,
                            contentDescription = stringResource(LocaleR.string.component_navigation_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { exportLauncher.launch(fileName) }) {
                        Icon(
                            imageVector = AppMd3Icons.Action.Share,
                            contentDescription = stringResource(LocaleR.string.component_profile_card_export),
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
    val selectedPaths = selected.filterValues { it }.keys
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    AppConfirmDialog(
        show = showDeleteConfirmDialog,
        title = stringResource(LocaleR.string.app_data_management_geo_files_delete_confirm_title),
        message = stringResource(LocaleR.string.app_data_management_geo_files_delete_confirm_message)
            .format(selectedPaths.size),
        onDismissRequest = { showDeleteConfirmDialog = false },
        onConfirm = {
            val paths = selectedPaths.toSet()
            showDeleteConfirmDialog = false
            onDelete(paths)
        },
        confirmText = stringResource(LocaleR.string.component_button_delete),
        confirmDestructive = true,
    )

    AppActionBottomSheet(
        show = show,
        title = stringResource(LocaleR.string.app_data_management_geo_files_history_title),
        onDismissRequest = onDismiss,
        startAction = { AppBottomSheetCloseAction(onClick = onDismiss) },
        endAction = {
            IconButton(
                enabled = selectedPaths.isNotEmpty(),
                onClick = { showDeleteConfirmDialog = true },
            ) {
                Icon(
                    imageVector = AppMd3Icons.Action.Delete,
                    contentDescription = stringResource(LocaleR.string.component_button_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
        content = {
            if (entries.isEmpty()) {
                PreferenceListItem(
                    title = stringResource(LocaleR.string.app_data_management_geo_files_empty_history),
                    summary = stringResource(LocaleR.string.app_data_management_geo_files_empty_history_summary),
                )
            } else {
                entries.forEach { entry ->
                    PreferenceListItem(
                        title = entry.name,
                        summary = stringResource(LocaleR.string.app_data_management_geo_files_cache_item_summary).format(
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
    val selectedNames = selected.filterValues { it }.keys
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    AppConfirmDialog(
        show = showDeleteConfirmDialog,
        title = stringResource(LocaleR.string.app_data_management_logs_delete_confirm_title),
        message = stringResource(LocaleR.string.app_data_management_logs_delete_confirm_message)
            .format(selectedNames.size),
        onDismissRequest = { showDeleteConfirmDialog = false },
        onConfirm = {
            val names = selectedNames.toSet()
            showDeleteConfirmDialog = false
            onDelete(names)
        },
        confirmText = stringResource(LocaleR.string.component_button_delete),
        confirmDestructive = true,
    )

    AppActionBottomSheet(
        show = show,
        title = stringResource(LocaleR.string.app_data_management_logs_management_title),
        onDismissRequest = onDismiss,
        startAction = { AppBottomSheetCloseAction(onClick = onDismiss) },
        endAction = {
            IconButton(
                enabled = selectedNames.isNotEmpty(),
                onClick = { showDeleteConfirmDialog = true },
            ) {
                Icon(
                    imageVector = AppMd3Icons.Action.Delete,
                    contentDescription = stringResource(LocaleR.string.component_button_delete),
                    tint = MaterialTheme.colorScheme.error,
            )
            }
        },
        content = {
            if (entries.isEmpty()) {
                PreferenceListItem(
                    title = stringResource(LocaleR.string.app_data_management_logs_empty_logs),
                    summary = stringResource(LocaleR.string.app_data_management_logs_empty_logs_summary),
                )
            } else {
                entries.forEach { entry ->
                    PreferenceListItem(
                        title = if (entry.isRecording) {
                            stringResource(LocaleR.string.app_data_management_logs_recording_file_title).format(entry.name)
                        } else {
                            entry.name
                        },
                        summary = stringResource(LocaleR.string.app_data_management_logs_log_item_summary).format(
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
