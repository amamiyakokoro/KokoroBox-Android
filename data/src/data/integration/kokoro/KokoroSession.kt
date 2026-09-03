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
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KokoroApi {
    const val API_BASE_URL = "https://amamiyakoko.ro/api"
    const val APP_REDIRECT_URI = "kokoro://oauth/callback"
    const val LOGIN_URL = "$API_BASE_URL/app/auth/login"
    const val TOKEN_URL = "$API_BASE_URL/app/auth/token"
    const val ME_URL = "$API_BASE_URL/app/me"
    const val REVOKE_URL = "$API_BASE_URL/app/auth/revoke"
    const val SUBSCRIPTION_OPTIONS_URL = "$API_BASE_URL/app/subscription/options"
    const val SUBSCRIPTION_RESOLVE_URL = "$API_BASE_URL/app/subscription/resolve"
    const val SUBSCRIPTION_CONFIG_URL = "$API_BASE_URL/app/subscription/config"
    const val LEGACY_SUBSCRIPTION_CONFIG_URL = "$API_BASE_URL/config"

    fun loginUrl(state: String): String = Uri.parse(LOGIN_URL).buildUpon()
        .appendQueryParameter("redirect_uri", APP_REDIRECT_URI)
        .appendQueryParameter("state", state)
        .build()
        .toString()

    fun isAuthenticatedSubscriptionUrl(url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals("amamiyakoko.ro", ignoreCase = true) &&
            uri.path == "/api/app/subscription/config"
    }

    fun isAuthorizedApiUrl(url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals("amamiyakoko.ro", ignoreCase = true) &&
            uri.path in AUTHORIZED_API_PATHS
    }

    fun isLegacySubscriptionUrl(url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        return uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals("amamiyakoko.ro", ignoreCase = true) &&
            uri.path == "/api/config" &&
            !uri.getQueryParameter("uuid").isNullOrBlank()
    }

    fun isManagedSubscriptionUrl(url: String): Boolean =
        isAuthenticatedSubscriptionUrl(url) || isLegacySubscriptionUrl(url)

    private val AUTHORIZED_API_PATHS = setOf(
        "/api/app/me",
        "/api/app/subscription/options",
        "/api/app/subscription/resolve",
        "/api/app/subscription/config",
    )
}

/**
 * Owns the OAuth session shared by the Compose UI and the profile download service.
 * Tokens are never exposed through profile URLs or logs.
 */
class KokoroSession(context: Context) {
    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }
    private val httpClient = OkHttpClient()
    private val tokenStore = KokoroKeystoreTokenStore(appContext, json)
    private val secureRandom = SecureRandom()

    fun beginLogin(): String {
        val stateBytes = ByteArray(32).also(secureRandom::nextBytes)
        val state = Base64.encodeToString(stateBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        tokenStore.update { it.copy(pendingState = state, pendingStateCreatedAt = System.currentTimeMillis()) }
        return KokoroApi.loginUrl(state)
    }

    suspend fun handleOAuthCallback(uri: Uri) = withContext(Dispatchers.IO) {
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
        val stateAge = System.currentTimeMillis() - stored.pendingStateCreatedAt
        val stateFresh = stateAge in 0..OAUTH_STATE_MAX_AGE_MS
        tokenStore.update { it.copy(pendingState = null, pendingStateCreatedAt = 0L) }
        if (!validState || !stateFresh) throw IOException("OAuth state validation failed")
        uri.getQueryParameter("error")?.let { throw IOException("OAuth authorization was cancelled") }
        val code = uri.getQueryParameter("code")?.takeIf(String::isNotBlank)
            ?: throw IOException("Missing authorization code")

        val response = requestToken(
            TokenRequest(
                grantType = "authorization_code",
                code = code,
                redirectUri = KokoroApi.APP_REDIRECT_URI,
            ),
        )
        tokenStore.replaceTokens(response.toStoredCredentials())
    }

    suspend fun executeAuthorized(request: Request): Response = withContext(Dispatchers.IO) {
        require(KokoroApi.isAuthorizedApiUrl(request.url.toString())) {
            "Refusing to attach Kokoro credentials to an untrusted URL"
        }
        val initialToken = validAccessToken() ?: throw KokoroAuthenticationRequiredException()
        val first = httpClient.newCall(request.withBearer(initialToken)).execute()
        if (first.code != 401) return@withContext first

        first.close()
        val refreshed = validAccessToken(rejectedAccessToken = initialToken, forceRefresh = true)
            ?: throw KokoroAuthenticationRequiredException()
        val second = httpClient.newCall(request.withBearer(refreshed)).execute()
        if (second.code == 401 || second.code == 403) {
            tokenStore.clearTokens()
        }
        second
    }

    /**
     * Downloads a Mihomo subscription through the App endpoint. Older server deployments do not
     * expose that endpoint yet, so a 404 falls back to the public endpoint with a UUID obtained
     * transiently from /app/me. The UUID is never returned to callers or persisted locally.
     */
    suspend fun executeSubscriptionConfig(request: Request): Response = withContext(Dispatchers.IO) {
        require(KokoroApi.isAuthenticatedSubscriptionUrl(request.url.toString())) {
            "Refusing to download a Kokoro subscription from an untrusted URL"
        }

        val primary = executeAuthorized(request)
        if (primary.code != 404) return@withContext primary
        primary.close()

        val accountRequest = Request.Builder()
            .url(KokoroApi.ME_URL)
            .header("Accept", "application/json")
            .build()
        val proxyUuid = executeAuthorized(accountRequest).use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unable to retrieve Kokoro subscription credentials (HTTP ${response.code})")
            }
            json.parseToJsonElement(response.body.string())
                .jsonObject["proxy_uuid"]
                ?.jsonPrimitive
                ?.contentOrNull
                ?.takeIf(String::isNotBlank)
                ?: throw IOException("The Kokoro account has no subscription credential")
        }

        val fallbackBuilder = KokoroApi.LEGACY_SUBSCRIPTION_CONFIG_URL.toHttpUrl().newBuilder()
            .addQueryParameter("uuid", proxyUuid)
            .addQueryParameter("client", "meta")
        fun copyQueryParameter(name: String) {
            request.url.queryParameter(name)?.let { fallbackBuilder.addQueryParameter(name, it) }
        }
        copyQueryParameter("protocol")
        copyQueryParameter("plan")
        copyQueryParameter("isp")
        copyQueryParameter("mode")
        fallbackBuilder.addQueryParameter(
            "rule",
            request.url.queryParameter("rule_source") ?: "origin",
        )
        fallbackBuilder.addQueryParameter(
            "match",
            if (request.url.queryParameter("final_route") == "direct") "direct" else "none",
        )
        fallbackBuilder.addQueryParameter(
            "rule_update",
            if (request.url.queryParameter("rule_provider_auto_update")?.toBooleanStrictOrNull() != false) {
                "enable"
            } else {
                "disable"
            },
        )
        val profileAutoUpdate = request.url.queryParameter("profile_update")
            ?.toBooleanStrictOrNull()
            ?: true
        fallbackBuilder.addQueryParameter(
            "update",
            if (profileAutoUpdate) {
                request.url.queryParameter("profile_update_hours") ?: "1"
            } else {
                "off"
            },
        )
        val fallbackRequest = request.newBuilder()
            .removeHeader("Authorization")
            .url(fallbackBuilder.build())
            .build()
        httpClient.newCall(fallbackRequest).execute()
    }

    suspend fun revoke() = withContext(Dispatchers.IO) {
        val accessToken = tokenStore.load().accessToken
        try {
            if (!accessToken.isNullOrBlank()) {
                val request = Request.Builder()
                    .url(KokoroApi.REVOKE_URL)
                    .header("Authorization", "Bearer $accessToken")
                    .post(ByteArray(0).toRequestBody(null))
                    .build()
                runCatching { httpClient.newCall(request).execute().close() }
            }
        } finally {
            tokenStore.clearTokens()
        }
    }

    fun clearTokens() = tokenStore.clearTokens()

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
            val replacement = requestToken(
                TokenRequest(grantType = "refresh_token", refreshToken = refreshToken),
            ).toStoredCredentials()
            tokenStore.replaceTokens(replacement)
            replacement.accessToken
        } catch (error: Exception) {
            if (error is KokoroTokenRequestException && error.statusCode in 400..499) {
                tokenStore.clearTokens()
            }
            throw error
        }
    }

    private fun requestToken(payload: TokenRequest): TokenResponse {
        val request = Request.Builder()
            .url(KokoroApi.TOKEN_URL)
            .header("Accept", "application/json")
            .post(json.encodeToString(payload).toRequestBody(JSON_MEDIA_TYPE))
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (response.code !in 200..299) {
                throw KokoroTokenRequestException(response.code)
            }
            return json.decodeFromString(response.body.string())
        }
    }

    private fun Request.withBearer(accessToken: String): Request = newBuilder()
        .header("Authorization", "Bearer $accessToken")
        .build()

    private fun TokenResponse.toStoredCredentials(): StoredAuthData {
        val now = System.currentTimeMillis()
        return StoredAuthData(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresAt = now + expiresIn.coerceAtLeast(1) * 1_000L,
            refreshTokenExpiresAt = now + refreshExpiresIn.coerceAtLeast(1) * 1_000L,
        )
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        val refreshMutex = Mutex()
        const val ACCESS_TOKEN_REFRESH_MARGIN_MS = 60_000L
        const val OAUTH_STATE_MAX_AGE_MS = 10 * 60_000L
    }
}

class KokoroAuthenticationRequiredException : IOException("Kokoro sign-in is required")

private class KokoroTokenRequestException(val statusCode: Int) :
    IOException("Token exchange failed with HTTP $statusCode")

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
private data class StoredAuthData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val accessTokenExpiresAt: Long = 0L,
    val refreshTokenExpiresAt: Long = 0L,
    val pendingState: String? = null,
    val pendingStateCreatedAt: Long = 0L,
)

private class KokoroKeystoreTokenStore(context: Context, private val json: Json) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): StoredAuthData = synchronized(storeLock) {
        val encoded = preferences.getString(ENCRYPTED_DATA_KEY, null) ?: return@synchronized StoredAuthData()
        runCatching {
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

    fun update(transform: (StoredAuthData) -> StoredAuthData) = synchronized(storeLock) {
        saveLocked(transform(load()))
    }

    fun replaceTokens(replacement: StoredAuthData) = synchronized(storeLock) {
        val current = load()
        saveLocked(
            replacement.copy(
                pendingState = current.pendingState,
                pendingStateCreatedAt = current.pendingStateCreatedAt,
            ),
        )
    }

    fun clearTokens() = synchronized(storeLock) {
        val current = load()
        saveLocked(
            StoredAuthData(
                pendingState = current.pendingState,
                pendingStateCreatedAt = current.pendingStateCreatedAt,
            ),
        )
    }

    private fun saveLocked(value: StoredAuthData) {
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
        val storeLock = Any()
        const val PREFERENCES_NAME = "amamiya_keystore_credentials"
        const val ENCRYPTED_DATA_KEY = "encrypted_auth_data"
        const val KEY_ALIAS = "yumebox_amamiya_oauth_aes"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH_BYTES = 12
    }
}
