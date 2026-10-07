package com.amamiyakokoro.box.core.util

import org.junit.Assert.*
import org.junit.Test

class LogMessageTokensTest {
    private fun List<LogToken>.text() = joinToString("") { it.value }

    @Test fun desktopConnectionFormatSeparatesNetworkAndRoutingWithoutLosingContent() {
        val message = "[TCP] 127.0.0.1:63064(codex) --> chatgpt.com:443 match RuleSet(openai) using Direct-Special.anytls[Direct-Special.kokoro.anytls]"
        val parsed = parseLogMessage(message)
        assertEquals("[TCP] 127.0.0.1:63064(codex) --> chatgpt.com:443", parsed.primary.text())
        assertEquals("match RuleSet(openai) using Direct-Special.anytls[Direct-Special.kokoro.anytls]", parsed.secondary.text())
        assertEquals(message, "${parsed.primary.text()} ${parsed.secondary.text()}")
        assertEquals(listOf(LogTokenKind.Protocol, LogTokenKind.Address, LogTokenKind.Port,
            LogTokenKind.Process, LogTokenKind.Domain, LogTokenKind.Port),
            parsed.primary.filter { it.kind != LogTokenKind.Text }.map { it.kind })
        assertEquals(listOf(LogTokenKind.Keyword, LogTokenKind.Rule, LogTokenKind.Keyword, LogTokenKind.Route),
            parsed.secondary.filter { it.kind != LogTokenKind.Text }.map { it.kind })
        assertEquals("Direct-Special.anytls[Direct-Special.kokoro.anytls]", parsed.secondary.last().value)
    }

    @Test fun androidProcessUidIsOneToken() {
        for (process in listOf("(com.amamiyakokoro.box uid=10285)", "(org.zwanoo.android.speedtest, uid=10447)")) {
            val message = "[TCP] 127.0.0.1:51670$process --> api.ip.sb:443 match Match using DIRECT"
            val parsed = parseLogMessage(message)
            assertEquals(listOf(process), parsed.primary.filter { it.kind == LogTokenKind.Process }.map { it.value })
            assertEquals(message, "${parsed.primary.text()} ${parsed.secondary.text()}")
            assertEquals(LogToken(LogTokenKind.Route, "DIRECT"), parsed.secondary.last())
        }
    }

    @Test fun routesAreKeptWholeAndCaseIsPreserved() {
        for (route in listOf("DIRECT", "REJECT", "REJECT-DROP", "PASS", "direct", "Proxy group[Node name]")) {
            val parsed = parseLogMessage("[UDP] mihomo --> dns.example.com:443 match DomainSuffix(example.com) using $route")
            assertEquals(LogToken(LogTokenKind.Route, route), parsed.secondary.last())
            assertTrue(parsed.secondary.any { it.kind == LogTokenKind.Rule && it.value == "DomainSuffix(example.com)" })
        }
    }

    @Test fun ipv6EndpointsAndZoneIdsKeepAddressesSeparateFromPorts() {
        val parsed = parseLogMessage("[TCP] [fe80::1%wlan0]:51534 --> [::ffff:192.0.2.1]:443 using DIRECT")
        assertEquals(listOf("[fe80::1%wlan0]", "[::ffff:192.0.2.1]"),
            parsed.primary.filter { it.kind == LogTokenKind.Address }.map { it.value })
        assertEquals(listOf(":51534", ":443"), parsed.primary.filter { it.kind == LogTokenKind.Port }.map { it.value })
        assertEquals("using DIRECT", parsed.secondary.text())
    }

    @Test fun proxyChainMarkerDoesNotSplitNodeNamesIntoDomains() {
        val parsed = parseLogMessage("[TCP] mihomo --> chatgpt.com:443 proxy chain Proxy[Node.anytls]")
        assertEquals("proxy chain Proxy[Node.anytls]", parsed.secondary.text())
        assertEquals(LogToken(LogTokenKind.Route, "Proxy[Node.anytls]"), parsed.secondary.last())
    }

    @Test fun onlyExplicitErrorWordsAreHighlighted() {
        val parsed = parseLogMessage("error: connection failed after timeout; upstream refused")
        assertEquals(listOf("error:", "failed", "timeout", "refused"),
            parsed.primary.filter { it.kind == LogTokenKind.Error }.map { it.value })
        assertFalse(parseLogMessage("failure handling remains deterministic").primary.any { it.kind == LogTokenKind.Error })
    }

    @Test fun domainNamesAreNotMisreadAsProtocolsRoutesOrErrorWords() {
        val parsed = parseLogMessage("tcp.example.com direct.example.com failed.example.com")
        assertEquals(listOf("tcp.example.com", "direct.example.com", "failed.example.com"),
            parsed.primary.filter { it.kind == LogTokenKind.Domain }.map { it.value })
        assertFalse(parsed.primary.any { it.kind in setOf(LogTokenKind.Protocol, LogTokenKind.Route, LogTokenKind.Error) })
    }

    @Test fun unrecognizedAndMultilineMessagesKeepTheirOriginalText() {
        for (message in listOf("", "未知日誌\n  keep\tspacing", "configuration loaded successfully", "[QUIC] custom transport")) {
            val parsed = parseLogMessage(message)
            assertEquals(message, parsed.primary.text())
            assertTrue(parsed.secondary.isEmpty())
        }
    }
}
