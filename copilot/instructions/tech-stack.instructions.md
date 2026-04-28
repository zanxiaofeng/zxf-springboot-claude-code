---
applyTo: "**/pom.xml,**/*.java,**/*.yml,**/*.yaml,**/*.properties"
---

# Tech Stack

- Java 21
- Spring Boot 3.5.x
- Maven 3.9+
- MySQL 8.0 (production)
- H2 (testing)

## Testing
- JUnit 5
- AssertJ
- Spring Cloud Contract 4.3.0
- WireMock (via spring-cloud-contract-wiremock)

## Infrastructure
- Flyway 10.x (database migration)
- Spring Data JPA
- Spring Validation
- Spring Web
- Spring Security (CSRF disabled for demo)
- RestTemplate (downstream HTTP client)

## Downstream Integration
- WireMock 3.x (test stubbing for downstream services)
- RestTemplate with 3s connect / 5s read timeout
