#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" app/src/main/java/se/traceiq/app/IpAddress.java tests/IpAddressTest.java app/src/main/java/se/traceiq/app/GeoMap.java tests/GeoMapTest.java app/src/main/java/se/traceiq/app/ThreatValues.java tests/ThreatValuesTest.java
java -cp "$OUT" se.traceiq.app.IpAddressTest

java -cp "$OUT" se.traceiq.app.GeoMapTest

java -cp "$OUT" se.traceiq.app.ThreatValuesTest
