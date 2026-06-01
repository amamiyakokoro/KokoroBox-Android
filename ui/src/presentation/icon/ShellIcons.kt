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

package com.github.yumelira.yumebox.presentation.icon

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Backward-compatible shell icon mapping.
 *
 * Prefer [AppMd3Icons] in new code. This object is kept to avoid touching all
 * existing shell call sites at once while the MD3 icon catalog is centralized in :ui.
 */
object ShellIcons {
    val NavigateForward: ImageVector = AppMd3Icons.Shell.NavigateForward
    val AddProfile: ImageVector = AppMd3Icons.Shell.AddProfile
    val UpdateProfiles: ImageVector = AppMd3Icons.Shell.UpdateProfiles
    val OpenProxy: ImageVector = AppMd3Icons.Shell.OpenProxy
    val OpenProfiles: ImageVector = AppMd3Icons.Shell.OpenProfiles
    val OpenSettings: ImageVector = AppMd3Icons.Shell.OpenSettings
    val StartProxy: ImageVector = AppMd3Icons.Shell.StartProxy
    val StopProxy: ImageVector = AppMd3Icons.Shell.StopProxy
}
