package com.amamiyakokoro.box.core.model

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class ConnectionDiagnosticTest {
    @Test fun testsRecordedOutboundRatherThanCurrentGroup() {
        val connection = ConnectionInfo(chains = listOf("old-node", "Fallback", "Selector"))
        assertEquals("old-node", connection.diagnosticProxyName())
        assertEquals("", ConnectionInfo().diagnosticProxyName())
    }

    @Test fun missingOrMalformedHostIsSafe() {
        assertEquals("", ConnectionInfo().diagnosticHost())
        assertEquals("", ConnectionInfo(metadata = buildJsonObject { put("host", JsonNull) }).diagnosticHost())
        assertEquals("", ConnectionInfo(metadata = buildJsonObject { put("host", buildJsonObject {}) }).diagnosticHost())
        assertEquals("api.ip.sb", ConnectionInfo(metadata = buildJsonObject { put("host", " api.ip.sb ") }).diagnosticHost())
    }

    @Test fun decodesPartialDNSFailureAlongsideSuccessfulRoute() {
        val result = Json.decodeFromString<ConnectionDiagnostic>("""{
            "dnsMode":"fake-ip",
            "dns":[{"type":"A","status":"failed","rcode":"NXDOMAIN","answers":[],"durationMillis":12}],
            "route":{"proxyName":"node","status":"ok","delay":42,"testUrl":"https://example.com"}
        }""")
        assertEquals("NXDOMAIN", result.dns.single().rcode)
        assertEquals(42, result.route.delay)
        assertEquals(-1, RouteDiagnostic().delay)
    }
}
