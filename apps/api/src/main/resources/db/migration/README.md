
# Database Migrations

This directory contains version-controlled Flyway SQL migrations
for the PostgreSQL application database.

## Naming convention

Versioned migration:

V1__description.sql
V2__description.sql
V3__description.sql

Repeatable migration:

R__description.sql

## Rules

1. Never modify a migration that has already been applied.
2. Create a new migration for every subsequent schema change.
3. Use descriptive migration names.
4. Keep migrations focused and atomic.
5. Validate migrations against PostgreSQL.
6. Do not perform manual schema changes outside migrations.
7. Avoid placing business-specific schema in platform-only migrations.

## First application migration

V1__create_business_persistence.sql establishes:

- Cloud workspace persistence.
- Business template identity and version records.
- Code-backed and declarative template source types.
- Template visibility and governance lifecycle metadata.
- Generic business records.
- JSONB business configuration.
- The initial Coffee Shop code-backed template version.

The Coffee Shop Java implementation remains authoritative for
code-backed template behavior.

Declarative definitions are stored as structured data and must
be validated and interpreted only by approved platform capabilities.
They must never be executed as arbitrary source code.

## Test database

The test profile uses H2 with a structurally equivalent schema under:

src/test/resources/db/test-migration

The H2 migration exists to support fast automated tests without
Docker or an external PostgreSQL service.

It is not a substitute for validating the PostgreSQL migration.
The application must also be started against a clean PostgreSQL
database before this slice is considered complete.
