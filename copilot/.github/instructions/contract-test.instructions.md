---
name: "Contract Testing"
description: "Spring Cloud Contract testing with Groovy DSL and RestAssuredMockMvc"
applyTo: "**/*.groovy,**/contract/**/*.java"
---

# Contract Test Guide

## Directory
```
src/test/resources/contracts/
└── {entity}/
    ├── shouldCreate{Entity}.groovy
    ├── shouldReturn{Entity}ById.groovy
    ├── shouldReturn404When{Entity}NotFound.groovy
    └── shouldRejectInvalidCreate{Entity}.groovy
```

## Groovy DSL Template
```groovy
Contract.make {
    description "..."
    request {
        method POST()
        url '/api/v1/{resources}'
        headers { header 'Content-Type', 'application/json' }
        body([ {field1}: 'test-value-1', {field2}: 'test-value-2' ])
    }
    response {
        status 201
        headers {
            header 'Content-Type', 'application/json'
            header 'Location', $(regex('/api/v1/{resources}/\\d+'))
        }
        body([
            id: $(regex(positiveInt())),
            {field1}: 'test-value-1',
            {field2}: 'test-value-2',
            createdAt: $(regex(iso8601WithOffset())),
            status: 'ACTIVE'
        ])
    }
}
```

## Base Test Class
```java
@SpringBootTest(classes = {Project}Application.class, webEnvironment = MOCK)
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
