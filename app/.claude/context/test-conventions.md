# Test Conventions

## Test Layers
| Layer | Annotation | DB | Client | Naming |
|-------|-----------|-----|--------|--------|
| Integration | @SpringBootTest(webEnvironment = RANDOM_PORT) + TestRestTemplate | H2 | @AutoConfigureWireMock (when stubbing downstream) | *IT |
| Contract | @AutoConfigureStubRunner | H2 | RestAssuredMockMvc | *ContractTest |

## Rules
1. Tests must be independent, no @DependsOn
2. Use @Sql or TestDataBuilder to prepare data
3. One assertion subject per test (Arrange-Act-Assert)
4. @DirtiesContext used sparingly
5. No real MySQL in tests (use H2)
6. Downstream calls must be stubbed via WireMock in integration tests

## Builder Usage
```java
// Integration test — prepare data via API or TestDataBuilder
User user = UserBuilder.aUser().withUsername("test").build();

// Integration test — persist via repository (test support only)
User saved = userRepository.save(UserBuilder.aUser().withEmail("unique@test.com").build());

// Fixture reuse
User admin = UserFixtures.activeUser();
```
