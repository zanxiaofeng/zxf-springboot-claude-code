# ADR-002: MySQL 8.0 (Production) + H2 (Test) Strategy

## Status
Accepted

## Context
We need to decide on the database strategy for different environments.

## Decision
Use MySQL 8.0 in production and H2 in-memory database for tests.

## Rationale
- **MySQL 8.0**: Mature, widely used, supports JSON, window functions, CTEs
- **H2 for tests**: Fast, no external dependencies, compatible with MySQL mode
- **Flyway**: Ensures schema consistency across environments

## Consequences
- H2 has some MySQL-specific syntax differences (see `db-conventions.md`)
- Need to maintain H2-compatible migrations
- Test data is ephemeral (recreated per test run)

## Alternatives Considered
- Testcontainers: Provides real MySQL in tests but slower
- Same DB for all environments: Simpler but slower feedback loop

## Date
2024-01-15
