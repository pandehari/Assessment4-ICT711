#!/usr/bin/env bash
# Compiles the application sources into out/production.
set -euo pipefail
cd "$(dirname "$0")"

SRC_DIR="src"
OUT_DIR="out/production"

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

find "$SRC_DIR" -name '*.java' > out/sources.txt
# -serial / -this-escape are expected on the Swing subclasses and add only noise.
javac -d "$OUT_DIR" -Xlint:all,-serial,-this-escape @out/sources.txt

echo "Compiled to $OUT_DIR"
