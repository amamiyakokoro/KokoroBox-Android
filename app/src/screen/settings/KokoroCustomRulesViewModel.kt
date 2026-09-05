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
    SAVE_OUTCOME_UNKNOWN,
    REQUEST_FAILED,
}

internal data class KokoroCustomRulesUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val options: KokoroCustomRulesOptions = KokoroCustomRulesOptions(),
    val defaultRuleSet: KokoroRuleSet? = null,
    val draftRules: List<KokoroCustomRuleInput> = emptyList(),
    val dirty: Boolean = false,
    val status: KokoroRulesStatus = KokoroRulesStatus.IDLE,
    val validationReason: KokoroRulesValidationReason? = null,
    val validationRuleIndex: Int? = null,
    val retryAfterSeconds: Long? = null,
    val conflict: KokoroRulesConflict? = null,
)

internal class KokoroCustomRulesViewModel(
    private val client: KokoroCustomRulesClient,
    private val accountClient: KokoroAccountClient,
) : ViewModel() {
    private val _state = MutableStateFlow(KokoroCustomRulesUiState())
    val state = _state.asStateFlow()

    fun load() {
        if (_state.value.loading && _state.value.defaultRuleSet != null) return
        viewModelScope.launch {
            _state.update { it.copy(loading = true, status = KokoroRulesStatus.IDLE) }
            try {
                val editorData = client.getEditorData()
                val defaultRuleSet = checkNotNull(
                    editorData.state.defaultRuleSet,
                ) { "Kokoro default rule set is unavailable" }
                _state.value = _state.value.copy(
                    loading = false,
                    options = editorData.options,
                    defaultRuleSet = defaultRuleSet,
                    draftRules = defaultRuleSet.rules.map { it.asInput() },
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

    fun save() {
        val selected = _state.value.defaultRuleSet ?: return
        val localRules = _state.value.draftRules
        viewModelScope.launch {
            _state.update { it.copy(saving = true, status = KokoroRulesStatus.IDLE) }
            try {
                // Capabilities can change independently of the installed client.
                val freshOptions = client.getOptions()
                _state.update { it.copy(options = freshOptions) }
                val updated = client.replaceRules(selected.id, selected.revision, localRules, freshOptions)
                replaceDefaultRuleSet(updated, preserveDraft = false)
                _state.update { it.copy(saving = false, dirty = false, status = KokoroRulesStatus.SAVED) }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                handleSaveFailure(error, selected.id, localRules)
            }
        }
    }

    fun useRemoteConflict() {
        val conflict = _state.value.conflict ?: return
        replaceDefaultRuleSet(conflict.remoteSet, preserveDraft = false)
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
        replaceDefaultRuleSet(conflict.remoteSet, preserveDraft = true)
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
        val remote = runCatching {
            client.getState().defaultRuleSet?.takeIf { it.id == setId }
        }.getOrNull()
        if (remote == null) {
            _state.update { it.copy(saving = false, status = KokoroRulesStatus.NOT_FOUND) }
            return
        }
        replaceDefaultRuleSet(remote, preserveDraft = true)
        _state.update {
            it.copy(
                saving = false,
                conflict = KokoroRulesConflict(remote, localRules),
                draftRules = localRules,
                dirty = true,
            )
        }
    }

    private fun replaceDefaultRuleSet(updated: KokoroRuleSet, preserveDraft: Boolean) {
        check(updated.name == DEFAULT_RULE_SET_NAME) { "Unexpected Kokoro rule set" }
        _state.update { current ->
            current.copy(
                defaultRuleSet = updated,
                draftRules = if (preserveDraft) current.draftRules else updated.rules.map { it.asInput() },
            )
        }
    }

    private companion object {
        const val DEFAULT_RULE_SET_NAME = "default"
    }
}
