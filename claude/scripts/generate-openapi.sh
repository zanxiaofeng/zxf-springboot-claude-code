#!/bin/bash
# generate-openapi.sh - Generates OpenAPI specification from code
# Usage: ./scripts/generate-openapi.sh

set -e
cd "$(dirname "$0")/.."

echo "==> Generating OpenAPI spec..."
mvn springdoc-openapi:generate

echo "==> OpenAPI spec generated at:"
echo "   target/openapi.json"
