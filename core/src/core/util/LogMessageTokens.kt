/*
 * This file is part of KokoroBox.
 * Licensed under the GNU Affero General Public License, version 3 or later.
 */
package com.amamiyakokoro.box.core.util

enum class LogTokenKind { Text, Protocol, Address, Domain, Port, Process, Rule, Route, Keyword, Error }

data class LogToken(val kind: LogTokenKind, val value: String)

data class ParsedLogMessage(val primary: List<LogToken>, val secondary: List<LogToken> = emptyList())

private val tokenPatterns = listOf(
    LogTokenKind.Protocol to """\[(?:TCP|UDP|HTTP|HTTPS|SOCKS5|DNS)(?:\([^)]*\))?]|\b(?:TCP|UDP|HTTP|HTTPS|SOCKS5|DNS)\b(?:\([^)]*\))?(?![\w.-])""",
    LogTokenKind.Rule to """\b(?:RuleSet|Rule-Set|GeoIP|GeoSite|DomainSuffix|DomainKeyword|DomainRegex|Domain|IPCIDR6?|IP-CIDR6?|SrcIPCIDR|ProcessName|ProcessPath|Network|MATCH)\([^)]*\)""",
    LogTokenKind.Address to """\b(?:\d{1,3}\.){3}\d{1,3}\b|\[(?=[0-9a-f:.]*:)[0-9a-f:.]+(?:%[\w.-]+)?]""",
    LogTokenKind.Port to """:\d{1,5}\b""",
    LogTokenKind.Process to """\([^()\s]+(?:[,\s]+uid=\d+)?\)""",
    LogTokenKind.Domain to """\b(?:[a-z\d](?:[a-z\d-]{0,61}[a-z\d])?\.)+[a-z\d-]{2,63}\b""",
    LogTokenKind.Route to """\b(?:DIRECT|REJECT(?:-DROP)?|PASS)\b""",
    LogTokenKind.Error to """\berror:|\b(?:failed|timeout|refused)\b""",
    LogTokenKind.Keyword to """\bmatch\b""",
)
private val semanticTokens = Regex(
    tokenPatterns.joinToString("|") { (kind, pattern) -> "(?<${kind.name}>$pattern)" },
    RegexOption.IGNORE_CASE,
)
private val detailMarker = Regex("""\s+(?=(?:match|using|proxy chain)\b)""", RegexOption.IGNORE_CASE)
private val routeMarker = Regex("""\b(?:using|proxy chain)\b""", RegexOption.IGNORE_CASE)

/** Display-only parsing, mirroring Desktop's primary connection / secondary routing lines. */
fun parseLogMessage(value: String): ParsedLogMessage {
    val marker = detailMarker.find(value) ?: return ParsedLogMessage(tokenizeLogText(value))
    return ParsedLogMessage(
        primary = tokenizeLogText(value.substring(0, marker.range.first).trimEnd()),
        secondary = tokenizeLogDetails(value.substring(marker.range.first).trim()),
    )
}

private fun tokenizeLogText(value: String): List<LogToken> = buildList {
    var cursor = 0
    for (match in semanticTokens.findAll(value)) {
        if (match.range.first > cursor) add(LogToken(LogTokenKind.Text, value.substring(cursor, match.range.first)))
        val kind = tokenPatterns.first { (kind, _) -> match.groups[kind.name] != null }.first
        add(LogToken(kind, match.value))
        cursor = match.range.last + 1
    }
    if (cursor < value.length) add(LogToken(LogTokenKind.Text, value.substring(cursor)))
}

private fun tokenizeLogDetails(value: String): List<LogToken> {
    val marker = routeMarker.find(value) ?: return tokenizeLogText(value)
    val routeValue = value.substring(marker.range.last + 1)
    val leadingSpace = routeValue.takeWhile(Char::isWhitespace)
    val target = routeValue.substring(leadingSpace.length)
    return buildList {
        addAll(tokenizeLogText(value.substring(0, marker.range.first)))
        add(LogToken(LogTokenKind.Keyword, marker.value))
        if (leadingSpace.isNotEmpty()) add(LogToken(LogTokenKind.Text, leadingSpace))
        if (target.isNotEmpty()) add(LogToken(LogTokenKind.Route, target))
    }
}
