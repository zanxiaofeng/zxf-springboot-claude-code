# Spring Boot 3 REST API Demo Project

A structured Spring Boot 3.x REST API demo project following best practices including TDD, Contract Testing, Flyway Migration, and CI/CD.

## Architecture

```
my-project/
├── .github/
│   ├── copilot-instructions.md           # GitHub Copilot main instructions
│   ├── copilot-instructions.d/           # Sub-instructions (conventions, workflows)
│   ├── prompts/                          # Prompt templates
│   └── workflows/                        # CI/CD Pipeline
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
- JUnit 5 / AssertJ
- Spring Cloud Contract
- Lombok

## Project Standards

- **Architecture**: Four-layer architecture (Domain → Application → Infrastructure → Interfaces)
- **TDD**: Red → Green → Refactor workflow
- **Contract First**: Write Contract Test before API implementation
- **Code Standards**: All code includes JavaDoc, constructor injection, DTOs use record
- **Testing**: Integration tests (TestRestTemplate + H2 on real server) + Contract tests (Spring Cloud Contract)
