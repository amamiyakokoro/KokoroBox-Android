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
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal object AmamiyaApi {
    const val API_BASE_URL = "https://amamiyakoko.ro/api"
    const val APP_REDIRECT_URI = "kokoro://oauth/callback"
    const val LOGIN_URL = "$API_BASE_URL/app/auth/login"
    const val TOKEN_URL = "$API_BASE_URL/app/auth/token"
    const val ME_URL = "$API_BASE_URL/app/me"
    const val REVOKE_URL = "$API_BASE_URL/app/auth/revoke"
    const val CONFIG_URL = "$API_BASE_URL/config"

    fun loginUrl(state: String): String = Uri.parse(LOGIN_URL).buildUpon()
        .appendQueryParameter("redirect_uri", APP_REDIRECT_URI)
        .appendQueryParameter("state", state)
        .build()
        .toString()

    fun buildConfigUrl(proxyUuid: String, plan: String?, options: AmamiyaConfigOptions): String {
        require(proxyUuid.isNotBlank()) { "proxy_uuid is required" }
        return Uri.parse(CONFIG_URL).buildUpon()
            .appendQueryParameter("uuid", proxyUuid)
            .apply {
                plan?.takeIf(String::isNotBlank)?.let { appendQueryParameter("plan", it) }
                options.isp?.let { appendQueryParameter("isp", it) }
            }
            .appendQueryParameter("protocol", options.protocol)
            .appendQueryParameter("client", "meta")
            .appendQueryParameter("rule", options.rule)
            .appendQueryParameter("mode", options.mode)
            .appendQueryParameter("match", options.match)
            .appendQueryParameter("rule_update", options.ruleUpdate)
            .appendQueryParameter("update", options.update)
            .build()
            .toString()
    }
}

internal data class AmamiyaConfigOptions(
    val protocol: String = "vmess",
    val isp: String? = null,
    val mode: String = "relay",
    val rule: String = "origin",
    val match: String = "none",
    val ruleUpdate: String = "enable",
    val update: String = "on",
)

internal data class AmamiyaAccount(
    val displayName: String?,
    val subscriptions: List<AmamiyaSubscription>,
)

internal data class AmamiyaSubscription(
    val proxyUuid: String,
    val plan: String?,
    val supportedIsps: List<String>,
    val usedBytes: Long?,
    val totalBytes: Long?,
    val expiresAt: String?,
)

internal sealed interface AmamiyaAuthState {
    data object Checking : AmamiyaAuthState
    data object LoggedOut : AmamiyaAuthState
    data class Authenticated(val account: AmamiyaAccount) : AmamiyaAuthState
    data class Error(val message: String) : AmamiyaAuthState
}

class AmamiyaAccountClient(context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = OkHttpClient()
    private val tokenStore = AmamiyaKeystoreTokenStore(context.applicationContext, json)
    private val refreshMutex = Mutex()
    private val secureRandom = SecureRandom()

    internal fun beginLogin(): String {
        val stateBytes = ByteArray(32).also(secureRandom::nextBytes)
        val state = Base64.encodeToString(stateBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        tokenStore.update { it.copy(pendingState = state, pendingStateCreatedAt = System.currentTimeMillis()) }
        return AmamiyaApi.loginUrl(state)
    }

    internal suspend fun handleOAuthCallback(uri: Uri) = withContext(Dispatchers.IO) {
        require(uri.scheme == "kokoro" && uri.host == "oauth" && uri.path == "/callback") {
            "Invalid OAuth callback"
        }
        val stored = tokenStore.load()
        val expectedState = stored.pendingState ?: throw IOException("Missing OAuth state")
        val receivedState = uri.getQueryParameter("state") ?: ""
        val validState = MessageDigest.isEqual(
            expectedState.toByteArray(Charsets.UTF_8),
            receivedState.toByteArray(Charsets.UTF_8),
        )
        val stateFresh = System.currentTimeMillis() - stored.pendingStateCreatedAt <= OAUTH_STATE_MAX_AGE_MS
        tokenStore.update { it.copy(pendingState = null, pendingStateCreatedAt = 0L) }
        if (!validState || !stateFresh) throw IOException("OAuth state validation failed")
        uri.getQueryParameter("error")?.let { throw IOException("OAuth authorization was cancelled") }
        val code = uri.getQueryParameter("code")?.takeIf(String::isNotBlank)
            ?: throw IOException("Missing authorization code")

        val response = requestToken(
            TokenRequest(
                grantType = "authorization_code",
                code = code,
                redirectUri = AmamiyaApi.APP_REDIRECT_URI,
            ),
        )
        tokenStore.replaceTokens(response.toStoredCredentials())
    }

    internal suspend fun getAccount(): AmamiyaAccount? = withContext(Dispatchers.IO) {
        val initialToken = validAccessToken() ?: return@withContext null
        val first = executeMe(initialToken)
        val response = if (first.code == 401) {
            first.close()
            val refreshed = validAccessToken(rejectedAccessToken = initialToken, forceRefresh = true)
                ?: return@withContext null
            executeMe(refreshed)
        } else {
            first
        }

        response.use {
            when (it.code) {
                401, 403 -> {
                    tokenStore.clearTokens()
                    null
                }

                in 200..299 -> parseAccount(it.body.string())
                else -> throw IOException("amamiyakoko.ro returned HTTP ${it.code}")
            }
        }
    }

    internal suspend fun revoke() = withContext(Dispatchers.IO) {
        val accessToken = tokenStore.load().accessToken
        try {
            if (!accessToken.isNullOrBlank()) {
                val request = Request.Builder()
                    .url(AmamiyaApi.REVOKE_URL)
                    .header("Authorization", "Bearer $accessToken")
                    .post(ByteArray(0).toRequestBody(null))
                    .build()
                runCatching { httpClient.newCall(request).execute().close() }
            }
        } finally {
            tokenStore.clearTokens()
        }
    }

    private fun executeMe(accessToken: String) = httpClient.newCall(
        Request.Builder()
            .url(AmamiyaApi.ME_URL)
            .header("Accept", "application/json")
            .header("Authorization", "Bearer $accessToken")
            .build(),
    ).execute()

    private suspend fun validAccessToken(
        rejectedAccessToken: String? = null,
        forceRefresh: Boolean = false,
    ): String? = refreshMutex.withLock {
        val current = tokenStore.load()
        if (rejectedAccessToken != null && current.accessToken != rejectedAccessToken) {
            return@withLock current.accessToken
        }
        val stillValid = current.accessToken != null &&
            current.accessTokenExpiresAt > System.currentTimeMillis() + ACCESS_TOKEN_REFRESH_MARGIN_MS
        if (!forceRefresh && stillValid) return@withLock current.accessToken
        val refreshToken = current.refreshToken ?: return@withLock current.accessToken?.takeIf { stillValid }
        if (current.refreshTokenExpiresAt <= System.currentTimeMillis()) {
            tokenStore.clearTokens()
            return@withLock null
        }

        try {
            val response = requestToken(TokenRequest(grantType = "refresh_token", refreshToken = refreshToken))
            val replacement = response.toStoredCredentials()
            tokenStore.replaceTokens(replacement)
            replacement.accessToken
        } catch (e: Exception) {
            tokenStore.clearTokens()
            throw e
        }
    }

    private fun requestToken(payload: TokenRequest): TokenResponse {
        val request = Request.Builder()
            .url(AmamiyaApi.TOKEN_URL)
            .header("Accept", "application/json")
            .post(json.encodeToString(payload).toRequestBody(JSON_MEDIA_TYPE))
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (response.code !in 200..299) {
                throw IOException("Token exchange failed with HTTP ${response.code}")
            }
            return json.decodeFromString(response.body.string())
        }
    }

    private fun TokenResponse.toStoredCredentials(): StoredAuthData {
        val now = System.currentTimeMillis()
        return StoredAuthData(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresAt = now + expiresIn.coerceAtLeast(1) * 1_000L,
            refreshTokenExpiresAt = now + refreshExpiresIn.coerceAtLeast(1) * 1_000L,
        )
    }

    private fun parseAccount(rawJson: String): AmamiyaAccount {
        val response = json.decodeFromString<MeResponse>(rawJson)
        val uuid = response.proxyUuid?.takeIf(String::isNotBlank)
        val detailByPlan = response.planDetails.associateBy(PlanDetails::name)
        val plans = response.plans.ifEmpty { listOfNotNull(response.planDetails.firstOrNull()?.name) }
        val selectablePlans: List<String?> = if (plans.isEmpty()) listOf(null) else plans
        val subscriptions = if (uuid == null) {
            emptyList()
        } else {
            selectablePlans.map { plan ->
                AmamiyaSubscription(
                    proxyUuid = uuid,
                    plan = plan,
                    supportedIsps = plan?.let { detailByPlan[it]?.supportedIsps }.orEmpty(),
                    usedBytes = response.trafficUsage,
                    totalBytes = response.bandwidthLimit,
                    expiresAt = response.subscriptionExpiresAt,
                )
            }
        }
        return AmamiyaAccount(response.username, subscriptions)
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        const val ACCESS_TOKEN_REFRESH_MARGIN_MS = 60_000L
        const val OAUTH_STATE_MAX_AGE_MS = 10 * 60_000L
    }
}

@Serializable
private data class TokenRequest(
    @SerialName("grant_type") val grantType: String,
    val code: String? = null,
    @SerialName("redirect_uri") val redirectUri: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
)

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("refresh_expires_in") val refreshExpiresIn: Long,
)

@Serializable
private data class MeResponse(
    val username: String? = null,
    @SerialName("proxy_uuid") val proxyUuid: String? = null,
    val plans: List<String> = emptyList(),
    @SerialName("plans_details") val planDetails: List<PlanDetails> = emptyList(),
    @SerialName("traffic_usage") val trafficUsage: Long? = null,
    @SerialName("bandwidth_limit") val bandwidthLimit: Long? = null,
    @SerialName("subscription_expires_at") val subscriptionExpiresAt: String? = null,
)

@Serializable
private data class PlanDetails(
    val name: String,
    @SerialName("supported_isps") val supportedIsps: List<String> = emptyList(),
)

@Serializable
private data class StoredAuthData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val accessTokenExpiresAt: Long = 0L,
    val refreshTokenExpiresAt: Long = 0L,
    val pendingState: String? = null,
    val pendingStateCreatedAt: Long = 0L,
)

private class AmamiyaKeystoreTokenStore(context: Context, private val json: Json) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Synchronized
    fun load(): StoredAuthData {
        val encoded = preferences.getString(ENCRYPTED_DATA_KEY, null) ?: return StoredAuthData()
        return runCatching {
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            require(combined.size > IV_LENGTH_BYTES)
            val iv = combined.copyOfRange(0, IV_LENGTH_BYTES)
            val ciphertext = combined.copyOfRange(IV_LENGTH_BYTES, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            json.decodeFromString<StoredAuthData>(cipher.doFinal(ciphertext).toString(Charsets.UTF_8))
        }.getOrElse {
            preferences.edit().remove(ENCRYPTED_DATA_KEY).commit()
            StoredAuthData()
        }
    }

    @Synchronized
    fun update(transform: (StoredAuthData) -> StoredAuthData) = save(transform(load()))

    @Synchronized
    fun replaceTokens(replacement: StoredAuthData) {
        val current = load()
        save(replacement.copy(pendingState = current.pendingState, pendingStateCreatedAt = current.pendingStateCreatedAt))
    }

    @Synchronized
    fun clearTokens() {
        val current = load()
        save(StoredAuthData(pendingState = current.pendingState, pendingStateCreatedAt = current.pendingStateCreatedAt))
    }

    private fun save(value: StoredAuthData) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(json.encodeToString(value).toByteArray(Charsets.UTF_8))
        val encoded = Base64.encodeToString(cipher.iv + ciphertext, Base64.NO_WRAP)
        check(preferences.edit().putString(ENCRYPTED_DATA_KEY, encoded).commit()) {
            "Unable to persist encrypted credentials"
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val PREFERENCES_NAME = "amamiya_keystore_credentials"
        const val ENCRYPTED_DATA_KEY = "encrypted_auth_data"
        const val KEY_ALIAS = "yumebox_amamiya_oauth_aes"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH_BYTES = 12
    }
}
