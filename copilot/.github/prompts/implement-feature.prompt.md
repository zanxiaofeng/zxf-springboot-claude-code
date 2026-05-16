---
name: "Implement Feature"
description: "Implement a new feature following the TDD workflow — from requirement analysis through tests and implementation"
agent: "agent"
---

Implement a new feature following the TDD workflow. Start by reading the requirement doc, then follow the strict TDD cycle.

## Steps
1. Read the requirement doc in `docs/requirements/`
2. Prepare test data: add seed data to `sql/init/data.sql`, create JSON fixtures under `test-data/{entity}/`
3. Write failing API test (Red) with WebTestClient + JSON fixtures + @Sql, stub downstream via WireMock
4. Minimal implementation (Green): Controller → Service → Repository
5. Refactor: check against `.github/instructions/` conventions
6. Write Contract Test for each new endpoint
7. Update docs: `docs/design/api-spec-v1.md`, `docs/design/domain-model.md`

## Downstream Integration (if applicable)
1. Define `{Service}Client` interface in `domain/downstream/`
2. Create `{Service}ClientImpl` in `infrastructure/downstream/` — see `downstream-conventions.instructions.md` for HTTP client usage
3. Add base URL to `application.yml` and `application-test.yml`
4. Create MockFactory/Verifier in `apitest/support/mocks/`

## Validation
- `mvn test` passes
- `./scripts/run-contract-tests.sh` passes (if contract tests added)
