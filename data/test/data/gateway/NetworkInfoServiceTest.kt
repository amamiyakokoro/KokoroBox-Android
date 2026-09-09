package com.github.yumelira.yumebox.data.gateway

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NetworkInfoServiceTest {
    @Test
    fun usesTheFirstValidEndpointAndSendsJsonHeaders() = runBlocking {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            assertEquals("application/json", request.header("Accept"))
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("""{"ip":" 203.0.113.1 ","country_code":" TW "}""".toResponseBody())
                .build()
        }.build()
        val service = NetworkInfoService(client, listOf("https://ip.example/json"))

        assertEquals(IpInfo("203.0.113.1", "TW"), service.getExternalIp())
    }

    @Test
    fun fallsBackAfterAnInvalidResponse() = runBlocking {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val body = when (chain.request().url.host) {
                "invalid.example" -> "not-json"
                else -> """{"ip":"203.0.113.2","country":"JP"}"""
            }
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(body.toResponseBody())
                .build()
        }.build()
        val service = NetworkInfoService(
            client,
            listOf("https://invalid.example/json", "https://valid.example/json"),
        )

        assertEquals(IpInfo("203.0.113.2", "JP", "JP"), service.getExternalIp())
        assertNull(NetworkInfoService(client, emptyList()).getExternalIp())
    }
}
