#!/usr/bin/env python3
"""Reject native outputs that do not match the app's JNI namespace."""

import argparse
import os
from pathlib import Path


RETIRED_NAMESPACE = "com/github/yumelira/yumebox/core/bridge"


def jni_symbol(class_name: str, method_name: str) -> bytes:
    return f"Java_{class_name.replace('/', '_')}_{method_name}".encode()


def contains_retired_namespace(contents: bytes) -> bool:
    return any(
        marker.encode() in contents
        for marker in (
            RETIRED_NAMESPACE,
            RETIRED_NAMESPACE.replace("/", "_"),
            RETIRED_NAMESPACE.replace("/", "."),
        )
    )


def relative_path(path: Path, root: Path) -> str:
    return os.path.relpath(path, root)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--cpp-source", type=Path, required=True)
    parser.add_argument("--rust-source", type=Path, required=True)
    parser.add_argument("--bridge-class-name", required=True)
    parser.add_argument("--callback-class-name", required=True)
    parser.add_argument("--root", type=Path, required=True)
    parser.add_argument("--bridge-libraries", type=Path, nargs="+", required=True)
    parser.add_argument("--override-libraries", type=Path, nargs="+", required=True)
    parser.add_argument("--other-libraries", type=Path, nargs="+", required=True)
    args = parser.parse_args()

    callback_class = args.callback_class_name.encode()
    retired_namespace = RETIRED_NAMESPACE.encode()
    required_methods = ("nativeCompilePreview", "nativeCompileToFile")
    failures: list[str] = []

    cpp_source = args.cpp_source.read_bytes()
    if b'"' + callback_class + b'"' not in cpp_source:
        failures.append(f"Native bridge source does not reference {args.callback_class_name}")
    if retired_namespace in cpp_source:
        failures.append("Native bridge source still contains the retired Yumebox JNI namespace")

    rust_source = args.rust_source.read_bytes()
    for method_name in required_methods:
        symbol = jni_symbol(args.bridge_class_name, method_name)
        if symbol not in rust_source:
            failures.append(f"Rust compiler source does not export {symbol.decode()}")

    for library in args.bridge_libraries:
        relative = relative_path(library, args.root)
        if not library.is_file():
            failures.append(f"Missing {relative}")
            continue

        contents = library.read_bytes()
        if callback_class not in contents:
            failures.append(f"{relative} has a stale JNI namespace")
        if contains_retired_namespace(contents):
            failures.append(f"{relative} still contains the retired Yumebox JNI namespace")

    for library in args.override_libraries:
        relative = relative_path(library, args.root)
        if not library.is_file():
            failures.append(f"Missing {relative}")
            continue

        contents = library.read_bytes()
        for method_name in required_methods:
            symbol = jni_symbol(args.bridge_class_name, method_name)
            if symbol not in contents:
                failures.append(f"{relative} does not export {symbol.decode()}")
        if contains_retired_namespace(contents):
            failures.append(f"{relative} still contains the retired Yumebox JNI namespace")

    for library in args.other_libraries:
        relative = relative_path(library, args.root)
        if not library.is_file():
            failures.append(f"Missing {relative}")
            continue
        if contains_retired_namespace(library.read_bytes()):
            failures.append(f"{relative} still contains the retired Yumebox JNI namespace")

    if failures:
        for failure in failures:
            print(f"error: {failure}")
        print("Run: kotlin scripts/native-build.main.kts --all")
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
