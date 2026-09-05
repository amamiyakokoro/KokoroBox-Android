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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.security.KeyStore
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

    fun loginUrl(state: String, codeChallenge: String): String = LOGIN_URL.toHttpUrl().newBuilder()
        .addQueryParameter("redirect_uri", APP_REDIRECT_URI)
        .addQueryParameter("state", state)
        .addQueryParameter("code_challenge", codeChallenge)
        .addQueryParameter("code_challenge_method", "S256")
        .build()
        .toString()

    fun isAuthenticatedSubscriptionUrl(url: String): Boolean {
        val uri = trustedApiUrl(url) ?: return false
        return uri.encodedPath == "/api/app/subscription/config"
    }

    fun isAuthorizedApiUrl(url: String): Boolean {
        val uri = trustedApiUrl(url) ?: return false
        val path = uri.encodedPath
        return path in AUTHORIZED_API_PATHS || CUSTOM_RULE_SET_PATH.matches(path)
    }

    fun isLegacySubscriptionUrl(url: String): Boolean {
        val uri = trustedApiUrl(url) ?: return false
        return uri.encodedPath == "/api/config" && !uri.queryParameter("uuid").isNullOrBlank()
    }

    private fun trustedApiUrl(url: String) = url.toHttpUrlOrNull()?.takeIf {
        it.scheme == "https" && it.host == "amamiyakoko.ro" && it.port == 443 &&
            it.username.isEmpty() && it.password.isEmpty() && it.fragment == null
    }

    fun isManagedSubscriptionUrl(url: String): Boolean =
        isAuthenticatedSubscriptionUrl(url) || isLegacySubscriptionUrl(url)

    private val AUTHORIZED_API_PATHS = setOf(
        "/api/app/me",
        "/api/app/subscription/options",
        "/api/app/subscription/resolve",
        "/api/app/subscription/config",
        "/api/app/custom-rules",
        "/api/app/custom-rules/options",
        "/api/app/custom-rules/sets",
    )
    private val CUSTOM_RULE_SET_PATH =
        Regex("^/api/app/custom-rules/sets/[1-9][0-9]*(?:/rules)?$")
}

/**
 * Owns the OAuth session shared by the Compose UI and the profile download service.
 * Tokens are never exposed through profile URLs or logs.
 */
class KokoroSession internal constructor(
    private val tokenStore: KokoroAuthStore,
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val now: () -> Long = System::currentTimeMillis,
    private val secureRandom: SecureRandom = SecureRandom(),
) {
    constructor(context: Context) : this(
        KokoroKeystoreTokenStore(context.applicationContext, Json { ignoreUnknownKeys = true }),
    )

    private val json = Json { ignoreUnknownKeys = true }
    // Single-use codes and rotating refresh tokens must never be automatically replayed
    // or forwarded to a redirect target. No HTTP/body logger is installed on this client.
    private val tokenClient = httpClient.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .retryOnConnectionFailure(false)
        .build()

    suspend fun beginLogin(): String {
        var createdState: String? = null
        return try {
            withContext(Dispatchers.IO) {
                refreshMutex.withLock {
                    val pending = KokoroOAuth.newLogin(secureRandom, now())
                    createdState = pending.state
                    tokenStore.update { it.copy(pendingLogin = pending) }
                    KokoroApi.loginUrl(pending.state, KokoroOAuth.challenge(requireNotNull(pending.codeVerifier)))
                }
            }
        } catch (error: Exception) {
            withContext(NonCancellable + Dispatchers.IO) { clearPendingLogin(createdState) }
            throw error
        }
    }

    /** Called if the system browser could not be opened; never clears a newer attempt. */
    suspend fun cancelLogin(loginUrl: String) = withContext(NonCancellable + Dispatchers.IO) {
        clearPendingLogin(loginUrl.toHttpUrlOrNull()?.queryParameter("state"))
    }

    private fun clearPendingLogin(state: String?) {
        if (state == null) return
        tokenStore.update { current ->
            if (current.pendingLogin?.state == state) current.copy(pendingLogin = null) else current
        }
    }

    suspend fun handleOAuthCallback(uri: Uri) = handleOAuthCallback(uri.toString())

    internal suspend fun handleOAuthCallback(rawUri: String) = withContext(Dispatchers.IO) {
        refreshMutex.withLock {
            discardExpiredLogin()
            val callback = KokoroOAuth.parseCallback(rawUri)
            var claimed: PendingKokoroLogin? = null
            tokenStore.update { current ->
                val pending = current.pendingLogin
                when {
                    pending == null -> current
                    !pending.isFresh(now()) || !KokoroOAuth.isValidVerifier(pending.codeVerifier) ||
                        pending.redirectUri != KokoroApi.APP_REDIRECT_URI -> current.copy(pendingLogin = null)
                    !KokoroOAuth.sameState(pending.state, callback.state) -> current
                    else -> {
                        claimed = pending
                        current.copy(pendingLogin = null)
                    }
                }
            }
            val pending = claimed ?: throw IOException("No matching pending PKCE login; sign in again")
            // Consume before the network request, also on cancellation, malformed success or failure.
            if (callback.error != null) throw IOException("OAuth authorization was cancelled or denied")
            val code = callback.code?.takeIf(String::isNotBlank)
                ?: throw IOException("Missing authorization code")
            val response = requestToken(
                TokenRequest(
                    grantType = "authorization_code",
                    code = code,
                    redirectUri = pending.redirectUri,
                    codeVerifier = requireNotNull(pending.codeVerifier),
                ),
            )
            currentCoroutineContext().ensureActive()
            tokenStore.replaceTokens(response.toStoredCredentials())
        }
    }

    private fun discardExpiredLogin() {
        val pending = tokenStore.load().pendingLogin ?: return
        if (!pending.isFresh(now())) clearPendingLogin(pending.state)
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
        refreshMutex.withLock {
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
                tokenStore.update { StoredAuthData() }
            }
        }
    }

    fun clearTokens() = tokenStore.clearTokens()

    private suspend fun validAccessToken(
        rejectedAccessToken: String? = null,
        forceRefresh: Boolean = false,
    ): String? = refreshMutex.withLock {
        discardExpiredLogin()
        val current = tokenStore.load()
        if (rejectedAccessToken != null && current.accessToken != rejectedAccessToken) {
            return@withLock current.accessToken
        }
        val stillValid = current.accessToken != null &&
            current.accessTokenExpiresAt > now() + ACCESS_TOKEN_REFRESH_MARGIN_MS
        if (!forceRefresh && stillValid) return@withLock current.accessToken
        val refreshToken = current.refreshToken ?: return@withLock current.accessToken?.takeIf { stillValid }
        if (current.refreshTokenExpiresAt <= now()) {
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
        val tokenResponse = try {
            tokenClient.newCall(request).execute()
        } catch (_: IOException) {
            throw IOException("Unable to contact Kokoro token endpoint")
        }
        tokenResponse.use { response ->
            if (response.code !in 200..299) {
                throw KokoroTokenRequestException(response.code)
            }
            return try {
                json.decodeFromString<TokenResponse>(response.body.string()).also {
                    check(it.accessToken.isNotBlank() && it.refreshToken.isNotBlank())
                }
            } catch (_: Exception) {
                // Serialization errors may echo credentials from the response body.
                throw IOException("Invalid Kokoro token response")
            }
        }
    }

    private fun Request.withBearer(accessToken: String): Request = newBuilder()
        .header("Authorization", "Bearer $accessToken")
        .build()

    private fun TokenResponse.toStoredCredentials(): StoredAuthData {
        val now = now()
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
    @SerialName("code_verifier") val codeVerifier: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
) {
    override fun toString(): String = "TokenRequest([redacted])"
}

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("refresh_expires_in") val refreshExpiresIn: Long,
) {
    override fun toString(): String = "TokenResponse([redacted])"
}

private class KokoroKeystoreTokenStore(context: Context, private val json: Json) : KokoroAuthStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun load(): StoredAuthData = synchronized(storeLock) {
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

    override fun update(transform: (StoredAuthData) -> StoredAuthData) = synchronized(storeLock) {
        saveLocked(transform(load()))
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
