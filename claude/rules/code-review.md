---
files: ["**/*.java"]
---

# Code Review Checklist

## Checklist
- [ ] JavaDoc on all public classes and methods
- [ ] Constructor injection (no @Autowired on fields)
- [ ] DTOs use record
- [ ] Service has interface + impl
- [ ] No business logic in Controller
- [ ] Test naming: `*IT` for integration, `*ContractTest` for contract
- [ ] Contract Test covers new endpoints
- [ ] Flyway migration for DB changes
- [ ] Error handling uses BusinessException
- [ ] Downstream client interface exists in domain layer (if calling external services)
- [ ] WireMock stubs present for downstream calls in integration tests

## Architecture Review
- [ ] Four-layer separation maintained (Domain -> Application -> Infrastructure -> Interfaces)
- [ ] Domain layer has no Spring/framework dependencies
- [ ] Repository is interface in domain, implementation in infrastructure
- [ ] Downstream client is interface in domain, implementation in infrastructure

## Refactoring Safety
- Never refactor without passing tests
- One refactoring at a time
- Prefer small commits
