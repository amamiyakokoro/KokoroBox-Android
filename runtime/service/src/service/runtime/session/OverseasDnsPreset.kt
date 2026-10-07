/*
 * This file is part of KokoroBox.
 * Licensed under the GNU Affero General Public License, version 3 or later.
 */
package com.amamiyakokoro.box.service.runtime.session

internal val OVERSEAS_DNS_OVERRIDE = """
    {
      "dns-force": {
        "enable": true,
        "ipv6": true,
        "respect-rules": false,
        "enhanced-mode": "fake-ip",
        "fake-ip-range": "198.18.0.1/16",
        "fake-ip-filter": [
          "+.lan",
          "+.local",
          "time.*.com",
          "ntp.*.com",
          "+.market.xiaomi.com"
        ],
        "fake-ip-filter-mode": "blacklist",
        "use-hosts": false,
        "use-system-hosts": false,
        "default-nameserver": ["1.1.1.1", "8.8.8.8"],
        "nameserver": ["https://1.1.1.1/dns-query", "https://8.8.8.8/dns-query"],
        "proxy-server-nameserver": ["https://1.1.1.1/dns-query", "https://8.8.8.8/dns-query"],
        "direct-nameserver": ["https://1.1.1.1/dns-query", "https://8.8.8.8/dns-query"],
        "direct-nameserver-follow-policy": false,
        "nameserver-policy": {},
        "proxy-server-nameserver-policy": {},
        "fallback": [],
        "fallback-filter": {}
      },
      "clash-for-android": {
        "append-system-dns": false
      }
    }
""".trimIndent()
