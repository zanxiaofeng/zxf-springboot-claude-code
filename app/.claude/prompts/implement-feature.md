# Implement Feature Prompt Template

## Pre-conditions
- [ ] Requirement doc exists in `docs/requirements/`
- [ ] ADR recorded if new tech introduced

## Steps
1. Read requirement doc, extract business rules and acceptance criteria
2. Identify required Builder, check `src/test/support/builder/`
3. Write failing integration test (Red) — TestRestTemplate + H2 + real server
4. Minimal implementation (Green)
5. Refactor and check conventions compliance
6. Write Contract Test
7. Update api-spec-v1.md and claude.md Sprint status

## Downstream Integration Steps (if applicable)
1. Define `{Service}Client` interface in `domain/downstream/`
2. Create `{Service}ClientImpl` in `infrastructure/downstream/` using RestTemplate
3. Add `RestTemplateConfig` in `infrastructure/config/` if not present
4. Add downstream base URL to `application.yml` and `application-test.yml`
5. Create WireMock stubs under `src/test/resources/wiremock/`
6. Stub downstream calls in integration tests via `wireMockServer.stubFor(...)`

## Output
- Implementation code
- Integration tests
- Contract tests
- WireMock stubs (if downstream involved)
- Updated documentation
