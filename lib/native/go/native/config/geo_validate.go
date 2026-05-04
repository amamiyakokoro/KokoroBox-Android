package config

import (
	"encoding/json"
	"fmt"
	"net"
	"os"
	"strings"

	"github.com/metacubex/mihomo/component/geodata/router"
	"github.com/oschwald/maxminddb-golang"
	"google.golang.org/protobuf/proto"
)

type GeoValidationResult struct {
	Valid   bool   `json:"valid"`
	Message string `json:"message,omitempty"`
}

func ValidateGeoFile(path string, typ string) (result GeoValidationResult) {
	defer func() {
		if r := recover(); r != nil {
			result = GeoValidationResult{Valid: false, Message: fmt.Sprintf("panic: %v", r)}
		}
	}()

	info, err := os.Stat(path)
	if err != nil {
		return GeoValidationResult{Valid: false, Message: err.Error()}
	}
	if info.Size() < 64*1024 {
		return GeoValidationResult{Valid: false, Message: "file is too small"}
	}

	switch strings.ToLower(typ) {
	case "geosite":
		return validateGeoSite(path)
	case "geoip", "country", "asn":
		return validateMaxMindLike(path)
	default:
		return GeoValidationResult{Valid: true}
	}
}

func ValidateGeoFileJson(path string, typ string) string {
	bytes, err := json.Marshal(ValidateGeoFile(path, typ))
	if err != nil {
		return `{"valid":false,"message":"marshal validation result failed"}`
	}
	return string(bytes)
}

func validateMaxMindLike(path string) GeoValidationResult {
	reader, err := maxminddb.Open(path)
	if err != nil {
		return GeoValidationResult{Valid: false, Message: err.Error()}
	}
	defer reader.Close()

	samples := []string{"1.1.1.1", "8.8.8.8", "114.114.114.114"}
	for _, sample := range samples {
		ip := net.ParseIP(sample)
		if ip == nil {
			continue
		}
		var record any
		if err := reader.Lookup(ip, &record); err != nil {
			return GeoValidationResult{Valid: false, Message: err.Error()}
		}
	}
	return GeoValidationResult{Valid: true}
}

func validateGeoSite(path string) GeoValidationResult {
	data, err := os.ReadFile(path)
	if err != nil {
		return GeoValidationResult{Valid: false, Message: err.Error()}
	}
	var list router.GeoSiteList
	if err := proto.Unmarshal(data, &list); err != nil {
		return GeoValidationResult{Valid: false, Message: err.Error()}
	}
	if len(list.Entry) == 0 {
		return GeoValidationResult{Valid: false, Message: "empty geosite list"}
	}
	for _, entry := range list.Entry {
		if entry != nil && strings.TrimSpace(entry.GetCountryCode()) != "" && len(entry.GetDomain()) > 0 {
			return GeoValidationResult{Valid: true}
		}
	}
	return GeoValidationResult{Valid: false, Message: "no usable geosite entry"}
}
