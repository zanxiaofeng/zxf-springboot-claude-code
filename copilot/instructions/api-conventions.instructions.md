---
applyTo: "**/interfaces/**/*.java,**/*.md"
---

# API Design Conventions

## URL Pattern
- Base path: `/api/v{version}/{resource}`
- Plural nouns: `/users`, `/orders`
- No verbs in URL (use HTTP Method)

## HTTP Method Semantics
| Method | Purpose | Success Code |
|--------|---------|-------------|
| GET    | Query   | 200 |
| POST   | Create  | 201 |
| PUT    | Full update | 200 |
| PATCH  | Partial update | 200 |
| DELETE | Delete  | 204 |

## Response Body
```json
{
  "code": "SUCCESS",
  "data": { },
  "message": null,
  "timestamp": "2026-04-27T12:00:00+08:00",
  "traceId": "abc123"
}
```

## Error Response
```json
{
  "code": "VALIDATION_ERROR",
  "data": null,
  "message": "Invalid request parameters",
  "timestamp": "2026-04-27T12:00:00+08:00",
  "traceId": "abc123",
  "errors": [
    { "field": "email", "message": "must be a valid email" }
  ]
}
```

## Downstream Side Effects
When an endpoint triggers a downstream call, document it in the API spec:
- Endpoint URL, payload format, failure mode
- Example: `POST /api/v1/users` sends `POST /api/v1/notifications/user-created`

## Adding a New Endpoint

### Pre-conditions
- [ ] API spec updated in `docs/design/api-spec-v1.md`
- [ ] DTOs defined as record
- [ ] Service interface has the method

### Steps
1. Add method to Service implementation
2. Create/update Controller endpoint
3. Write integration test (TestRestTemplate + H2 on real server)
4. Write Contract Test
5. Update OpenAPI spec

### Validation
- URL follows `/api/v1/{resource}` pattern
- HTTP Method matches action
- Response uses `ApiResponse<T>`
- Contract Test covers success and error scenarios
