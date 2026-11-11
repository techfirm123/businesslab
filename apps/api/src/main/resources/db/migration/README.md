# Database Migrations

This directory contains version-controlled Flyway SQL migrations.

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

The directory is intentionally starting without a versioned migration.

Flyway will create its schema history table during initialization.

The first actual application-domain migration will be introduced
with the Generic Business Foundation implementation.
