#!/usr/bin/env bash
# Compiles application + test sources and runs the JUnit 5 suite.
#
#   ./test.sh                               run everything
#   ./test.sh --select-class=epms.SorterTest   run one class
#   ./test.sh --details=none                pass any option straight to the launcher
set -euo pipefail
cd "$(dirname "$0")"

JUNIT_JAR="lib/junit-platform-console-standalone.jar"
SRC_OUT="out/production"
TEST_OUT="out/test"

if [ ! -f "$JUNIT_JAR" ]; then
    echo "Missing $JUNIT_JAR - download it with:" >&2
    echo "  curl -L -o $JUNIT_JAR \\" >&2
    echo "    https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar" >&2
    exit 1
fi

./build.sh

rm -rf "$TEST_OUT"
mkdir -p "$TEST_OUT"
find test -name '*.java' > out/test-sources.txt
javac -cp "$SRC_OUT:$JUNIT_JAR" -d "$TEST_OUT" @out/test-sources.txt

LAUNCH_ARGS=("$@")
if [ ${#LAUNCH_ARGS[@]} -eq 0 ]; then
    LAUNCH_ARGS=(--scan-classpath --details=tree)
fi

java -jar "$JUNIT_JAR" execute -cp "$SRC_OUT:$TEST_OUT" "${LAUNCH_ARGS[@]}"
