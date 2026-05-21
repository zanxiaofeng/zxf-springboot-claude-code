---
name: "Code Review"
description: "Systematic code review for quality, architecture compliance, test coverage, and security in Java/Spring Boot projects"
agent: "agent"
---

Perform a systematic code review following the project's architecture and coding standards.

## Required Context

Before reviewing, read these instruction files to understand conventions:
- `.github/instructions/architecture.instructions.md` — four-layer architecture, dependency rules
- `.github/instructions/code-review.instructions.md` — review checklist
- `.github/instructions/validation.instructions.md` — Bean Validation conventions
- `.github/instructions/logging.instructions.md` — logging standards

## Steps

1. **Identify changed files** — list all files to review
2. **Load relevant instructions** — read the instruction files above; also load domain-specific rules:
   - `api-conventions.instructions.md` for Controller changes
   - `service-conventions.instructions.md` for Service changes
   - `db-conventions.instructions.md` for Entity/migration changes
   - `test-conventions.instructions.md` for test changes
   - `downstream-conventions.instructions.md` for downstream client changes
3. **Read each changed file** — read in full, plus related files for context
4. **Detect anti-patterns** — search for common violations:
   - `@Autowired` field injection (must use constructor injection)
   - Business logic in Controller (`if`/`for`/`while` in Controller methods)
   - Service injecting `JpaRepository` directly (bypasses Domain Port)
   - `EnumType.ORDINAL` (must use STRING)
   - `new Date()` / `java.util.Date` / `LocalDateTime` (must use `OffsetDateTime`)
   - `LoggerFactory.getLogger` (must use `@Slf4j`)
   - Missing `@Version` on `@Entity` classes
   - Hardcoded credentials/secrets
5. **Cross-reference against rules** — verify each change against relevant instruction files
6. **Generate structured review report**

## Review Checklist

### Code Quality
- [ ] JavaDoc on all public classes and methods
- [ ] Constructor injection (`@RequiredArgsConstructor`), no `@Autowired` on fields
- [ ] DTOs use `record`
- [ ] Service has interface + impl
- [ ] No business logic in Controller
- [ ] `@Slf4j` for logging, no manual Logger
- [ ] `OffsetDateTime` for timestamps

### Architecture
- [ ] Four-layer separation maintained
- [ ] Domain layer has no Spring/framework dependencies
- [ ] Repository is interface in domain, implementation in infrastructure
- [ ] Downstream client is interface in domain, implementation in infrastructure

### Testing
- [ ] Test naming: `*ApiTests` for API tests, `*ContractTest` for contract tests
- [ ] Contract Test covers new endpoints
- [ ] WireMock stubs present for downstream calls

### Data
- [ ] Flyway migration for DB changes
- [ ] `@Enumerated(STRING)`, never ORDINAL
- [ ] `@Version` on all mutable entities

### Security
- [ ] `@Valid` on all request DTOs
- [ ] No hardcoded secrets
- [ ] Error responses don't leak internal details

## Output Format

### Summary
**Verdict:** Approve / Request Changes / Needs Discussion

### Metrics
| Metric | Value |
|--------|-------|
| Critical issues | N |
| High issues | N |
| Medium issues | N |
| Architecture compliance | Pass / Fail |
| Estimated test coverage | N% |

### Critical Issues (must fix)
1. **[CRITICAL]** `{file}:{line}` — description
   - **Rule violated:** reference
   - **Suggested fix:** approach

### High Issues (strongly recommended)
1. **[HIGH]** `{file}:{line}` — description

### Suggestions
1. **[MEDIUM]** `{file}:{line}` — description

### Questions
1. — clarification needed

### Praise
1. — what was done well
