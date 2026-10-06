package com.amamiyakokoro.box.data.integration.kokoro

import okhttp3.HttpUrl
import java.net.IDN
import java.util.Locale

/** Normalize connection metadata without accepting IP addresses or a display host with a port. */
fun kokoroConnectionRuleHost(host: String): String? {
    val candidate = host.trim().trimEnd('.').lowercase(Locale.ROOT)
    if (candidate.isEmpty() || ':' in candidate || candidate.all { it.isDigit() || it == '.' }) return null
    return runCatching { IDN.toASCII(candidate, IDN.USE_STD3_ASCII_RULES).lowercase(Locale.ROOT) }
        .getOrNull()?.takeIf { it.length <= 253 && it.split('.').all { label -> label.isNotEmpty() } }
}

/** Use the bundled public suffix list, including private suffixes such as github.io. */
fun kokoroConnectionRulePayload(host: String, type: String): String? {
    val normalized = kokoroConnectionRuleHost(host) ?: return null
    if (type != "DOMAIN-SUFFIX") return normalized
    return HttpUrl.Builder().scheme("https").host(normalized).build().topPrivateDomain()
        ?: normalized
}

fun preferredKokoroDomainRuleType(types: List<String>): String =
    listOf("DOMAIN-SUFFIX", "DOMAIN").firstOrNull { it in types }
        ?: types.firstOrNull() ?: "DOMAIN-SUFFIX"

/** New exceptions run first; a terminal MATCH remains last and appears only once. */
fun insertKokoroConnectionRule(
    rules: List<KokoroCustomRuleInput>,
    rule: KokoroCustomRuleInput,
): List<KokoroCustomRuleInput> = if (rule.type == "MATCH") {
    rules.filterNot { it.type == "MATCH" } + rule
} else {
    listOf(rule) + rules.filterNot { it == rule }
}
