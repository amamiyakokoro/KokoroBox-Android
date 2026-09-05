/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroAuthenticationRequiredException
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroCustomRuleInput
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroCustomRulesClient
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroCustomRulesOptions
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroRuleSet
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroRulesApiException
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroRulesSaveOutcomeUnknownException
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroRulesValidationException
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroRulesValidationReason
import com.github.yumelira.yumebox.screen.profiles.KokoroAccountClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class KokoroRulesConflict(
    val remoteSet: KokoroRuleSet,
    val localRules: List<KokoroCustomRuleInput>,
)

internal enum class KokoroRulesStatus {
    IDLE,
    SAVED,
    AUTH_REQUIRED,
    LOAD_FAILED,
    VALIDATION_FAILED,
    NOT_FOUND,
    RATE_LIMITED,
    SET_CONFLICT,
    SAVE_OUTCOME_UNKNOWN,
    REQUEST_FAILED,
}

internal data class KokoroCustomRulesUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val options: KokoroCustomRulesOptions = KokoroCustomRulesOptions(),
    val sets: List<KokoroRuleSet> = emptyList(),
    val selectedSetId: Long? = null,
    val draftRules: List<KokoroCustomRuleInput> = emptyList(),
    val dirty: Boolean = false,
    val status: KokoroRulesStatus = KokoroRulesStatus.IDLE,
    val validationReason: KokoroRulesValidationReason? = null,
    val validationRuleIndex: Int? = null,
    val retryAfterSeconds: Long? = null,
    val conflict: KokoroRulesConflict? = null,
) {
    val selectedSet: KokoroRuleSet? get() = sets.firstOrNull { it.id == selectedSetId }
}

internal class KokoroCustomRulesViewModel(
    private val client: KokoroCustomRulesClient,
    private val accountClient: KokoroAccountClient,
) : ViewModel() {
    private val _state = MutableStateFlow(KokoroCustomRulesUiState())
    val state = _state.asStateFlow()

    fun load() {
        if (_state.value.loading && _state.value.sets.isNotEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, status = KokoroRulesStatus.IDLE) }
            try {
                val editorData = client.getEditorData()
                val loadedSets = editorData.state.sets
                val selected = loadedSets.firstOrNull { it.id == _state.value.selectedSetId }
                    ?: loadedSets.firstOrNull { it.name == "default" }
                    ?: loadedSets.firstOrNull()
                _state.value = _state.value.copy(
                    loading = false,
                    options = editorData.options,
                    sets = loadedSets,
                    selectedSetId = selected?.id,
                    draftRules = selected?.rules.orEmpty().map { it.asInput() },
                    dirty = false,
                    status = KokoroRulesStatus.IDLE,
                    conflict = null,
                )
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                _state.update {
                    it.copy(
                        loading = false,
                        status = if (error is KokoroAuthenticationRequiredException) {
                            KokoroRulesStatus.AUTH_REQUIRED
                        } else {
                            KokoroRulesStatus.LOAD_FAILED
                        },
                    )
                }
            }
        }
    }

    suspend fun beginLogin(): String = accountClient.beginLogin()

    suspend fun cancelLogin(loginUrl: String) = accountClient.cancelLogin(loginUrl)

    fun selectSet(setId: Long) {
        val selected = _state.value.sets.firstOrNull { it.id == setId } ?: return
        _state.update {
            it.copy(
                selectedSetId = selected.id,
                draftRules = selected.rules.map { rule -> rule.asInput() },
                dirty = false,
                status = KokoroRulesStatus.IDLE,
                conflict = null,
            )
        }
    }

    fun addRule(rule: KokoroCustomRuleInput) {
        _state.update { it.copy(draftRules = it.draftRules + rule, dirty = true, status = KokoroRulesStatus.IDLE) }
    }

    fun updateRule(index: Int, rule: KokoroCustomRuleInput) {
        _state.update { current ->
            if (index !in current.draftRules.indices) current else current.copy(
                draftRules = current.draftRules.toMutableList().also { it[index] = rule },
                dirty = true,
                status = KokoroRulesStatus.IDLE,
            )
        }
    }

    fun deleteRule(index: Int) {
        _state.update { current ->
            if (index !in current.draftRules.indices) current else current.copy(
                draftRules = current.draftRules.toMutableList().also { it.removeAt(index) },
                dirty = true,
                status = KokoroRulesStatus.IDLE,
            )
        }
    }

    fun moveRule(index: Int, offset: Int) {
        _state.update { current ->
            val destination = index + offset
            if (index !in current.draftRules.indices || destination !in current.draftRules.indices) current
            else current.copy(
                draftRules = current.draftRules.toMutableList().also {
                    val moved = it.removeAt(index)
                    it.add(destination, moved)
                },
                dirty = true,
                status = KokoroRulesStatus.IDLE,
            )
        }
    }

    fun createSet(name: String) = mutateSet {
        val created = client.createSet(name)
        _state.update {
            it.copy(
                sets = it.sets + created,
                selectedSetId = created.id,
                draftRules = emptyList(),
                dirty = false,
            )
        }
    }

    fun renameSelectedSet(name: String) {
        val selected = _state.value.selectedSet ?: return
        mutateSet {
            val renamed = client.renameSet(selected.id, name, selected.revision)
            replaceRemoteSet(renamed, preserveDraft = true)
        }
    }

    fun deleteSelectedSet() {
        val selected = _state.value.selectedSet ?: return
        if (selected.name == "default") return
        mutateSet {
            client.deleteSet(selected.id, selected.revision)
            val remaining = _state.value.sets.filterNot { it.id == selected.id }
            val replacement = remaining.firstOrNull { it.name == "default" } ?: remaining.firstOrNull()
            _state.update {
                it.copy(
                    sets = remaining,
                    selectedSetId = replacement?.id,
                    draftRules = replacement?.rules.orEmpty().map { rule -> rule.asInput() },
                    dirty = false,
                )
            }
        }
    }

    fun save() {
        val selected = _state.value.selectedSet ?: return
        val localRules = _state.value.draftRules
        viewModelScope.launch {
            _state.update { it.copy(saving = true, status = KokoroRulesStatus.IDLE) }
            try {
                // Capabilities can change independently of the installed client.
                val freshOptions = client.getOptions()
                _state.update { it.copy(options = freshOptions) }
                val updated = client.replaceRules(selected.id, selected.revision, localRules, freshOptions)
                replaceRemoteSet(updated, preserveDraft = false)
                _state.update { it.copy(saving = false, dirty = false, status = KokoroRulesStatus.SAVED) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                handleSaveFailure(error, selected.id, localRules)
            }
        }
    }

    fun useRemoteConflict() {
        val conflict = _state.value.conflict ?: return
        replaceRemoteSet(conflict.remoteSet, preserveDraft = false)
        _state.update {
            it.copy(
                draftRules = conflict.remoteSet.rules.map { rule -> rule.asInput() },
                dirty = false,
                status = KokoroRulesStatus.IDLE,
                conflict = null,
            )
        }
    }

    fun keepLocalConflict() {
        val conflict = _state.value.conflict ?: return
        replaceRemoteSet(conflict.remoteSet, preserveDraft = true)
        _state.update {
            it.copy(
                draftRules = conflict.localRules,
                dirty = true,
                status = KokoroRulesStatus.IDLE,
                conflict = null,
            )
        }
    }

    fun clearStatus() = _state.update {
        it.copy(status = KokoroRulesStatus.IDLE, validationReason = null, validationRuleIndex = null)
    }

    private fun mutateSet(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, status = KokoroRulesStatus.IDLE) }
            try {
                block()
                _state.update { it.copy(saving = false) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                val status = when (error) {
                    is KokoroAuthenticationRequiredException -> KokoroRulesStatus.AUTH_REQUIRED
                    is KokoroRulesApiException -> when (error.statusCode) {
                        404 -> KokoroRulesStatus.NOT_FOUND
                        429 -> KokoroRulesStatus.RATE_LIMITED
                        409 -> KokoroRulesStatus.SET_CONFLICT
                        else -> KokoroRulesStatus.REQUEST_FAILED
                    }
                    else -> KokoroRulesStatus.REQUEST_FAILED
                }
                _state.update { it.copy(saving = false, status = status) }
                if (status == KokoroRulesStatus.NOT_FOUND) load()
                if (status == KokoroRulesStatus.SET_CONFLICT) reloadSetsPreservingDraft()
            }
        }
    }

    private suspend fun handleSaveFailure(
        error: Exception,
        setId: Long,
        localRules: List<KokoroCustomRuleInput>,
    ) {
        when {
            error is KokoroRulesValidationException -> _state.update {
                it.copy(
                    saving = false,
                    status = KokoroRulesStatus.VALIDATION_FAILED,
                    validationReason = error.reason,
                    validationRuleIndex = error.ruleIndex,
                )
            }
            error is KokoroRulesApiException && error.statusCode == 409 -> loadConflict(setId, localRules)
            error is KokoroRulesApiException && error.statusCode == 404 -> {
                _state.update { it.copy(saving = false, status = KokoroRulesStatus.NOT_FOUND) }
                load()
            }
            error is KokoroRulesApiException && error.statusCode == 422 -> {
                runCatching { client.getOptions() }.getOrNull()?.let { fresh ->
                    _state.update { it.copy(options = fresh) }
                }
                _state.update { it.copy(saving = false, status = KokoroRulesStatus.VALIDATION_FAILED) }
            }
            error is KokoroRulesApiException && error.statusCode == 429 -> _state.update {
                it.copy(
                    saving = false,
                    status = KokoroRulesStatus.RATE_LIMITED,
                    retryAfterSeconds = error.retryAfterSeconds,
                )
            }
            error is KokoroAuthenticationRequiredException -> _state.update {
                it.copy(saving = false, status = KokoroRulesStatus.AUTH_REQUIRED)
            }
            error is KokoroRulesSaveOutcomeUnknownException -> {
                _state.update { it.copy(saving = false, status = KokoroRulesStatus.SAVE_OUTCOME_UNKNOWN) }
                loadConflict(setId, localRules)
            }
            else -> _state.update { it.copy(saving = false, status = KokoroRulesStatus.REQUEST_FAILED) }
        }
    }

    private suspend fun loadConflict(setId: Long, localRules: List<KokoroCustomRuleInput>) {
        val remote = runCatching { client.getState().sets.firstOrNull { it.id == setId } }.getOrNull()
        if (remote == null) {
            _state.update { it.copy(saving = false, status = KokoroRulesStatus.NOT_FOUND) }
            return
        }
        replaceRemoteSet(remote, preserveDraft = true)
        _state.update {
            it.copy(
                saving = false,
                conflict = KokoroRulesConflict(remote, localRules),
                draftRules = localRules,
                dirty = true,
            )
        }
    }

    private suspend fun reloadSetsPreservingDraft() {
        val remoteSets = runCatching { client.getState().sets }.getOrNull() ?: return
        val selectedId = _state.value.selectedSetId
        val selectedStillExists = remoteSets.any { it.id == selectedId }
        val replacement = remoteSets.firstOrNull { it.name == "default" } ?: remoteSets.firstOrNull()
        _state.update {
            it.copy(
                sets = remoteSets,
                selectedSetId = if (selectedStillExists) selectedId else replacement?.id,
                draftRules = if (selectedStillExists) it.draftRules
                else replacement?.rules.orEmpty().map { rule -> rule.asInput() },
                dirty = selectedStillExists && it.dirty,
            )
        }
    }

    private fun replaceRemoteSet(updated: KokoroRuleSet, preserveDraft: Boolean) {
        _state.update { current ->
            current.copy(
                sets = current.sets.map { if (it.id == updated.id) updated else it },
                draftRules = if (preserveDraft) current.draftRules else updated.rules.map { it.asInput() },
            )
        }
    }
}
