---
name: "Tech Stack"
description: "Technology stack versions and dependencies (Java 21, Spring Boot 3.5, Maven)"
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
- Lombok (boilerplate reduction: @Data, @Builder, @Slf4j, @RequiredArgsConstructor)
- Apache Commons Lang 3 (StringUtils, ObjectUtils)
- Flyway (version managed by Spring Boot BOM)
- Spring Data JPA
- Spring Validation
- Spring Web
- Spring Security (CSRF configuration per project requirements)
- RestTemplate / RestClient (downstream HTTP client; RestClient preferred for new projects)

## Downstream Integration
- WireMock 3.x (test stubbing for downstream services)
- RestTemplate with 3s connect / 5s read timeout
