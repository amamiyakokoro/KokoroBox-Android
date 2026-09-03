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
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroApi
import com.github.yumelira.yumebox.data.integration.kokoro.KokoroSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Request
import java.io.IOException

internal object AmamiyaApi {
    const val API_BASE_URL = KokoroApi.API_BASE_URL
    const val APP_REDIRECT_URI = KokoroApi.APP_REDIRECT_URI
    const val LOGIN_URL = KokoroApi.LOGIN_URL
    const val TOKEN_URL = KokoroApi.TOKEN_URL
    const val ME_URL = KokoroApi.ME_URL
    const val REVOKE_URL = KokoroApi.REVOKE_URL
    const val OPTIONS_URL = KokoroApi.SUBSCRIPTION_OPTIONS_URL
    const val CONFIG_URL = KokoroApi.SUBSCRIPTION_CONFIG_URL

    fun isManagedConfigUrl(source: String): Boolean = KokoroApi.isManagedSubscriptionUrl(source)

    fun buildConfigUrl(settings: MihomoSubscriptionSettings): String {
        val normalizedMode = if (settings.protocol == "vmess") "relay" else settings.mode
        val update = if (settings.subscriptionAutoUpdate) {
            settings.updateIntervalHours.coerceAtLeast(1).toString()
        } else {
            "off"
        }
        return Uri.parse(CONFIG_URL).buildUpon()
            .appendQueryParameter("protocol", settings.protocol)
            .appendQueryParameter("plan", settings.plan)
            .apply {
                settings.isp.takeIf(String::isNotBlank)?.let { appendQueryParameter("isp", it) }
            }
            .appendQueryParameter("rule", settings.ruleSource)
            .appendQueryParameter("mode", normalizedMode)
            .appendQueryParameter("match", if (settings.finalRoute == "direct") "direct" else "none")
            .appendQueryParameter(
                "rule_update",
                if (settings.ruleProviderAutoUpdate) "enable" else "disable",
            )
            .appendQueryParameter("update", update)
            .build()
            .toString()
    }

    fun parseConfigSettings(source: String): MihomoSubscriptionSettings? {
        if (!isManagedConfigUrl(source)) return null
        val uri = Uri.parse(source)
        val protocol = uri.getQueryParameter("protocol")
            ?.takeIf { it in SUPPORTED_PROTOCOLS }
            ?: "vmess"
        val rawUpdate = uri.getQueryParameter("update") ?: "1"
        return MihomoSubscriptionSettings(
            protocol = protocol,
            plan = uri.getQueryParameter("plan").orEmpty(),
            isp = uri.getQueryParameter("isp").orEmpty().takeIf { it in SUPPORTED_ISPS }.orEmpty(),
            mode = if (protocol != "vmess" && uri.getQueryParameter("mode") == "direct") {
                "direct"
            } else {
                "relay"
            },
            ruleSource = if (uri.getQueryParameter("rule") == "mirror") "mirror" else "origin",
            finalRoute = if (uri.getQueryParameter("match") == "direct") "direct" else "proxy",
            ruleProviderAutoUpdate = uri.getQueryParameter("rule_update") != "disable",
            subscriptionAutoUpdate = rawUpdate != "off",
            updateIntervalHours = rawUpdate.toIntOrNull()?.takeIf { it > 0 } ?: 1,
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

internal typealias AmamiyaConfigOptions = MihomoSubscriptionSettings

internal data class AmamiyaAccount(
    val displayName: String?,
    val subscriptions: List<AmamiyaSubscription>,
)

internal data class AmamiyaSubscription(
    val plan: String,
    val description: String?,
    val supportedIsps: List<String>,
    val usedBytes: Long?,
    val totalBytes: Long?,
    val expiresAt: String?,
)

internal data class AmamiyaSubscriptionOptions(
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
        val supportedPlanIsps = plans.firstOrNull { it.name == plan }?.supportedIsps.orEmpty()
        val isp = settings.isp.takeIf { candidate ->
            isps.any { it.value == candidate } &&
                (candidate.isBlank() || supportedPlanIsps.isEmpty() || candidate in supportedPlanIsps)
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
        fun fallback(account: AmamiyaAccount? = null): AmamiyaSubscriptionOptions {
            val plans = account?.subscriptions.orEmpty().map {
                PlanOption(it.plan, it.description, it.supportedIsps)
            }
            return AmamiyaSubscriptionOptions(
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

internal sealed interface AmamiyaAuthState {
    data object Checking : AmamiyaAuthState
    data object LoggedOut : AmamiyaAuthState
    data class Authenticated(val account: AmamiyaAccount) : AmamiyaAuthState
    data class Error(val message: String) : AmamiyaAuthState
}

class AmamiyaAccountClient(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val session = KokoroSession(context)

    internal fun beginLogin(): String = session.beginLogin()

    internal suspend fun handleOAuthCallback(uri: Uri) = session.handleOAuthCallback(uri)

    internal suspend fun getAccount(): AmamiyaAccount? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(AmamiyaApi.ME_URL)
            .header("Accept", "application/json")
            .build()
        session.executeAuthorized(request).use { response ->
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
        account: AmamiyaAccount? = null,
    ): AmamiyaSubscriptionOptions = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(AmamiyaApi.OPTIONS_URL)
            .header("Accept", "application/json")
            .build()
        session.executeAuthorized(request).use { response ->
            if (response.code !in 200..299) {
                throw IOException("Subscription options returned HTTP ${response.code}")
            }
            val payload = json.decodeFromString<OptionsResponse>(response.body.string())
            require(payload.format.equals("mihomo", ignoreCase = true)) { "Unsupported subscription format" }
            payload.toUiOptions(account)
        }
    }

    internal suspend fun revoke() = session.revoke()

    private fun parseAccount(rawJson: String): AmamiyaAccount {
        val response = json.decodeFromString<MeResponse>(rawJson)
        val detailByPlan = response.planDetails.associateBy(PlanDetails::name)
        val plans = response.plans.ifEmpty { response.planDetails.map(PlanDetails::name) }
        return AmamiyaAccount(
            displayName = response.username,
            subscriptions = plans.distinct().map { plan ->
                val detail = detailByPlan[plan]
                AmamiyaSubscription(
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
}

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
    val format: String,
    val protocols: List<ProtocolOptionResponse> = emptyList(),
    val plans: List<PlanOptionResponse> = emptyList(),
    val isps: List<IspOptionResponse> = emptyList(),
    @SerialName("rule_sources") val ruleSources: List<String> = emptyList(),
    @SerialName("final_routes") val finalRoutes: List<String> = emptyList(),
    @SerialName("update_interval") val updateInterval: UpdateIntervalResponse = UpdateIntervalResponse(),
    val defaults: DefaultsResponse = DefaultsResponse(),
) {
    fun toUiOptions(account: AmamiyaAccount? = null): AmamiyaSubscriptionOptions {
        val fallback = AmamiyaSubscriptionOptions.fallback(account)
        val resolvedProtocols = protocols.map {
            AmamiyaSubscriptionOptions.ProtocolOption(it.value, it.label, it.supportsDirect)
        }.ifEmpty { fallback.protocols }
        val resolvedPlans = plans.map {
            AmamiyaSubscriptionOptions.PlanOption(it.name, it.description, it.supportedIsps)
        }.ifEmpty { fallback.plans }
        val resolvedIsps = isps.map {
            AmamiyaSubscriptionOptions.IspOption(it.value, it.label)
        }.ifEmpty { fallback.isps }
        val minHours = updateInterval.minHours.coerceAtLeast(1)
        val maxHours = updateInterval.maxHours.coerceAtLeast(minHours)
        return AmamiyaSubscriptionOptions(
            protocols = resolvedProtocols,
            plans = resolvedPlans,
            isps = resolvedIsps,
            ruleSources = ruleSources.ifEmpty { fallback.ruleSources },
            finalRoutes = finalRoutes.ifEmpty { fallback.finalRoutes },
            minUpdateHours = minHours,
            maxUpdateHours = maxHours,
            defaults = MihomoSubscriptionSettings(
                protocol = defaults.protocol,
                plan = resolvedPlans.firstOrNull()?.name.orEmpty(),
                mode = defaults.mode,
                ruleSource = defaults.ruleSource,
                finalRoute = defaults.finalRoute,
                ruleProviderAutoUpdate = defaults.ruleProviderAutoUpdate,
                subscriptionAutoUpdate = defaults.subscriptionAutoUpdate,
                updateIntervalHours = defaults.updateIntervalHours.coerceIn(minHours, maxHours),
            ),
        )
    }
}

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
    val protocol: String = "vmess",
    val mode: String = "relay",
    @SerialName("rule_source") val ruleSource: String = "origin",
    @SerialName("final_route") val finalRoute: String = "proxy",
    @SerialName("rule_provider_auto_update") val ruleProviderAutoUpdate: Boolean = true,
    @SerialName("subscription_auto_update") val subscriptionAutoUpdate: Boolean = true,
    @SerialName("update_interval_hours") val updateIntervalHours: Int = 1,
)
