# Spring Boot 3 REST API Demo Project

A structured Spring Boot 3.x REST API demo project following best practices including TDD, Contract Testing, Flyway Migration, and CI/CD.

## Architecture

```
my-project/
├── .cursorrules                          # Claude Code global rules
├── claude.md                             # Project context anchor
├── .claude/                              # Claude Code configuration
│   ├── config.json                       # Auto-load triggers
│   ├── skills/                           # Callable skills (TDD, Contract, etc.)
│   ├── context/                          # Dynamic context
│   ├── prompts/                          # Reusable prompt templates
│   └── templates/                        # Templates
├── docs/                                 # Documentation
│   ├── requirements/                     # Requirements
│   ├── design/                           # Design docs (ADR, API spec, domain model)
│   └── conventions/                      # Team conventions
├── src/
│   ├── main/java/.../
│   │   ├── domain/                       # Domain layer (Entity, Value Object, Repository Interface)
│   │   ├── application/                  # Application layer (Service, DTO, Mapper)
│   │   ├── infrastructure/              # Infrastructure (RepositoryImpl, Config, Security)
│   │   └── interfaces/                   # Interface layer (Controller, ExceptionHandler, Filter)
│   ├── main/resources/
│   │   └── db/migration/               # Flyway migrations (H2 & MySQL compatible)
│   └── test/                             # Tests
│       ├── unit/                         # Pure unit tests (Mockito)
│       ├── integration/                  # Integration tests (@SpringBootTest + H2)
│       ├── contract/                     # Contract tests (Spring Cloud Contract)
│       └── support/                      # Test utilities (Builder, Fixture, Randomizer)
├── scripts/                              # Utility scripts
├── .github/workflows/                   # CI/CD Pipeline
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
# Quick test (unit only, skip contract)
./scripts/fast-test.sh

# Full CI pipeline (unit + contract + integration)
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
- JUnit 5 / Mockito / AssertJ
- Spring Cloud Contract
- Lombok

## Project Standards

- **Architecture**: Four-layer architecture (Domain → Application → Infrastructure → Interfaces)
- **TDD**: Red → Green → Refactor workflow
- **Contract First**: Write Contract Test before API implementation
- **Code Standards**: All code includes JavaDoc, constructor injection, DTOs use record
- **Testing**: Unit tests (Mockito) + Integration tests (H2) + Contract tests (Stub)
