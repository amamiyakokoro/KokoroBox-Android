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

package com.amamiyakokoro.box.screen.profiles


import com.amamiyakokoro.box.presentation.theme.UiDp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.amamiyakokoro.box.App
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.AppDialog
import com.amamiyakokoro.box.service.runtime.entity.Profile
import java.io.File

internal fun openProfileConfigPreview(
    targetFile: File,
    missingMessage: String,
    editable: Boolean,
    onReadFailed: (String) -> Unit,
    onPreviewPrepared: (String, ((String) -> Unit)?) -> Unit,
) {
    val configFile = if (targetFile.exists()) {
        targetFile
    } else {
        val runtimeFile = targetFile.parentFile?.resolve("runtime.yaml")
        if (runtimeFile?.exists() == true) runtimeFile else null
    }

    if (configFile == null) {
        onReadFailed(missingMessage)
        return
    }

    val configContent = runCatching { configFile.readText() }.getOrElse {
        onReadFailed(it.message ?: "Failed to read profile")
        return
    }

    val saveCallback = if (editable) {
        { updatedContent: String ->
            runCatching {
                configFile.writeText(updatedContent)
            }
                .getOrElse {
                    throw IllegalStateException(
                        it.message ?: App.instance.getString(
                            LocaleR.string.profiles_page_settings_dialog_save_failed,
                        ),
                        it,
                    )
                }
        }
    } else {
        null
    }

    onPreviewPrepared(configContent, saveCallback)
}

@Composable
internal fun ProfileEditOptionsDialog(
    show: Boolean,
    onOpenConfig: () -> Unit,
    onEditSettings: () -> Unit,
    onDismiss: () -> Unit,
    onDismissFinished: () -> Unit,
) {
    AppDialog(
        show = show,
        title = stringResource(LocaleR.string.profiles_page_settings_dialog_edit_profile),
        onDismissRequest = onDismiss,
        onDismissFinished = onDismissFinished,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(UiDp.dp12),
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenConfig,
            ) {
                Text(stringResource(LocaleR.string.profiles_page_settings_dialog_open_config))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onEditSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(LocaleR.string.profiles_page_settings_dialog_edit_settings),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

internal fun importedProfileDir(profile: Profile): File {
    return App.instance.filesDir.resolve("imported").resolve(profile.uuid.toString())
}

internal fun importedConfigFile(profile: Profile): File {
    return importedProfileDir(profile).resolve("config.yaml")
}
