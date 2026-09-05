package com.github.yumelira.yumebox.data.integration.kokoro

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.security.SecureRandom

class KokoroOAuthTest {
    @Test
    fun rfc7636ChallengeHashesAsciiVerifierWithoutPadding() {
        assertEquals(
            "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            KokoroOAuth.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"),
        )
    }

    @Test
    fun independentRandomValuesForEveryAttempt() {
        val random = SecureRandom()
        val values = mutableSetOf<String>()
        repeat(100) {
            val login = KokoroOAuth.newLogin(random, 1_000L)
            listOf(login.state, requireNotNull(login.codeVerifier)).forEach { value ->
                assertEquals(43, value.length)
                assertTrue(value.matches(Regex("[A-Za-z0-9_-]+")))
                assertTrue(values.add(value))
            }
            assertEquals(601_000L, login.expiresAt)
        }
    }

    @Test
    fun verifierFormatBoundaries() {
        listOf("a".repeat(43), "a".repeat(128), "a".repeat(39) + "-._~").forEach {
            assertTrue(KokoroOAuth.isValidVerifier(it))
            assertFalse(KokoroOAuth.challenge(it).contains('='))
        }
        listOf(null, "", "a".repeat(42), "a".repeat(129), "a".repeat(42) + "+",
            "a".repeat(42) + "/", "a".repeat(42) + "=", "a".repeat(42) + "é").forEach {
            assertFalse(KokoroOAuth.isValidVerifier(it))
        }
    }

    @Test
    fun strictCallbackOriginAndEncoding() {
        val valid = "kokoro://oauth/callback?state=state&code=code"
        val forged = listOf(
            valid.replace("kokoro:", "https:"), valid.replace("kokoro:", "KOKORO:"),
            valid.replace("oauth/", "OAUTH/"), valid.replace("oauth/", "evil/"),
            valid.replace("oauth/", "oauth:443/"), valid.replace("oauth/", "oauth:/"),
            valid.replace("oauth/", "user@oauth/"), valid.replace("oauth/", "oauth.evil/"),
            valid.replace("oauth/", "%6fauth/"), valid.replace("/callback", "/callback/"),
            valid.replace("/callback", "/%63allback"), valid.replace("/callback", "/../callback"),
            "$valid#", "$valid#fragment", "kokoro:oauth/callback?state=state&code=code",
            "$valid&state=other", "$valid&%73tate=other", "$valid&code=other",
            "$valid&error=one&error=two", "$valid&error=%00", "$valid&code_verifier=a&code_verifier=b",
            "$valid&error=%ZZ", "$valid&error=%FF", "kokoro://oauth/callback?code=secret",
            "kokoro://oauth/callback?state=&code=secret",
        )
        forged.forEach { raw ->
            val failure = assertThrows(IOException::class.java) { KokoroOAuth.parseCallback(raw) }
            assertNull(failure.cause)
            assertFalse(failure.message.orEmpty().contains(raw))
        }
        val parsed = KokoroOAuth.parseCallback("kokoro://oauth/callback?state=state&code=a%2Bb%2Fc")
        assertEquals("state", parsed.state)
        assertEquals("a+b/c", parsed.code)
    }
}
