# API Conventions

## URL Pattern
- Base path: `/api/v{version}/{resource}`
- Plural nouns: `/users`, `/orders`
- Actions via HTTP Method, not URL verbs

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
  "data": {},
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
