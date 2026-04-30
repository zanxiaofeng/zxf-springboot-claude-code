# Testing Conventions

## Test Layers
| Layer | Tool | DB | Downstream | Naming |
|-------|------|-----|------------|--------|
| API Test | WebTestClient + @Sql + JSON fixtures + DatabaseVerifier | H2 | WireMock (MockFactory/Verifier) | *ApiTests |
| Contract | @AutoConfigureStubRunner | H2 | RestAssuredMockMvc | *ContractTest |

## Rules
1. Tests must be independent, no @DependsOn
2. Use @Sql seed data to prepare data
3. Follow Given/When/Then pattern
4. No real MySQL in tests (use H2)
5. Downstream calls must be stubbed via WireMock (MockFactory/Verifier)
6. JSON fixtures in `test-data/{entity}/` with `${variable}` template support
