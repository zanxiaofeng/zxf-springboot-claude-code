# Spring Boot 3 REST API Demo Project

A structured Spring Boot 3.x REST API demo project following best practices including TDD, Contract Testing, and Flyway Migration.

## Architecture

```
my-project/
├── docs/                                 # Documentation
│   ├── requirements/                     # Requirements
│   ├── design/                           # Design docs (ADR, API spec, domain model)
│   └── conventions/                      # Team conventions
├── src/
│   ├── main/java/.../
│   │   ├── domain/                       # Domain layer (Entity, Value Object, Repository Interface)
│   │   ├── application/                  # Application layer (Service, DTO, Mapper)
│   │   ├── infrastructure/              # Infrastructure (RepositoryImpl, Config, Security)
│   │   └── interfaces/                   # Interface layer (Controller, ExceptionHandler)
│   ├── main/resources/
│   │   └── db/migration/                 # Flyway migrations (H2 & MySQL compatible)
│   └── test/
│       ├── apitest/                      # API tests (WebTestClient + JSON fixtures + @Sql)
│       └── contract/                     # Contract tests (Spring Cloud Contract)
├── scripts/                              # Utility scripts
├── docker-compose.yml                    # Docker Compose
├── pom.xml                               # Maven config
└── README.md
```

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8.0 (for production) or Docker

### Run with Docker
```bash
docker-compose up -d
```

### Run Locally
```bash
# 1. Start MySQL
docker-compose up -d mysql

# 2. Setup database
./scripts/setup-dev-env.sh

# 3. Run application
mvn spring-boot:run
```

### Run Tests
```bash
# Quick test (skip contract)
./scripts/fast-test.sh

# Full CI pipeline (integration + contract)
./scripts/full-ci.sh

# Contract tests only
./scripts/run-contract-tests.sh
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | /api/v1/users | Create user |
| GET    | /api/v1/users/{id} | Get user by ID |
| GET    | /api/v1/users | List users (paginated) |
| PUT    | /api/v1/users/{id} | Update user |
| DELETE | /api/v1/users/{id} | Delete user |

## Tech Stack

- Java 21
- Spring Boot 3.5.x
- Spring Data JPA
- Spring Validation
- Spring Security
- Flyway Migration
- MySQL 8.0 (prod) / H2 (test)
- JUnit 5 / AssertJ / JSONAssert
- Spring Cloud Contract
- WireMock
- Lombok

## Project Standards

- **Architecture**: Four-layer architecture (Domain -> Application -> Infrastructure -> Interfaces)
- **TDD**: Red -> Green -> Refactor workflow
- **Contract First**: Write Contract Test before API implementation
- **Code Standards**: All code includes JavaDoc, constructor injection, DTOs use record
- **API Tests**: WebTestClient + JSON fixtures + DatabaseVerifier + @Sql seed data
- **Contract Tests**: Spring Cloud Contract with Groovy DSL

## GitHub Copilot Configuration

This project includes comprehensive GitHub Copilot configuration for AI-assisted development.

### File Structure

```
.github/
├── copilot-instructions.md              # Repository-wide instructions (build, test, architecture)
├── workflows/
│   └── copilot-setup-steps.yml          # Coding agent environment setup (JDK 21, Maven)
├── instructions/                        # Path-specific instructions (*.instructions.md)
│   ├── architecture.instructions.md     # Four-layer hexagonal architecture rules
│   ├── api-conventions.instructions.md  # REST API design patterns
│   ├── apitest-guide.instructions.md    # API test conventions and templates
│   ├── code-review.instructions.md      # Code review checklist
│   ├── contract-test.instructions.md    # Spring Cloud Contract guide
│   ├── db-conventions.instructions.md   # Database entity and migration rules
│   ├── db-migration.instructions.md     # Flyway migration directory structure
│   ├── downstream-conventions.instructions.md  # Downstream integration patterns
│   ├── java-coding-standard.instructions.md     # Java coding standards + contract programming
│   ├── logging.instructions.md          # Logging conventions (@Slf4j)
│   ├── service-conventions.instructions.md      # Service layer patterns
│   ├── tdd-workflow.instructions.md     # TDD workflow steps
│   ├── tech-stack.instructions.md       # Technology versions and dependencies
│   ├── test-conventions.instructions.md # Test layer conventions
│   └── validation.instructions.md       # Bean validation patterns
└── prompts/                             # Reusable prompt files (*.prompt.md)
    ├── add-endpoint.prompt.md           # Guide for adding new REST endpoints
    ├── implement-feature.prompt.md      # Full TDD feature implementation workflow
    └── refactor-module.prompt.md        # Safe refactoring guide
```

### How It Works

- **`copilot-instructions.md`**: Applied to all Copilot interactions (Chat, code review, coding agent). Contains build commands, project structure, and key rules.
- **`*.instructions.md` files**: Path-specific instructions applied based on `applyTo` glob patterns. Automatically loaded by Copilot cloud agent and code review when working on matching files.
- **`copilot-setup-steps.yml`**: Pre-configures the coding agent's environment (installs JDK 21, caches Maven dependencies, compiles the project) before the agent starts working.
- **`*.prompt.md` files**: Reusable prompts for common workflows (add endpoint, implement feature, refactor). Available in VS Code Copilot Chat via "Attach context > Prompt...".

### Documentation Sources

| Topic | Source |
|-------|--------|
| Repository custom instructions | [Adding repository custom instructions for GitHub Copilot](https://docs.github.com/en/copilot/customizing-copilot/adding-repository-custom-instructions-for-github-copilot) |
| Path-specific instructions (`*.instructions.md`) | [Adding custom instructions for GitHub Copilot](https://docs.github.com/en/copilot/customizing-copilot/adding-custom-instructions-for-github-copilot) |
| Coding agent environment setup | [Customizing the development environment for Copilot coding agent](https://docs.github.com/en/copilot/customizing-copilot/customizing-the-development-environment-for-copilot-coding-agent) |
| Best practices for coding agent | [Best practices for using Copilot to work on tasks](https://docs.github.com/en/copilot/using-github-copilot/coding-agent/best-practices-for-using-copilot-to-work-on-tasks) |
| Prompt files | [Enabling and using prompt files](https://docs.github.com/en/copilot/customizing-copilot/adding-repository-custom-instructions-for-github-copilot#enabling-and-using-prompt-files) |
| Copilot coding agent overview | [About assigning tasks to Copilot](https://docs.github.com/en/copilot/using-github-copilot/coding-agent/about-assigning-tasks-to-copilot) |
| Copilot code review guidelines | [Using custom instructions to unlock the power of Copilot code review](https://docs.github.com/en/copilot/tutorials/use-custom-instructions) |
| VS Code custom instructions | [Use custom instructions in VS Code](https://code.visualstudio.com/docs/copilot/customization/custom-instructions) |
