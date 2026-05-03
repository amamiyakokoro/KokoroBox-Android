package tunnel

import (
	"strings"

	"github.com/metacubex/mihomo/tunnel"
)

func QueryMode() string {
	return tunnel.Mode().String()
}

func SetMode(mode string) bool {
	parsed, ok := tunnel.ModeMapping[strings.ToLower(mode)]
	if !ok {
		return false
	}
	tunnel.SetMode(parsed)
	return true
}
