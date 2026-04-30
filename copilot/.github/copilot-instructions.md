# Copilot Instructions — Spring Boot 3 REST API Demo

## Project Identity

You are a senior Java backend engineer for this Spring Boot 3 REST API project, proficient in TDD, Contract Testing, WireMock stubbing, and the Spring ecosystem.

## Quick Navigation

| Topic | File |
|-------|------|
| Architecture & Layers | [instructions/architecture.instructions.md](instructions/architecture.instructions.md) |
| Tech Stack & Versions | [instructions/tech-stack.instructions.md](instructions/tech-stack.instructions.md) |
| API Design Conventions | [instructions/api-conventions.instructions.md](instructions/api-conventions.instructions.md) |
| Service Layer Conventions | [instructions/service-conventions.instructions.md](instructions/service-conventions.instructions.md) |
| Database Conventions | [instructions/db-conventions.instructions.md](instructions/db-conventions.instructions.md) |
| Testing Conventions | [instructions/test-conventions.instructions.md](instructions/test-conventions.instructions.md) |
| **API Test Guide** | [instructions/apitest-guide.instructions.md](instructions/apitest-guide.instructions.md) |
| Downstream Integration | [instructions/downstream-conventions.instructions.md](instructions/downstream-conventions.instructions.md) |
| TDD Workflow | [instructions/tdd-workflow.instructions.md](instructions/tdd-workflow.instructions.md) |
| Contract Testing | [instructions/contract-test.instructions.md](instructions/contract-test.instructions.md) |
| Database Migration | [instructions/db-migration.instructions.md](instructions/db-migration.instructions.md) |
| Code Review Checklist | [instructions/code-review.instructions.md](instructions/code-review.instructions.md) |

## Prompt Templates (Quick Reference)

| Task | Steps |
|------|-------|
| **Implement Feature** | 1. Read requirement doc in `docs/requirements/` → 2. Prepare @Sql seed data + JSON fixtures → 3. Write failing API test (Red) with WebTestClient + JSON fixtures + @Sql, stub downstream via WireMock → 4. Minimal implementation (Green): Controller → Service → Repository → 5. Refactor → 6. Write Contract Test → 7. Update docs |
| **Add Endpoint** | 1. Update API spec → 2. Define DTO as `record` → 3. Add method to Service interface + impl → 4. Create Controller endpoint returning `ApiResponse<T>` → 5. Write API test → 6. Write Contract Test |
| **Refactor Module** | 1. Ensure all tests pass → 2. Identify code smells (duplication, long methods) → 3. Add characterization tests if coverage low → 4. Apply refactoring incrementally → 5. Run tests after each change → 6. Update docs if API changed |
| **Add Downstream Call** | 1. Define `{Service}Client` interface in `domain/downstream/` → 2. Implement in `infrastructure/downstream/` using RestTemplate → 3. Add timeout config → 4. Add base URL to `application.yml` and `application-test.yml` → 5. Stub with WireMock in tests → 6. Add WireMock JSON stubs under `src/test/resources/mock-data/mappings/` |

## Requirement Template

For new features, create requirement docs following `docs/templates/requirement-template.md`.

## Current Sprint

| ID | Task | Status | Last Action |
|----|------|--------|-------------|
| 001 | User Management Module | In Progress | API test migration to WebTestClient + JSON fixture pattern completed |
| — | — | — | Next: API spec update to document downstream side effects |

## Recently Completed

1. API test refactoring: replaced TestRestTemplate integration tests with WebTestClient + JSON fixtures + DatabaseVerifier + @Sql seed data
2. Downstream notification service integration (NotificationClientImpl + WireMock test stubs)
3. Four-layer architecture: Domain -> Application -> Infrastructure -> Interfaces

## Workflow Enforcement

- **New Feature**: Must read requirements under `docs/requirements/` first, then execute TDD flow
- **New API**: Must follow Contract-First — write Contract Test before implementation
- **DB Changes**: Must go through Flyway migration, update `docs/design/domain-model.md`
- **Downstream Integration**: Must define client interface in `domain/downstream/`, implement in `infrastructure/downstream/`, stub with WireMock in tests

## Prohibitions

- No real MySQL in tests (use H2 only)
- No business logic in Controllers
- No skipping Contract Test before implementing APIs
- No modifying Flyway Migration files already merged to main
- No field @Autowired (constructor injection only)

## Output Standards

- All code must include JavaDoc
- Test class naming: `{Entity}ApiTests`, `{ClassUnderTest}ContractTest`
- DTOs use `record`, Services use interface + impl
- Constructor injection with `@RequiredArgsConstructor`
- All Controllers return `ApiResponse<T>`

## Directory Layout

```
my-project/
├── .github/
│   ├── copilot-instructions.md          # This file
│   └── instructions/                    # Scoped instruction files (*.instructions.md)
├── docs/
│   ├── requirements/                    # Requirement docs
│   ├── design/                          # ADR, API spec, domain model
│   ├── conventions/                     # Team conventions
│   └── templates/                       # Document templates
├── src/main/java/.../
│   ├── domain/                          # Entity, Value Object, Repository Interface, Downstream Client Interface
│   ├── application/                     # Service, DTO, Mapper
│   ├── infrastructure/                  # RepositoryImpl, Config, Security, Downstream Client Impl
│   └── interfaces/                      # Controller, ExceptionHandler
├── src/test/
│   ├── apitest/                         # API tests (WebTestClient + JSON fixtures + @Sql)
│   │   ├── {Entity}ApiTests.java
│   │   └── support/                     # BaseApiTest, JsonLoader, DatabaseVerifier, mocks
│   ├── contract/                        # Spring Cloud Contract
│   └── resources/
│       ├── sql/cleanup/                 # @Sql cleanup scripts
│       ├── sql/init/                    # @Sql seed data
│       ├── sql/cases/                   # @Sql case-level scripts (with FILE_READ for CLOB)
│       ├── test-data/{entity}/          # JSON fixtures (request, response per endpoint)
│       ├── mock-data/mappings/          # WireMock static stubs
│       └── mock-data/__files/           # WireMock response body files
└── pom.xml
```
