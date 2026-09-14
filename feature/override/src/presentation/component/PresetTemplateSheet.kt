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
import com.amamiyakokoro.box.presentation.theme.UiDp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.core.model.officialMrsPresetIconUrl
import com.amamiyakokoro.box.data.util.OverridePresetItem
import com.amamiyakokoro.box.data.util.OverridePresetRegion
import com.amamiyakokoro.box.data.util.OverridePresetTemplateSelection
import com.amamiyakokoro.box.data.util.orderedBasePresetItems
import com.amamiyakokoro.box.data.util.orderedPresetRegions
import com.amamiyakokoro.box.data.util.orderedServicePresetItems
import com.amamiyakokoro.box.data.util.sortPresetItems
import com.amamiyakokoro.box.data.util.sortPresetRegions

@Composable
fun OverridePresetTemplateSheet(
    show: Boolean,
    initialSelection: OverridePresetTemplateSelection = OverridePresetTemplateSelection(),
    onDismiss: () -> Unit,
    onConfirm: (OverridePresetTemplateSelection) -> Unit,
) {
    val presetTemplateTitle = stringResource(LocaleR.string.override_draft_preset_template)
    val applyLabel = stringResource(LocaleR.string.override_draft_apply)
    val presetApplySummary = stringResource(LocaleR.string.override_draft_preset_apply_summary)
    val groupTypeTitle = stringResource(LocaleR.string.override_draft_group_type_title)
    val urlTestGroupType = stringResource(LocaleR.string.override_draft_group_type_url_test)
    val fallbackGroupType = stringResource(LocaleR.string.override_draft_group_type_fallback)
    val urlTestRegionGroupTitle = stringResource(LocaleR.string.override_draft_url_test_region_group_title)
    val fallbackRegionGroupTitle = stringResource(LocaleR.string.override_draft_fallback_region_group_title)
    val basicRoutingTitle = stringResource(LocaleR.string.override_draft_basic_routing)
    val serviceRoutingTitle = stringResource(LocaleR.string.override_draft_service_routing)
    val selectedUrlTestRegions = remember(show) { mutableStateListOf<OverridePresetRegion>() }
    val selectedFallbackRegions = remember(show) { mutableStateListOf<OverridePresetRegion>() }
    val enabledItems = remember(show) { mutableStateListOf<OverridePresetItem>() }
    var enableUrlTestGroup by remember(show) { mutableStateOf(initialSelection.enableUrlTestGroup) }
    var enableFallbackGroup by remember(show) { mutableStateOf(initialSelection.enableFallbackGroup) }

    LaunchedEffect(show, initialSelection) {
        selectedUrlTestRegions.clear()
        selectedUrlTestRegions.addAll(sortPresetRegions(initialSelection.urlTestRegions))
        selectedFallbackRegions.clear()
        selectedFallbackRegions.addAll(sortPresetRegions(initialSelection.fallbackRegions))
        enabledItems.clear()
        enabledItems.addAll(sortPresetItems(initialSelection.enabledItems))
        enableUrlTestGroup = initialSelection.enableUrlTestGroup
        enableFallbackGroup = initialSelection.enableFallbackGroup
    }

    AppActionBottomSheet(
        show = show,
        modifier = Modifier,
        title = presetTemplateTitle,
        enableNestedScroll = false,
        contentScrollEnabled = false,
        dragHandleColor = Color.Transparent,
        startAction = {
            AppBottomSheetCloseAction(onClick = onDismiss)
        },
        endAction = {
            AppBottomSheetConfirmAction(
                contentDescription = applyLabel,
                onClick = {
                    onConfirm(
                        OverridePresetTemplateSelection(
                            urlTestRegions = selectedUrlTestRegions.toSet(),
                            fallbackRegions = selectedFallbackRegions.toSet(),
                            enabledItems = enabledItems.toSet(),
                            enableUrlTestGroup = enableUrlTestGroup,
                            enableFallbackGroup = enableFallbackGroup,
                        ),
                    )
                },
            )
        },
        onDismissRequest = onDismiss,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = UiDp.dp560)
                .padding(bottom = UiDp.dp12),
            verticalArrangement = Arrangement.spacedBy(UiDp.dp8),
        ) {
            item(key = "preset-template-intro") {
                Card(applyHorizontalPadding = false) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = UiDp.dp16, vertical = UiDp.dp14),
                        verticalArrangement = Arrangement.spacedBy(UiDp.dp4),
                    ) {
                        AppText(
                            text = presetApplySummary,
                            color = appOnSurfaceVariantColor(),
                        )
                    }
                }
            }

            item(key = "preset-group-types") {
                RoutingSwitchCard(
                    title = groupTypeTitle,
                    items = listOf("urltest", "fallback"),
                    iconUrl = { type ->
                        officialMrsPresetIconUrl(
                            if (type == "urltest") "Urltest" else "Available",
                        )
                    },
                    isChecked = { type ->
                        when (type) {
                            "urltest" -> enableUrlTestGroup
                            else -> enableFallbackGroup
                        }
                    },
                    onCheckedChange = { item, checked ->
                        when (item) {
                            "urltest" -> enableUrlTestGroup = checked
                            "fallback" -> enableFallbackGroup = checked
                        }
                    },
                    itemTitle = {
                        if (it == "urltest") {
                            urlTestGroupType
                        } else {
                            fallbackGroupType
                        }
                    },
                    applyHorizontalPadding = false,
                    titleContent = { title ->
                        OverridePresetTemplateGroupTitle(title)
                    },
                )
            }

            item(key = "preset-urltest-regions") {
                RoutingSwitchCard(
                    title = urlTestRegionGroupTitle,
                    items = orderedPresetRegions(),
                    iconUrl = OverridePresetRegion::icon,
                    isChecked = { region -> region in selectedUrlTestRegions },
                    onCheckedChange = { region, checked ->
                        toggleSelection(selectedUrlTestRegions, region, checked)
                    },
                    itemTitle = OverridePresetRegion::displayName,
                    applyHorizontalPadding = false,
                    titleContent = { title ->
                        OverridePresetTemplateGroupTitle(title)
                    },
                )
            }

            item(key = "preset-fallback-regions") {
                RoutingSwitchCard(
                    title = fallbackRegionGroupTitle,
                    items = orderedPresetRegions(),
                    iconUrl = OverridePresetRegion::icon,
                    isChecked = { region -> region in selectedFallbackRegions },
                    onCheckedChange = { region, checked ->
                        toggleSelection(selectedFallbackRegions, region, checked)
                    },
                    itemTitle = OverridePresetRegion::displayName,
                    applyHorizontalPadding = false,
                    titleContent = { title ->
                        OverridePresetTemplateGroupTitle(title)
                    },
                )
            }

            item(key = "preset-base-items") {
                RoutingSwitchCard(
                    title = basicRoutingTitle,
                    items = orderedBasePresetItems(),
                    iconUrl = OverridePresetItem::icon,
                    isChecked = { item -> item in enabledItems },
                    onCheckedChange = { item, checked -> toggleSelection(enabledItems, item, checked) },
                    itemTitle = OverridePresetItem::title,
                    applyHorizontalPadding = false,
                    titleContent = { title ->
                        OverridePresetTemplateGroupTitle(title)
                    },
                )
            }

            item(key = "preset-service-items") {
                RoutingSwitchCard(
                    title = serviceRoutingTitle,
                    items = orderedServicePresetItems(),
                    iconUrl = OverridePresetItem::icon,
                    isChecked = { item -> item in enabledItems },
                    onCheckedChange = { item, checked -> toggleSelection(enabledItems, item, checked) },
                    itemTitle = OverridePresetItem::title,
                    applyHorizontalPadding = false,
                    titleContent = { title ->
                        OverridePresetTemplateGroupTitle(title)
                    },
                )
            }
        }
    }
}

@Composable
private fun OverridePresetTemplateGroupTitle(title: String) {
    AppText(
        text = title,
        color = appOnSurfaceVariantColor(),
        modifier = Modifier.padding(horizontal = UiDp.dp16, vertical = UiDp.dp8),
    )
}

private fun <T> toggleSelection(
    items: MutableList<T>,
    item: T,
    checked: Boolean,
) {
    if (checked) {
        if (item !in items) {
            items.add(item)
        }
    } else {
        items.remove(item)
    }
}
