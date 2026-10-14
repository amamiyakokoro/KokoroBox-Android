package tunnel

import (
	"context"
	"net/netip"
	"strings"
	"sync"
	"time"

	"github.com/metacubex/mihomo/adapter/outboundgroup"
	"github.com/metacubex/mihomo/component/resolver"
	C "github.com/metacubex/mihomo/constant"
	MT "github.com/metacubex/mihomo/tunnel"
	D "github.com/miekg/dns"
	"golang.org/x/net/idna"
)

type DNSDiagnostic struct {
	Type           string   `json:"type"`
	Status         string   `json:"status"`
	Rcode          string   `json:"rcode,omitempty"`
	Answers        []string `json:"answers"`
	DurationMillis int64    `json:"durationMillis"`
	Error          string   `json:"error,omitempty"`
}

type RouteDiagnostic struct {
	ProxyName string `json:"proxyName"`
	Status    string `json:"status"`
	Delay     int    `json:"delay"`
	TestURL   string `json:"testUrl"`
	Error     string `json:"error,omitempty"`
}

type ConnectionDiagnostic struct {
	DNSMode string          `json:"dnsMode"`
	DNS     []DNSDiagnostic `json:"dns"`
	Route   RouteDiagnostic `json:"route"`
}

// A single, bounded, user-requested operation. Never changes selectors or clears caches.
func DiagnoseConnection(host, proxyName string) ConnectionDiagnostic {
	ctx, cancel := context.WithTimeout(context.Background(), healthCheckTimeout)
	defer cancel()
	r := resolver.DefaultResolver
	mode := "disabled"
	if r != nil {
		mode = "normal"
		if resolver.FakeIPEnabled() {
			mode = "fake-ip"
		} else if resolver.MappingEnabled() {
			mode = "redir-host"
		}
	}
	result := ConnectionDiagnostic{DNSMode: mode, DNS: make([]DNSDiagnostic, 2)}
	var wg sync.WaitGroup
	for i, qtype := range []uint16{D.TypeA, D.TypeAAAA} {
		wg.Add(1)
		go func(i int, qtype uint16) {
			defer wg.Done()
			result.DNS[i] = diagnoseDNS(ctx, host, qtype, r)
		}(i, qtype)
	}
	wg.Add(1)
	go func() { defer wg.Done(); result.Route = diagnoseRoute(ctx, proxyName, findDiagnosticProxy(proxyName)) }()
	wg.Wait()
	return result
}

type dnsExchanger interface {
	ExchangeContext(context.Context, *D.Msg) (*D.Msg, error)
}

func diagnoseDNS(ctx context.Context, host string, qtype uint16, r dnsExchanger) DNSDiagnostic {
	result := DNSDiagnostic{Type: D.TypeToString[qtype], Answers: []string{}}
	host = strings.TrimSpace(host)
	if host == "" {
		result.Status = "no_host"
		return result
	}
	if _, err := netip.ParseAddr(strings.Trim(host, "[]")); err == nil {
		result.Status = "ip_literal"
		return result
	}
	ascii, err := idna.Lookup.ToASCII(host)
	if err != nil {
		result.Status = "invalid_host"
		return result
	}
	if _, valid := D.IsDomainName(ascii); !valid {
		result.Status = "invalid_host"
		return result
	}
	if r == nil {
		result.Status = "dns_disabled"
		return result
	}
	msg := &D.Msg{}
	msg.SetQuestion(D.Fqdn(ascii), qtype)
	started := time.Now()
	response, err := r.ExchangeContext(ctx, msg)
	result.DurationMillis = time.Since(started).Milliseconds()
	if err != nil {
		result.Status = "failed"
		result.Error = err.Error()
		return result
	}
	if response == nil {
		result.Status = "failed"
		return result
	}
	result.Rcode = D.RcodeToString[response.Rcode]
	if response.Rcode != D.RcodeSuccess {
		result.Status = "failed"
		return result
	}
	for _, answer := range response.Answer {
		switch rr := answer.(type) {
		case *D.A:
			result.Answers = append(result.Answers, rr.A.String())
		case *D.AAAA:
			result.Answers = append(result.Answers, rr.AAAA.String())
		case *D.CNAME:
			result.Answers = append(result.Answers, "CNAME "+rr.Target)
		}
		if len(result.Answers) >= 16 {
			break
		}
	}
	result.Status = "ok"
	if len(result.Answers) == 0 {
		result.Status = "no_records"
	}
	return result
}

func findDiagnosticProxy(name string) C.Proxy {
	if proxy := MT.Proxies()[name]; proxy != nil {
		return proxy
	}
	for _, provider := range MT.Providers() {
		for _, proxy := range provider.Proxies() {
			if proxy.Name() == name {
				return proxy
			}
		}
	}
	return nil
}

func diagnoseRoute(ctx context.Context, name string, proxy C.Proxy) RouteDiagnostic {
	result := RouteDiagnostic{ProxyName: name, Delay: -1, TestURL: defaultTestURL}
	if name == "" {
		result.Status = "no_route"
		return result
	}
	if proxy == nil {
		result.Status = "not_found"
		return result
	}
	switch proxy.Type() {
	case C.Reject, C.RejectDrop, C.Pass, C.PassRule:
		result.Status = "not_testable"
		return result
	}
	// Testing an automatic group could test a different node from this recorded connection.
	if _, group := proxy.Adapter().(outboundgroup.ProxyGroup); group {
		result.Status = "group_only"
		return result
	}
	delay, err := proxy.URLTest(ctx, defaultTestURL, nil)
	if err != nil || delay == 0xffff {
		result.Status = "failed"
		if err != nil {
			result.Error = err.Error()
		}
		return result
	}
	result.Status = "ok"
	result.Delay = int(delay)
	return result
}
