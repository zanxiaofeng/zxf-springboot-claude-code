#!/bin/bash
# fast-test.sh - Quick local test runner (skips contract tests)
# Usage: ./scripts/fast-test.sh [maven-options]

set -e
cd "$(dirname "$0")/.."

echo "==> Running unit tests only (contract tests skipped)..."
mvn clean test -Pfast-test "$@"
echo "==> Tests passed."
