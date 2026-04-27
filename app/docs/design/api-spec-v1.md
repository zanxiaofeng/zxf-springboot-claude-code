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

## Users

### POST /api/v1/users
Create a new user.

**Request Body:**
```json
{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "SecurePass123!"
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
    "createdAt": "2026-04-27T12:00:00+08:00",
    "status": "ACTIVE"
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
    "createdAt": "2026-04-27T12:00:00+08:00",
    "status": "ACTIVE"
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
  "email": "john.updated@example.com"
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
