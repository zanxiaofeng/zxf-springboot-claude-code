---
name: "Security Audit"
description: "Security audit for OWASP Top 10 compliance, vulnerability detection, and secure coding practices in Java/Spring Boot projects"
agent: "agent"
---

Perform a comprehensive security audit of the codebase following OWASP Top 10 guidelines.

## Required Context

Read these instruction files before auditing:
- `.github/instructions/validation.instructions.md` — input validation conventions
- `.github/instructions/downstream-conventions.instructions.md` — downstream security
- `.github/instructions/logging.instructions.md` — logging standards (what NOT to log)

## Steps

1. **Scan all Controllers** — find every endpoint via `@.*Mapping` annotations
2. **Check authentication/authorization** — verify security config covers all endpoints
3. **Check input validation** — verify `@Valid` on all `@RequestBody` parameters, check DTOs for validation annotations
4. **Check SQL injection** — search for native queries, string concatenation in SQL, `createNativeQuery`
5. **Check sensitive data** — search for hardcoded passwords, API keys, tokens; check `toString()` and responses for sensitive fields
6. **Check downstream security** — verify HTTPS, timeout configuration, error handling
7. **Check error information leakage** — verify exception handlers don't expose stack traces or internal details
8. **Check CORS and CSRF** — verify configuration is restrictive
9. **Generate risk report**

## Vulnerability Detection Patterns

### Hardcoded Secrets (A07:2021)
Search for: string literals resembling passwords, API keys, tokens in Java and config files.

### SQL Injection (A03:2021)
Search for: `nativeQuery = true`, string concatenation in `@Query`, `createNativeQuery`, `createSQLQuery`.

### Input Validation (A03:2021)
Search for: `@RequestBody` without `@Valid`, request DTOs without `@NotBlank`/`@NotNull`/`@Size`.

### Sensitive Data Exposure (A01:2021)
Search for: `password`/`token`/`secret` in `@ToString`, log statements, and response DTOs.

### Broken Access Control (A01:2021)
Search for: endpoints without `@PreAuthorize`/`@Secured`/`@RolesAllowed`, missing CSRF config.

### Downstream Security
Search for: `http://` (not HTTPS) in production config, missing `connectTimeout`/`readTimeout`.

## Security Checklist

- [ ] All endpoints have access control
- [ ] `@Valid` on all request DTOs
- [ ] No hardcoded secrets in code or config
- [ ] SQL uses parameterized queries or JPA
- [ ] Error responses don't leak stack traces or SQL
- [ ] Downstream calls use HTTPS with timeouts
- [ ] No sensitive data in URLs
- [ ] CORS is restrictive
- [ ] Sensitive fields excluded from `toString()` and responses
- [ ] Logging doesn't include passwords/tokens/request bodies
- [ ] `BusinessException` messages don't reveal internal state
- [ ] `DataIntegrityViolationException` messages sanitized

## Output Format

### Risk Summary
| Metric | Value |
|--------|-------|
| Overall risk | Low / Medium / High / Critical |
| Critical findings | N |
| Warnings | N |
| Files scanned | N |

### Critical Findings
1. **[CRITICAL]** `{file}:{line}` — OWASP: {A0X:2021 Name}
   - **Vulnerability:** description
   - **Remediation:** fix

### Warnings
1. **[WARNING]** `{file}:{line}` — description
   - **Recommendation:** how to address

### OWASP Top 10 Compliance
| Category | Status | Notes |
|----------|--------|-------|
| A01: Broken Access Control | Pass/Fail | ... |
| A02: Cryptographic Failures | Pass/Fail | ... |
| A03: Injection | Pass/Fail | ... |
| A04: Insecure Design | Pass/Fail | ... |
| A05: Security Misconfiguration | Pass/Fail | ... |
| A06: Vulnerable Components | Pass/Fail | ... |
| A07: Auth Failures | Pass/Fail | ... |
| A08: Data Integrity Failures | Pass/Fail | ... |
| A09: Logging Failures | Pass/Fail | ... |
| A10: SSRF | Pass/Fail | ... |
