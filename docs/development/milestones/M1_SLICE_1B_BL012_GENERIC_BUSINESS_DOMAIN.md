# M1 — Slice 1B — BL-012: Generic Business Domain Model

**Project:** BusinessLab
**Milestone:** M1 — Generic Platform Foundation
**Slice:** 1B — Generic Business Foundation
**Task:** BL-012 — Generic Business Domain Model
**Status:** COMPLETED

## 1. Objective

Establish the generic business domain model that will underpin
all business types supported by BusinessLab.

The model must remain independent of individual industries
and business templates.

## 2. Workspace Domain

Implemented:

- WorkspaceId
- Workspace
- WorkspaceStorageMode
- WorkspaceStatus

### Workspace responsibilities

- Provides an organisational boundary for businesses.
- Defines the storage mode.
- Maintains workspace lifecycle.
- Supports workspace renaming.
- Supports archival.
- Supports LOCAL to CLOUD storage-mode promotion.

### Storage modes

LOCAL:
Business data is managed in browser storage.

CLOUD:
Business data is persisted by BusinessLab.

Cloud promotion is currently a domain transition.
The actual data migration process has not yet been implemented.

### Workspace lifecycle

ACTIVE
ARCHIVED

Archived workspaces cannot be modified.

## 3. Business Domain

Implemented:

- BusinessId
- Business
- BusinessStatus
- BusinessTemplateReference
- BusinessConfiguration

### Business responsibilities

- Maintains an independent business identity.
- Belongs to a Workspace.
- References a versioned Business Template.
- Maintains business metadata.
- Maintains generic business configuration.
- Enforces lifecycle transitions.
- Maintains creation and modification timestamps.

### Business lifecycle

DRAFT
ACTIVE
PAUSED
CLOSED
ARCHIVED

Supported transitions:

- DRAFT -> ACTIVE
- DRAFT -> ARCHIVED
- ACTIVE -> PAUSED
- ACTIVE -> CLOSED
- PAUSED -> ACTIVE
- PAUSED -> CLOSED
- PAUSED -> ARCHIVED
- CLOSED -> ARCHIVED

Invalid transitions are rejected.

Closed and archived businesses cannot be modified.

## 4. Business Template Reference

Each business references:

- Template ID
- Template version

Template identity is independent of the Business aggregate.

Example:

coffee-shop / 1.0.0

Template implementations and registration are deferred
to BL-013.

## 5. Business Configuration

Implemented an immutable generic configuration object.

Supported values:

- String
- Boolean
- Null
- Integer and decimal numbers
- Nested maps
- Lists

Nested collections are also protected against mutation.

Template-specific configuration validation will be introduced
with the Business Template contract.

## 6. Architecture Decisions

1. Workspace is the organisational and persistence boundary.
2. Businesses belong to workspaces.
3. Business identity is independent of business type.
4. Individual business behaviour belongs to template implementations.
5. Business domain objects are immutable.
6. Business configurations use generic JSON-compatible structures.
7. Template references preserve the exact template version.
8. Storage mode is represented at workspace level.
9. Domain objects do not depend on Spring Boot or database APIs.
10. Ownership and membership are deferred to identity implementation.

## 7. Files Created

### Workspace domain

- apps/api/src/main/java/com/businesslab/workspace/domain/WorkspaceId.java
- apps/api/src/main/java/com/businesslab/workspace/domain/WorkspaceStorageMode.java
- apps/api/src/main/java/com/businesslab/workspace/domain/WorkspaceStatus.java
- apps/api/src/main/java/com/businesslab/workspace/domain/Workspace.java

### Business domain

- apps/api/src/main/java/com/businesslab/business/domain/BusinessId.java
- apps/api/src/main/java/com/businesslab/business/domain/BusinessStatus.java
- apps/api/src/main/java/com/businesslab/business/domain/BusinessTemplateReference.java
- apps/api/src/main/java/com/businesslab/business/domain/BusinessConfiguration.java
- apps/api/src/main/java/com/businesslab/business/domain/Business.java

### Tests

- apps/api/src/test/java/com/businesslab/workspace/domain/WorkspaceTest.java
- apps/api/src/test/java/com/businesslab/business/domain/BusinessTest.java
- apps/api/src/test/java/com/businesslab/business/domain/BusinessConfigurationTest.java

## 8. Validation Results

Developer-confirmed results:

- Workspace domain tests: PASSED
- Business lifecycle tests: PASSED
- Business configuration tests: PASSED
- Backend test task: PASSED
- Backend build: PASSED

No persistence or API integration was introduced in this task.

## 9. Known Limitations

Not yet implemented:

- Business Template contract
- Template Registry
- Template-specific configuration validation
- Workspace persistence
- Business persistence
- Workspace ownership and membership
- Business creation API
- Business retrieval API
- Optimistic locking
- Cloud migration execution

These are assigned to subsequent implementation tasks.

## 10. Next Implementation Task

BL-013 — Business Template Contract and Registry.

Objectives:

1. Define the BusinessTemplate interface.
2. Define template metadata.
3. Define template versioning.
4. Define supported business capabilities.
5. Define template configuration validation.
6. Implement the Template Registry.
7. Register the first Coffee Shop template.
8. Add automated registry and validation tests.

## 11. Development Position

M1 Slice 1A — Repository Bootstrap: COMPLETED

BL-011 — Flyway Foundation: COMPLETED

BL-012 — Generic Business Domain Model: COMPLETED

BL-013 — Business Template Contract and Registry: NEXT

---

BusinessLab engineering principle:

The generic Business domain must remain independent of
industry-specific implementations.
