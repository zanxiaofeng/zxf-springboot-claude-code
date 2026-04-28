# Claude Project Context

## Quick Navigation
- Requirements: `docs/requirements/`
- Design Docs: `docs/design/`
- Conventions: `docs/conventions/`
- TDD Flow: `.claude/skills/tdd-workflow.md`
- Contract Test: `.claude/skills/contract-test.md`
- Downstream Integration: `.claude/context/downstream-conventions.md`

## Current Sprint
| ID | Task | Status | Last Action |
|----|------|--------|-------------|
| 001 | User Management Module | In Progress | Downstream notification integration completed (NotificationClient + WireMock stubs) |
| — | — | — | Next: API spec update to document downstream side effects |

## Recently Completed
1. ✅ Downstream notification service integration (NotificationClientImpl + WireMock test stubs)
2. ✅ Integration test migration: MockMvc → TestRestTemplate with real server + WireMock
3. ✅ Four-layer architecture: Domain → Application → Infrastructure → Interfaces

## Tech Stack Versions
- Java 21, Spring Boot 3.5.x, Maven
- MySQL 8.0 (prod), H2 (test)
- JUnit 5, AssertJ, Spring Cloud Contract 4.3.0, WireMock 3.x
