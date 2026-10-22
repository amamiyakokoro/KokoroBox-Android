package config

import (
	"reflect"
	"testing"

	"cfa/native/app"
	mihomoConfig "github.com/metacubex/mihomo/config"
	C "github.com/metacubex/mihomo/constant"
)

// Overrides are compiled by Rust. Go consumes the final YAML rather than the old override types.
func TestCompiledConfigPreservesOverrideFields(t *testing.T) {
	yaml := `geosite-matcher: mph
interface-name: wlan0
global-client-fingerprint: chrome
external-controller-cors:
  allow-origins: [https://start.example, https://replace.example, https://end.example]
  allow-private-network: true
dns:
  enable: true
  fake-ip-range: 198.18.0.1/16
  ipv6-timeout: 120
  cache-max-size: 4096
sniffer:
  enable: true
  sniff:
    HTTP:
      override-destination: true
geox-url:
  geosite: https://example.com/geosite.dat
clash-for-android:
  append-system-dns: true
profile:
  store-selected: true
tun:
  auto-route: true
  mtu: 1500
`
	raw, err := mihomoConfig.UnmarshalRawConfig([]byte(yaml))
	if err != nil {
		t.Fatal(err)
	}
	if raw.GeositeMatcher != "mph" || raw.Interface != "wlan0" || raw.GlobalClientFingerprint != "chrome" {
		t.Fatalf("lost general overrides: matcher=%q interface=%q fingerprint=%q", raw.GeositeMatcher, raw.Interface, raw.GlobalClientFingerprint)
	}
	wantOrigins := []string{"https://start.example", "https://replace.example", "https://end.example"}
	if !reflect.DeepEqual(raw.ExternalControllerCors.AllowOrigins, wantOrigins) || !raw.ExternalControllerCors.AllowPrivateNetwork {
		t.Fatalf("lost CORS overrides: %+v", raw.ExternalControllerCors)
	}
	if !raw.DNS.Enable || raw.DNS.FakeIPRange != "198.18.0.1/16" || raw.DNS.IPv6Timeout != 120 || raw.DNS.CacheMaxSize != 4096 {
		t.Fatalf("lost DNS overrides: %+v", raw.DNS)
	}
	overrideDest := raw.Sniffer.Sniff["HTTP"].OverrideDest
	if !raw.Sniffer.Enable || overrideDest == nil || !*overrideDest || raw.GeoXUrl.GeoSite != "https://example.com/geosite.dat" {
		t.Fatal("lost sniffer or Geo data overrides")
	}
	if !raw.ClashForAndroid.AppendSystemDNS || !raw.Profile.StoreSelected || !raw.Tun.AutoRoute || raw.Tun.MTU != 1500 {
		t.Fatal("lost Android, profile or TUN overrides")
	}
}

func TestCompiledConfigCorsPreservesExplicitFalseAndCoreDefault(t *testing.T) {
	for _, tc := range []struct {
		name, setting string
		want          bool
	}{
		{"omitted uses Mihomo default", "", true},
		{"explicit false survives", "  allow-private-network: false\n", false},
	} {
		t.Run(tc.name, func(t *testing.T) {
			raw, err := mihomoConfig.UnmarshalRawConfig([]byte("external-controller-cors:\n  allow-origins: [https://forced.example]\n" + tc.setting))
			if err != nil {
				t.Fatal(err)
			}
			if !reflect.DeepEqual(raw.ExternalControllerCors.AllowOrigins, []string{"https://forced.example"}) || raw.ExternalControllerCors.AllowPrivateNetwork != tc.want {
				t.Fatalf("unexpected compiled CORS: %+v", raw.ExternalControllerCors)
			}
		})
	}
}

const compiledProxyFixture = `mixed-port: 0
dns:
  enable: true
  nameserver: [1.1.1.1]
proxies:
  - name: "日本 [VM]"
    type: vmess
    server: 192.0.2.1
    port: 443
    uuid: 00000000-0000-4000-8000-000000000001
    alterId: 0
    cipher: auto
  - name: Trojan
    type: trojan
    server: 192.0.2.2
    port: 443
    password: example
proxy-groups:
  - name: Second
    type: select
    icon: https://example.com/icon.svg
    proxies: ["日本 [VM]", Trojan, DIRECT]
  - name: First
    type: load-balance
    proxies: [DIRECT]
rules:
  - MATCH,DIRECT
clash-for-android:
  ui-subtitle-pattern: '\[VM\]'
`

func isolatePreviewState(t *testing.T) string {
	t.Helper()
	previous := app.SubtitlePattern()
	previousHome := C.Path.HomeDir()
	profileDir := t.TempDir()
	C.SetHomeDir(profileDir)
	t.Cleanup(func() {
		C.SetHomeDir(previousHome)
		pattern := ""
		if previous != nil {
			pattern = previous.String()
		}
		app.ApplySubtitlePattern(pattern)
	})
	return profileDir
}

func TestCompiledGroupPreviewPreservesOrderTypesAndSubtitle(t *testing.T) {
	profileDir := isolatePreviewState(t)
	groups, err := QueryProxyGroupsFromCompiledYaml(compiledProxyFixture, profileDir, false)
	if err != nil {
		t.Fatal(err)
	}
	names := make([]string, len(groups))
	for i, group := range groups {
		names[i] = group.Name
	}
	if !reflect.DeepEqual(names, []string{"GLOBAL", "Second", "First"}) {
		t.Fatalf("lost group order: %v", names)
	}
	group := groups[1]
	if group.Type != "Selector" || group.Icon != "https://example.com/icon.svg" || len(group.Proxies) != 3 {
		t.Fatalf("unexpected group preview: %+v", group)
	}
	node := group.Proxies[0]
	if node.Name != "日本 [VM]" || node.Title != "日本" || node.Subtitle != "[VM]" || node.Type != "Vmess" {
		t.Fatalf("lost node identity/type/subtitle: %+v", node)
	}
	if group.Proxies[1].Type != "Trojan" || group.Proxies[2].Type != "Direct" {
		t.Fatalf("wrong proxy types: %+v", group.Proxies)
	}
}

func TestCompiledGroupPreviewFiltersNonSelectableGroups(t *testing.T) {
	profileDir := isolatePreviewState(t)
	groups, err := QueryProxyGroupsFromCompiledYaml(compiledProxyFixture, profileDir, true)
	if err != nil {
		t.Fatal(err)
	}
	if len(groups) != 2 || groups[0].Name != "GLOBAL" || groups[1].Name != "Second" {
		t.Fatalf("non-selectable group was not filtered: %+v", groups)
	}
}

func TestCompiledConfigQueriesRejectInvalidYamlAndProxyReferences(t *testing.T) {
	profileDir := isolatePreviewState(t)
	for _, yaml := range []string{"dns: [", "proxy-groups:\n  - name: Invalid\n    type: select\n    proxies: [missing-node]\n"} {
		if _, err := QueryProxyGroupsFromCompiledYaml(yaml, profileDir, false); err == nil {
			t.Fatalf("accepted invalid compiled profile: %s", yaml)
		}
	}
	if _, err := QueryConfigFromCompiledYaml("dns: ["); err == nil {
		t.Fatal("accepted malformed YAML")
	}
}

func TestCompiledConfigQueryPreservesUnknownFields(t *testing.T) {
	root, err := QueryConfigFromCompiledYaml("custom-field:\n  values: [one, two]\n")
	if err != nil {
		t.Fatal(err)
	}
	want := map[string]any{"custom-field": map[string]any{"values": []any{"one", "two"}}}
	if !reflect.DeepEqual(root, want) {
		t.Fatalf("lost unknown configuration fields: %#v", root)
	}
}
