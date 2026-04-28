# Add Endpoint Prompt Template

## Pre-conditions
- [ ] API spec updated in `docs/design/api-spec-v1.md`
- [ ] DTOs defined as record
- [ ] Service interface has the method

## Steps
1. Add method to Service implementation
2. Create/update Controller endpoint
3. Write integration test (TestRestTemplate + H2 on real server)
4. Write Contract Test
5. Update OpenAPI spec

## Validation
- URL follows `/api/v1/{resource}` pattern
- HTTP Method matches action
- Response uses `ApiResponse<T>`
- Contract Test covers success and error scenarios
