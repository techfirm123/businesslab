# M1 — Slice 1B — BL-014: Generic Business Persistence Foundation

**Project:** BusinessLab  
**Milestone:** M1 — Generic Platform Foundation  
**Task:** BL-014 — Generic Business Persistence and APIs  
**Checkpoint:** BL-014.1 and BL-014.2  
**Status:** Persistence foundation implemented; developer-confirmed validation passed  
**Branch:** `feature/m1-generic-business-foundation`

## 1. Objective

Establish persistent cloud workspace and business storage using PostgreSQL and Spring JDBC.

Preserve the generic business domain and exact template-version references introduced in BL-012 and BL-013.

Prepare the persistence foundation for both platform-managed code-backed templates and future declarative, AI-generated templates.

## 2. Database foundation — BL-014.1

Introduced the first application migration:

`apps/api/src/main/resources/db/migration/V1__create_business_persistence.sql`

Created five tables:

- `workspace`
- `business_template`
- `business_template_version`
- `business`
- `business_configuration`

### Template persistence decisions

Two template source types are supported:

- `CODE_BACKED`: platform-managed Java implementation.
- `DECLARATIVE`: structured, versioned definition stored as JSONB.

Template versions retain their own identity and governance metadata.

Declarative template lifecycle statuses:

- DRAFT
- VALIDATING
- APPROVED
- PUBLISHED
- DEPRECATED
- REJECTED

Template visibility:

- PLATFORM
- PRIVATE
- SHARED
- PUBLIC

Published versions are intended to remain immutable. Updates create new versions.

The existing Coffee Shop implementation is seeded as:

- Template ID: `coffee-shop`
- Version: `1.0.0`
- Source: `CODE_BACKED`
- Status: `PUBLISHED`

The Java implementation remains the runtime authority for this version.

### Business persistence

Business records reference an exact template ID and version through a composite foreign key.

Business configuration is stored separately using JSONB in PostgreSQL.

This keeps the generic business record independent of industry-specific configuration structures.

## 3. Test database

The H2 test migration is maintained separately at:

`apps/api/src/test/resources/db/test-migration/V1__create_test_schema.sql`

The test profile uses H2 JSON, while PostgreSQL uses JSONB.

Test migration configuration:

`apps/api/src/test/resources/application-test.yml`

The test migration location was corrected to match the actual resource directory.

H2 validates common persistence behavior. PostgreSQL-specific behavior should also be checked against the local PostgreSQL database.

## 4. Domain and persistence implementation — BL-014.2

### Domain updates

Updated:

- `apps/api/src/main/java/com/businesslab/business/domain/Business.java`
- `apps/api/src/main/java/com/businesslab/workspace/domain/Workspace.java`

Added persistence reconstitution methods so database records can be restored without replaying lifecycle transitions.

### Persistence ports

Added:

- `apps/api/src/main/java/com/businesslab/business/application/BusinessRepository.java`
- `apps/api/src/main/java/com/businesslab/workspace/application/WorkspaceRepository.java`

### JDBC adapters

Added:

- `apps/api/src/main/java/com/businesslab/business/infrastructure/persistence/JdbcBusinessRepository.java`
- `apps/api/src/main/java/com/businesslab/workspace/infrastructure/persistence/JdbcWorkspaceRepository.java`

Responsibilities include:

- Persisting business and workspace records.
- Restoring domain aggregates.
- Persisting and retrieving business configuration.
- Preserving exact template references.
- Supporting workspace-based business retrieval.
- Handling PostgreSQL JSONB and H2 JSON persistence.

Jackson 3 `JsonMapper` is used for JSON serialization and deserialization.

The configuration reader handles JSON documents returned directly as well as JSON strings containing a JSON document.

### Application services

Added:

- `apps/api/src/main/java/com/businesslab/business/application/BusinessApplicationService.java`
- `apps/api/src/main/java/com/businesslab/business/application/BusinessConfigurationValidationException.java`
- `apps/api/src/main/java/com/businesslab/business/application/WorkspaceBusinessCreationResult.java`
- `apps/api/src/main/java/com/businesslab/platform/time/ClockConfiguration.java`

Business creation currently supports registered code-backed templates.

The application service:

1. Resolves the exact template reference.
2. Verifies the workspace is active and cloud-backed.
3. Validates business configuration against the template.
4. Creates the business in DRAFT state.
5. Persists the business and configuration.

Workspace creation and first-business creation execute within one transaction.

Validation or template-resolution failures roll back the workspace creation.

## 5. Declarative template foundation

The declarative template domain was extended with:

- `apps/api/src/main/java/com/businesslab/templates/domain/TemplateSourceType.java`
- `apps/api/src/main/java/com/businesslab/templates/domain/TemplateVisibility.java`
- `apps/api/src/main/java/com/businesslab/templates/domain/DeclarativeTemplateLifecycleStatus.java`
- `apps/api/src/main/java/com/businesslab/templates/domain/DeclarativeTemplateDefinition.java`

The definition is structured JSON-compatible data, not executable source code.

Arbitrary Java, Python, SQL or scripts are not supported as declarative template behavior.

The schema and domain model establish the storage foundation. The declarative interpreter, semantic validator, AI generation and publication APIs remain future work.

## 6. Tests added or updated

Added:

- `apps/api/src/test/java/com/businesslab/business/application/BusinessPersistenceIntegrationTest.java`
- `apps/api/src/test/java/com/businesslab/templates/domain/DeclarativeTemplateDefinitionTest.java`
- `apps/api/src/test/java/com/businesslab/business/application/PostgresBusinessPersistenceIntegrationTest.java`

Updated:

- `apps/api/src/test/java/com/businesslab/platform/database/FlywayMigrationIntegrationTest.java`
- `apps/api/src/test/resources/db/test-migration/V1__create_test_schema.sql`
- `apps/api/src/test/resources/application-test.yml`

The persistence tests cover:

- Workspace and business creation.
- Business retrieval.
- Business retrieval by workspace.
- Exact template-version binding.
- Configuration JSON round trips.
- Decimal configuration values.
- Invalid configuration rejection.
- Unknown template-version rejection.
- Transaction rollback.
- Declarative definition immutability and JSON-compatible values.

The PostgreSQL-specific integration test is environment-gated using `BUSINESSLAB_RUN_POSTGRES_TESTS=true`.

## 7. Validation results

Developer-confirmed:

- Backend clean test run: PASSED.
- Backend test suite: PASSED.
- Backend quality check: PASSED.

The optional PostgreSQL-specific test is skipped during ordinary test runs unless explicitly enabled.

## 8. Architecture decisions

1. PostgreSQL is the authoritative store for cloud workspaces and businesses.
2. Local-only workspaces remain a browser IndexedDB responsibility.
3. Cloud workspace persistence is separate from user identity and entitlement enforcement.
4. Business configuration is persisted as JSONB.
5. H2 tests use a separate compatible JSON schema.
6. Business records always reference an exact template version.
7. Code-backed templates continue using the Spring-managed registry.
8. Declarative templates are stored as structured data.
9. Declarative definitions must never be executed as arbitrary source code.
10. Workspace and initial business creation are atomic.
11. Template-specific configuration validation remains owned by the template implementation.

## 9. Known limitations

Not yet implemented:

- REST APIs for workspace creation and retrieval.
- REST APIs for business creation and retrieval.
- Authentication and ownership enforcement.
- Workspace membership and sharing.
- Authorization for workspace access.
- Declarative template repository and lifecycle persistence adapters.
- Declarative template interpreter.
- Declarative template semantic validation.
- AI Template Studio and its specialist agents.
- Business creation using declarative templates.

The current application service supports code-backed template creation only.

## 10. Next implementation checkpoint

**BL-014.3 — Workspace and Business REST APIs**

Expected scope:

1. Define request and response DTOs.
2. Implement workspace endpoints.
3. Implement business creation endpoint.
4. Implement business retrieval endpoints.
5. Implement workspace business listing.
6. Add request validation.
7. Add consistent API error responses.
8. Add controller integration tests.
9. Document API contracts and example requests.

Unauthenticated development endpoints must remain clearly identified as development-only. Production ownership and authorization enforcement require the identity foundation.

## 11. Milestone status

- M1 Slice 1A — Repository Bootstrap: COMPLETED
- BL-011 — Flyway Foundation: COMPLETED
- BL-012 — Generic Business Domain: COMPLETED
- BL-013 — Template Contract and Registry: COMPLETED
- BL-014.1 — Persistence Schema Foundation: VALIDATED
- BL-014.2 — JDBC Persistence and Application Service: VALIDATED
- BL-014.3 — Workspace and Business REST APIs: NEXT

---

**BusinessLab engineering principle**

The platform owns generic infrastructure and deterministic execution.

Business templates own their industry-specific definitions and behavior.

AI agents may propose and generate structured template definitions, but the platform remains responsible for validation, approval and execution safety.
