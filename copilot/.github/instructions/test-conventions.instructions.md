---
name: "Test Conventions"
description: "Testing conventions for API tests and contract tests with H2 and WireMock"
applyTo: "**/test/**/*.java,**/*ApiTests.java,**/*ContractTest.java"
---
# Testing Conventions

## Test Layers
| Layer | Tool | DB | Downstream | Naming |
|-------|------|-----|------------|--------|
| API Test | WebTestClient + @Sql + JSON fixtures + DatabaseVerifier | H2 | WireMock (MockFactory/Verifier) | *ApiTests |
| Contract | @SpringBootTest(MOCK) + RestAssuredMockMvc | H2 | RestAssuredMockMvc | *ContractTest |

## Core Rules
1. Tests must be independent, no `@DependsOn`
2. No real MySQL in tests (use H2)

For comprehensive API test conventions including naming, fixtures, templates, support class reference, WireMock patterns, assertion system, and checklists, see `apitest-guide.instructions.md`.
