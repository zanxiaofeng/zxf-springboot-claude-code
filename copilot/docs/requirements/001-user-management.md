# REQ-001: User Management

## Status
- [x] Requirement Analysis
- [x] Technical Design
- [ ] Implementation
- [ ] Testing
- [ ] Completed

## Background
User management is the core module of the system, providing basic CRUD operations for user accounts.

## Functional Requirements

### UC-001: Create User
**Given** no user with the same email exists  
**When** a POST request to `/api/v1/users` with valid data is received  
**Then** the user is created and a 201 response with user data is returned

#### Acceptance Criteria
1. Username is required, 3-20 characters, alphanumeric
2. Email is required, must be valid format
3. Password is required, min 8 characters, at least 1 uppercase, 1 number, 1 special char
4. Bio is optional, max 500 characters
5. Returns 409 CONFLICT if email already exists

### UC-002: Get User by ID
**Given** a user exists in the system  
**When** a GET request to `/api/v1/users/{id}` is received  
**Then** the user data is returned with 200 OK

#### Acceptance Criteria
1. Returns 404 NOT FOUND if user does not exist
2. Returns full user data excluding password

### UC-003: List Users
**Given** users exist in the system  
**When** a GET request to `/api/v1/users` is received  
**Then** a paginated list of users is returned

### UC-004: Update User
**Given** a user exists  
**When** a PUT request to `/api/v1/users/{id}` with valid data is received  
**Then** the user is updated and 200 OK is returned

### UC-005: Delete User
**Given** a user exists  
**When** a DELETE request to `/api/v1/users/{id}` is received  
**Then** the user is deleted and 204 No Content is returned

## Non-Functional Requirements
- Response time < 200ms (P95)
- All endpoints covered by Contract Tests

## Database Changes
- New `users` table (see `db/migration/V1__create_users_table.sql`)

## Related Docs
- API Spec: `docs/design/api-spec-v1.md#users`
- Domain Model: `docs/design/domain-model.md#user`
