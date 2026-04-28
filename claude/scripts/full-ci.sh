#!/bin/bash
# full-ci.sh - Full CI pipeline (unit + contract + integration tests)
# Usage: ./scripts/full-ci.sh

set -e
cd "$(dirname "$0")/.."

echo "==> Full CI Pipeline"
echo ""

echo "[1/5] Cleaning..."
mvn clean

echo "[2/5] Compiling..."
mvn compile

echo "[3/5] Unit tests..."
mvn test -Dtest="*Test" -DexcludedGroups="integration,contract"

echo "[4/5] Contract tests..."
mvn verify -Pcontract-tests

echo "[5/5] Integration tests..."
mvn test -Dtest="*IT"

echo ""
echo "==> All stages passed."
