#!/bin/bash
# run-contract-tests.sh - Runs contract tests and generates stubs
# Usage: ./scripts/run-contract-tests.sh

set -e
cd "$(dirname "$0")/.."

echo "==> Running contract tests..."
mvn clean verify -Pcontract-tests

echo "==> Contract tests passed. Stubs available at:"
echo "   target/stubs/"
