# Contract Test Skill

## Directory
```
src/test/resources/contracts/
├── users/
│   ├── shouldCreateUser.groovy
│   ├── shouldReturnUserById.groovy
│   ├── shouldReturn404WhenUserNotFound.groovy
│   └── shouldRejectInvalidCreateUser.groovy
└── orders/
    └── ...
```

## Groovy DSL Template
```groovy
package contracts.users
import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Create new user - success scenario"
    request {
        method POST()
        url '/api/v1/users'
        headers { header 'Content-Type', 'application/json' }
        body([ username: 'john.doe', email: 'john@example.com', password: 'SecurePass123!' ])
    }
    response {
        status 201
        headers {
            header 'Content-Type', 'application/json'
            header 'Location', $(regex('/api/v1/users/\\d+'))
        }
        body([
            id: $(regex(positiveInt())),
            username: 'john.doe',
            email: 'john@example.com',
            createdAt: $(regex(iso8601WithOffset())),
            status: 'ACTIVE'
        ])
    }
}
```

## Checklist
- [ ] Request/Response fields match docs/design/api-spec-v1.md
- [ ] Use regex for dynamic values (id, timestamp, email)
- [ ] Error scenario Contracts covered (400, 404, 500)
- [ ] `./scripts/run-contract-tests.sh` passes after Stub generation
