#!/usr/bin/env bash
# Runs only the performance/growth-rate tests and shows their console output.
set -euo pipefail
cd "$(dirname "$0")"
exec ./test.sh --select-class=epms.AlgorithmPerformanceTest --details=none
