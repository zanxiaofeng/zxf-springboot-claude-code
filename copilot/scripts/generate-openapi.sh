#!/bin/bash
# generate-openapi.sh - Starts the app and extracts the OpenAPI spec
# Usage: ./scripts/generate-openapi.sh

set -e
cd "$(dirname "$0")/.."

echo "==> Starting application to generate OpenAPI spec..."
mvn spring-boot:run -Dspring-boot.run.arguments="--springdoc.api-docs.enabled=true" &
APP_PID=$!

# Wait for app to start
echo "==> Waiting for application to start..."
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/v3/api-docs > /dev/null 2>&1; then
        break
    fi
    sleep 2
done

echo "==> Downloading OpenAPI spec..."
curl -s http://localhost:8080/v3/api-docs -o target/openapi.json

echo "==> Stopping application..."
kill $APP_PID 2>/dev/null || true

echo "==> OpenAPI spec generated at:"
echo "   target/openapi.json"
