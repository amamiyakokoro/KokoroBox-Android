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



package com.amamiyakokoro.box.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.amamiyakokoro.box.presentation.theme.AppTheme
import com.amamiyakokoro.box.presentation.theme.UiDp
import com.amamiyakokoro.box.presentation.theme.horizontalPadding
import com.amamiyakokoro.box.presentation.theme.topPadding

/**
 * App-level wrapper around Miuix `Title`.
 * Keeps KokoroBox default spacing while avoiding direct Miuix usage at call sites.
 */
@Composable
fun Title(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .horizontalPadding(left = AppTheme.spacing.screenHorizontal)
            .topPadding()
            .padding(bottom = UiDp.dp8),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}
