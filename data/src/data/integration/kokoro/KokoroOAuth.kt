package com.github.yumelira.yumebox.data.integration.kokoro

import kotlinx.serialization.Serializable
import java.io.IOException
import java.net.URI
import java.net.URLDecoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal object KokoroOAuth {
    const val PENDING_LOGIN_TTL_MS = 10 * 60_000L
    private val verifierPattern = Regex("[A-Za-z0-9._~-]{43,128}")

    fun isValidVerifier(verifier: String?): Boolean = verifier != null && verifierPattern.matches(verifier)

    fun challenge(verifier: String): String {
        require(isValidVerifier(verifier)) { "Invalid PKCE verifier format" }
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    fun newLogin(random: SecureRandom, now: Long): PendingKokoroLogin {
        fun randomValue(): String = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(ByteArray(32).also(random::nextBytes))
        return PendingKokoroLogin(
            state = randomValue(),
            codeVerifier = randomValue(),
            redirectUri = KokoroApi.APP_REDIRECT_URI,
            createdAt = now,
            expiresAt = now + PENDING_LOGIN_TTL_MS,
        )
    }

    fun sameState(expected: String, received: String): Boolean = MessageDigest.isEqual(
        expected.toByteArray(Charsets.UTF_8),
        received.toByteArray(Charsets.UTF_8),
    )

    fun parseCallback(raw: String): KokoroCallback {
        // Never expose URI parser exceptions: their messages can contain the full callback.
        val uri = try {
            if (raw.length > 16_384) throw IOException()
            URI(raw)
        } catch (_: Exception) {
            throw IOException("Invalid OAuth callback")
        }
        if (uri.isOpaque || uri.scheme != "kokoro" || uri.rawAuthority != "oauth" ||
            uri.rawPath != "/callback" || uri.rawFragment != null
        ) throw IOException("Invalid OAuth callback")

        val parameters = linkedMapOf<String, String>()
        val query = uri.rawQuery ?: throw IOException("Missing OAuth state")
        for (part in query.split('&')) {
            val pair = part.split('=', limit = 2)
            val key = decode(pair[0])
            val value = decode(pair.getOrElse(1) { "" })
            if (key.isEmpty() || parameters.put(key, value) != null) {
                throw IOException("Duplicate or invalid OAuth callback parameters")
            }
        }
        val state = parameters["state"]?.takeIf(String::isNotBlank)
            ?: throw IOException("Missing OAuth state")
        return KokoroCallback(state, parameters["code"], parameters["error"])
    }

    private fun decode(value: String): String = try {
        URLDecoder.decode(value, "UTF-8").also {
            if (it.any { char -> char.isISOControl() || char == '\uFFFD' }) throw IOException()
        }
    } catch (_: Exception) {
        throw IOException("Invalid OAuth callback encoding")
    }
}

@Serializable
internal data class PendingKokoroLogin(
    val state: String,
    val codeVerifier: String? = null,
    val redirectUri: String = KokoroApi.APP_REDIRECT_URI,
    val createdAt: Long,
    val expiresAt: Long,
) {
    fun isFresh(now: Long): Boolean = now >= createdAt && now < expiresAt &&
        expiresAt > createdAt && expiresAt - createdAt <= KokoroOAuth.PENDING_LOGIN_TTL_MS

    override fun toString(): String = "PendingKokoroLogin([redacted])"
}

internal class KokoroCallback(val state: String, val code: String?, val error: String?)
