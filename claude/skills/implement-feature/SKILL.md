---
name: implement-feature
description: Implement a new feature following the TDD workflow — from requirement analysis through integration tests, implementation, contract tests, and documentation update.
---

# Implement Feature

## Pre-conditions
- [ ] Requirement doc exists in `docs/requirements/`
- [ ] ADR recorded if new tech introduced

## Steps

1. **Read requirement doc** — extract business rules and acceptance criteria
2. **Identify or create TestDataBuilder** — check `src/test/support/builder/`
3. **Write failing integration test (Red)** — TestRestTemplate + H2 + real server (`RANDOM_PORT`)
4. **Minimal implementation (Green)** — Controller -> Service -> Repository in layers
5. **Refactor** — check against conventions, extract duplicates, optimize naming
6. **Write Contract Test** — Spring Cloud Contract Groovy DSL for each new endpoint
7. **Update documentation** — `docs/design/api-spec-v1.md`, `docs/design/domain-model.md`, and `CLAUDE.md` Sprint status

## Downstream Integration Steps (if applicable)

1. Define `{Service}Client` interface in `domain/downstream/`
2. Create `{Service}ClientImpl` in `infrastructure/downstream/` using RestTemplate
3. Add `RestTemplateConfig` in `infrastructure/config/` if not present
4. Add downstream base URL to `application.yml` and `application-test.yml`
5. Create WireMock stubs under `src/test/resources/wiremock/`
6. Stub downstream calls in integration tests via `wireMockServer.stubFor(...)`

## Output
- Implementation code (Domain -> Application -> Infrastructure -> Interfaces)
- Integration tests (`*IT.java`)
- Contract tests (`*.groovy`)
- WireMock stubs (if downstream involved, under `src/test/resources/wiremock/`)
- Updated documentation
