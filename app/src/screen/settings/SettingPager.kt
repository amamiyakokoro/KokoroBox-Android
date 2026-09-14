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



package com.amamiyakokoro.box.screen.settings

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amamiyakokoro.box.BuildConfig
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.common.util.toast
import com.amamiyakokoro.box.presentation.component.*
import com.amamiyakokoro.box.presentation.component.Card
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.theme.AppTheme
import com.ramcosta.composedestinations.generated.destinations.AboutScreenDestination
import com.ramcosta.composedestinations.generated.destinations.AppDataManagementScreenDestination
import com.ramcosta.composedestinations.generated.destinations.AppSettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.KokoroSettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.LabScreenDestination
import com.ramcosta.composedestinations.generated.destinations.LogScreenDestination
import com.ramcosta.composedestinations.generated.destinations.MetaFeatureScreenDestination
import com.ramcosta.composedestinations.generated.destinations.NetworkSettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OverrideScreenDestination
import org.koin.androidx.compose.koinViewModel

@Composable
private fun CircularIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    iconSize: Float = 1f,
) {
    val spacing = AppTheme.spacing
    val radii = AppTheme.radii
    val componentSizes = AppTheme.sizes

    Box(
        modifier = modifier
            .padding(start = spacing.space4, end = spacing.space16)
            .requiredSize(componentSizes.settingsIconSlotSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .layout { measurable, _ ->
                    val containerSize = componentSizes.settingsIconContainerSize.roundToPx()
                    val parentSize = componentSizes.settingsIconSlotSize.roundToPx()
                    val offset = (containerSize - parentSize) / 2

                    val placeable = measurable.measure(
                        androidx.compose.ui.unit.Constraints.fixed(containerSize, containerSize)
                    )
                    layout(parentSize, parentSize) {
                        placeable.place(-offset, -offset)
                    }
                }
                .size(componentSizes.settingsIconContainerSize)
                .clip(RoundedCornerShape(radii.radius16))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(componentSizes.settingsIconGlyphSize)
                    .graphicsLayer(
                        scaleX = iconSize,
                        scaleY = iconSize,
                        transformOrigin = TransformOrigin.Center,
                    )
            )
        }
    }
}

@SuppressLint("LocalContextResourcesRead")
@Composable
fun SettingPager(
    mainInnerPadding: PaddingValues,
    lazyListState: LazyListState,
) {
    val appSettingsViewModel = koinViewModel<AppSettingsViewModel>()
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val appContext = context.applicationContext
    val backupInProgress by appSettingsViewModel.backupInProgress.collectAsStateWithLifecycle()
    val exportSuccess = stringResource(LocaleR.string.app_settings_backup_export_success)
    val exportFailedDetail = stringResource(LocaleR.string.app_settings_backup_export_failed_detail)
    val importSuccess = stringResource(LocaleR.string.app_settings_backup_import_success)
    val importFailedDetail = stringResource(LocaleR.string.app_settings_backup_import_failed_detail)
    val unknownError = stringResource(LocaleR.string.util_error_unknown_error)

    val versionInfo = BuildConfig.VERSION_NAME
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        appSettingsViewModel.exportUserSettingsBackup(appContext.contentResolver, uri) { result ->
            result.onSuccess {
                appContext.toast(exportSuccess)
            }.onFailure { throwable ->
                appContext.toast(exportFailedDetail.format(throwable.message ?: unknownError), copyable = true)
            }
        }
    }
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        appSettingsViewModel.importUserSettingsBackup(appContext.contentResolver, uri) { result ->
            result.onSuccess {
                appContext.toast(importSuccess)
            }.onFailure { throwable ->
                appContext.toast(importFailedDetail.format(throwable.message ?: unknownError), copyable = true)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(title = stringResource(LocaleR.string.settings_title))
        },
    ) { innerPadding ->
        ScreenLazyColumn(
            innerPadding = combinePaddingValues(innerPadding, mainInnerPadding),
            lazyListState = lazyListState,
        ) {

            item {
                Title(stringResource(LocaleR.string.settings_section_kokoro))
                Card {
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_kokoro_title),
                        summary = stringResource(LocaleR.string.settings_kokoro_summary),
                        imageVector = AppMd3Icons.Settings.Kokoro,
                        onClick = {
                            navigator.navigate(KokoroSettingsScreenDestination) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
            item {
                Title(stringResource(LocaleR.string.settings_section_ui_settings))
                Card {
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_ui_settings_app),
                        summary = stringResource(LocaleR.string.settings_ui_settings_app_summary),
                        imageVector = AppMd3Icons.Settings.App,
                        onClick = { navigator.navigate(AppSettingsScreenDestination) { launchSingleTop = true } },
                    )
                }
            }
            item {
                Title(stringResource(LocaleR.string.settings_section_network_settings))
                Card {
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_network_settings_network),
                        summary = stringResource(LocaleR.string.settings_network_settings_network_summary),
                        imageVector = AppMd3Icons.Settings.Network,
                        onClick = { navigator.navigate(NetworkSettingsScreenDestination) { launchSingleTop = true } },
                    )
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_network_settings_override),
                        summary = stringResource(LocaleR.string.settings_network_settings_override_summary),
                        imageVector = AppMd3Icons.Settings.Override,
                        onClick = { navigator.navigate(OverrideScreenDestination) { launchSingleTop = true } },
                    )
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_network_settings_meta_features),
                        summary = stringResource(LocaleR.string.settings_network_settings_meta_features_summary),
                        imageVector = AppMd3Icons.Settings.MetaFeatures,
                        onClick = {
                            navigator.navigate(MetaFeatureScreenDestination) {
                                launchSingleTop = true
                            }
                        },
                    )
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_network_settings_lab),
                        summary = stringResource(LocaleR.string.settings_network_settings_lab_summary),
                        imageVector = AppMd3Icons.Settings.Lab,
                        onClick = {
                            navigator.navigate(LabScreenDestination) { launchSingleTop = true }
                        },
                    )
                }
            }
            item {
                Title(stringResource(LocaleR.string.settings_section_data_settings))
                Card {
                    if (backupInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_data_settings_export_backup),
                        summary = stringResource(LocaleR.string.settings_data_settings_export_backup_summary),
                        imageVector = AppMd3Icons.Settings.ExportBackup,
                        enabled = !backupInProgress,
                        onClick = { exportBackupLauncher.launch("kokorobox-settings-backup.json") },
                    )
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_data_settings_import_backup),
                        summary = stringResource(LocaleR.string.settings_data_settings_import_backup_summary),
                        imageVector = AppMd3Icons.Settings.ImportBackup,
                        enabled = !backupInProgress,
                        onClick = { importBackupLauncher.launch("application/json") },
                    )
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_data_settings_app_data_management),
                        summary = stringResource(LocaleR.string.settings_data_settings_app_data_management_summary),
                        imageVector = AppMd3Icons.Settings.AppDataManagement,
                        onClick = { navigator.navigate(AppDataManagementScreenDestination) { launchSingleTop = true } },
                    )
                }
            }
            item {
                Title(stringResource(LocaleR.string.settings_section_more))

                Card {
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_more_logs),
                        summary = stringResource(LocaleR.string.settings_more_logs_summary),
                        imageVector = AppMd3Icons.Settings.Logs,
                        onClick = { navigator.navigate(LogScreenDestination) { launchSingleTop = true } },
                    )
                    SettingsEntryItem(
                        title = stringResource(LocaleR.string.settings_more_about),
                        summary = stringResource(LocaleR.string.settings_more_about_summary),
                        imageVector = AppMd3Icons.Settings.About,
                        onClick = { navigator.navigate(AboutScreenDestination) { launchSingleTop = true } },
                        endActions = {
                            VersionBadge(versionInfo)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsEntryItem(
    title: String,
    summary: String,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    endActions: @Composable (RowScope.() -> Unit)? = null,
) {
    PreferenceArrowItem(
        title = title,
        summary = summary,
        onClick = onClick,
        enabled = enabled,
        startAction = {
            CircularIcon(
                imageVector = imageVector,
                contentDescription = null,
            )
        },
        endActions = endActions,
    )
}

@Composable
private fun VersionBadge(
    versionInfo: String?
) {
    val spacing = AppTheme.spacing
    val componentSizes = AppTheme.sizes

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .height(componentSizes.versionBadgeHeight)
            .padding(end = spacing.space12)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = spacing.space12),
            horizontalArrangement = Arrangement.spacedBy(spacing.space8)
        ) {
            Text(
                text = versionInfo ?: "Unknown",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
