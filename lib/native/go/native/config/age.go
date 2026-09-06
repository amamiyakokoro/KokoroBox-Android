package config

import (
	"strings"

	"github.com/metacubex/mihomo/component/age"
)

func SetGlobalSecretKeys(secretKeys ...string) {
	trimmed := make([]string, 0, len(secretKeys))
	for _, secretKey := range secretKeys {
		key := strings.TrimSpace(secretKey)
		if key == "" {
			continue
		}
		trimmed = append(trimmed, key)
	}
	age.SetGlobalSecretKeys(trimmed...)
}

func GenX25519KeyPair() (secretKey string, publicKey string, err error) {
	return age.GenX25519KeyPair()
}

func GenHybridKeyPair() (secretKey string, publicKey string, err error) {
	return age.GenHybridKeyPair()
}

func ToPublicKeys(secretKeys ...string) (publicKeys []string, err error) {
	trimmed := make([]string, 0, len(secretKeys))
	for _, secretKey := range secretKeys {
		trimmed = append(trimmed, strings.TrimSpace(secretKey))
	}
	return age.ToPublicKeys(trimmed...)
}

func VerifySecretKeys(secretKeys ...string) error {
	trimmed := make([]string, 0, len(secretKeys))
	for _, secretKey := range secretKeys {
		trimmed = append(trimmed, strings.TrimSpace(secretKey))
	}
	return age.VeritySecretKeys(trimmed...)
}

func VerifyPublicKeys(publicKeys ...string) error {
	trimmed := make([]string, 0, len(publicKeys))
	for _, publicKey := range publicKeys {
		trimmed = append(trimmed, strings.TrimSpace(publicKey))
	}
	return age.VerityPublicKeys(trimmed...)
}
