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


package com.amamiyakokoro.box.screen.onboarding

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.R
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.AppActionBottomSheet
import com.amamiyakokoro.box.presentation.theme.UiDp

@SuppressLint("LocalContextResourcesRead")
@Composable
internal fun PrivacyPolicySheet(show: MutableState<Boolean>) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val loadFailed = stringResource(LocaleR.string.onboarding_sheet_load_failed)
    val policyText = remember(loadFailed) {
        runCatching {
            context.resources.openRawResource(R.raw.privacy_policy)
                .bufferedReader()
                .use { it.readText() }
        }.getOrElse { loadFailed }
    }

    AppActionBottomSheet(
        show = show.value,
        title = stringResource(LocaleR.string.onboarding_sheet_privacy_policy_title),
        onDismissRequest = { show.value = false },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = UiDp.dp450)
                .verticalScroll(scrollState),
        ) {
            Text(
                text = policyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
