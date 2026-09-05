package com.github.yumelira.yumebox.data.integration.kokoro

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.util.Collections

class KokoroCustomRulesClientTest {
    private class MemoryStore : KokoroAuthStore {
        private var value = StoredAuthData(
            accessToken = "access",
            refreshToken = "refresh",
            accessTokenExpiresAt = Long.MAX_VALUE,
            refreshTokenExpiresAt = Long.MAX_VALUE,
        )

        @Synchronized override fun load() = value
        @Synchronized override fun update(transform: (StoredAuthData) -> StoredAuthData) {
            value = transform(value)
        }
    }

    private class Transport : Interceptor {
        val requests: MutableList<Request> = Collections.synchronizedList(mutableListOf())
        var response: (Request) -> Pair<Int, String> = { 200 to STATE }

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            requests += request
            val (code, body) = response(request)
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("test")
                .body(body.toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private class Fixture {
        val transport = Transport()
        private val httpClient = OkHttpClient.Builder().addInterceptor(transport).build()
        val client = KokoroCustomRulesClient(
            KokoroSession(MemoryStore(), httpClient, now = { 1_000L }),
        )
    }

    @Test
    fun bearerAllowlistAcceptsOnlyCanonicalCustomRulePaths() {
        listOf(
            "https://amamiyakoko.ro/api/app/custom-rules",
            "https://amamiyakoko.ro/api/app/custom-rules/options",
            "https://amamiyakoko.ro/api/app/custom-rules/sets",
            "https://amamiyakoko.ro/api/app/custom-rules/sets/12",
            "https://amamiyakoko.ro/api/app/custom-rules/sets/12/rules",
        ).forEach { assertTrue(it, KokoroApi.isAuthorizedApiUrl(it)) }
        listOf(
            "https://evil.example/api/app/custom-rules",
            "https://amamiyakoko.ro/api/app/custom-rules/sets/0",
            "https://amamiyakoko.ro/api/app/custom-rules/sets/-1",
            "https://amamiyakoko.ro/api/app/custom-rules/sets/12/other",
            "https://amamiyakoko.ro/api/app/custom-rules/sets/12/rules/extra",
        ).forEach { assertFalse(it, KokoroApi.isAuthorizedApiUrl(it)) }
    }

    @Test
    fun stateAndOptionsDecodeWithoutLeakingAuthenticationIntoUrls() = runBlocking {
        val f = Fixture()
        f.transport.response = { request ->
            if (request.url.encodedPath.endsWith("/options")) 200 to OPTIONS else 200 to STATE
        }

        val state = f.client.getState()
        val options = f.client.getOptions()

        assertEquals("default", state.sets.single().name)
        assertEquals(listOf("DIRECT", "REJECT"), options.targets)
        f.transport.requests.forEach {
            assertEquals("Bearer access", it.header("Authorization"))
            assertTrue(it.url.queryParameterNames.isEmpty())
        }
    }

    @Test
    fun editorLoadReportsServerFailureWithoutCancellingCaller() = runBlocking {
        val f = Fixture()
        f.transport.response = { request ->
            if (request.url.encodedPath.endsWith("/options")) 200 to OPTIONS else 500 to "{}"
        }

        val result = runCatching { f.client.getEditorData() }

        val error = result.exceptionOrNull()
        assertTrue(error is KokoroRulesApiException)
        assertEquals(500, (error as KokoroRulesApiException).statusCode)
        assertEquals(2, f.transport.requests.size)
    }

    @Test
    fun replacePreservesOrderAndExpectedRevision() = runBlocking {
        val f = Fixture()
        f.transport.response = { 200 to UPDATED_SET }
        val rules = listOf(
            KokoroCustomRuleInput("DOMAIN-SUFFIX", "example.com", "DIRECT"),
            KokoroCustomRuleInput("MATCH", "", "DIRECT"),
        )

        val updated = f.client.replaceRules(12, 4, rules, decodedOptions())

        assertEquals(5, updated.revision)
        val request = f.transport.requests.single()
        assertEquals("PUT", request.method)
        assertEquals("/api/app/custom-rules/sets/12/rules", request.url.encodedPath)
        val body = Json.parseToJsonElement(
            Buffer().also { requireNotNull(request.body).writeTo(it) }.readUtf8(),
        ).jsonObject
        assertEquals(4, body.getValue("expected_revision").jsonPrimitive.content.toInt())
        val sentRules = body.getValue("rules").jsonArray
        assertEquals("example.com", sentRules[0].jsonObject["payload"]?.jsonPrimitive?.content)
        assertTrue(sentRules[1].jsonObject["payload"]?.jsonPrimitive?.content == "null")
    }

    @Test
    fun conflictIsReportedWithoutAutomaticRetry() = runBlocking {
        val f = Fixture()
        f.transport.response = { 409 to CONFLICT }

        val error = try {
            f.client.replaceRules(
                12,
                4,
                listOf(KokoroCustomRuleInput("DOMAIN", "example.com", "DIRECT")),
                decodedOptions(),
            )
            fail("Expected conflict")
            error("unreachable")
        } catch (error: KokoroRulesApiException) {
            error
        }

        assertEquals(409, error.statusCode)
        assertEquals(5, error.currentRevision)
        assertEquals(1, f.transport.requests.size)
    }

    @Test
    fun timeoutIsReconciledByReadingOrderedRemoteRules() = runBlocking {
        repeat(2) { matching ->
            val f = Fixture()
            f.transport.response = { request ->
                if (request.method == "PUT") throw IOException("timeout")
                200 to if (matching == 0) MATCHING_STATE else STATE
            }
            val operation = runCatching {
                f.client.replaceRules(
                    12,
                    4,
                    listOf(KokoroCustomRuleInput("DOMAIN", "example.com", "DIRECT")),
                    decodedOptions(),
                )
            }
            if (matching == 0) {
                assertEquals(5, operation.getOrThrow().revision)
            } else {
                assertTrue(operation.exceptionOrNull() is KokoroRulesSaveOutcomeUnknownException)
            }
            assertEquals(listOf("PUT", "GET"), f.transport.requests.map { it.method })
        }
    }

    @Test
    fun localValidationRejectsInvalidMatchAndProviderRules() {
        val options = decodedOptions()
        listOf(
            listOf(
                KokoroCustomRuleInput("MATCH", null, "DIRECT"),
                KokoroCustomRuleInput("DOMAIN", "example.com", "DIRECT"),
            ) to KokoroRulesValidationReason.MATCH_NOT_LAST,
            listOf(KokoroCustomRuleInput("MATCH", null, "REJECT")) to
                KokoroRulesValidationReason.MATCH_REJECT,
            listOf(KokoroCustomRuleInput("RULE-SET", "unknown", "DIRECT")) to
                KokoroRulesValidationReason.UNSUPPORTED_PROVIDER,
        ).forEach { (rules, expected) ->
            val error = try {
                validateCustomRules(rules, options)
                fail("Expected validation error")
                error("unreachable")
            } catch (error: KokoroRulesValidationException) {
                error
            }
            assertEquals(expected, error.reason)
        }
    }

    private fun decodedOptions() = Json.decodeFromString<KokoroCustomRulesOptions>(OPTIONS)

    private companion object {
        const val OPTIONS = """{"schema_version":1,"rule_types":["DOMAIN-SUFFIX","DOMAIN-KEYWORD","DOMAIN","IP-CIDR","PROCESS-NAME","RULE-SET","MATCH"],"targets":["DIRECT","REJECT"],"rule_providers":[{"name":"geosite-private","behavior":"domain"}],"limits":{"max_rule_sets":5,"max_rules_per_set":200,"max_name_length":64,"max_payload_length":1024}}"""
        const val STATE = """{"schema_version":1,"sets":[{"id":12,"name":"default","revision":4,"created_at":"2026-09-05T10:00:00","updated_at":"2026-09-05T11:30:00","rules":[]}]}"""
        const val MATCHING_STATE = """{"schema_version":1,"sets":[{"id":12,"name":"default","revision":5,"created_at":"2026-09-05T10:00:00","updated_at":"2026-09-05T11:31:00","rules":[{"id":51,"type":"DOMAIN","payload":"example.com","target":"DIRECT","priority":0,"updated_at":"2026-09-05T11:31:00"}]}]}"""
        const val UPDATED_SET = """{"id":12,"name":"default","revision":5,"created_at":"2026-09-05T10:00:00","updated_at":"2026-09-05T11:31:00","rules":[]}"""
        const val CONFLICT = """{"detail":{"message":"Rule set changed; reload before saving","current_revision":5}}"""
    }
}
