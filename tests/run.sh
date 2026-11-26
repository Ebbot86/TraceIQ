#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" app/src/main/java/se/traceiq/app/IpAddress.java tests/IpAddressTest.java
java -cp "$OUT" se.traceiq.app.IpAddressTest
