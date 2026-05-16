# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Purpose

This is a **showcase repository** demonstrating AI Coding tool configuration best practices. It contains two identical Spring Boot 3 REST API projects, each with a complete, independent set of AI tool configurations:

- **`claude/`** — Spring Boot project configured for Claude Code (`CLAUDE.md` + `.claude/rules/` + `.claude/skills/` + `.claude/agents/`)
- **`copilot/`** — Spring Boot project configured for GitHub Copilot (`.github/copilot-instructions.md` + `.github/instructions/` + `.github/prompts/`)

Both share identical source code, pom.xml, docker-compose.yml, scripts, and design docs. The **only difference** is the AI configuration layer.

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
