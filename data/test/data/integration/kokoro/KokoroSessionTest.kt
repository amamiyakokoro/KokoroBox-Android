package com.github.yumelira.yumebox.data.integration.kokoro

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class KokoroSessionTest {
    private val json = Json { ignoreUnknownKeys = true }

    // Serialization exercises process-recreation data shape, not Android Keystore or OS dispatch.
    private class SerializedStore : KokoroAuthStore {
        private val json = Json { ignoreUnknownKeys = true }
        var serialized = "{}"
        @Synchronized override fun load(): StoredAuthData = json.decodeFromString(serialized)
        @Synchronized override fun update(transform: (StoredAuthData) -> StoredAuthData) {
            serialized = json.encodeToString(transform(load()))
        }
    }

    private class Transport : Interceptor {
        val requests: MutableList<Request> = Collections.synchronizedList(mutableListOf())
        var respond: (Request) -> Pair<Int, String> = { 200 to TOKENS }
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            requests.add(request)
            val (status, body) = respond(request)
            return Response.Builder().request(request).protocol(Protocol.HTTP_1_1)
                .code(status).message("test").body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private class Fixture {
        val store = SerializedStore()
        val transport = Transport()
        var time = 1_000_000L
        val client = OkHttpClient.Builder().addInterceptor(transport).build()
        fun session() = KokoroSession(store, client, now = { time })
        val session = session()
        suspend fun begin(): PendingKokoroLogin {
            session.beginLogin()
            return requireNotNull(store.load().pendingLogin)
        }
    }

    private fun callback(pending: PendingKokoroLogin, extra: String = "code=one-time-code") =
        "kokoro://oauth/callback?state=${pending.state}&$extra"

    private fun body(request: Request) = json.parseToJsonElement(
        Buffer().also { requireNotNull(request.body).writeTo(it) }.readUtf8(),
    ).jsonObject.mapValues { it.value.jsonPrimitive.content }

    private suspend fun rejected(block: suspend () -> Unit): IOException {
        try { block() } catch (e: IOException) { return e }
        throw AssertionError("Expected callback/request to be rejected")
    }

    @Test
    fun loginUrlAndTokenBodyUseSamePendingVerifier() = runBlocking {
        val f = Fixture()
        val url = f.session.beginLogin().toHttpUrl()
        val pending = requireNotNull(f.store.load().pendingLogin)
        assertEquals(KokoroApi.LOGIN_URL, url.newBuilder().query(null).build().toString())
        assertEquals(setOf("redirect_uri", "state", "code_challenge", "code_challenge_method"), url.queryParameterNames)
        assertEquals(KokoroApi.APP_REDIRECT_URI, url.queryParameter("redirect_uri"))
        assertEquals(pending.state, url.queryParameter("state"))
        assertEquals(KokoroOAuth.challenge(requireNotNull(pending.codeVerifier)), url.queryParameter("code_challenge"))
        assertEquals("S256", url.queryParameter("code_challenge_method"))
        assertFalse(url.toString().contains(pending.codeVerifier))
        f.session.handleOAuthCallback(callback(pending))
        val request = f.transport.requests.single()
        assertEquals("POST", request.method)
        assertEquals(KokoroApi.TOKEN_URL, request.url.toString())
        assertTrue(request.body!!.contentType().toString().startsWith("application/json"))
        assertEquals(mapOf("grant_type" to "authorization_code", "code" to "one-time-code",
            "redirect_uri" to KokoroApi.APP_REDIRECT_URI, "code_verifier" to pending.codeVerifier), body(request))
        val credentials = f.store.load()
        assertNull(credentials.pendingLogin)
        assertEquals("new-access", credentials.accessToken)
        assertEquals("new-refresh", credentials.refreshToken)
        assertEquals(f.time + 3_600_000, credentials.accessTokenExpiresAt)
        assertEquals(f.time + 2_592_000_000, credentials.refreshTokenExpiresAt)
        assertFalse(credentials.toString().contains("new-access"))
        assertFalse(pending.toString().contains(pending.codeVerifier))
    }

    @Test
    fun invalidMissingAndDuplicateStateDoNotConsumeLegitimateLogin() = runBlocking {
        val f = Fixture()
        val pending = f.begin()
        listOf("kokoro://oauth/callback?state=wrong&code=secret", "kokoro://oauth/callback?code=secret",
            callback(pending) + "&state=${pending.state}", callback(pending) + "&code=second",
            callback(pending).replace("oauth/", "attacker@oauth:99/"), callback(pending) + "#fragment").forEach {
            rejected { f.session.handleOAuthCallback(it) }
            assertEquals(pending, f.store.load().pendingLogin)
        }
        assertTrue(f.transport.requests.isEmpty())
        f.session.handleOAuthCallback(callback(pending))
        assertEquals(1, f.transport.requests.size)
    }

    @Test
    fun callbackWithoutPendingOrReplayedIsRejected() = runBlocking {
        val f = Fixture()
        rejected { f.session.handleOAuthCallback("kokoro://oauth/callback?state=unknown&code=secret") }
        val pending = f.begin()
        f.session.handleOAuthCallback(callback(pending))
        rejected { f.session.handleOAuthCallback(callback(pending)) }
        assertEquals(1, f.transport.requests.size)
    }

    @Test
    fun concurrentCallbackIsExchangedOnlyOnceAcrossSessions() = runBlocking {
        val f = Fixture()
        val pending = f.begin()
        val successes = List(8) { async(Dispatchers.Default) {
            try { f.session().handleOAuthCallback(callback(pending)); true } catch (_: IOException) { false }
        } }.awaitAll()
        assertEquals(1, successes.count { it })
        assertEquals(1, f.transport.requests.size)
    }

    @Test
    fun expiryAndClockRollbackDiscardPending() = runBlocking {
        listOf(KokoroOAuth.PENDING_LOGIN_TTL_MS, -1L).forEach { delta ->
            val f = Fixture()
            val pending = f.begin()
            f.time += delta
            rejected { f.session.handleOAuthCallback(callback(pending)) }
            assertNull(f.store.load().pendingLogin)
            assertTrue(f.transport.requests.isEmpty())
        }
    }

    @Test
    fun deniedMissingCodeAndAmbiguousSuccessConsumePending() = runBlocking {
        listOf("error=access_denied", "other=value", "code=", "code=secret&error=access_denied").forEach { query ->
            val f = Fixture()
            val pending = f.begin()
            rejected { f.session.handleOAuthCallback(callback(pending, query)) }
            assertNull(f.store.load().pendingLogin)
            assertTrue(f.transport.requests.isEmpty())
        }
    }

    @Test
    fun serializedPendingSurvivesSessionRecreation() = runBlocking {
        val f = Fixture()
        val pending = f.begin()
        val recreatedStore = SerializedStore().apply { serialized = f.store.serialized }
        val recreated = KokoroSession(recreatedStore, f.client, now = { f.time })
        recreated.handleOAuthCallback(callback(pending))
        assertEquals(pending.codeVerifier, body(f.transport.requests.single())["code_verifier"])
        assertNull(recreatedStore.load().pendingLogin)
    }

    @Test
    fun missingOrInvalidVerifierAndRedirectRequireNewLogin() = runBlocking {
        repeat(3) { variant ->
            val f = Fixture()
            val pending = f.begin()
            f.store.update { it.copy(pendingLogin = when (variant) {
                0 -> pending.copy(codeVerifier = null)
                1 -> pending.copy(codeVerifier = "invalid")
                else -> pending.copy(redirectUri = "another://oauth/callback")
            }) }
            rejected { f.session.handleOAuthCallback(callback(pending)) }
            assertNull(f.store.load().pendingLogin)
            assertTrue(f.transport.requests.isEmpty())
        }
    }

    @Test
    fun legacyPendingIsIgnoredWithoutDiscardingExistingTokens() {
        val store = SerializedStore().apply {
            serialized = """{"accessToken":"existing","refreshToken":"refresh","pendingState":"legacy","pendingStateCreatedAt":123}"""
        }
        assertNull(store.load().pendingLogin)
        assertEquals("existing", store.load().accessToken)
        store.update { it }
        assertFalse(store.serialized.contains("pendingState"))
    }

    @Test
    fun newLoginInvalidatesOldAttemptWithoutMixingVerifiers() = runBlocking {
        val f = Fixture()
        val firstUrl = f.session.beginLogin()
        val first = requireNotNull(f.store.load().pendingLogin)
        val second = f.begin()
        assertNotEquals(first.state, second.state)
        assertNotEquals(first.codeVerifier, second.codeVerifier)
        rejected { f.session.handleOAuthCallback(callback(first)) }
        f.session.cancelLogin(firstUrl)
        assertEquals(second, f.store.load().pendingLogin)
        f.session.handleOAuthCallback(callback(second))
        assertEquals(second.codeVerifier, body(f.transport.requests.single())["code_verifier"])
    }

    @Test
    fun browserLaunchCancellationClearsMatchingPending() = runBlocking {
        val f = Fixture()
        val url = f.session.beginLogin()
        f.session.cancelLogin(url)
        assertNull(f.store.load().pendingLogin)
        assertTrue(f.transport.requests.isEmpty())
    }

    @Test
    fun token400And422NeverRetryOrDowngrade() = runBlocking {
        listOf(400, 422).forEach { status ->
            val f = Fixture()
            f.transport.respond = { status to """{"detail":"sensitive-server-body"}""" }
            val pending = f.begin()
            val error = rejected { f.session.handleOAuthCallback(callback(pending)) }
            assertTrue(error.message.orEmpty().contains(status.toString()))
            assertFalse(error.message.orEmpty().contains("sensitive-server-body"))
            assertNull(f.store.load().pendingLogin)
            rejected { f.session.handleOAuthCallback(callback(pending)) }
            assertEquals(pending.codeVerifier, body(f.transport.requests.single())["code_verifier"])
        }
    }

    @Test
    fun networkFailureConsumesPendingAndMalformedResponseIsRedacted() = runBlocking {
        repeat(2) { variant ->
            val f = Fixture()
            f.transport.respond = {
                if (variant == 0) throw IOException("transport-secret")
                200 to """{"access_token":"secret-access","expires_in":"invalid-secret"}"""
            }
            val pending = f.begin()
            val error = rejected { f.session.handleOAuthCallback(callback(pending)) }
            assertFalse(error.stackTraceToString().contains("secret"))
            assertNull(f.store.load().pendingLogin)
            assertNull(f.store.load().accessToken)
            assertEquals(1, f.transport.requests.size)
        }
    }

    @Test
    fun cancellingExchangeCannotRestoreConsumedPendingOrCommitTokens() = runBlocking {
        val f = Fixture()
        val pending = f.begin()
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        f.transport.respond = {
            started.countDown()
            check(release.await(5, TimeUnit.SECONDS))
            200 to TOKENS
        }
        val job = Job()
        val exchange = async(Dispatchers.Default + job) { f.session.handleOAuthCallback(callback(pending)) }
        try {
            assertTrue(started.await(5, TimeUnit.SECONDS))
            assertNull(f.store.load().pendingLogin)
            job.cancel()
        } finally {
            release.countDown()
        }
        exchange.join()
        assertTrue(exchange.isCancelled)
        assertNull(f.store.load().pendingLogin)
        assertNull(f.store.load().accessToken)
        assertEquals(1, f.transport.requests.size)
    }

    @Test
    fun failedPendingConsumptionNeverSendsCode() = runBlocking {
        val f = Fixture()
        val pending = f.begin()
        val failingStore = object : KokoroAuthStore {
            override fun load() = f.store.load()
            override fun update(transform: (StoredAuthData) -> StoredAuthData) {
                throw IOException("storage unavailable")
            }
        }
        val session = KokoroSession(failingStore, f.client, now = { f.time })
        rejected { session.handleOAuthCallback(callback(pending)) }
        assertTrue(f.transport.requests.isEmpty())
        assertNull(f.store.load().accessToken)
    }

    private fun Fixture.seedTokens(expired: Boolean = true) {
        store.update { it.copy(accessToken = "old-access", refreshToken = "old-refresh",
            accessTokenExpiresAt = if (expired) time - 1 else time + 3_600_000,
            refreshTokenExpiresAt = time + 2_592_000_000) }
        transport.respond = { request ->
            if (request.url.toString() == KokoroApi.TOKEN_URL) 200 to TOKENS else 200 to "{}"
        }
    }

    @Test
    fun refreshSingleFlightRotatesBothTokensWithoutVerifierAndPreservesPending() = runBlocking {
        val f = Fixture()
        f.seedTokens()
        val pending = f.begin()
        List(8) { async(Dispatchers.Default) {
            f.session().executeAuthorized(Request.Builder().url(KokoroApi.ME_URL).build()).use { assertEquals(200, it.code) }
        } }.awaitAll()
        val refresh = f.transport.requests.single { it.url.toString() == KokoroApi.TOKEN_URL }
        assertEquals(mapOf("grant_type" to "refresh_token", "refresh_token" to "old-refresh"), body(refresh))
        assertEquals("new-access", f.store.load().accessToken)
        assertEquals("new-refresh", f.store.load().refreshToken)
        assertEquals(pending, f.store.load().pendingLogin)
        f.transport.requests.filter { it.url.toString() == KokoroApi.ME_URL }.forEach {
            assertEquals("Bearer new-access", it.header("Authorization"))
        }
    }

    @Test
    fun protected401RefreshesAndReplaysOnce() = runBlocking {
        val f = Fixture()
        f.seedTokens(expired = false)
        f.transport.respond = { request -> when {
            request.url.toString() == KokoroApi.TOKEN_URL -> 200 to TOKENS
            request.header("Authorization") == "Bearer old-access" -> 401 to "{}"
            else -> 200 to "{}"
        } }
        f.session.executeAuthorized(Request.Builder().url(KokoroApi.ME_URL).build()).close()
        assertEquals(3, f.transport.requests.size)
        assertEquals("Bearer new-access", f.transport.requests.last().header("Authorization"))
    }

    @Test
    fun refresh401ClearsTokensAndDoesNotLoop() = runBlocking {
        val f = Fixture()
        f.seedTokens()
        f.transport.respond = { 401 to "{}" }
        repeat(2) { rejected { f.session.executeAuthorized(Request.Builder().url(KokoroApi.ME_URL).build()).close() } }
        assertNull(f.store.load().accessToken)
        assertNull(f.store.load().refreshToken)
        assertEquals(1, f.transport.requests.size)
    }

    @Test
    fun explicitLogoutClearsPendingAndTokensEvenIfRevokeFails() = runBlocking {
        val f = Fixture()
        f.seedTokens(expired = false)
        f.begin()
        f.transport.respond = { 401 to "{}" }
        f.session.revoke()
        assertEquals(StoredAuthData(), f.store.load())
    }

    private companion object {
        const val TOKENS = """{"token_type":"Bearer","access_token":"new-access","expires_in":3600,"refresh_token":"new-refresh","refresh_expires_in":2592000}"""
    }
}
