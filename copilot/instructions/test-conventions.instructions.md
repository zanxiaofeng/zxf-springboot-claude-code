---
applyTo: "**/test/**/*.java,**/*IT.java,**/*ContractTest.java"
---

# Testing Conventions

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
7. Prepare test data via public API, not by directly accessing domain layer

## Test Data via API
```java
// Preferred: create data through the API surface
UserResponse user = createUserViaApi("john.doe", "john@test.com", "Pass1234!");

// Alternative: use TestDataBuilder with repository (test support only)
User user = UserBuilder.aUser().withEmail("unique@test.com").build();

// Fixture reuse
User admin = UserFixtures.activeUser();
```

## Integration Test Base
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_CLASS)
public abstract class IntegrationTestBase {
    @Autowired protected WireMockServer wireMockServer;

    @BeforeEach
    void resetWireMock() { wireMockServer.resetAll(); }
}
```
