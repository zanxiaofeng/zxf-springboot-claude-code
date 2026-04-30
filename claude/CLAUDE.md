# CLAUDE.md — Spring Boot 3 REST API Demo

## Project Identity

You are a senior Java backend engineer for this Spring Boot 3 REST API project, proficient in TDD, Contract Testing, WireMock stubbing, and the Spring ecosystem.

## Quick Navigation

| Topic | Import |
|-------|--------|
| Architecture & Layers | @.claude/rules/architecture.md |
| Tech Stack & Versions | @.claude/rules/tech-stack.md |
| API Design Conventions | @.claude/rules/api-conventions.md |
| Service Layer Conventions | @.claude/rules/service-conventions.md |
| Database Conventions | @.claude/rules/db-conventions.md |
| Testing Conventions | @.claude/rules/test-conventions.md |
| Downstream Integration | @.claude/rules/downstream-conventions.md |
| TDD Workflow | @.claude/rules/tdd-workflow.md |
| Contract Testing | @.claude/rules/contract-test.md |
| Database Migration | @.claude/rules/db-migration.md |
| Code Review Checklist | @.claude/rules/code-review.md |

## Current Sprint

| ID | Task | Status | Last Action |
|----|------|--------|-------------|
| 001 | User Management Module | In Progress | API test migration to WebTestClient + JSON fixture pattern completed |

## Recently Completed

1. API test refactoring: replaced TestRestTemplate integration tests with WebTestClient + JSON fixtures + DatabaseVerifier + @Sql seed data
2. Downstream notification service integration (NotificationClientImpl + WireMock test stubs)
3. Four-layer architecture: Domain -> Application -> Infrastructure -> Interfaces
4. Claude Code config restructured into `.claude/` standard directory

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
- Test class naming: `{Entity}ApiTests` (API tests), `{ClassUnderTest}ContractTest` (contract)
- DTOs use `record`, Services use interface + impl
- Constructor injection with `@RequiredArgsConstructor`
- All Controllers return `ApiResponse<T>`

## Directory Layout

```
my-project/
├── CLAUDE.md                            # This file
├── .claude/
│   ├── settings.json                    # Permissions + config
│   ├── rules/                           # Scoped instruction files
│   ├── skills/                          # Reusable workflows
│   │   ├── implement-feature/SKILL.md
│   │   ├── add-endpoint/SKILL.md
│   │   └── refactor-module/SKILL.md
│   └── agents/                          # Specialized subagents
│       ├── code-reviewer.md
│       └── security-auditor.md
├── docs/
│   ├── requirements/                    # Requirement docs
│   ├── design/                          # ADR, API spec, domain model
│   ├── conventions/                     # Team conventions
│   └── templates/                       # Document templates
├── src/main/java/.../
│   ├── domain/                          # Entity, VO, Repository Interface, Downstream Client Interface
│   ├── application/                     # Service, DTO, Mapper
│   ├── infrastructure/                  # RepositoryImpl, Config, Security, Downstream Client Impl
│   └── interfaces/                      # Controller, ExceptionHandler
├── src/test/
│   ├── apitest/                         # API tests (WebTestClient + JSON fixtures + @Sql)
│   │   ├── {Entity}ApiTests.java
│   │   └── support/                     # BaseApiTest, JsonLoader, DatabaseVerifier, mocks
│   ├── contract/                        # Contract tests (Spring Cloud Contract)
│   └── support/                         # Builder, Fixture, Randomizer
├── src/test/resources/
│   ├── sql/cleanup/                     # @Sql cleanup scripts
│   ├── sql/init/                        # @Sql seed data
│   ├── test-data/{entity}/             # JSON fixtures (request, response per endpoint)
│   ├── contracts/                       # Spring Cloud Contract Groovy files
│   └── wiremock/                        # WireMock JSON stubs
├── scripts/                             # Utility scripts
└── pom.xml
```
