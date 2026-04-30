---
files: ["**/test/**/*.java", "**/*ApiTests.java", "**/*ContractTest.java"]
---

# Testing Conventions

## Test Layers
| Layer | Tool | DB | Downstream | Naming |
|-------|------|-----|------------|--------|
| API Test | WebTestClient + @Sql + JSON fixtures + DatabaseVerifier | H2 | WireMock (MockFactory/Verifier) | *ApiTests |
| Contract | @AutoConfigureStubRunner | H2 | RestAssuredMockMvc | *ContractTest |

## Rules
1. Tests must be independent, no @DependsOn
2. Use @Sql seed data (not runtime API calls) to prepare data
3. One assertion subject per test (Given/When/Then)
4. No real MySQL in tests (use H2)
5. Downstream calls must be stubbed via WireMock (NotificationMockFactory/Verifier)
6. JSON fixtures in `test-data/{entity}/` with `${variable}` template support
7. Use JSONAssert + JsonComparatorFactory for response validation (ignores dynamic fields)

## API Test Structure
```java
// Test class extends BaseApiTest
public class UserApiTests extends BaseApiTest {
    // Given: load JSON fixture + setup WireMock mock
    String request = JsonLoader.load("user/post/request.json", Map.of("username", "new.user", ...));
    NotificationMockFactory.mockNotificationAccepted();

    // When: call API via WebTestClient helper
    ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(),
        request, String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

    // Then: assert response with JSONAssert
    JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);

    // And: verify DB state + downstream calls
    assertThat(databaseVerifier.countUsers()).isEqualTo(initialCount + 1);
    NotificationMockVerifier.verifyNotificationCalledWith(username, email);
}
```