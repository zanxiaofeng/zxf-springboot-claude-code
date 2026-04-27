# TDD Workflow

## Step-by-Step (Strict Order)
1. **Requirement Analysis**: Read the corresponding requirement doc under `docs/requirements/`
2. **Identify Builder**: Create {Entity}Builder or reuse existing one from `src/test/support/builder/`
3. **Write Failing Test (Red)**: Unit test for Service layer (Mockito), Integration test for Controller (MockMvc + H2)
4. **Minimal Implementation (Green)**: Implement Controller -> Service -> Repository in layers
5. **Refactor**: Check against `docs/conventions/`, extract duplicates, optimize naming
6. **Contract Test (API layer)**: Write Contract for each new endpoint, generate Stub and verify API contract
7. **Documentation Update**: Update `docs/design/api-spec-v1.md` and claude.md Sprint status

## Builder Usage in TDD
```java
// Unit test - pure Builder
User user = UserBuilder.aUser().withUsername("test").build();

// Integration test - persistable Builder
User saved = persistableUserBuilder.withEmail("unique@test.com").buildAndPersist();

// Fixture reuse
User admin = UserFixtures.activeUser();
```
