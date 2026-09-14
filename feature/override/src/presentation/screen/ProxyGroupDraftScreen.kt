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


package com.amamiyakokoro.box.presentation.screen
import com.amamiyakokoro.box.presentation.theme.UiDp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.*
import com.amamiyakokoro.box.presentation.component.md3.YumeMd3DropdownPreference
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.util.OverrideExtraFieldDraft
import com.amamiyakokoro.box.presentation.util.OverrideProxyGroupDraft
import com.amamiyakokoro.box.presentation.util.OverrideProxyGroupTypePresets
import com.amamiyakokoro.box.presentation.util.OverrideStructuredEditorStore
import com.amamiyakokoro.box.presentation.util.rememberCurrentReferenceCatalog
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.compose.material3.Scaffold

@Composable
fun OverrideProxyGroupDraftEditorScreen(
    navigator: DestinationsNavigator,
) {
    val listState = rememberLazyListState()
    val proxyNodeLabel = stringResource(LocaleR.string.override_editor_proxy_node)
    val proxyGroupLabel = stringResource(LocaleR.string.override_editor_proxy_group)
    val nameLabel = stringResource(LocaleR.string.override_draft_name)
    val nameRequiredMessage = stringResource(LocaleR.string.override_draft_name_required)
    val typeEmptyMessage = stringResource(LocaleR.string.override_editor_type_empty)
    val title = OverrideStructuredEditorStore.proxyGroupDraftEditorTitle.ifBlank { proxyGroupLabel }
    val initialValue = remember { OverrideStructuredEditorStore.proxyGroupDraftEditorValue }
    val saveFabController = rememberOverrideFabController()

    var name by remember { mutableStateOf(initialValue?.name.orEmpty()) }
    var type by remember { mutableStateOf(initialValue?.type?.ifBlank { "select" } ?: "select") }
    var proxies by remember { mutableStateOf(initialValue?.proxies.orEmpty()) }
    var useText by remember { mutableStateOf(initialValue?.use.orEmpty().joinToString("\n")) }
    var url by remember { mutableStateOf(initialValue?.url.orEmpty()) }
    var intervalText by remember { mutableStateOf(initialValue?.interval?.toString().orEmpty()) }
    var timeoutText by remember { mutableStateOf(initialValue?.timeout?.toString().orEmpty()) }
    var maxFailedTimesText by remember { mutableStateOf(initialValue?.maxFailedTimes?.toString().orEmpty()) }
    var interfaceName by remember { mutableStateOf(initialValue?.interfaceName.orEmpty()) }
    var routingMarkText by remember { mutableStateOf(initialValue?.routingMark?.toString().orEmpty()) }
    var filter by remember { mutableStateOf(initialValue?.filter.orEmpty()) }
    var excludeFilter by remember { mutableStateOf(initialValue?.excludeFilter.orEmpty()) }
    var excludeType by remember { mutableStateOf(initialValue?.excludeType.orEmpty()) }
    var expectedStatus by remember { mutableStateOf(initialValue?.expectedStatus.orEmpty()) }
    var icon by remember { mutableStateOf(initialValue?.icon.orEmpty()) }
    var lazy by remember { mutableStateOf(initialValue?.lazy) }
    var disableUdp by remember { mutableStateOf(initialValue?.disableUdp) }
    var includeAll by remember { mutableStateOf(initialValue?.includeAll) }
    var includeAllProxies by remember { mutableStateOf(initialValue?.includeAllProxies) }
    var includeAllProviders by remember { mutableStateOf(initialValue?.includeAllProviders) }
    var hidden by remember { mutableStateOf(initialValue?.hidden) }
    var extraFields by remember { mutableStateOf(initialValue?.extraFields.orEmpty()) }
    var editingExtraKey by remember { mutableStateOf<String?>(null) }
    var showExtraFieldDialog by remember { mutableStateOf(false) }
    var showProxySelector by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val selectedPresetIndex by remember(type) {
        derivedStateOf {
            OverrideProxyGroupTypePresets.indexOfFirst { it.equals(type, ignoreCase = true) }.coerceAtLeast(0)
        }
    }
    val referenceCatalog = rememberCurrentReferenceCatalog()
    val excludedGroupNames = remember(name, initialValue?.name) {
        buildSet {
            name.trim().takeIf(String::isNotBlank)?.let(::add)
            initialValue?.name?.trim()?.takeIf(String::isNotBlank)?.let(::add)
        }
    }
    val availableProxyGroupNames = remember(referenceCatalog.proxyGroupNames, excludedGroupNames) {
        referenceCatalog.proxyGroupNames.filterNot(excludedGroupNames::contains)
    }
    val proxySelectionGroups = remember(
        referenceCatalog.proxyNames,
        availableProxyGroupNames,
        proxyNodeLabel,
        proxyGroupLabel,
    ) {
        listOfNotNull(
            referenceCatalog.proxyNames.takeIf { it.isNotEmpty() }?.let { values ->
                OverrideSelectionGroup(
                    title = proxyNodeLabel,
                    items = values,
                )
            },
            availableProxyGroupNames.takeIf { it.isNotEmpty() }?.let { values ->
                OverrideSelectionGroup(
                    title = proxyGroupLabel,
                    items = values,
                )
            },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            OverrideStructuredEditorStore.clearProxyGroupDraftEditor()
        }
    }

    Scaffold(
        floatingActionButton = {
            OverrideAnimatedFab(
                controller = saveFabController,
                visible = true,
                imageVector = AppMd3Icons.Action.Save,
                contentDescription = stringResource(LocaleR.string.override_editor_save_proxy_group),
                onClick = {
                    if (name.trim().isBlank()) {
                        errorText = nameRequiredMessage
                        return@OverrideAnimatedFab
                    }
                    if (type.trim().isBlank()) {
                        errorText = typeEmptyMessage
                        return@OverrideAnimatedFab
                    }
                    OverrideStructuredEditorStore.submitProxyGroupDraft(
                        OverrideProxyGroupDraft(
                            name = name.trim(),
                            type = type.trim(),
                            proxies = proxies,
                            use = parseMultilineValues(useText),
                            url = url.trim(),
                            interval = intervalText.trim().toIntOrNull(),
                            lazy = lazy,
                            timeout = timeoutText.trim().toIntOrNull(),
                            maxFailedTimes = maxFailedTimesText.trim().toIntOrNull(),
                            disableUdp = disableUdp,
                            interfaceName = interfaceName.trim(),
                            routingMark = routingMarkText.trim().toIntOrNull(),
                            includeAll = includeAll,
                            includeAllProxies = includeAllProxies,
                            includeAllProviders = includeAllProviders,
                            filter = filter.trim(),
                            excludeFilter = excludeFilter.trim(),
                            excludeType = excludeType.trim(),
                            expectedStatus = expectedStatus.trim(),
                            hidden = hidden,
                            icon = icon.trim(),
                            extraFields = extraFields,
                            uiId = initialValue?.uiId ?: OverrideProxyGroupDraft().uiId,
                        ),
                    )
                    navigator.navigateUp()
                },
            )
        },
        topBar = {
            TopBar(
                title = title,
            )
        },
    ) { innerPadding ->
        val mainLikePadding = rememberStandalonePageMainPadding()
        ScreenLazyColumn(

            innerPadding = combinePaddingValues(innerPadding, mainLikePadding),
            lazyListState = listState,
            onScrollDirectionChanged = saveFabController::onScrollDirectionChanged,
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(OverrideSectionSpacing),
                ) {
                    OverrideSection(stringResource(LocaleR.string.override_draft_basic_info)) {
                        OverrideSelectorCard {
                            YumeMd3DropdownPreference(
                                title = stringResource(LocaleR.string.override_editor_rule_type),
                                items = OverrideProxyGroupTypePresets,
                                selectedIndex = selectedPresetIndex,
                                onSelectedIndexChange = { index ->
                                    if (index in OverrideProxyGroupTypePresets.indices) {
                                        type = OverrideProxyGroupTypePresets[index]
                                        errorText = null
                                    }
                                },
                            )
                        }
                        OverrideFormFieldColumn {
                            OverrideFormField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    errorText = null
                                },
                                label = nameLabel,
                                errorText = errorText?.takeIf { it == nameRequiredMessage },
                            )
                            OverrideFormField(
                                value = url,
                                onValueChange = { url = it },
                                label = stringResource(LocaleR.string.override_proxy_group_field_url),
                            )
                            OverrideFormField(
                                value = intervalText,
                                onValueChange = { intervalText = it.filter(Char::isDigit) },
                                label = stringResource(LocaleR.string.override_proxy_group_field_interval),
                            )
                            OverrideFormField(
                                value = timeoutText,
                                onValueChange = { timeoutText = it.filter(Char::isDigit) },
                                label = stringResource(LocaleR.string.override_proxy_group_field_timeout),
                            )
                            OverrideFormField(
                                value = maxFailedTimesText,
                                onValueChange = { maxFailedTimesText = it.filter(Char::isDigit) },
                                label = stringResource(LocaleR.string.override_proxy_group_field_max_failed_times),
                            )
                        }
                    }
                    OverrideSection(stringResource(LocaleR.string.override_editor_member_source)) {
                        OverrideSelectorCard {
                            PreferenceArrowItem(
                                title = stringResource(LocaleR.string.override_proxy_group_field_proxies),
                                onClick = {
                                    showProxySelector = true
                                    errorText = null
                                },
                                holdDownState = showProxySelector,
                            )
                        }
                        OverrideFormFieldColumn {
                            OverrideFormField(
                                value = useText,
                                onValueChange = { useText = it },
                                label = stringResource(LocaleR.string.override_proxy_group_field_use),
                                supportText = stringResource(LocaleR.string.override_proxy_group_field_use_hint),
                                modifier = Modifier.heightIn(min = UiDp.dp100),
                                maxLines = 8,
                            )
                        }
                    }
                    OverridePlainFormSection(stringResource(LocaleR.string.override_editor_health_check_and_filter)) {
                        OverrideFormField(
                            value = interfaceName,
                            onValueChange = { interfaceName = it },
                            label = stringResource(LocaleR.string.override_proxy_group_field_interface_name),
                        )
                        OverrideFormField(
                            value = routingMarkText,
                            onValueChange = { routingMarkText = it.filter(Char::isDigit) },
                            label = stringResource(LocaleR.string.override_proxy_group_field_routing_mark),
                        )
                        OverrideFormField(
                            value = filter,
                            onValueChange = { filter = it },
                            label = stringResource(LocaleR.string.override_proxy_group_field_filter),
                        )
                        OverrideFormField(
                            value = excludeFilter,
                            onValueChange = { excludeFilter = it },
                            label = stringResource(LocaleR.string.override_proxy_group_field_exclude_filter),
                        )
                        OverrideFormField(
                            value = excludeType,
                            onValueChange = { excludeType = it },
                            label = stringResource(LocaleR.string.override_proxy_group_field_exclude_type),
                        )
                        OverrideFormField(
                            value = expectedStatus,
                            onValueChange = { expectedStatus = it },
                            label = stringResource(LocaleR.string.override_proxy_group_field_expected_status),
                        )
                        OverrideFormField(
                            value = icon,
                            onValueChange = { icon = it },
                            label = stringResource(LocaleR.string.override_proxy_group_field_icon),
                        )
                    }
                    OverrideCardSection(stringResource(LocaleR.string.override_draft_boolean_options)) {
                        NullableBooleanSelector(title = stringResource(LocaleR.string.override_proxy_group_field_lazy), value = lazy, onValueChange = { lazy = it })
                        NullableBooleanSelector(title = stringResource(LocaleR.string.override_proxy_group_field_disable_udp), value = disableUdp, onValueChange = { disableUdp = it })
                        NullableBooleanSelector(title = stringResource(LocaleR.string.override_proxy_group_field_include_all), value = includeAll, onValueChange = { includeAll = it })
                        NullableBooleanSelector(title = stringResource(LocaleR.string.override_proxy_group_field_include_all_proxies), value = includeAllProxies, onValueChange = { includeAllProxies = it })
                        NullableBooleanSelector(title = stringResource(LocaleR.string.override_proxy_group_field_include_all_providers), value = includeAllProviders, onValueChange = { includeAllProviders = it })
                        NullableBooleanSelector(title = stringResource(LocaleR.string.override_proxy_group_field_hidden), value = hidden, onValueChange = { hidden = it })
                    }
                    OverrideSection(stringResource(LocaleR.string.override_draft_extra_fields)) {
                        OverrideExtraFieldsCard(
                            title = stringResource(LocaleR.string.override_draft_extra_fields),
                            fields = extraFields,
                            onAddClick = {
                                editingExtraKey = null
                                showExtraFieldDialog = true
                            },
                            onEditClick = { key, _ ->
                                editingExtraKey = key
                                showExtraFieldDialog = true
                            },
                            onDeleteClick = { key ->
                                extraFields = extraFields - key
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(OverrideSectionBottomSpacing))
                }
            }
        }
        OverrideExtraFieldDialog(
            show = showExtraFieldDialog,
            title = stringResource(
                if (editingExtraKey == null) LocaleR.string.override_draft_add_extra_field
                else LocaleR.string.override_draft_edit_extra_field,
            ),
            initialValue = editingExtraKey?.let(extraFields::toExtraFieldDraft),
            onConfirm = { draft: OverrideExtraFieldDraft ->
                extraFields = extraFields.updateExtraField(editingExtraKey, draft)
                editingExtraKey = null
                showExtraFieldDialog = false
            },
            onDismiss = {
                editingExtraKey = null
                showExtraFieldDialog = false
            },
        )
        OverrideMultiValueSelectionSheet(
            show = showProxySelector,
            title = stringResource(LocaleR.string.override_editor_select_proxy_group_member),
            values = proxies,
            groups = proxySelectionGroups,
            customInputLabel = stringResource(LocaleR.string.override_editor_custom_member),
            onDismiss = { showProxySelector = false },
            onConfirm = { selectedValues ->
                proxies = selectedValues
                errorText = null
                showProxySelector = false
            },
        )
    }
}

private fun parseMultilineValues(rawValue: String): List<String> {
    return rawValue
        .lines()
        .map(String::trim)
        .filter(String::isNotBlank)
}
