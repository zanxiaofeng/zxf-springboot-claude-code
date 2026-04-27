#!/bin/bash
# setup-dev-env.sh - One-time development environment setup
# Usage: ./scripts/setup-dev-env.sh

set -e
cd "$(dirname "$0")/.."

echo "==> Setting up development environment..."

# Create local dev database (requires MySQL running)
echo "==> Creating demo database..."
mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || echo "   (Skipped: MySQL may not be running locally)"

echo "==> Running Flyway migrations..."
mvn flyway:migrate -Dflyway.url=jdbc:mysql://localhost:3306/demo -Dflyway.user=root -Dflyway.password=root || true

echo "==> Dev environment setup complete."
echo ""
echo "   Start the app:    mvn spring-boot:run"
echo "   Run tests:        ./scripts/fast-test.sh"
echo "   Run all tests:    ./scripts/full-ci.sh"
