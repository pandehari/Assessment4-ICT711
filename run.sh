#!/usr/bin/env bash
# Builds if needed, then launches the application.
# Any arguments are passed straight through to epms.App, e.g.
#   ./run.sh --gui
#   ./run.sh --text --data data/employees.csv
set -euo pipefail
cd "$(dirname "$0")"

OUT_DIR="out/production"
if [ ! -d "$OUT_DIR" ] || [ -n "$(find src -name '*.java' -newer "$OUT_DIR" 2>/dev/null)" ]; then
    ./build.sh
fi

exec java -cp "$OUT_DIR" epms.App "$@"
