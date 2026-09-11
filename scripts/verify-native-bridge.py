#!/usr/bin/env python3
"""Reject native bridge outputs that do not match the app's JNI namespace."""

import argparse
import os
from pathlib import Path


RETIRED_NAMESPACE = b"com/github/yumelira/yumebox/core/bridge/"


def relative_path(path: Path, root: Path) -> str:
    return os.path.relpath(path, root)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--class-name", required=True)
    parser.add_argument("--root", type=Path, required=True)
    parser.add_argument("bridge_libraries", type=Path, nargs="+")
    args = parser.parse_args()

    expected = args.class_name.encode()
    expected_source = b'"' + expected + b'"'
    failures: list[str] = []

    if expected_source not in args.source.read_bytes():
        failures.append(f"Native bridge source does not reference {args.class_name}")

    for library in args.bridge_libraries:
        relative = relative_path(library, args.root)
        if not library.is_file():
            failures.append(f"Missing {relative}")
            continue

        contents = library.read_bytes()
        if expected not in contents:
            failures.append(f"{relative} has a stale JNI namespace")
        if RETIRED_NAMESPACE in contents:
            failures.append(f"{relative} still contains the retired Yumebox JNI namespace")

    if failures:
        for failure in failures:
            print(f"error: {failure}")
        print("Run: kotlin scripts/native-build.main.kts --cpp")
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
