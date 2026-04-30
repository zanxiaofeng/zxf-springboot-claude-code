---
name: implement-feature
description: Implement a new feature following the TDD workflow — from requirement analysis through API tests, implementation, contract tests, and documentation update.
---

# Implement Feature

## Pre-conditions
- [ ] Requirement doc exists in `docs/requirements/`
- [ ] ADR recorded if new tech introduced

## Steps

1. **Read requirement doc** — extract business rules and acceptance criteria
2. **Prepare test data** — add seed data to `sql/init/data.sql`, create JSON fixtures under `test-data/{entity}/`
3. **Write failing API test (Red)** — WebTestClient + JSON fixtures + @Sql seed data + DatabaseVerifier
4. **Minimal implementation (Green)** — Controller -> Service -> Repository in layers
5. **Refactor** — check against conventions, extract duplicates, optimize naming
6. **Write Contract Test** — Spring Cloud Contract Groovy DSL for each new endpoint
7. **Update documentation** — `docs/design/api-spec-v1.md`, `docs/design/domain-model.md`, and `CLAUDE.md` Sprint status

## Downstream Integration Steps (if applicable)

1. Define `{Service}Client` interface in `domain/downstream/`
2. Create `{Service}ClientImpl` in `infrastructure/downstream/` using RestTemplate
3. Add `RestTemplateConfig` in `infrastructure/config/` if not present
4. Add downstream base URL to `application.yml` and `application-test.yml`
5. Create MockFactory/Verifier in `apitest/support/mocks/` for WireMock stubs

## Output
- Implementation code (Domain -> Application -> Infrastructure -> Interfaces)
- API tests (`*ApiTests.java`)
- JSON fixtures under `test-data/{entity}/`
- Contract tests (`*.groovy`)
- Updated documentation