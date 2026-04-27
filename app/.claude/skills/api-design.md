# API Design Skill

## Process
1. Read requirement doc
2. Define resource model (DTO)
3. Design URL + HTTP Method
4. Write Contract Test
5. Implement endpoint

## Naming
- Resource always plural: `/users`, not `/user`
- No verbs in URL: use HTTP Method to express action
- DTO naming: `{Action}{Entity}Request`, `{Entity}Response`

## Example
```
POST   /api/v1/users       -> Create user
GET    /api/v1/users/{id}  -> Get user by id
PUT    /api/v1/users/{id}  -> Full update
PATCH  /api/v1/users/{id}  -> Partial update
DELETE /api/v1/users/{id}  -> Delete user
```
