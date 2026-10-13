package tunnel

import (
	"github.com/metacubex/mihomo/tunnel/statistic"
	"testing"
)

type countOnlyTracker struct {
	statistic.Tracker
	id string
}

func (c countOnlyTracker) ID() string { return c.id }
func (c countOnlyTracker) Info() *statistic.TrackerInfo {
	panic("count must not resolve connection metadata")
}

func TestConnectionCountWithoutMetadata(t *testing.T) {
	manager := new(statistic.Manager)
	if got := connectionCount(manager); got != 0 {
		t.Fatalf("empty = %d", got)
	}
	a, b := countOnlyTracker{id: "a"}, countOnlyTracker{id: "b"}
	manager.Join(a)
	manager.Join(b)
	manager.Join(a)
	if got := connectionCount(manager); got != 2 {
		t.Fatalf("active = %d", got)
	}
	manager.Leave(a)
	if got := connectionCount(manager); got != 1 {
		t.Fatalf("after close = %d", got)
	}
	manager.Leave(b)
	if got := connectionCount(manager); got != 0 {
		t.Fatalf("closed = %d", got)
	}
}
