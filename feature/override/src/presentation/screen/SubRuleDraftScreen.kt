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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.*
import com.amamiyakokoro.box.presentation.icon.AppMd3Icons
import com.amamiyakokoro.box.presentation.util.*
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import androidx.compose.material3.Scaffold

@Composable
fun OverrideSubRuleDraftEditorScreen(
    navigator: DestinationsNavigator,
    onOpenRuleListEditor: (
        title: String,
        values: OverrideListModeValues<List<String>>,
        availableModes: List<OverrideListEditorMode>,
        selectedMode: OverrideListEditorMode,
        referenceCatalog: OverrideReferenceCatalog,
        callback: (OverrideListModeValues<List<String>>) -> Unit,
    ) -> Unit,
) {
    val listState = rememberLazyListState()
    val subRuleGroupLabel = stringResource(LocaleR.string.override_draft_sub_rule_group)
    val nameRequiredMessage = stringResource(LocaleR.string.override_draft_name_required)
    val editSubRulesLabel = stringResource(LocaleR.string.override_draft_edit_sub_rules)
    val title = OverrideStructuredEditorStore.subRuleDraftEditorTitle.ifBlank { subRuleGroupLabel }
    val storeDraft = OverrideStructuredEditorStore.subRuleDraftEditorValue
    val saveFabController = rememberOverrideFabController()
    val draftUiId = remember { storeDraft?.uiId ?: OverrideSubRuleGroupDraft().uiId }

    var name by remember { mutableStateOf(storeDraft?.name.orEmpty()) }
    var rules by remember { mutableStateOf(storeDraft?.rules.orEmpty()) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val referenceCatalog = rememberCurrentReferenceCatalog()

    fun syncDraftSession(
        updatedName: String = name,
        updatedRules: List<String> = rules,
    ) {
        OverrideStructuredEditorStore.updateSubRuleDraftEditorSession(
            OverrideSubRuleGroupDraft(
                name = updatedName,
                rules = updatedRules,
                uiId = OverrideStructuredEditorStore.subRuleDraftEditorValue?.uiId ?: draftUiId,
            ),
        )
    }

    LaunchedEffect(storeDraft?.name, storeDraft?.rules) {
        val latestDraft = OverrideStructuredEditorStore.subRuleDraftEditorValue ?: return@LaunchedEffect
        if (name != latestDraft.name) {
            name = latestDraft.name
        }
        if (rules != latestDraft.rules) {
            rules = latestDraft.rules
        }
    }

    LaunchedEffect(draftUiId) {
        if (OverrideStructuredEditorStore.subRuleDraftEditorValue == null) {
            syncDraftSession()
        }
    }

    Scaffold(
        floatingActionButton = {
            OverrideAnimatedFab(
                controller = saveFabController,
                visible = true,
                imageVector = AppMd3Icons.Action.Save,
                contentDescription = stringResource(LocaleR.string.override_draft_save) + subRuleGroupLabel,
                onClick = {
                    if (name.trim().isBlank()) {
                        errorText = nameRequiredMessage
                        return@OverrideAnimatedFab
                    }
                    OverrideStructuredEditorStore.submitSubRuleDraft(
                        OverrideSubRuleGroupDraft(
                            name = name.trim(),
                            rules = rules,
                            uiId = OverrideStructuredEditorStore.subRuleDraftEditorValue?.uiId ?: draftUiId,
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
            modifier = Modifier.fillMaxSize(),
            lazyListState = listState,
            onScrollDirectionChanged = saveFabController::onScrollDirectionChanged,
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(OverrideSectionSpacing),
                ) {
                    OverridePlainFormSection(stringResource(LocaleR.string.override_draft_basic_info)) {
                        OverrideFormField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorText = null
                                syncDraftSession(updatedName = it)
                            },
                            label = stringResource(LocaleR.string.override_draft_name),
                            errorText = errorText,
                        )
                    }
                    OverrideCardSection(stringResource(LocaleR.string.override_draft_rule_list)) {
                        PreferenceArrowItem(
                            title = stringResource(LocaleR.string.override_draft_rule_list),
                            summary = if (rules.isEmpty()) {
                                stringResource(LocaleR.string.override_draft_no_rules)
                            } else {
                                stringResource(LocaleR.string.override_draft_rules_configured).format(rules.size)
                            },
                            onClick = {
                                syncDraftSession()
                                onOpenRuleListEditor(
                                    editSubRulesLabel,
                                    OverrideListModeValues(replaceValue = rules),
                                    listOf(OverrideListEditorMode.Replace),
                                    OverrideListEditorMode.Replace,
                                    referenceCatalog,
                                ) { updatedValues ->
                                    syncDraftSession(
                                        updatedName = OverrideStructuredEditorStore.subRuleDraftEditorValue?.name.orEmpty(),
                                        updatedRules = updatedValues.replaceValue.orEmpty(),
                                    )
                                }
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(OverrideSectionBottomSpacing))
                }
            }
        }
    }
}
