package com.amamiyakokoro.box.data.integration.kokoro

import org.junit.Assert.*
import org.junit.Test

class KokoroConnectionRulesTest {
    @Test fun suffixWinsRegardlessOfServerOrdering() {
        assertEquals("DOMAIN-SUFFIX", preferredKokoroDomainRuleType(listOf("DOMAIN", "MATCH", "DOMAIN-SUFFIX")))
        assertEquals("DOMAIN", preferredKokoroDomainRuleType(listOf("MATCH", "DOMAIN")))
    }

    @Test fun hostNormalizationPreservesSubdomainAndHandlesInternationalNames() {
        assertEquals("api.example.com", kokoroConnectionRuleHost("API.Example.COM."))
        assertEquals("xn--bcher-kva.example", kokoroConnectionRuleHost("bücher.example"))
    }

    @Test fun suffixUsesRegistrableDomainWhileDomainUsesFullHost() {
        assertEquals("ip.sb", kokoroConnectionRulePayload("api.ip.sb", "DOMAIN-SUFFIX"))
        assertEquals("api.ip.sb", kokoroConnectionRulePayload("api.ip.sb", "DOMAIN"))
        assertEquals("example.co.uk", kokoroConnectionRulePayload("api.example.co.uk", "DOMAIN-SUFFIX"))
        assertEquals("example.com.tw", kokoroConnectionRulePayload("api.example.com.tw", "DOMAIN-SUFFIX"))
        assertEquals("owner.github.io", kokoroConnectionRulePayload("api.owner.github.io", "DOMAIN-SUFFIX"))
        assertEquals("xn--bcher-kva.de", kokoroConnectionRulePayload("api.bücher.de", "DOMAIN-SUFFIX"))
    }

    @Test fun suffixFallbackPreservesLocalNamesAndDoesNotAcceptIps() {
        assertEquals("localhost", kokoroConnectionRulePayload("localhost", "DOMAIN-SUFFIX"))
        assertEquals("co.uk", kokoroConnectionRulePayload("co.uk", "DOMAIN-SUFFIX"))
        assertNull(kokoroConnectionRulePayload("198.18.0.1", "DOMAIN-SUFFIX"))
        assertNull(kokoroConnectionRulePayload("", "DOMAIN-SUFFIX"))
    }

    @Test fun ipAndMissingHostsNeverBecomeDomainRules() {
        listOf("", "198.18.0.1", "1.1.1.1", "::1", "[2001:db8::1]", "example.com:443", "bad,host", "a..com")
            .forEach { assertNull(it, kokoroConnectionRuleHost(it)) }
    }

    @Test fun newRuleRunsBeforeExistingRulesAndTerminalMatch() {
        val broad = KokoroCustomRuleInput("DOMAIN-SUFFIX", "example.com", "DIRECT")
        val terminal = KokoroCustomRuleInput("MATCH", null, "PROXY")
        val narrow = KokoroCustomRuleInput("DOMAIN-SUFFIX", "api.example.com", "PROXY")
        assertEquals(listOf(narrow, broad, terminal), insertKokoroConnectionRule(listOf(broad, terminal), narrow))
        assertEquals(listOf(narrow, broad, terminal), insertKokoroConnectionRule(listOf(broad, narrow, terminal), narrow))
    }

    @Test fun changingSeedToMatchReplacesTerminalWithoutPuttingItFirst() {
        val domain = KokoroCustomRuleInput("DOMAIN", "example.com", "DIRECT")
        val old = KokoroCustomRuleInput("MATCH", null, "PROXY")
        val replacement = KokoroCustomRuleInput("MATCH", null, "DIRECT")
        assertEquals(listOf(domain, replacement), insertKokoroConnectionRule(listOf(domain, old), replacement))
    }
}
