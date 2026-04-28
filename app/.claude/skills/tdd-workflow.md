# TDD Workflow

## Step-by-Step (Strict Order)
1. **Requirement Analysis**: Read the corresponding requirement doc under `docs/requirements/`
2. **Identify Builder**: Create {Entity}Builder or reuse existing one from `src/test/support/builder/`
3. **Write Failing Integration Test (Red)**: Write an integration test (`*IT`) using `TestRestTemplate` + H2 on a real server with random port. Stub downstream calls via WireMock where applicable.
4. **Minimal Implementation (Green)**: Implement Controller -> Service -> Repository in layers. Add downstream client interface in domain layer if needed.
5. **Refactor**: Check against `docs/conventions/`, extract duplicates, optimize naming
6. **Contract Test (API layer)**: Write Contract for each new endpoint, generate Stub and verify API contract
7. **Documentation Update**: Update `docs/design/api-spec-v1.md`, `docs/design/domain-model.md`, and claude.md Sprint status

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
3. Add `RestTemplateConfig` if not already present
4. Add downstream base URL to `application.yml` and `application-test.yml`
5. Stub downstream endpoints in integration tests using WireMock
6. Add WireMock JSON stubs under `src/test/resources/wiremock/`
