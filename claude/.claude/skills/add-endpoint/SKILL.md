---
name: add-endpoint
description: Add a new REST API endpoint following the contract-first approach with integration tests and Spring Cloud Contract tests.
---

# Add Endpoint

## Pre-conditions
- [ ] API spec updated in `docs/design/api-spec-v1.md`
- [ ] DTOs defined as `record`
- [ ] Service interface has the method

## Steps

1. **Add method to Service implementation** — business logic in Service layer
2. **Create/update Controller endpoint** — return `ApiResponse<T>`, URL follows `/api/v1/{resource}`
3. **Write integration test** — TestRestTemplate + H2 on real server
4. **Write Contract Test** — Spring Cloud Contract Groovy DSL
5. **Update OpenAPI spec** if applicable

## Validation Checklist
- [ ] URL follows `/api/v1/{resource}` pattern
- [ ] HTTP Method matches action semantics
- [ ] Response uses `ApiResponse<T>` wrapper
- [ ] Contract Test covers success scenario
- [ ] Contract Test covers error scenarios (400, 404, 500)
- [ ] Downstream side effects documented in API spec (if applicable)
