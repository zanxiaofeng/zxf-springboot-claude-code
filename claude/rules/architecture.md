---
files: ["**/*.java"]
---

# Architecture Overview

This is a demo Spring Boot 3 REST API project following hexagonal architecture (Domain-Driven Design).

## Four-Layer Architecture

```
┌─────────────────────────────────────┐
│  Interfaces   Controller, Exception │  HTTP only — no business logic
│               Handler, Filter       │
├─────────────────────────────────────┤
│  Application  Service, DTO, Mapper  │  Business logic, orchestration
├─────────────────────────────────────┤
│  Domain       Entity, Repository    │  Pure Java — no Spring deps
│               Interface, VO         │
├─────────────────────────────────────┤
│  Infrastructure RepositoryImpl,    │  Technical details
│               Config, Security      │
└─────────────────────────────────────┘
```

## Layer Rules

- **Domain**: No framework dependencies. Entities, Repository interfaces, Value Objects, domain exceptions
- **Application**: Services contain business logic. DTOs use `record`. Mappers convert Entity <-> DTO
- **Infrastructure**: Repository implementations, configuration, security, downstream HTTP clients
- **Interfaces**: Controllers return `ApiResponse<T>`. Exception handlers. No business logic

## Current Module

**User Management Module** — CRUD operations with downstream notification
