package tunnel

import (
	"context"
	"errors"
	"net"
	"testing"

	"github.com/metacubex/mihomo/adapter/outboundgroup"
	"github.com/metacubex/mihomo/common/utils"
	C "github.com/metacubex/mihomo/constant"
	D "github.com/miekg/dns"
)

type diagnosticDNSStub struct {
	reply    *D.Msg
	err      error
	question string
	calls    int
}

func (s *diagnosticDNSStub) ExchangeContext(ctx context.Context, msg *D.Msg) (*D.Msg, error) {
	s.calls++
	s.question = msg.Question[0].Name
	if ctx.Err() != nil {
		return nil, ctx.Err()
	}
	return s.reply, s.err
}

func TestDiagnosticDNSUsesCoreAnswersAndNormalizesIDN(t *testing.T) {
	s := &diagnosticDNSStub{reply: &D.Msg{Answer: []D.RR{
		&D.CNAME{Target: "target.example."}, &D.A{A: net.ParseIP("192.0.2.1")},
	}}}
	r := diagnoseDNS(context.Background(), "例子.com", D.TypeA, s)
	if s.question != "xn--fsqu00a.com." || r.Status != "ok" || r.Rcode != "NOERROR" || len(r.Answers) != 2 {
		t.Fatalf("question=%s result=%+v", s.question, r)
	}
}

func TestDiagnosticDNSSkipsUnavailableTargets(t *testing.T) {
	for _, tc := range []struct{ host, status string }{{"", "no_host"}, {"192.0.2.1", "ip_literal"}, {"[2001:db8::1]", "ip_literal"}, {"bad domain", "invalid_host"}} {
		s := &diagnosticDNSStub{}
		r := diagnoseDNS(context.Background(), tc.host, D.TypeA, s)
		if r.Status != tc.status || s.calls != 0 {
			t.Fatalf("host=%s result=%+v calls=%d", tc.host, r, s.calls)
		}
	}
	if r := diagnoseDNS(context.Background(), "example.com", D.TypeA, nil); r.Status != "dns_disabled" {
		t.Fatal(r)
	}
}

func TestDiagnosticDNSDistinguishesEmptyNXDomainAndTimeout(t *testing.T) {
	empty := &diagnosticDNSStub{reply: &D.Msg{}}
	if r := diagnoseDNS(context.Background(), "example.com", D.TypeAAAA, empty); r.Status != "no_records" {
		t.Fatal(r)
	}
	nx := &diagnosticDNSStub{reply: &D.Msg{MsgHdr: D.MsgHdr{Rcode: D.RcodeNameError}}}
	if r := diagnoseDNS(context.Background(), "example.com", D.TypeA, nx); r.Status != "failed" || r.Rcode != "NXDOMAIN" {
		t.Fatal(r)
	}
	ctx, cancel := context.WithCancel(context.Background())
	cancel()
	if r := diagnoseDNS(ctx, "example.com", D.TypeA, empty); r.Status != "failed" || r.Error == "" {
		t.Fatal(r)
	}
}

type diagnosticProxyStub struct {
	C.Proxy
	delay   uint16
	err     error
	called  bool
	kind    C.AdapterType
	adapter C.ProxyAdapter
}

func (p *diagnosticProxyStub) Type() C.AdapterType     { return p.kind }
func (p *diagnosticProxyStub) Adapter() C.ProxyAdapter { return p.adapter }
func (p *diagnosticProxyStub) URLTest(context.Context, string, utils.IntRanges[uint16]) (uint16, error) {
	p.called = true
	return p.delay, p.err
}

func TestDiagnosticRouteNeverReportsCachedSuccessAfterFailure(t *testing.T) {
	p := &diagnosticProxyStub{delay: 123, err: errors.New("timeout")}
	r := diagnoseRoute(context.Background(), "node", p)
	if r.Status != "failed" || r.Delay != -1 || r.Error != "timeout" {
		t.Fatal(r)
	}
	p.err = nil
	if r := diagnoseRoute(context.Background(), "node", p); r.Status != "ok" || r.Delay != 123 {
		t.Fatal(r)
	}
}

func TestDiagnosticRouteSkipsRejectMissingAndEmpty(t *testing.T) {
	p := &diagnosticProxyStub{kind: C.Reject}
	if r := diagnoseRoute(context.Background(), "REJECT", p); r.Status != "not_testable" || p.called {
		t.Fatal(r)
	}
	if r := diagnoseRoute(context.Background(), "removed", nil); r.Status != "not_found" {
		t.Fatal(r)
	}
	if r := diagnoseRoute(context.Background(), "", nil); r.Status != "no_route" {
		t.Fatal(r)
	}
}

func TestDiagnosticRouteNeverTestsAutomaticGroup(t *testing.T) {
	p := &diagnosticProxyStub{kind: C.Selector, adapter: &outboundgroup.Selector{}}
	if r := diagnoseRoute(context.Background(), "group", p); r.Status != "group_only" || p.called {
		t.Fatal(r)
	}
}
