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

package com.github.yumelira.yumebox.screen.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.yumelira.yumebox.presentation.component.Card
import com.github.yumelira.yumebox.presentation.component.LinkItem
import com.github.yumelira.yumebox.presentation.component.Title
import com.github.yumelira.yumebox.presentation.component.combinePaddingValues
import com.github.yumelira.yumebox.presentation.component.md3.YumeMd3PreferenceItem
import com.github.yumelira.yumebox.presentation.component.rememberStandalonePageMainPadding
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun AboutScreen(@Suppress("UNUSED_PARAMETER") navigator: DestinationsNavigator) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "关于 Lite",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = combinePaddingValues(innerPadding, mainLikePadding),
        ) {
            item {
                Title("版本")
                Card {
                    YumeMd3PreferenceItem(
                        title = "YumeBox MD3 Lite",
                        summary = "v0.5.3",
                        showDivider = false,
                    )
                }

                Title("简介")
                Card {
                    YumeMd3PreferenceItem(
                        title = "YumeBox MD3 Lite",
                        summary = "A Material Design 3 / Material You fork of YumeBox, an open-source Android client based on Mihomo",
                        showDivider = false,
                    )
                }

                Title("链接")
                Card {
                    LinkItem(
                        title = "YumeBox-MaterialDesign",
                        url = "https://github.com/Yizuka17/YumeBox-MaterialDesign",
                    )
                    LinkItem(
                        title = "源码",
                        url = "https://github.com/YumeLira/YumeBox",
                    )
                    LinkItem(
                        title = "许可证",
                        url = "https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/main/LICENSE",
                    )
                    LinkItem(
                        title = "隐私政策",
                        url = "https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/main/PRIVACY_POLICY.md",
                    )
                }
            }
        }
    }
}
