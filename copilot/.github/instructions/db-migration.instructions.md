---
name: "DB Migration"
description: "Flyway database migration guide with directory structure and destructive change process"
applyTo: "**/*.sql,**/*.java,**/*.yml,**/*.yaml,**/*.properties"
---

# DB Migration Guide

## Directory Structure
```
src/main/resources/db/
└── migration/              -- Core DDL (environment-agnostic, H2 & MySQL compatible)
    ├── V1__create_users_table.sql
    ├── V2__create_orders_table.sql
    └── V3__add_user_bio_column.sql

src/test/resources/sql/     -- Test data (@Sql scripts)
├── cleanup/                -- Truncate/delete before each test
│   └── clean-up.sql
├── init/                   -- Seed data for all tests
│   └── data.sql
└── cases/                  -- Case-level overrides (CLOB via FILE_READ)
    └── user-bio-test.sql
```

## H2 Compatibility

For H2 compatibility syntax mapping, see `db-conventions.instructions.md`.

## Destructive Change Process
1. Create new Migration file (e.g., V4__add_user_phone.sql)
2. Keep old column, add new column, dual-write in application
3. Clean old column in next version Migration
4. Record decision in docs/design/adr/
