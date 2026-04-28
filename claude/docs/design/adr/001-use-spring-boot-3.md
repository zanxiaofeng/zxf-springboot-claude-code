# ADR-001: Use Spring Boot 3.x

## Status
Accepted

## Context
We need to select a framework for building the REST API service.

## Decision
Use Spring Boot 3.x with Java 21.

## Rationale
- **Mature ecosystem**: Rich set of libraries and tools
- **Auto-configuration**: Reduces boilerplate configuration
- **Production-ready**: Built-in metrics, health checks, and monitoring
- **Community support**: Large community and extensive documentation
- **Native image support**: GraalVM native image compilation for better startup time

## Consequences
- Must use Java 17+ (Java 21 chosen for latest features)
- Requires Jakarta EE namespace (javax -> jakarta)
- Some Spring Boot 2.x dependencies may need migration

## Alternatives Considered
- Quarkus: Good for native compilation but smaller ecosystem
- Micronaut: Similar to Spring Boot but less mature

## Date
2024-01-15
