---
name: "TDD Workflow"
description: "Test-driven development workflow for writing failing tests first, then implementing"
applyTo: "**/*.java,**/*.groovy,**/*.md"
---

# TDD Workflow

## Step-by-Step (Strict Order)
1. **Requirement Analysis**: Read the corresponding requirement doc under `docs/requirements/`
2. **Prepare Test Data**: Add seed data to `src/test/resources/sql/init/data.sql`, create JSON fixtures under `src/test/resources/test-data/{entity}/`
3. **Write Failing API Test (Red)**: Write an API test (`*ApiTests`) using WebTestClient + JSON fixtures + @Sql seed data. Stub downstream calls via MockFactory where applicable.
4. **Minimal Implementation (Green)**: Implement Controller -> Service -> Repository in layers. Add downstream client interface in domain layer if needed.
5. **Refactor**: Check against `.github/instructions/`, extract duplicates, optimize naming
6. **Contract Test (API layer)**: Write Contract for each new endpoint, generate Stub and verify API contract
7. **Documentation Update**: Update `docs/design/api-spec-v1.md`, `docs/design/domain-model.md`, and Sprint status

## Downstream Integration in TDD
When a feature requires calling a downstream service:
1. Define `{ServiceName}Client` interface in `domain/downstream/`
2. Implement `{ServiceName}ClientImpl` in `infrastructure/downstream/`
3. Add `{Feature}Config` in `infrastructure/config/` if not already present
4. Add downstream base URL to `application.yml` and `application-test.yml`
5. Create `{Service}MockFactory`/`{Service}MockVerifier` in `apitest/support/mocks/` for WireMock stubs