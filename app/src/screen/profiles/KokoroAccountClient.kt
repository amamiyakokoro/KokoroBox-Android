/*
 * This file is part of YumeBox.
 *
 * YumeBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 */

package com.github.yumelira.yumebox.screen.profiles

import android.content.Context
import android.net.Uri
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroApi as KokoroBackendApi
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroAuthenticationRequiredException
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

internal object KokoroApi {
    const val API_BASE_URL = KokoroBackendApi.API_BASE_URL
    const val APP_REDIRECT_URI = KokoroBackendApi.APP_REDIRECT_URI
    const val LOGIN_URL = KokoroBackendApi.LOGIN_URL
    const val TOKEN_URL = KokoroBackendApi.TOKEN_URL
    const val ME_URL = KokoroBackendApi.ME_URL
    const val REVOKE_URL = KokoroBackendApi.REVOKE_URL
    const val OPTIONS_URL = KokoroBackendApi.SUBSCRIPTION_OPTIONS_URL
    const val RESOLVE_URL = KokoroBackendApi.SUBSCRIPTION_RESOLVE_URL
    const val CONFIG_URL = KokoroBackendApi.SUBSCRIPTION_CONFIG_URL

    fun isManagedConfigUrl(source: String): Boolean = KokoroBackendApi.isManagedSubscriptionUrl(source)

    fun buildConfigUrl(settings: MihomoSubscriptionSettings): String {
        val normalizedMode = if (settings.protocol == "vmess") "relay" else settings.mode
        return Uri.parse(CONFIG_URL).buildUpon()
            .appendQueryParameter("format", "mihomo")
            .appendQueryParameter("protocol", settings.protocol)
            .appendQueryParameter("plan", settings.plan)
            .apply {
                settings.isp.takeIf(String::isNotBlank)?.let { appendQueryParameter("isp", it) }
            }
            .appendQueryParameter("rule_source", settings.ruleSource)
            .appendQueryParameter("mode", normalizedMode)
            .appendQueryParameter("final_route", settings.finalRoute)
            .appendQueryParameter("rule_provider_auto_update", settings.ruleProviderAutoUpdate.toString())
            .appendQueryParameter("profile_update", settings.subscriptionAutoUpdate.toString())
            .appendQueryParameter("profile_update_hours", settings.updateIntervalHours.coerceIn(1, 720).toString())
            .build()
            .toString()
    }

    fun parseConfigSettings(source: String): MihomoSubscriptionSettings? {
        if (!isManagedConfigUrl(source)) return null
        val uri = Uri.parse(source)
        val protocol = uri.getQueryParameter("protocol")
            ?.takeIf { it in SUPPORTED_PROTOCOLS }
            ?: "vmess"
        val legacyUpdate = uri.getQueryParameter("update")
        val profileAutoUpdate = uri.getQueryParameter("profile_update")
            ?.toBooleanStrictOrNull()
            ?: (legacyUpdate != "off")
        val profileUpdateHours = uri.getQueryParameter("profile_update_hours")
            ?.toIntOrNull()
            ?: legacyUpdate?.toIntOrNull()
            ?: 1
        return MihomoSubscriptionSettings(
            protocol = protocol,
            plan = uri.getQueryParameter("plan").orEmpty(),
            isp = uri.getQueryParameter("isp").orEmpty().takeIf { it in SUPPORTED_ISPS }.orEmpty(),
            mode = if (protocol != "vmess" && uri.getQueryParameter("mode") == "direct") {
                "direct"
            } else {
                "relay"
            },
            ruleSource = if (
                (uri.getQueryParameter("rule_source") ?: uri.getQueryParameter("rule")) == "mirror"
            ) {
                "mirror"
            } else {
                "origin"
            },
            finalRoute = when {
                uri.getQueryParameter("final_route") == "direct" -> "direct"
                uri.getQueryParameter("match") == "direct" -> "direct"
                else -> "proxy"
            },
            ruleProviderAutoUpdate = uri.getQueryParameter("rule_provider_auto_update")
                ?.toBooleanStrictOrNull()
                ?: (uri.getQueryParameter("rule_update") != "disable"),
            subscriptionAutoUpdate = profileAutoUpdate,
            updateIntervalHours = profileUpdateHours.coerceIn(1, 720),
        )
    }

    fun intervalMillis(settings: MihomoSubscriptionSettings): Long =
        if (settings.subscriptionAutoUpdate) {
            settings.updateIntervalHours.coerceAtLeast(1) * 60L * 60L * 1_000L
        } else {
            0L
        }

    private val SUPPORTED_PROTOCOLS = setOf("vmess", "anytls", "hysteria2")
    private val SUPPORTED_ISPS = setOf("", "ct", "cu", "cm", "other")

    fun supportsProtocol(value: String): Boolean = value in SUPPORTED_PROTOCOLS
}

internal data class MihomoSubscriptionSettings(
    val protocol: String = "vmess",
    val plan: String = "",
    val isp: String = "",
    val mode: String = "relay",
    val ruleSource: String = "origin",
    val finalRoute: String = "proxy",
    val ruleProviderAutoUpdate: Boolean = true,
    val subscriptionAutoUpdate: Boolean = true,
    val updateIntervalHours: Int = 1,
)

internal typealias KokoroConfigOptions = MihomoSubscriptionSettings

internal data class KokoroAccount(
    val displayName: String?,
    val subscriptions: List<KokoroSubscription>,
)

internal data class KokoroSubscription(
    val plan: String,
    val description: String?,
    val supportedIsps: List<String>,
    val usedBytes: Long?,
    val totalBytes: Long?,
    val expiresAt: String?,
)

internal data class KokoroSubscriptionOptions(
    val protocols: List<ProtocolOption>,
    val plans: List<PlanOption>,
    val isps: List<IspOption>,
    val ruleSources: List<String>,
    val finalRoutes: List<String>,
    val minUpdateHours: Int,
    val maxUpdateHours: Int,
    val defaults: MihomoSubscriptionSettings,
) {
    data class ProtocolOption(
        val value: String,
        val label: String,
        val supportsDirect: Boolean,
    )

    data class PlanOption(
        val name: String,
        val description: String?,
        val supportedIsps: List<String>,
    )

    data class IspOption(val value: String, val label: String)

    fun normalize(settings: MihomoSubscriptionSettings): MihomoSubscriptionSettings {
        val protocol = settings.protocol.takeIf { candidate -> protocols.any { it.value == candidate } }
            ?: defaults.protocol
        val plan = settings.plan.takeIf { candidate -> plans.isEmpty() || plans.any { it.name == candidate } }
            ?: defaults.plan.takeIf(String::isNotBlank)
            ?: plans.firstOrNull()?.name.orEmpty()
        val supportedPlanIsps = plans.firstOrNull { it.name == plan }
            ?.supportedIsps
            .orEmpty()
            .filterNot { it == "all" }
        val isp = settings.isp.takeIf { candidate ->
            isps.any { it.value == candidate } &&
                (candidate.isBlank() || candidate in supportedPlanIsps)
        }.orEmpty()
        val supportsDirect = protocols.firstOrNull { it.value == protocol }?.supportsDirect == true
        return settings.copy(
            protocol = protocol,
            plan = plan,
            isp = isp,
            mode = if (supportsDirect && settings.mode == "direct") "direct" else "relay",
            ruleSource = settings.ruleSource.takeIf(ruleSources::contains) ?: defaults.ruleSource,
            finalRoute = settings.finalRoute.takeIf(finalRoutes::contains) ?: defaults.finalRoute,
            updateIntervalHours = settings.updateIntervalHours.coerceIn(minUpdateHours, maxUpdateHours),
        )
    }

    companion object {
        fun fallback(account: KokoroAccount? = null): KokoroSubscriptionOptions {
            val plans = account?.subscriptions.orEmpty().map {
                PlanOption(it.plan, it.description, it.supportedIsps)
            }
            return KokoroSubscriptionOptions(
                protocols = listOf(
                    ProtocolOption("vmess", "VMess", false),
                    ProtocolOption("anytls", "AnyTLS", true),
                    ProtocolOption("hysteria2", "Hysteria 2", true),
                ),
                plans = plans,
                isps = listOf(
                    IspOption("", "Default"),
                    IspOption("ct", "China Telecom"),
                    IspOption("cu", "China Unicom"),
                    IspOption("cm", "China Mobile"),
                    IspOption("other", "Other"),
                ),
                ruleSources = listOf("origin", "mirror"),
                finalRoutes = listOf("proxy", "direct"),
                minUpdateHours = 1,
                maxUpdateHours = 720,
                defaults = MihomoSubscriptionSettings(plan = plans.firstOrNull()?.name.orEmpty()),
            )
        }
    }
}

internal sealed interface KokoroAuthState {
    data object Checking : KokoroAuthState
    data object LoggedOut : KokoroAuthState
    data class Authenticated(val account: KokoroAccount) : KokoroAuthState
    data class Error(val message: String) : KokoroAuthState
}

class KokoroAccountClient(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val session = KokoroSession(context)

    internal fun beginLogin(): String = session.beginLogin()

    internal suspend fun handleOAuthCallback(uri: Uri) = session.handleOAuthCallback(uri)

    internal suspend fun getAccount(): KokoroAccount? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(KokoroApi.ME_URL)
            .header("Accept", "application/json")
            .build()
        val response = try {
            session.executeAuthorized(request)
        } catch (_: KokoroAuthenticationRequiredException) {
            return@withContext null
        }
        response.use {
            when (response.code) {
                401, 403 -> {
                    session.clearTokens()
                    null
                }

                in 200..299 -> parseAccount(response.body.string())
                else -> throw IOException("amamiyakoko.ro returned HTTP ${response.code}")
            }
        }
    }

    internal suspend fun getSubscriptionOptions(
        account: KokoroAccount? = null,
    ): KokoroSubscriptionOptions = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(KokoroApi.OPTIONS_URL)
            .header("Accept", "application/json")
            .build()
        session.executeAuthorized(request).use { response ->
            if (response.code !in 200..299) {
                throw IOException("Subscription options returned HTTP ${response.code}")
            }
            val payload = json.decodeFromString<OptionsResponse>(response.body.string())
            require(payload.formats.any { it.value == "mihomo" && !it.testing }) {
                "Mihomo subscriptions are unavailable"
            }
            payload.toUiOptions(account)
        }
    }

    internal suspend fun resolveSubscription(
        settings: MihomoSubscriptionSettings,
    ): ResolvedSubscription = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(KokoroApi.RESOLVE_URL)
            .header("Accept", "application/json")
            .post(
                json.encodeToString(SubscriptionSettingsRequest.from(settings))
                    .toRequestBody(JSON_MEDIA_TYPE),
            )
            .build()
        session.executeAuthorized(request).use { response ->
            if (!response.isSuccessful) {
                throw IOException("Kokoro rejected subscription settings (HTTP ${response.code})")
            }
            val payload = json.decodeFromString<ResolvedSubscriptionResponse>(response.body.string())
            require(payload.format == "mihomo") { "Kokoro returned an unsupported subscription format" }
            require(payload.contentType.substringBefore(';').trim() == "text/yaml") {
                "Kokoro returned an unexpected subscription content type"
            }
            require(KokoroBackendApi.isAuthenticatedSubscriptionUrl(payload.authenticatedConfigUrl)) {
                "Kokoro returned an untrusted subscription URL"
            }
            ResolvedSubscription(
                profileName = payload.profileName,
                authenticatedConfigUrl = payload.authenticatedConfigUrl,
            )
        }
    }

    internal suspend fun revoke() = session.revoke()

    private fun parseAccount(rawJson: String): KokoroAccount {
        val response = json.decodeFromString<MeResponse>(rawJson)
        val detailByPlan = response.planDetails.associateBy(PlanDetails::name)
        val plans = response.plans.ifEmpty { response.planDetails.map(PlanDetails::name) }
        return KokoroAccount(
            displayName = response.username,
            subscriptions = plans.distinct().map { plan ->
                val detail = detailByPlan[plan]
                KokoroSubscription(
                    plan = plan,
                    description = detail?.description,
                    supportedIsps = detail?.supportedIsps.orEmpty(),
                    usedBytes = response.trafficUsage,
                    totalBytes = response.bandwidthLimit,
                    expiresAt = response.subscriptionExpiresAt,
                )
            },
        )
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

internal data class ResolvedSubscription(
    val profileName: String,
    val authenticatedConfigUrl: String,
)

@Serializable
private data class MeResponse(
    val username: String? = null,
    val plans: List<String> = emptyList(),
    @SerialName("plans_details") val planDetails: List<PlanDetails> = emptyList(),
    @SerialName("traffic_usage") val trafficUsage: Long? = null,
    @SerialName("bandwidth_limit") val bandwidthLimit: Long? = null,
    @SerialName("subscription_expires_at") val subscriptionExpiresAt: String? = null,
)

@Serializable
private data class PlanDetails(
    val name: String,
    val description: String? = null,
    @SerialName("supported_isps") val supportedIsps: List<String> = emptyList(),
)

@Serializable
private data class OptionsResponse(
    val formats: List<FormatOptionResponse> = emptyList(),
    val protocols: List<ProtocolOptionResponse> = emptyList(),
    val plans: List<PlanOptionResponse> = emptyList(),
    val isps: List<IspOptionResponse> = emptyList(),
    @SerialName("rule_sources") val ruleSources: List<String> = emptyList(),
    @SerialName("final_routes") val finalRoutes: List<String> = emptyList(),
    @SerialName("profile_update") val profileUpdate: UpdateIntervalResponse = UpdateIntervalResponse(),
    val defaults: DefaultsResponse = DefaultsResponse(),
) {
    fun toUiOptions(account: KokoroAccount? = null): KokoroSubscriptionOptions {
        val fallback = KokoroSubscriptionOptions.fallback(account)
        val resolvedProtocols = protocols.filter { KokoroApi.supportsProtocol(it.value) }.map {
            KokoroSubscriptionOptions.ProtocolOption(it.value, it.label, it.supportsDirect)
        }.ifEmpty { fallback.protocols }
        val resolvedPlans = plans.map {
            KokoroSubscriptionOptions.PlanOption(it.name, it.description, it.supportedIsps)
        }.ifEmpty { fallback.plans }
        val resolvedIsps = isps.map {
            KokoroSubscriptionOptions.IspOption(it.value, it.label)
        }.ifEmpty { fallback.isps }
        val minHours = profileUpdate.minHours.coerceAtLeast(1)
        val maxHours = profileUpdate.maxHours.coerceAtLeast(minHours)
        val defaultPlan = defaults.plan
            ?.takeIf { candidate -> resolvedPlans.any { it.name == candidate } }
            ?: resolvedPlans.firstOrNull()?.name.orEmpty()
        return KokoroSubscriptionOptions(
            protocols = resolvedProtocols,
            plans = resolvedPlans,
            isps = resolvedIsps,
            ruleSources = ruleSources.ifEmpty { fallback.ruleSources },
            finalRoutes = finalRoutes.ifEmpty { fallback.finalRoutes },
            minUpdateHours = minHours,
            maxUpdateHours = maxHours,
            defaults = MihomoSubscriptionSettings(
                protocol = defaults.protocol,
                plan = defaultPlan,
                isp = defaults.isp.orEmpty(),
                mode = defaults.mode,
                ruleSource = defaults.ruleSource,
                finalRoute = defaults.finalRoute,
                ruleProviderAutoUpdate = defaults.ruleProviderAutoUpdate,
                subscriptionAutoUpdate = defaults.profileAutoUpdate,
                updateIntervalHours = defaults.profileUpdateHours.coerceIn(minHours, maxHours),
            ),
        )
    }
}

@Serializable
private data class FormatOptionResponse(
    val value: String,
    @SerialName("content_type") val contentType: String,
    val filename: String,
    val testing: Boolean = false,
)

@Serializable
private data class ProtocolOptionResponse(
    val value: String,
    val label: String,
    @SerialName("supports_direct") val supportsDirect: Boolean = false,
)

@Serializable
private data class PlanOptionResponse(
    val name: String,
    val description: String? = null,
    @SerialName("supported_isps") val supportedIsps: List<String> = emptyList(),
)

@Serializable
private data class IspOptionResponse(val value: String, val label: String)

@Serializable
private data class UpdateIntervalResponse(
    @SerialName("min_hours") val minHours: Int = 1,
    @SerialName("max_hours") val maxHours: Int = 720,
    @SerialName("default_hours") val defaultHours: Int = 1,
)

@Serializable
private data class DefaultsResponse(
    val format: String = "mihomo",
    val protocol: String = "vmess",
    val plan: String? = null,
    val isp: String? = null,
    val mode: String = "relay",
    @SerialName("rule_source") val ruleSource: String = "origin",
    @SerialName("final_route") val finalRoute: String = "proxy",
    @SerialName("rule_provider_auto_update") val ruleProviderAutoUpdate: Boolean = true,
    @SerialName("profile_auto_update") val profileAutoUpdate: Boolean = true,
    @SerialName("profile_update_hours") val profileUpdateHours: Int = 1,
)

@Serializable
private data class SubscriptionSettingsRequest(
    val format: String = "mihomo",
    val protocol: String,
    val plan: String,
    val isp: String? = null,
    val mode: String,
    @SerialName("rule_source") val ruleSource: String,
    @SerialName("final_route") val finalRoute: String,
    @SerialName("rule_provider_auto_update") val ruleProviderAutoUpdate: Boolean,
    @SerialName("profile_auto_update") val profileAutoUpdate: Boolean,
    @SerialName("profile_update_hours") val profileUpdateHours: Int,
) {
    companion object {
        fun from(settings: MihomoSubscriptionSettings) = SubscriptionSettingsRequest(
            protocol = settings.protocol,
            plan = settings.plan,
            isp = settings.isp.ifBlank { null },
            mode = if (settings.protocol == "vmess") "relay" else settings.mode,
            ruleSource = settings.ruleSource,
            finalRoute = settings.finalRoute,
            ruleProviderAutoUpdate = settings.ruleProviderAutoUpdate,
            profileAutoUpdate = settings.subscriptionAutoUpdate,
            profileUpdateHours = settings.updateIntervalHours.coerceIn(1, 720),
        )
    }
}

@Serializable
private data class ResolvedSubscriptionResponse(
    val format: String,
    @SerialName("content_type") val contentType: String,
    @SerialName("profile_name") val profileName: String,
    @SerialName("authenticated_config_url") val authenticatedConfigUrl: String,
)
