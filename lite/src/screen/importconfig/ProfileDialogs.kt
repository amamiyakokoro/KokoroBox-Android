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

package com.github.yumelira.yumebox.screen.importconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.yumelira.yumebox.presentation.component.AppDialog
import com.github.yumelira.yumebox.presentation.component.AppDialogDefaults
import com.github.yumelira.yumebox.presentation.component.DialogButtonRow
import com.github.yumelira.yumebox.service.runtime.entity.Profile
import dev.oom_wg.purejoy.mlang.MLang
import java.io.File

@Composable
internal fun DeleteConfirmDialog(
    show: Boolean,
    profileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDismissFinished: (() -> Unit)? = null,
) {
    AppDialog(
        show = show,
        modifier = Modifier,
        title = MLang.ProfilesPage.DeleteDialog.Title,
        titleColor = AppDialogDefaults.titleColor(),
        summary = MLang.ProfilesPage.DeleteDialog.Message.format(profileName),
        summaryColor = AppDialogDefaults.summaryColor(),
        backgroundColor = AppDialogDefaults.backgroundColor(),
        enableWindowDim = true,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismissFinished,
        outsideMargin = AppDialogDefaults.outsideMargin,
        insideMargin = AppDialogDefaults.insideMargin,
        defaultWindowInsetsPadding = true,
    ) {
        DialogButtonRow(
            onCancel = onDismiss,
            onConfirm = onConfirm,
            cancelText = MLang.ProfilesPage.Button.Cancel,
            confirmText = MLang.ProfilesPage.DeleteDialog.Confirm,
        )
    }
}

@Composable
internal fun ShareOptionsDialog(
    show: Boolean,
    profile: Profile,
    onDismiss: () -> Unit,
    onDismissFinished: (() -> Unit)? = null,
    onShareFile: (Profile) -> Unit,
    onShareLink: (Profile) -> Unit,
) {
    AppDialog(
        show = show,
        modifier = Modifier,
        title = MLang.ProfilesPage.ShareDialog.Title,
        titleColor = AppDialogDefaults.titleColor(),
        backgroundColor = AppDialogDefaults.backgroundColor(),
        enableWindowDim = true,
        onDismissRequest = onDismiss,
        onDismissFinished = onDismissFinished,
        outsideMargin = AppDialogDefaults.outsideMargin,
        insideMargin = AppDialogDefaults.insideMargin,
        defaultWindowInsetsPadding = true,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (profile.type == Profile.Type.Url) {
                Button(
                    onClick = { onShareLink(profile) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(MLang.ProfilesPage.ShareDialog.ShareLink)
                }
            }
            OutlinedButton(
                onClick = { onShareFile(profile) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(MLang.ProfilesPage.ShareDialog.ShareFile)
            }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = MLang.ProfilesPage.Button.Cancel,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

internal fun importedConfigFile(filesDir: File, profile: Profile): File {
    return filesDir.resolve("imported").resolve(profile.uuid.toString()).resolve("config.yaml")
}
