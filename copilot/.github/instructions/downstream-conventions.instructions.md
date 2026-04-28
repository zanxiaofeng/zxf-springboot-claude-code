---
applyTo: "**/*.java,**/*.yml,**/*.yaml,**/*.properties"
---

# Downstream Integration Conventions

## Design Principle
- Downstream service interfaces belong to the **domain layer** (`domain/downstream/`)
- Implementations belong to the **infrastructure layer** (`infrastructure/downstream/`)
- Use `RestTemplate` for HTTP calls with configured timeouts
- Never call downstream directly from Controller or Service without going through the domain interface

## Timeout Configuration
```java
@Bean
public RestTemplate downstreamRestTemplate(RestTemplateBuilder builder) {
    return builder
            .setConnectTimeout(Duration.ofSeconds(3))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
}
```

## Error Handling
- Downstream failures must not break the main business flow
- Return `false` or default value on failure
- Log errors at WARN or ERROR level
- Consider circuit breaker for production (not in demo)

## Testing with WireMock
1. Add `@AutoConfigureWireMock(port = 0)` to `IntegrationTestBase`
2. Use `wireMockServer.stubFor(...)` to define expected downstream responses
3. Use `wireMockServer.verify(...)` to confirm downstream was called
4. Reset stubs in `@BeforeEach` to ensure test isolation

## Configuration
Production: `app.downstream.{service}.base-url` in `application.yml`
Test: `app.downstream.{service}.base-url` pointing to `http://localhost:${wiremock.server.port}` in `application-test.yml`
