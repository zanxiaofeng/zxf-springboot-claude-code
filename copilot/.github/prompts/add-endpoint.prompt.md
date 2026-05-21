---
name: "Add Endpoint"
description: "Add a new REST API endpoint following the contract-first approach"
agent: "agent"
---

Add a new REST API endpoint following the contract-first approach.

## Pre-conditions
- API spec updated in `docs/design/api-spec-v1.md`
- DTOs defined as `record`
- Service interface has the method

## Steps
0. **Validate pre-conditions** — verify:
   - `docs/design/api-spec-v1.md` contains the endpoint definition
   - Service interface has the required method
   If any condition fails: output the specific missing item and stop.
1. Add method to Service implementation
2. Create/update Controller endpoint returning `ApiResponse<T>`
3. Write API test (WebTestClient + JSON fixtures + @Sql seed data)
4. Write Contract Test (Spring Cloud Contract Groovy DSL)
5. Update OpenAPI spec if applicable

## Validation Checklist
- URL follows `/api/v1/{resource}` pattern
- HTTP Method matches action semantics
- Response uses `ApiResponse<T>` wrapper
- Contract Test covers success and error scenarios
- `mvn test` passes
