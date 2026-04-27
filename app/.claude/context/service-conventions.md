# Service Conventions

## Layer Rules
- @Transactional only on Service layer, default readOnly = true
- Business logic must reside in Service, not Controller
- Use interface + impl pattern for Services
- Constructor injection only

## Transactional Rules
- Class-level: @Transactional(readOnly = true)
- Write operations: @Transactional (override class-level)
- Query methods: use default readOnly = true

## Naming
- Service interface: `{Entity}Service`
- Implementation: `{Entity}ServiceImpl`
- Methods: action + entity, e.g., `createUser`, `findUserById`
