# Spring Boot Conventions

## Layering
Domain -> Application -> Infrastructure -> Interfaces

## Transactional
- `@Transactional` only on Service layer
- Class-level default: `@Transactional(readOnly = true)`
- Write operations: `@Transactional` (overrides class-level)

## API Path
- `/api/v{version}/{resources}`
- Resources use plural nouns: `/users`

## Response Wrapper
- All Controllers return `ApiResponse<T>`

## DTOs
- Use `record` for all DTOs
- Immutable by design

## Services
- Use interface + impl pattern
- Business logic in Service, not Controller
