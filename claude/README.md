# Spring Boot 3 REST API Demo Project

A structured Spring Boot 3.x REST API demo project following best practices including TDD, Contract Testing, and Flyway Migration.

## Architecture

```
my-project/
├── docs/                                 # Documentation
│   ├── requirements/                     # Requirements
│   ├── design/                           # Design docs (ADR, API spec, domain model)
│   └── templates/                        # Document templates
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

---

## Claude Code Configuration

This project is configured for use with [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI. Below is the configuration structure and reference documentation.

### Configuration Overview

```
claude/
├── CLAUDE.md                  # Project instructions (entry point, < 200 lines)
├── CLAUDE.local.md            # Personal overrides (gitignored)
├── .claude/
│   ├── settings.json          # Permissions, hooks, env, effort level
│   ├── rules/                 # Path-scoped instruction files (15 rules)
│   ├── skills/                # Reusable workflow skills (3 skills)
│   └── agents/                # Custom subagents (4 agents)
├── .mcp.json                  # MCP server configuration
└── docs/                      # Design docs, requirements, templates
```

### CLAUDE.md

The project entry point for Claude Code instructions. Contains project identity, quick navigation to rule files, workflow enforcement rules, prohibitions, output standards, and directory layout.

**Reference**: [Memory and CLAUDE.md](https://docs.anthropic.com/en/docs/claude-code/memory)

| File | Scope | Purpose |
|------|-------|---------|
| `CLAUDE.md` | Shared (committed) | Team-wide project instructions |
| `CLAUDE.local.md` | Personal (gitignored) | Individual developer overrides |
| `.claude/rules/*.md` | Shared (committed) | Path-scoped conditional rules |

### Rules (`.claude/rules/`)

Rules use YAML frontmatter with `paths:` globs to conditionally load based on file access patterns. Rules without `paths:` load unconditionally at session start.

**Reference**: [Rules](https://docs.anthropic.com/en/docs/claude-code/memory#organize-rules-with-claude-rules)

| Rule | Scope | Loads When |
|------|-------|------------|
| `architecture.md` | `**/*.java` | Editing Java files |
| `tech-stack.md` | `**/pom.xml`, `**/*.java`, `**/*.yml` | Editing config or code |
| `api-conventions.md` | `**/interfaces/**/*.java`, `**/*.md` | Editing interfaces or docs |
| `service-conventions.md` | `**/application/**/*.java` | Editing service layer |
| `db-conventions.md` | `**/*.sql`, `**/*.java` | Editing SQL or Java |
| `db-migration.md` | `**/*.sql`, `**/*.java` | Editing SQL or Java |
| `test-conventions.md` | `**/test/**/*.java` | Editing test files |
| `apitest-guide.md` | `**/apitest/**/*.java` | Editing API tests |
| `contract-test.md` | `**/*.groovy`, `**/contract/**` | Editing contract tests |
| `downstream-conventions.md` | `**/*.java` | Editing Java files |
| `tdd-workflow.md` | `**/*.java`, `**/*.groovy`, `**/*.md` | Editing code, tests, or docs |
| `code-review.md` | `**/*.java` | Editing Java files |
| `validation.md` | `**/interfaces/**`, `**/application/**` | Editing API/service layer |
| `java-coding-standard.md` | `**/*.java` | Editing Java files |
| `logging.md` | `**/*.java` | Editing Java files |

### Skills (`.claude/skills/`)

Skills are reusable workflows invoked via `/skill-name` or automatically by Claude.

**Reference**: [Skills](https://docs.anthropic.com/en/docs/claude-code/skills)

| Skill | Trigger | Description |
|-------|---------|-------------|
| `implement-feature` | `/implement-feature` | Full TDD workflow from requirement to documentation |
| `add-endpoint` | `/add-endpoint` | Contract-first endpoint with API + Contract tests |
| `refactor-module` | `/refactor-module` | Safe incremental refactoring with test verification |

### Agents (`.claude/agents/`)

Custom subagents for specialized tasks. Claude delegates to agents automatically based on context.

**Reference**: [Subagents](https://docs.anthropic.com/en/docs/claude-code/sub-agents)

| Agent | Purpose | Auto-trigger |
|-------|---------|--------------|
| `code-reviewer` | Code quality, architecture, security review | After writing/modifying code |
| `security-auditor` | OWASP Top 10, input validation, secrets | Before commits |
| `tdd-guide` | Enforce TDD workflow, write tests first | New features, bug fixes |
| `build-error-resolver` | Fix Maven build/test failures | When build fails |

### Settings (`.claude/settings.json`)

**Reference**: [Settings](https://docs.anthropic.com/en/docs/claude-code/settings)

| Key | Value | Description |
|-----|-------|-------------|
| `permissions.defaultMode` | `acceptEdits` | Auto-accept file edits |
| `permissions.allow` | Maven, scripts, read/write | Pre-approved tools |
| `permissions.deny` | `rm -rf`, `git push`, secrets | Blocked dangerous operations |
| `hooks.PostToolUse` | `mvn compile` | Compile check after file edits |
| `hooks.Stop` | `mvn test` | Run tests before session ends |
| `effortLevel` | `high` | High effort mode |
| `language` | `zh-CN` | Chinese responses |
| `autoMemoryEnabled` | `true` | Auto memory across sessions |
| `env.JAVA_HOME` | JDK 21 path | Java runtime |

### Hooks

Hooks run shell commands at lifecycle events. This project uses:

| Hook | Matcher | Command | Purpose |
|------|---------|---------|---------|
| `PostToolUse` | `Edit\|Write` | `mvn compile -q` | Compile check after edits |
| `Stop` | — | `mvn test -q` | Full test run on session end |

**Reference**: [Hooks](https://docs.anthropic.com/en/docs/claude-code/hooks)

### Permissions

Permission evaluation order: **deny -> ask -> allow** (first match wins).

- **`deny`**: Blocks destructive commands (`rm -rf`, `git push --force`, `mvn deploy`, secret file reads)
- **`allow`**: Pre-approves Maven builds, test scripts, git read operations, file read/write/edit
- **`defaultMode`**: `acceptEdits` auto-accepts file edits and common filesystem operations

**Reference**: [Permissions](https://docs.anthropic.com/en/docs/claude-code/permissions)

---

## Claude Code Reference Documentation

### Official Documentation

| Topic | URL |
|-------|-----|
| Overview | https://docs.anthropic.com/en/docs/claude-code |
| Installation | https://docs.anthropic.com/en/docs/claude-code/install |
| CLAUDE.md & Memory | https://docs.anthropic.com/en/docs/claude-code/memory |
| Settings | https://docs.anthropic.com/en/docs/claude-code/settings |
| Skills | https://docs.anthropic.com/en/docs/claude-code/skills |
| Subagents | https://docs.anthropic.com/en/docs/claude-code/sub-agents |
| Hooks | https://docs.anthropic.com/en/docs/claude-code/hooks |
| Permissions | https://docs.anthropic.com/en/docs/claude-code/permissions |
| MCP Servers | https://docs.anthropic.com/en/docs/claude-code/mcp |
| CLI Reference | https://docs.anthropic.com/en/docs/claude-code/cli-reference |
| Tools Reference | https://docs.anthropic.com/en/docs/claude-code/tools-reference |

### Key Concepts

- **CLAUDE.md Hierarchy**: Managed policy > Project > User > Local. Files in subdirectories load on demand.
- **Rules with `paths:`**: Conditional loading based on file glob patterns.
- **Skills**: Reusable workflows with frontmatter (`name`, `description`, `arguments`, `allowed-tools`, `context`).
- **Agents**: Specialized subagents with `permissionMode`, `maxTurns`, `memory`, `background`, `isolation`.
- **Hooks**: Lifecycle events (`PreToolUse`, `PostToolUse`, `Stop`, `Notification`, etc.) with command/HTTP/MCP handlers.
- **Memory**: Auto-memory in `~/.claude/projects/` with `MEMORY.md` entrypoint (200 lines / 25KB loaded at startup).
- **MCP**: Model Context Protocol servers via `.mcp.json` (stdio, HTTP transports).
- **Effort Levels**: `low` / `medium` / `high` / `xhigh` / `max` — controls thinking budget.
