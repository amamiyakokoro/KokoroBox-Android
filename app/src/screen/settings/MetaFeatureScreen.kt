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



package com.github.yumelira.yumebox.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.github.yumelira.yumebox.common.util.toast
import com.github.yumelira.yumebox.core.model.GeoFileType
import com.github.yumelira.yumebox.core.model.GeoXItem
import com.github.yumelira.yumebox.core.model.geoXItems
import com.github.yumelira.yumebox.core.util.runtimeHomeDir
import com.github.yumelira.yumebox.presentation.component.*
import com.github.yumelira.yumebox.substore.util.SubStoreDownloadClient
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ConnectionScreenDestination
import com.ramcosta.composedestinations.generated.destinations.TrafficStatisticsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.oom_wg.purejoy.mlang.MLang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File

private const val MIN_GEO_FILE_SIZE_BYTES = 64 * 1024L

@Composable
@Destination<RootGraph>
fun MetaFeatureScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val downloadClient: SubStoreDownloadClient = koinInject()

    val showGeoXDownloadSheet = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(
                title = MLang.MetaFeature.Title,
            )
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
        ) {
            item {
                Title(MLang.MetaFeature.Section.ConnectionAndTraffic)
                Card {
                    PreferenceArrowItem(
                        title = MLang.Connection.Title,
                        summary = MLang.Connection.Summary,
                        onClick = {
                            navigator.navigate(ConnectionScreenDestination) {
                                launchSingleTop = true
                            }
                        },
                    )
                    PreferenceArrowItem(
                        title = MLang.TrafficStatistics.Title,
                        summary = MLang.TrafficStatistics.EntrySummary,
                        onClick = {
                            navigator.navigate(TrafficStatisticsScreenDestination) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
            item {
                Title(MLang.MetaFeature.Section.CustomRouting)
                Card {
                    PreferenceArrowItem(
                        title = MLang.MetaFeature.CustomRouting.Title,
                        summary = MLang.MetaFeature.CustomRouting.Summary,
                        onClick = {
                            navigator.navigate(com.ramcosta.composedestinations.generated.destinations.CustomRoutingRouteDestination) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
            item {
                Title(MLang.MetaFeature.Section.GeoXUpdate)
                Card {
                    PreferenceArrowItem(
                        title = MLang.MetaFeature.GeoX.OnlineUpdateTitle,
                        summary = MLang.MetaFeature.GeoX.OnlineUpdateSummary,
                        onClick = { showGeoXDownloadSheet.value = true },
                    )
                }
            }
        }

        GeoXDownloadSheet(
            show = showGeoXDownloadSheet,
            context = context,
            scope = scope,
            downloadClient = downloadClient,
        )
    }
}

@Composable
private fun GeoXDownloadSheet(
    show: MutableState<Boolean>,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    downloadClient: SubStoreDownloadClient,
) {
    val selectedItems = remember { mutableStateMapOf<GeoFileType, Boolean>() }

    AppActionBottomSheet(
        show = show.value,
        title = MLang.MetaFeature.Download.DialogTitle,
        onDismissRequest = { show.value = false },
        startAction = {
            AppBottomSheetCloseAction(
                onClick = { show.value = false },
            )
        },
        endAction = {
            AppBottomSheetConfirmAction(
                enabled = selectedItems.values.any { it },
                onClick = {
                    val itemsToDownload = geoXItems.filter { selectedItems[it.type] == true }
                    if (itemsToDownload.isEmpty()) {
                        context.toast(MLang.MetaFeature.Download.SelectFiles)
                        return@AppBottomSheetConfirmAction
                    }
                    show.value = false
                    downloadGeoXFiles(context, scope, downloadClient, itemsToDownload)
                },
            )
        },
        content = {
            Column {
                geoXItems.forEach { item ->
                    PreferenceListItem(
                        title = item.title,
                        endActions = {
                            Checkbox(
                                checked = selectedItems[item.type] ?: false,
                                onCheckedChange = { checked -> selectedItems[item.type] = checked }
                            )
                        },
                        onClick = {
                            selectedItems[item.type] = !(selectedItems[item.type] ?: false)
                        }
                    )
                }
            }
        })
}

private fun downloadGeoXFiles(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    downloadClient: SubStoreDownloadClient,
    items: List<GeoXItem>,
) {
    scope.launch {
        var successCount = 0
        withContext(Dispatchers.IO) {
            val runtimeHome = context.runtimeHomeDir
            runtimeHome.mkdirs()
            items.forEach { item ->
                val targetFile = File(runtimeHome, item.fileName)
                if (downloadClient.download(item.url, targetFile, validator = ::isGeoXDownloadUsable)) {
                    successCount++
                }
            }
        }
        context.toast(MLang.MetaFeature.Download.DownloadComplete.format(successCount, items.size))
    }
}

private fun isGeoXDownloadUsable(file: File): Boolean {
    if (!file.isFile || file.length() < MIN_GEO_FILE_SIZE_BYTES) return false
    return !looksLikeHttpErrorBody(file)
}

private fun looksLikeHttpErrorBody(file: File): Boolean {
    return runCatching {
        val buffer = ByteArray(512)
        val read = file.inputStream().buffered().use { input -> input.read(buffer) }
        if (read <= 0) return@runCatching true

        val head = String(buffer, 0, read, Charsets.UTF_8)
            .trimStart('\uFEFF', ' ', '\t', '\r', '\n')
            .lowercase()
        head.startsWith("<!doctype") ||
            head.startsWith("<html") ||
            head.startsWith("not found") ||
            head.startsWith("404") ||
            head.contains("<title>404") ||
            head.contains("rate limit")
    }.getOrDefault(false)
}
