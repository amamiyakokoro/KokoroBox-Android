package com.amamiyakokoro.box.service.runtime.session

import com.amamiyakokoro.box.core.model.ConfigurationOverride
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class OverseasDnsPresetTest {
    private val root = Json.parseToJsonElement(OVERSEAS_DNS_OVERRIDE).jsonObject
    private val dns = root.getValue("dns-force").jsonObject
    private fun servers(key: String) = dns.getValue(key).jsonArray.map { it.jsonPrimitive.content }

    @Test fun bootstrapUsesOnlyRequestedOverseasIps() {
        assertEquals(listOf("1.1.1.1", "8.8.8.8"), servers("default-nameserver"))
    }

    @Test fun normalProxyAndDirectQueriesShareTheSameGlobalResolvers() {
        val expected = listOf("https://1.1.1.1/dns-query", "https://8.8.8.8/dns-query")
        for (key in listOf("nameserver", "proxy-server-nameserver", "direct-nameserver")) {
            assertEquals(key, expected, servers(key))
        }
        for (regional in listOf("223.5.5.5", "119.29.29.29", "dns.alidns.com", "doh.pub", "geosite:")) {
            assertFalse(regional, OVERSEAS_DNS_OVERRIDE.contains(regional))
        }
    }

    @Test fun clearsSubscriptionPoliciesAndFallbackRatherThanMergingThem() {
        assertTrue(dns.getValue("nameserver-policy").jsonObject.isEmpty())
        assertTrue(dns.getValue("proxy-server-nameserver-policy").jsonObject.isEmpty())
        assertTrue(dns.getValue("fallback").jsonArray.isEmpty())
        assertTrue(dns.getValue("fallback-filter").jsonObject.isEmpty())
        assertFalse(dns.getValue("respect-rules").jsonPrimitive.boolean)
        assertFalse(dns.getValue("direct-nameserver-follow-policy").jsonPrimitive.boolean)
        assertFalse(root.getValue("clash-for-android").jsonObject.getValue("append-system-dns").jsonPrimitive.boolean)
    }

    @Test fun forcePresetDecodesWithExistingOverrideModel() {
        val config = Json.decodeFromString<ConfigurationOverride>(OVERSEAS_DNS_OVERRIDE)
        val forced = requireNotNull(config.dnsForce)
        assertEquals(true, forced.enable)
        assertEquals(listOf("1.1.1.1", "8.8.8.8"), forced.defaultServer)
        assertTrue(forced.nameserverPolicy!!.isEmpty())
        assertTrue(forced.fallback!!.isEmpty())
    }
}
