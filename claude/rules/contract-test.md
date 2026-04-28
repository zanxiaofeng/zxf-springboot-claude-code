---
files: ["**/*.groovy", "**/contract/**/*.java"]
---

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

## Groovy DSL Template
```groovy
Contract.make {
    description "..."
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

## Base Test Class
```java
@SpringBootTest(classes = DemoApplication.class, webEnvironment = MOCK)
@ActiveProfiles("test")
public abstract class ContractBaseTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(
            MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        );
    }
}
```

## Checklist
- [ ] Request/Response fields match `docs/design/api-spec-v1.md`
- [ ] Use regex for dynamic values (id, timestamp, email)
- [ ] Error scenarios (400, 404, 500) covered
- [ ] `./scripts/run-contract-tests.sh` passes after Stub generation
