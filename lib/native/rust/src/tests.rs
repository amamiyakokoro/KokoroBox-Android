use super::*;
use serde_json::json;
use std::sync::atomic::{AtomicU64, Ordering};

#[test]
fn applies_geosite_matcher_override() {
    let mut profile = json!({"geosite-matcher": "succinct"});
    apply_override_document(&mut profile, &json!({"geosite-matcher": "mph"}));
    assert_eq!(profile["geosite-matcher"], "mph");
}

#[test]
fn cors_override_prepends_replaces_and_appends_origins() {
    let mut profile = json!({"external-controller-cors": {
        "allow-origins": ["https://base.example"], "allow-private-network": false
    }});
    apply_override_document(
        &mut profile,
        &json!({"external-controller-cors": {
            "allow-origins-start": ["https://start.example"],
            "allow-origins": ["https://replace.example"],
            "allow-origins-end": ["https://end.example"],
            "allow-private-network": true
        }}),
    );
    assert_eq!(
        profile["external-controller-cors"],
        json!({
            "allow-origins": ["https://start.example", "https://replace.example", "https://end.example"],
            "allow-private-network": true
        })
    );
}

#[test]
fn forced_cors_replaces_whole_object_and_overrides_regular_patch() {
    let mut profile = json!({"external-controller-cors": {
        "allow-origins": ["https://base.example"], "allow-private-network": true
    }});
    apply_override_document(
        &mut profile,
        &json!({
            "external-controller-cors": {"allow-origins-end": ["https://ignored.example"]},
            "external-controller-cors-force": {"allow-origins": ["https://forced.example"]}
        }),
    );
    assert_eq!(
        profile["external-controller-cors"],
        json!({
            "allow-origins": ["https://forced.example"]
        })
    );
}

#[test]
fn merge_preserves_force_fields_and_explicit_false() {
    let patch = json!({
        "dns-force": {"enable": true, "fake-ip-range": "198.18.0.1/16"},
        "sniffer-force": {"enable": true, "sniff": {"HTTP": {"override-destination": true}}},
        "geox-url-force": {"geosite": "https://example.com/geosite.dat"},
        "external-controller-cors-force": {"allow-private-network": false}
    });
    let mut combined = json!({});
    merge_override_document(&mut combined, &patch);
    assert_eq!(combined, patch);
    let mut profile = json!({"external-controller-cors": {"allow-private-network": true}});
    apply_override_document(&mut profile, &combined);
    assert_eq!(profile["dns"]["fake-ip-range"], "198.18.0.1/16");
    assert_eq!(
        profile["sniffer"]["sniff"]["HTTP"]["override-destination"],
        true
    );
    assert_eq!(
        profile["geox-url"]["geosite"],
        "https://example.com/geosite.dat"
    );
    assert_eq!(
        profile["external-controller-cors"]["allow-private-network"],
        false
    );
}

#[test]
fn merges_extended_fields_before_applying_compiled_override() {
    let mut combined = json!({
        "external-controller-cors": {"allow-origins": ["https://base.example"]},
        "dns": {}, "clash-for-android": {}, "profile": {}, "tun": {}
    });
    merge_override_document(
        &mut combined,
        &json!({
            "external-controller-cors": {
                "allow-origins-start": ["https://start.example"],
                "allow-origins": ["https://override.example"], "allow-private-network": true
            },
            "interface-name": "wlan0", "global-client-fingerprint": "chrome",
            "dns": {"ipv6-timeout": 120, "cache-max-size": 4096},
            "clash-for-android": {"append-system-dns": true},
            "profile": {"store-selected": true},
            "tun": {"auto-route": true, "mtu": 1500}
        }),
    );
    let mut profile = json!({});
    apply_override_document(&mut profile, &combined);
    assert_eq!(
        profile,
        json!({
            "external-controller-cors": {
                "allow-origins": ["https://start.example", "https://override.example"],
                "allow-private-network": true
            },
            "interface-name": "wlan0", "global-client-fingerprint": "chrome",
            "dns": {"ipv6-timeout": 120, "cache-max-size": 4096},
            "clash-for-android": {"append-system-dns": true},
            "profile": {"store-selected": true},
            "tun": {"auto-route": true, "mtu": 1500}
        })
    );
}

struct Fixture(PathBuf);

impl Fixture {
    fn new() -> Self {
        static NEXT_ID: AtomicU64 = AtomicU64::new(0);
        let path = std::env::temp_dir().join(format!(
            "kokorobox-override-test-{}-{}",
            std::process::id(),
            NEXT_ID.fetch_add(1, Ordering::Relaxed)
        ));
        fs::create_dir(&path).expect("create test directory");
        Self(path)
    }
}

impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = fs::remove_dir_all(&self.0);
    }
}

#[test]
fn compile_pipeline_applies_ordered_files_and_writes_same_yaml_as_preview() {
    let fixture = Fixture::new();
    let source = fixture.0.join("config.yaml");
    let first = fixture.0.join("first.json");
    let second = fixture.0.join("second.json");
    let output = fixture.0.join("runtime/config.yaml");
    fs::write(&source, "geosite-matcher: succinct\ndns:\n  enable: true\n  nameserver: [9.9.9.9]\nexternal-controller-cors:\n  allow-origins: [https://base.example]\n  allow-private-network: true\n").unwrap();
    fs::write(
        &first,
        json!({
            "geosite-matcher": "mph",
            "external-controller-cors": {"allow-origins-start": ["https://first.example"]},
            "dns-force": {"enable": true, "nameserver": ["1.1.1.1"]}
        })
        .to_string(),
    )
    .unwrap();
    fs::write(
        &second,
        json!({
            "external-controller-cors": {
                "allow-origins-start": ["https://second.example"],
                "allow-origins-end": ["https://last.example"], "allow-private-network": false
            },
            "dns-force": {"enable": true, "nameserver": ["8.8.8.8"]}
        })
        .to_string(),
    )
    .unwrap();
    let request = json!({
        "profileUuid": "test-profile", "profileDir": fixture.0,
        "profilePath": source, "overridePaths": [first, second], "outputPath": output
    })
    .to_string();
    let preview = compile_request(&request, false).expect("compile preview");
    assert!(!output.exists());
    let compiled = compile_request(&request, true).expect("compile output file");
    assert!(compiled.success);
    assert_eq!(compiled.final_yaml, preview.final_yaml);
    assert_eq!(compiled.fingerprint, preview.fingerprint);
    assert_eq!(fs::read_to_string(&output).unwrap(), compiled.final_yaml);
    let result: JsonValue = serde_yaml::from_str(&compiled.final_yaml).unwrap();
    assert_eq!(result["geosite-matcher"], "mph");
    assert_eq!(
        result["external-controller-cors"],
        json!({
            "allow-origins": ["https://first.example", "https://second.example", "https://base.example", "https://last.example"],
            "allow-private-network": false
        })
    );
    assert_eq!(result["dns"]["nameserver"], json!(["8.8.8.8"]));
    assert!(!result.as_object().unwrap().contains_key("dns-force"));
}
