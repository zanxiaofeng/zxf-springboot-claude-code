# Project Overview

This is a demo Spring Boot 3 REST API project following hexagonal architecture (Domain-Driven Design).

## Architecture
The project follows a four-layer architecture:
- **Domain**: Entities, Value Objects, Repository Interfaces
- **Application**: Services, DTOs, Mappers, Events
- **Infrastructure**: Repository Implementations, Config, Security
- **Interfaces**: Controllers, Exception Handlers, Filters

## Module
Currently implementing: **User Management Module**
- User CRUD operations
- Input validation
- Contract-tested API endpoints

## Goal
Demonstrate best practices for Spring Boot 3 development with TDD, Contract Testing, and Flyway Migration.
