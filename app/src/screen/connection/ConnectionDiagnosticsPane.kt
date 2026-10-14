/* This file is part of KokoroBox. Licensed under the GNU AGPL, version 3 or later. */
package com.amamiyakokoro.box.screen.connection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amamiyakokoro.box.core.model.*
import com.amamiyakokoro.box.core.locale.R as LocaleR
import com.amamiyakokoro.box.presentation.component.Md3EIndeterminateCircularWavyProgressIndicator

@Composable
internal fun ConnectionDiagnosticsPane(connection: ConnectionInfo, viewModel: ConnectionDiagnosticsViewModel, active: Boolean) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleStartEffect(viewModel, active) {
        if (!active) viewModel.cancel()
        onStopOrDispose { viewModel.cancel() }
    }
    ConnectionDiagnosticsContent(connection, state, enabled = active, onRun = { viewModel.run(connection) })
}

@Composable
internal fun ConnectionDiagnosticsContent(
    connection: ConnectionInfo,
    state: ConnectionDiagnosticsState,
    enabled: Boolean = true,
    onRun: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DiagnosticCard {
            DiagnosticValue(stringResource(LocaleR.string.connection_diagnostics_host), connection.diagnosticHost().ifEmpty {
                stringResource(LocaleR.string.connection_diagnostics_no_host)
            })
            DiagnosticValue(stringResource(LocaleR.string.connection_diagnostics_matched_rule),
                listOf(connection.rule, connection.rulePayload).filter(String::isNotBlank).joinToString(" · ").ifEmpty { "—" })
            DiagnosticValue(stringResource(LocaleR.string.connection_diagnostics_route), connection.chains.asReversed().joinToString(" → ").ifEmpty { "—" })
        }
        Button(onClick = onRun, enabled = enabled && !state.running, modifier = Modifier.fillMaxWidth()) {
            if (state.running) {
                Md3EIndeterminateCircularWavyProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(stringResource(if (state.running) LocaleR.string.connection_diagnostics_running else LocaleR.string.connection_diagnostics_run))
        }
        state.failure?.let { failure ->
            Text(stringResource(when (failure) {
                DiagnosticFailure.Stopped -> LocaleR.string.connection_diagnostics_stopped
                DiagnosticFailure.Unsupported -> LocaleR.string.connection_diagnostics_unsupported
                DiagnosticFailure.Timeout -> LocaleR.string.connection_diagnostics_timeout
                DiagnosticFailure.Failed -> LocaleR.string.connection_diagnostics_failed
            }), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        state.result?.let { result ->
            DiagnosticCard {
                Text(stringResource(LocaleR.string.connection_diagnostics_dns), style = MaterialTheme.typography.titleSmall)
                Text(if (result.dnsMode == "disabled") stringResource(LocaleR.string.connection_diagnostics_dns_disabled)
                    else "${stringResource(LocaleR.string.connection_diagnostics_dns_mode)} · ${result.dnsMode}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                result.dns.forEach { dns ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dns.type, style = MaterialTheme.typography.labelLarge)
                        if (dns.status == "ok" || dns.status == "no_records") {
                            Text("${dns.durationMillis} ms", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (dns.status != "ok") Text(diagnosticStatus(dns.status),
                        color = if (dns.status == "failed" || dns.status == "invalid_host") MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                    if (dns.rcode.isNotEmpty() && dns.rcode != "NOERROR") Text(dns.rcode,
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (dns.answers.isNotEmpty()) SelectionContainer {
                        Text(dns.answers.joinToString("\n"), style = MaterialTheme.typography.bodyMedium)
                    }
                    if (dns.error.isNotEmpty()) Text(dns.error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                Text(stringResource(LocaleR.string.connection_diagnostics_dns_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DiagnosticCard {
                Text(stringResource(LocaleR.string.connection_diagnostics_latency), style = MaterialTheme.typography.titleSmall)
                DiagnosticValue(stringResource(LocaleR.string.connection_diagnostics_node), result.route.proxyName.ifEmpty { "—" })
                Text(if (result.route.status == "ok") "${result.route.delay} ms" else diagnosticStatus(result.route.status),
                    style = MaterialTheme.typography.bodyMedium, color = if (result.route.status == "failed")
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                if (result.route.error.isNotEmpty()) Text(result.route.error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                DiagnosticValue(stringResource(LocaleR.string.connection_diagnostics_endpoint), result.route.testUrl)
                Text(stringResource(LocaleR.string.connection_diagnostics_latency_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DiagnosticCard(content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun DiagnosticValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SelectionContainer { Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) }
    }
}

@Composable
private fun diagnosticStatus(status: String): String = stringResource(when (status) {
    "ok" -> LocaleR.string.connection_diagnostics_ok
    "no_host" -> LocaleR.string.connection_diagnostics_no_host
    "ip_literal" -> LocaleR.string.connection_diagnostics_ip_literal
    "invalid_host" -> LocaleR.string.connection_diagnostics_invalid_host
    "dns_disabled" -> LocaleR.string.connection_diagnostics_dns_disabled
    "no_records" -> LocaleR.string.connection_diagnostics_no_records
    "no_route" -> LocaleR.string.connection_diagnostics_no_route
    "not_found" -> LocaleR.string.connection_diagnostics_not_found
    "not_testable" -> LocaleR.string.connection_diagnostics_not_testable
    "group_only" -> LocaleR.string.connection_diagnostics_group_only
    else -> LocaleR.string.connection_diagnostics_failed
})

@androidx.compose.ui.tooling.preview.Preview(name = "Diagnostics / narrow", widthDp = 320, heightDp = 700)
@androidx.compose.ui.tooling.preview.Preview(name = "Diagnostics / dark", widthDp = 320, heightDp = 700, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConnectionDiagnosticsPreview() {
    com.amamiyakokoro.box.presentation.theme.YumeTheme {
        Surface {
            ConnectionDiagnosticsContent(
                connection = ConnectionInfo(metadata = kotlinx.serialization.json.buildJsonObject {
                    put("host", kotlinx.serialization.json.JsonPrimitive("chatgpt.com"))
                }, chains = listOf("Direct-Special.kokoro.anytls", "Direct-Special"), rule = "RuleSet", rulePayload = "openai"),
                state = ConnectionDiagnosticsState(result = ConnectionDiagnostic("fake-ip", listOf(
                    DnsDiagnostic("A", "ok", "NOERROR", listOf("104.26.13.31"), 24),
                    DnsDiagnostic("AAAA", "no_records", "NOERROR", durationMillis = 28),
                ), RouteDiagnostic("Direct-Special.kokoro.anytls", "ok", 132, "https://www.gstatic.com/generate_204"))),
                onRun = {},
            )
        }
    }
}
