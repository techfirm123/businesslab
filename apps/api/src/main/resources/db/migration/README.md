
# Database Migrations

This directory contains version-controlled Flyway SQL migrations
for the PostgreSQL application database.

## Naming convention

Versioned migration:

- V1__description.sql
- V2__description.sql
- V3__description.sql
- V4__description.sql

Repeatable migration:

- R__description.sql

## Rules

1. Never modify a migration that has already been applied.
2. Create a new migration for every subsequent schema change.
3. Use descriptive migration names.
4. Keep migrations focused and atomic.
5. Validate migrations against PostgreSQL.
6. Do not perform manual schema changes outside migrations.
7. Avoid placing business-specific schema in platform-only migrations.

## Migration history

### V1 — Business persistence

`V1__create_business_persistence.sql` establishes:

- Cloud workspace persistence.
- Business template identity and version records.
- Code-backed and declarative template source types.
- Template visibility and governance lifecycle metadata.
- Generic business records.
- JSONB business configuration.
- The initial Coffee Shop code-backed template version.

### V2 — User identity

`V2__create_user_identity_foundation.sql` establishes:

- Application users.
- Normalized email identity.
- User lifecycle status.
- Password credential storage.

### V3 — Persistent sessions

`V3__create_persistent_http_sessions.sql` establishes the
JDBC-backed Spring Security session persistence tables.

### V4 — Workspace membership

`V4__create_workspace_membership.sql` establishes:

- Workspace-to-user membership.
- Workspace membership roles: OWNER, ADMIN, MEMBER and VIEWER.
- Membership lifecycle: ACTIVE, SUSPENDED and REMOVED.
- One active membership identity per user per workspace record.
- At most one OWNER membership per workspace through a PostgreSQL
  partial unique index.
- User- and workspace-oriented membership indexes.

Existing workspaces created before V4 receive no automatic owner.
Their ownership cannot safely be inferred from existing data.
They remain inaccessible through membership-scoped APIs until
a controlled recovery or assignment process is implemented.

## Template execution

The Coffee Shop Java implementation remains authoritative for
code-backed template behavior.

Declarative definitions are stored as structured data and must
be validated and interpreted only by approved platform capabilities.
They must never be executed as arbitrary source code.

## Test database

The test profile uses H2 with equivalent migration table structures
under:

`src/test/resources/db/test-migration`

The H2 V4 migration omits the PostgreSQL partial unique index because
that filtered-index syntax is database-specific. Application-level
tests cover the workspace creation path, but H2 does not replace
PostgreSQL constraint validation.

The application must also be started against PostgreSQL before
this slice is considered complete.
