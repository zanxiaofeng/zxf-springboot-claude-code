# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Purpose

This is an **AI Agent Harness** — a scaffolding project providing industry-standard specifications, workflows, and sub-agent configurations for AI-assisted Spring Boot 3 Java API development. It ships two complete, tool-specific configurations:

- **`claude/`** — Harness for Claude Code (`CLAUDE.md` + `.claude/rules/` + `.claude/skills/` + `.claude/agents/`)
- **`copilot/`** — Harness for GitHub Copilot (`.github/copilot-instructions.md` + `.github/instructions/` + `.github/prompts/`)

Both directories contain an identical Spring Boot 3 demo application to validate the harness. The demo code serves as a **reference implementation** proving the specifications work end-to-end.

### Core Principles

1. **Harness over demo** — The primary deliverable is the AI configuration layer (rules, workflows, agents), not the Spring Boot application itself
2. **Universal standards** — Rules, workflows, and specifications in `claude/` and `copilot/` target **all Spring Boot 3 REST API projects**, not just this demo. They must reflect industry best practices, established standards, or battle-tested personal practices — not project-specific hacks
3. **Compliant generated code** — All source code in both directories must conform to the standards defined in the harness rules. Code is the proof that the harness works, so it must be exemplary

## Working in Subdirectories

All build and test commands must be run from within `claude/` or `copilot/`:

```bash
cd claude  # or copilot

mvn compile -q                          # Build
mvn test                                 # Run all tests
mvn test -Dtest="*ApiTests"             # Run API tests only
./scripts/fast-test.sh                   # Quick test (skip contract)
./scripts/run-contract-tests.sh          # Contract tests only
./scripts/full-ci.sh                     # Full CI pipeline
```

When editing code, changes should be made in **both directories** to keep them in sync. The root `.claude/settings.local.json` has rsync/cp permissions to facilitate syncing.

## Architecture (Spring Boot projects)

Four-layer hexagonal architecture (identical in both):

```
domain/          → Entity, Repository Interface, Downstream Client Interface, BusinessException
application/     → Service (interface + impl), DTO (record), Mapper
infrastructure/  → RepositoryImpl, Config, Security, Downstream Client Impl
interfaces/      → Controller, ExceptionHandler, ApiResponse<T>
```

Key conventions:
- Constructor injection only (`@RequiredArgsConstructor`), no field `@Autowired`
- DTOs use `record`, Services use interface + impl pattern
- All Controllers return `ApiResponse<T>`
- `@Transactional(readOnly = true)` at class level on Services
- `@Slf4j` (Lombok) for logging, `OffsetDateTime` for timestamps
- All DDL via Flyway migration, never modify merged migrations
- No real MySQL in tests — H2 only, WireMock for downstream stubs
- Contract-First: write Contract Test before API implementation

## Syncing Between Directories

When adding or modifying content in one directory, apply the same change to the other. The mapping:

| Claude Code | GitHub Copilot |
|-------------|----------------|
| `CLAUDE.md` | `.github/copilot-instructions.md` |
| `.claude/rules/*.md` (with `files:` glob) | `.github/instructions/*.instructions.md` (with `applyTo:` glob) |
| `.claude/skills/*/SKILL.md` | `.github/prompts/*.prompt.md` |
| — | `.github/workflows/copilot-setup-steps.yml` (coding agent env setup) |

## Updating Harness Specifications

When modifying rules, workflows, or agents:

1. **Think generically** — Every rule should apply to any Spring Boot 3 REST API project, not just this demo. Avoid hardcoding demo-specific entity names, field names, or business logic into specifications
2. **Reference industry standards** — Ground rules in established practices: DDD patterns, REST conventions, Spring team recommendations, effective Java idioms
3. **Demonstrate with examples** — Use `{Entity}`, `{Project}`, or generic names in specifications; the demo code provides concrete instances
4. **Sync both harnesses** — Changes to Claude rules must be reflected in Copilot instructions, and vice versa

## Root `docs/` Directory

Contains reference material (AI coding articles, official documentation) used during development. Not part of either Spring Boot project.

## README Maintenance

After any significant change to the repository (adding/removing files, modifying configurations, changing structure), you MUST:

1. **Review all affected READMEs** — check that file lists, counts, directory trees, and descriptions match the actual state
2. **Update the following READMEs as needed**:
   - `README.md` (root) — project overview, AI config directory structure, module counts, file index
   - `claude/README.md` — rules/skills/agents counts and descriptions
   - `copilot/README.md` — instructions/prompts/workflows counts and descriptions
   - `claude/docs/requirements/README.md` and `copilot/docs/requirements/README.md` — requirement status
3. **Verify counts** — rule/instruction file counts, agent counts, skill/prompt counts must match actual files
4. **Verify paths** — all referenced file paths must exist
5. **Keep both subprojects in sync** — `claude/README.md` and `copilot/README.md` should reflect identical project structure
