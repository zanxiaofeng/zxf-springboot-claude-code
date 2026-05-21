---
paths:
  - "**/interfaces/**/*.java"
  - "**/docs/**/*.md"
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
| DELETE | Delete  | 200 |

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
  "code": "002001",
  "data": null,
  "message": "Request validation failed",
  "timestamp": "2026-04-27T12:00:00+08:00",
  "traceId": "abc123",
  "errors": [
    { "field": "email", "message": "must be a valid email", "rejectedValue": "invalid" }
  ]
}
```

## Downstream Side Effects
When an endpoint triggers a downstream call, document it in the API spec:
- Endpoint URL, payload format, failure mode
- Example: `POST /api/v1/users` sends `POST /api/v1/notifications/user-created`

## API Versioning Strategy
- **URL-based versioning**: `/api/v1/...`, `/api/v2/...`
- **When to bump version**: breaking changes (removing fields, changing types, renaming endpoints)
- **Non-breaking changes** (adding optional fields, new endpoints) do NOT require version bump
- **Version coexistence**: both versions run simultaneously, old version deprecated with sunset header
- **Controller organization**: `{Entity}V1Controller`, `{Entity}V2Controller` — separate classes, same or different packages
- **Deprecation**: `@Deprecated` annotation + `Sunset` response header, minimum 6 months overlap before removal
