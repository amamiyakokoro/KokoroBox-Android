/* This file is part of KokoroBox. Licensed under the GNU AGPL, version 3 or later. */
package com.amamiyakokoro.box.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Serializable
data class ConnectionDiagnostic(
    val dnsMode: String = "disabled",
    val dns: List<DnsDiagnostic> = emptyList(),
    val route: RouteDiagnostic = RouteDiagnostic(),
)

@Serializable
data class DnsDiagnostic(
    val type: String,
    val status: String,
    val rcode: String = "",
    val answers: List<String> = emptyList(),
    val durationMillis: Long = 0,
    val error: String = "",
)

@Serializable
data class RouteDiagnostic(
    val proxyName: String = "",
    val status: String = "no_route",
    val delay: Int = -1,
    val testUrl: String = "",
    val error: String = "",
)

/** Mihomo appends groups after the outbound node; test the recorded node, not today's group selection. */
fun ConnectionInfo.diagnosticProxyName(): String = chains.firstOrNull().orEmpty()

fun ConnectionInfo.diagnosticHost(): String =
    (metadata["host"] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
