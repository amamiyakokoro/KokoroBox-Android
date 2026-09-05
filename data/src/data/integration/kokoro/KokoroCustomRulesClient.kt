/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.data.integration.kokoro

import android.content.Context
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

private const val CUSTOM_RULES_URL = "${KokoroApi.API_BASE_URL}/app/custom-rules"
private const val CUSTOM_RULES_OPTIONS_URL = "$CUSTOM_RULES_URL/options"
private const val CUSTOM_RULE_SETS_URL = "$CUSTOM_RULES_URL/sets"

class KokoroCustomRulesClient internal constructor(
    private val session: KokoroSession,
) {
    constructor(context: Context) : this(KokoroSession(context.applicationContext))

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun getState(): KokoroCustomRulesState =
        executeJson<KokoroCustomRulesState>(
            Request.Builder().url(CUSTOM_RULES_URL).get().build(),
            expectedCode = 200,
        ).also { requireSupportedSchema(it.schemaVersion) }

    suspend fun getOptions(): KokoroCustomRulesOptions =
        executeJson<KokoroCustomRulesOptions>(
            Request.Builder().url(CUSTOM_RULES_OPTIONS_URL).get().build(),
            expectedCode = 200,
        ).also { requireSupportedSchema(it.schemaVersion) }

    suspend fun getEditorData(): KokoroCustomRulesEditorData = supervisorScope {
        val options = async { getOptions() }
        val state = async { getState() }
        KokoroCustomRulesEditorData(
            options = options.await(),
            state = state.await(),
        )
    }

    suspend fun createSet(name: String): KokoroRuleSet = executeJson(
        request = jsonRequest(
            url = CUSTOM_RULE_SETS_URL.toHttpUrl(),
            method = "POST",
            body = RuleSetCreate(normalizeSetName(name)),
        ),
        expectedCode = 201,
    )

    suspend fun renameSet(setId: Long, name: String, expectedRevision: Int): KokoroRuleSet = executeJson(
        request = jsonRequest(
            url = setUrl(setId),
            method = "PATCH",
            body = RuleSetRename(normalizeSetName(name), requireRevision(expectedRevision)),
        ),
        expectedCode = 200,
    )

    suspend fun deleteSet(setId: Long, expectedRevision: Int) {
        val url = setUrl(setId).newBuilder()
            .addQueryParameter("expected_revision", requireRevision(expectedRevision).toString())
            .build()
        val response = session.executeAuthorized(Request.Builder().url(url).delete().build())
        response.use {
            if (it.code != 204) throw it.toRulesApiException(json)
        }
    }

    suspend fun replaceRules(
        setId: Long,
        expectedRevision: Int,
        rules: List<KokoroCustomRuleInput>,
        options: KokoroCustomRulesOptions,
    ): KokoroRuleSet {
        validateCustomRules(rules, options)
        val normalizedRules = rules.map(KokoroCustomRuleInput::normalized)
        val request = jsonRequest(
            url = setUrl(setId).newBuilder().addPathSegment("rules").build(),
            method = "PUT",
            body = RuleSetReplace(requireRevision(expectedRevision), normalizedRules),
        )
        return try {
            executeJson(request, expectedCode = 200)
        } catch (error: IOException) {
            if (error is KokoroRulesApiException ||
                error is KokoroAuthenticationRequiredException ||
                error is KokoroRulesValidationException
            ) {
                throw error
            }
            val remote = runCatching {
                getState().sets.firstOrNull { it.id == setId }
            }.getOrNull()
            if (remote != null && remote.rules.map(KokoroCustomRule::asInput) == normalizedRules) {
                remote
            } else {
                throw KokoroRulesSaveOutcomeUnknownException(error)
            }
        }
    }

    private suspend inline fun <reified T> executeJson(request: Request, expectedCode: Int): T {
        return session.executeAuthorized(request.newBuilder().header("Accept", "application/json").build()).use {
            if (it.code != expectedCode) throw it.toRulesApiException(json)
            try {
                json.decodeFromString<T>(it.body.string())
            } catch (_: Exception) {
                throw IOException("Invalid Kokoro custom-rules response")
            }
        }
    }

    private inline fun <reified T> jsonRequest(url: HttpUrl, method: String, body: T): Request =
        Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .method(method, json.encodeToString(body).toRequestBody(JSON_MEDIA_TYPE))
            .build()

    private fun setUrl(setId: Long): HttpUrl {
        require(setId > 0L) { "Invalid Kokoro rule-set ID" }
        return CUSTOM_RULE_SETS_URL.toHttpUrl().newBuilder().addPathSegment(setId.toString()).build()
    }

    private fun normalizeSetName(name: String): String = name.trim().also {
        require(it.isNotEmpty() && it.length <= 64) { "Rule-set name must contain 1–64 characters" }
    }

    private fun requireRevision(revision: Int): Int = revision.also {
        require(it >= 1) { "Invalid Kokoro rule-set revision" }
    }

    private fun requireSupportedSchema(schemaVersion: Int) {
        if (schemaVersion != 1) throw IOException("Unsupported Kokoro custom-rules schema")
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

data class KokoroCustomRulesEditorData(
    val options: KokoroCustomRulesOptions,
    val state: KokoroCustomRulesState,
)

@Serializable
data class KokoroCustomRulesState(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    val sets: List<KokoroRuleSet> = emptyList(),
)

@Serializable
data class KokoroCustomRulesOptions(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    @SerialName("rule_types") val ruleTypes: List<String> = emptyList(),
    val targets: List<String> = emptyList(),
    @SerialName("rule_providers") val ruleProviders: List<KokoroRuleProvider> = emptyList(),
    val limits: Map<String, Int> = emptyMap(),
) {
    val maxRuleSets: Int get() = (limits["max_rule_sets"] ?: 5).coerceAtLeast(1)
    val maxRulesPerSet: Int get() = (limits["max_rules_per_set"] ?: 200).coerceAtLeast(0)
    val maxNameLength: Int get() = (limits["max_name_length"] ?: 64).coerceAtLeast(1)
    val maxPayloadLength: Int get() = (limits["max_payload_length"] ?: 1024).coerceAtLeast(1)
}

@Serializable
data class KokoroRuleProvider(val name: String, val behavior: String)

@Serializable
data class KokoroRuleSet(
    val id: Long,
    val name: String,
    val revision: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val rules: List<KokoroCustomRule> = emptyList(),
)

@Serializable
data class KokoroCustomRule(
    val id: Long,
    val type: String,
    val payload: String? = null,
    val target: String,
    val priority: Int,
    @SerialName("updated_at") val updatedAt: String,
) {
    fun asInput(): KokoroCustomRuleInput = KokoroCustomRuleInput(type, payload, target).normalized()
}

@Serializable
data class KokoroCustomRuleInput(
    val type: String,
    val payload: String? = null,
    val target: String,
) {
    fun normalized(): KokoroCustomRuleInput {
        val normalizedType = type.uppercase()
        return copy(
            type = normalizedType,
            payload = if (normalizedType == "MATCH") null else payload,
        )
    }
}

enum class KokoroRulesValidationReason {
    TOO_MANY_RULES,
    UNSUPPORTED_TYPE,
    MISSING_PAYLOAD,
    INVALID_PAYLOAD,
    UNSUPPORTED_TARGET,
    INVALID_TARGET,
    UNSUPPORTED_PROVIDER,
    DUPLICATE_MATCH,
    MATCH_NOT_LAST,
    MATCH_REJECT,
}

class KokoroRulesValidationException(
    val reason: KokoroRulesValidationReason,
    val ruleIndex: Int? = null,
) : IOException("Invalid Kokoro custom rules: $reason")

class KokoroRulesApiException(
    val statusCode: Int,
    val currentRevision: Int? = null,
    val retryAfterSeconds: Long? = null,
) : IOException("Kokoro custom-rules request failed with HTTP $statusCode")

class KokoroRulesSaveOutcomeUnknownException(cause: IOException) :
    IOException("Kokoro custom-rules save outcome is unknown", cause)

fun validateCustomRules(
    rules: List<KokoroCustomRuleInput>,
    options: KokoroCustomRulesOptions,
) {
    if (rules.size > options.maxRulesPerSet) {
        throw KokoroRulesValidationException(KokoroRulesValidationReason.TOO_MANY_RULES)
    }
    var matchIndex: Int? = null
    rules.forEachIndexed { index, rule ->
        val type = rule.type
        val payload = rule.payload
        val target = rule.target
        fun invalid(reason: KokoroRulesValidationReason): Nothing =
            throw KokoroRulesValidationException(reason, index)

        if (type !in options.ruleTypes) invalid(KokoroRulesValidationReason.UNSUPPORTED_TYPE)
        if (target !in options.targets) invalid(KokoroRulesValidationReason.UNSUPPORTED_TARGET)
        if (!target.isSafeRuleToken(128)) invalid(KokoroRulesValidationReason.INVALID_TARGET)
        if (type == "MATCH") {
            if (!payload.isNullOrEmpty()) invalid(KokoroRulesValidationReason.INVALID_PAYLOAD)
            if (target == "REJECT") invalid(KokoroRulesValidationReason.MATCH_REJECT)
            if (matchIndex != null) invalid(KokoroRulesValidationReason.DUPLICATE_MATCH)
            matchIndex = index
        } else {
            if (payload.isNullOrEmpty()) invalid(KokoroRulesValidationReason.MISSING_PAYLOAD)
            if (!payload.isSafeRuleToken(options.maxPayloadLength)) {
                invalid(KokoroRulesValidationReason.INVALID_PAYLOAD)
            }
            if (type == "RULE-SET" && options.ruleProviders.none {
                    it.name == payload && it.behavior == "domain"
                }
            ) {
                invalid(KokoroRulesValidationReason.UNSUPPORTED_PROVIDER)
            }
        }
    }
    if (matchIndex != null && matchIndex != rules.lastIndex) {
        throw KokoroRulesValidationException(KokoroRulesValidationReason.MATCH_NOT_LAST, matchIndex)
    }
}

private fun String.isSafeRuleToken(maxLength: Int): Boolean =
    isNotEmpty() && length <= maxLength && this == trim() && ',' !in this && none(Char::isISOControl)

@Serializable
private data class RuleSetCreate(val name: String)

@Serializable
private data class RuleSetRename(
    val name: String,
    @SerialName("expected_revision") val expectedRevision: Int,
)

@Serializable
private data class RuleSetReplace(
    @SerialName("expected_revision") val expectedRevision: Int,
    val rules: List<KokoroCustomRuleInput>,
)

private fun Response.toRulesApiException(json: Json): KokoroRulesApiException {
    val currentRevision = if (code == 409) {
        runCatching {
            json.parseToJsonElement(body.string()).jsonObject["detail"]
                ?.jsonObject
                ?.get("current_revision")
                ?.jsonPrimitive
                ?.intOrNull
        }.getOrNull()
    } else {
        null
    }
    return KokoroRulesApiException(
        statusCode = code,
        currentRevision = currentRevision,
        retryAfterSeconds = header("Retry-After")?.toLongOrNull(),
    )
}
