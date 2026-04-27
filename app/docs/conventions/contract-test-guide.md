# Contract Test Guide

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

## DSL Template
```groovy
Contract.make {
    description "..."
    request { ... }
    response { ... }
}
```

## Checklist
- [ ] Fields match api-spec-v1.md
- [ ] Regex for dynamic values (id, timestamp)
- [ ] Error scenarios (400, 404, 500)
- [ ] run-contract-tests.sh passes
