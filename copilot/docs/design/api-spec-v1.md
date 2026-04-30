# API Specification v1

## Base URL
`/api/v1`

## Authentication
Not required for demo purposes.

## Common Response Format
All responses use `ApiResponse<T>` wrapper:
```json
{
  "code": "SUCCESS",
  "data": { ... },
  "message": null,
  "timestamp": "2026-04-27T12:00:00+08:00",
  "traceId": "abc123"
}
```

## Downstream Integration
Certain endpoints trigger calls to the downstream notification service:
- Base URL: `http://localhost:8090` (production), random port via WireMock (tests)
- Timeout: 3s connect / 5s read
- Failure mode: downstream failures are logged but do not break the main response

## Users

### POST /api/v1/users
Create a new user.

**Downstream Side Effect**: On successful creation, sends a `POST /api/v1/notifications/user-created` request to the downstream notification service with `{userId, username, email, eventType: "USER_CREATED"}`.

**Request Body:**
```json
{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "bio": "Full-stack developer"
}
```

**Response 201:**
```json
{
  "code": "SUCCESS",
  "data": {
    "id": 1,
    "username": "john.doe",
    "email": "john@example.com",
    "status": "ACTIVE",
    "bio": "Full-stack developer",
    "createdAt": "2026-04-27T12:00:00+08:00"
  },
  "message": null
}
```

### GET /api/v1/users/{id}
Get user by ID.

**Response 200:**
```json
{
  "code": "SUCCESS",
  "data": {
    "id": 1,
    "username": "john.doe",
    "email": "john@example.com",
    "status": "ACTIVE",
    "bio": "Full-stack developer",
    "createdAt": "2026-04-27T12:00:00+08:00"
  }
}
```

### GET /api/v1/users
List all users (paginated).

**Query Parameters:**
- page (default: 0)
- size (default: 20)

**Response 200:**
```json
{
  "code": "SUCCESS",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

### PUT /api/v1/users/{id}
Update user.

**Request Body:**
```json
{
  "username": "john.doe.updated",
  "email": "john.updated@example.com",
  "bio": "Updated bio text"
}
```

**Response 200:** Updated user data

### DELETE /api/v1/users/{id}
Delete user.

**Response 204:** No content

## Error Codes
| Code | Description |
|------|-------------|
| SUCCESS | Success |
| VALIDATION_ERROR | Validation failed |
| USER_NOT_FOUND | User not found |
| USER_ALREADY_EXISTS | User already exists |
| INTERNAL_ERROR | Internal server error |
