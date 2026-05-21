# Copilot Instructions — Spring Boot 3 REST API Project

## Build & Test Commands

- **Build:** `mvn compile -q`
- **Run all tests:** `mvn test`
- **Run API tests only:** `mvn test -Dtest="*ApiTests"`
- **Run contract tests:** `./scripts/run-contract-tests.sh`
- **Fast test (skip contract):** `./scripts/fast-test.sh`
- **Full CI:** `./scripts/full-ci.sh`
- **Always run** `mvn compile` before committing to verify no compilation errors

## Project Overview

Java 21 + Spring Boot 3.5.x + Maven 3.9+ REST API project. Four-layer hexagonal architecture (Domain → Application → Infrastructure → Interfaces). MySQL 8.0 production, H2 in-memory for tests.

## Repository Structure

```
src/main/java/{base-package}/
├── domain/           # Entity, Repository Interface, Downstream Client Interface, BusinessException
├── application/      # Service (interface + impl), DTO (record), Mapper
├── infrastructure/   # JpaAdapter, Config, Security, Downstream Client Impl
└── interfaces/       # Controller, ExceptionHandler, ApiResponse<T>

src/test/java/{base-package}/
├── apitest/          # API tests: WebTestClient + JSON fixtures + @Sql + DatabaseVerifier
│   └── support/      # BaseApiTest, JsonLoader, JsonComparatorFactory, DatabaseVerifier, mocks/
└── contract/         # Contract base test (Spring Cloud Contract generates tests from .groovy)

src/test/resources/
├── sql/cleanup/      # @Sql cleanup scripts (run before each test)
├── sql/init/         # @Sql seed data
├── sql/cases/        # Case-level SQL (CLOB via FILE_READ)
├── test-data/{entity}/  # JSON fixtures with ${variable} templates
├── mock-data/        # WireMock static stubs (mappings/ + __files/)
└── contracts/        # Spring Cloud Contract Groovy DSL files
```

## Coding Standards

- Constructor injection only (`@RequiredArgsConstructor`), no field `@Autowired`
- DTOs use `record`, Services use interface + impl pattern
- All Controllers return `ApiResponse<T>`
- `@Transactional(readOnly = true)` at class level on Services; write methods override with `@Transactional`
- Business logic in Service only, never in Controller
- Domain entities use JPA annotations (pragmatic simplification); no Spring service annotations in domain layer
- Use `@Slf4j` (Lombok) for logging, `OffsetDateTime` for timestamps, `BusinessException` for errors
- Test naming: `{Entity}ApiTests` for API tests, `{ClassUnderTest}ContractTest` for contract tests

## API Conventions

- URL pattern: `/api/v{version}/{resource}` (plural nouns, no verbs)
- Response wrapper: `ApiResponse<T>` with `code`, `data`, `message`, `timestamp`, `traceId`

## Key Rules

- No real MySQL in tests — use H2 only
- All DDL via Flyway migration (`V{version}__{description}.sql`), never modify merged migrations
- New APIs: TDD (write failing API test first, implement to pass, then formalize with Contract Test)
- Downstream calls: define interface in `domain/downstream/`, implement in `infrastructure/downstream/`, stub with WireMock in tests
- See `.github/instructions/` for detailed conventions per layer
- Validation: use `@Valid`/`@Validated` on Controller, validation annotations on DTOs, `GlobalExceptionHandler` for error responses — see `validation.instructions.md`
- Logging: use `@Slf4j`, SLF4J placeholders, always include exception object in error logs — see `logging.instructions.md`
