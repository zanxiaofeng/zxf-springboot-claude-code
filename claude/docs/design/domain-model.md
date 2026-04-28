# Domain Model

## User

### Attributes
| Attribute | Type | Constraints |
|-----------|------|-------------|
| id | Long | PK, auto-generated |
| username | String | 3-20 chars, alphanumeric, unique |
| email | String | Valid email format, unique |
| password | String | Min 8 chars, 1 uppercase, 1 number, 1 special |
| status | UserStatus | ACTIVE or INACTIVE |
| createdAt | OffsetDateTime | Auto-set on creation |
| updatedAt | OffsetDateTime | Auto-updated on modification |

### Status
```java
public enum UserStatus {
    ACTIVE, INACTIVE
}
```

### Relationships
None (standalone entity for demo).

## Value Objects
- **Email**: Validates email format
- **Password**: Validates password strength

## Repository
- `UserRepository` (interface in domain layer)
- `UserJpaRepository` (JPA repository in infrastructure layer)
- `UserJpaAdapter` (adapter bridging domain interface to JPA)
