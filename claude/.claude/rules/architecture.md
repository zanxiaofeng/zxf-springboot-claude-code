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
│  Domain       Entity, Repository    │  Business entities, JPA annotations
│               Interface, VO         │  allowed for simplicity
├─────────────────────────────────────┤
│  Infrastructure RepositoryImpl,    │  Technical details
│               Config, Security      │
└─────────────────────────────────────┘
```

## Layer Rules

- **Domain**: Business entities with JPA annotations (pragmatic simplification). Repository interfaces, Value Objects, domain exceptions, downstream client interfaces. No Spring service/component annotations
- **Application**: Services contain business logic. DTOs use `record`. Mappers convert Entity <-> DTO
- **Infrastructure**: Repository implementations, configuration, security, downstream HTTP clients
- **Interfaces**: Controllers return `ApiResponse<T>`. Exception handlers. No business logic

## Current Module

**User Management Module** — CRUD operations with downstream notification
