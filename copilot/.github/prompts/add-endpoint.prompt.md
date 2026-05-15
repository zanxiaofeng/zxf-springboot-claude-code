Add a new REST API endpoint following the contract-first approach.

## Pre-conditions
- API spec updated in `docs/design/api-spec-v1.md`
- DTOs defined as `record`
- Service interface has the method

## Steps
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
