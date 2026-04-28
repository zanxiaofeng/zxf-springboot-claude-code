---
applyTo: "**/*.java,**/*.groovy,**/*.md"
---

# TDD Workflow

## Step-by-Step (Strict Order)
1. **Requirement Analysis**: Read the corresponding requirement doc under `docs/requirements/`
2. **Identify Builder**: Create {Entity}Builder or reuse existing one from `src/test/support/builder/`
3. **Write Failing Integration Test (Red)**: Write an integration test (`*IT`) using `TestRestTemplate` + H2 on a real server with random port. Stub downstream calls via WireMock where applicable.
4. **Minimal Implementation (Green)**: Implement Controller -> Service -> Repository in layers. Add downstream client interface in domain layer if needed.
5. **Refactor**: Check against `docs/conventions/`, extract duplicates, optimize naming
6. **Contract Test (API layer)**: Write Contract for each new endpoint, generate Stub and verify API contract
7. **Documentation Update**: Update `docs/design/api-spec-v1.md`, `docs/design/domain-model.md`, and `.github/copilot-instructions.md` Sprint status

## Builder Usage in TDD
```java
// Integration test — prepare data via API or TestDataBuilder
User user = UserBuilder.aUser().withUsername("test").build();

// Integration test — persist via repository (test support only)
User saved = userRepository.save(UserBuilder.aUser().withEmail("unique@test.com").build());

// Fixture reuse
User admin = UserFixtures.activeUser();
```

## Downstream Integration in TDD
When a feature requires calling a downstream service:
1. Define `NotificationClient` interface in `domain/downstream/`
2. Implement `NotificationClientImpl` in `infrastructure/downstream/`
3. Add `RestTemplateConfig` in `infrastructure/config/` if not already present
4. Add downstream base URL to `application.yml` and `application-test.yml`
5. Stub downstream endpoints in integration tests using WireMock
6. Add WireMock JSON stubs under `src/test/resources/wiremock/`

## Feature Implementation Guide

### Pre-conditions
- [ ] Requirement doc exists in `docs/requirements/`
- [ ] ADR recorded if new tech introduced

### Steps
1. Read requirement doc, extract business rules and acceptance criteria
2. Identify required Builder, check `src/test/support/builder/`
3. Write failing integration test (Red) — TestRestTemplate + H2 + real server
4. Minimal implementation (Green)
5. Refactor and check conventions compliance
6. Write Contract Test
7. Update api-spec-v1.md and `.github/copilot-instructions.md` Sprint status

### Downstream Integration Steps (if applicable)
1. Define `{Service}Client` interface in `domain/downstream/`
2. Create `{Service}ClientImpl` in `infrastructure/downstream/` using RestTemplate
3. Add `RestTemplateConfig` in `infrastructure/config/` if not present
4. Add downstream base URL to `application.yml` and `application-test.yml`
5. Create WireMock stubs under `src/test/resources/wiremock/`
6. Stub downstream calls in integration tests via `wireMockServer.stubFor(...)`

### Output
- Implementation code
- Integration tests
- Contract tests
- WireMock stubs (if downstream involved)
- Updated documentation
