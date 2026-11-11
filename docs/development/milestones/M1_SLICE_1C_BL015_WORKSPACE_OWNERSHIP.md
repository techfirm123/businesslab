# M1 — BL-015.3: Workspace Ownership & Membership Foundation

**Status:** Implementation complete; local validation passed  
**Branch:** `feature/m1-businesslab-workspace-ownership`  
**Milestone:** M1 — BusinessLab Platform Foundation

---

## 1. Objective

Implement the initial workspace ownership and membership foundation for BusinessLab.

The primary objective is to establish a reliable relationship between authenticated users and workspaces, ensuring that workspace access is governed by persisted membership rather than possession of a workspace UUID.

This slice establishes:

- Workspace membership domain model.
- Workspace roles and membership lifecycle.
- Atomic workspace and owner-membership creation.
- JDBC membership persistence.
- Authenticated workspace creation.
- User-scoped workspace listing.
- Membership-protected workspace retrieval.
- Cross-user access prevention.
- Structured authorization errors.
- Database migration and integration tests.

## 2. Architectural decisions

### 2.1 Membership as the authorization boundary

A workspace is no longer considered accessible merely because its identifier is known.

A user must have an active membership associated with the requested workspace.

The application layer performs the membership check before returning workspace details.

Inaccessible workspaces are reported as not found, avoiding disclosure of another user's workspace existence.

### 2.2 Identity integration

The existing identity foundation uses UUID-backed `UserId` values and normalized email addresses.

A new `CurrentUserService` resolves the authenticated Spring Security principal to an active BusinessLab user.

Controllers pass the resolved `UserId` to application services.

The domain and application layers do not depend directly on Spring Security authentication objects.

### 2.3 Membership roles

The following roles are established:

| Role | Workspace access | Business creation | Membership management |
|---|---|---|---|
| OWNER | Full | Yes | Yes |
| ADMIN | Yes | Yes | Yes |
| MEMBER | Yes | Yes | No |
| VIEWER | Read-only | No | No |

The role permission methods are established in the domain model.

### 2.4 Membership lifecycle

Membership statuses:

- ACTIVE
- SUSPENDED
- REMOVED

The domain supports role changes, suspension, reactivation and removal for non-owner memberships.

Owner membership cannot be suspended, removed or demoted through ordinary member lifecycle operations.

Ownership transfer requires a dedicated operation and is intentionally deferred.

### 2.5 Workspace creation

Workspace creation is handled by `WorkspaceApplicationService`.

The operation creates:

1. A cloud workspace.
2. An active OWNER membership for the authenticated user.

Both records are persisted within one database transaction.

Failure to create either record causes the transaction to roll back.

### 2.6 Existing workspace records

The V4 migration does not automatically assign owners to workspaces created before membership support.

Existing ownership cannot safely be inferred from the previous schema.

Such workspaces remain inaccessible through membership-scoped endpoints until an explicit recovery or assignment process is implemented.

---

## 3. Domain implementation

### Workspace membership

New domain types:

- `WorkspaceMembership`
- `WorkspaceMembershipId`
- `WorkspaceMembershipRole`
- `WorkspaceMembershipStatus`

`WorkspaceMembership` is an immutable domain entity with:

- Membership ID.
- Workspace ID.
- User ID.
- Role.
- Lifecycle status.
- Creation timestamp.
- Update timestamp.

Domain operations return updated immutable instances.

### Identity resolution

`CurrentUserService` resolves the currently authenticated principal using the existing `UserRepository`.

It requires the account to be ACTIVE before returning the user.

The authentication `/me` endpoint also uses this service, preventing a suspended account from being treated as an active application identity through an existing session.

### Workspace authorization

`WorkspaceAccessService` centralizes membership checks:

- `requireActiveMembership`
- `requireBusinessCreationAccess`
- `requireMemberManagementAccess`

These operations establish the reusable authorization boundary for upcoming workspace and business operations.

---

## 4. Persistence implementation

### Membership repository

New `WorkspaceMembershipRepository` port supports:

- Save membership.
- Update membership.
- Find membership by workspace and user.
- Find active memberships for a user.

New JDBC implementation:

`JdbcWorkspaceMembershipRepository`

The repository persists and reconstitutes membership entities.

### Workspace repository

The existing `WorkspaceRepository` now supports:

`findAllForUser(UserId userId)`

The JDBC implementation retrieves workspaces by joining `workspace` and `workspace_membership`, filtering to active memberships.

This ensures workspace listings are user-scoped at the persistence layer.

---

## 5. Database migration

### Production migration

`V4__create_workspace_membership.sql`

Creates:

`workspace_membership`

Columns:

- `membership_id`
- `workspace_id`
- `user_id`
- `role`
- `status`
- `created_at`
- `updated_at`

Database constraints include:

- Primary key on membership ID.
- Unique `(workspace_id, user_id)` pair.
- Foreign key to workspace.
- Foreign key to application user.
- Role check constraint.
- Status check constraint.
- Timestamp-order constraint.
- Partial unique index allowing at most one OWNER membership per workspace.

Indexes support user-oriented and workspace-oriented membership queries.

The partial unique index is a PostgreSQL-specific production constraint.

### H2 migration

`V4__create_workspace_membership.sql`

A structurally equivalent H2 test migration was added.

The H2 migration omits the PostgreSQL-specific partial unique index. PostgreSQL migration startup verifies that the production schema accepts the database-specific index.

### Migration documentation

`apps/api/src/main/resources/db/migration/README.md` was updated with the V4 migration details and existing-workspace ownership considerations.

Previously applied migrations V1–V3 were not modified.

---

## 6. Application and API implementation

### Workspace application service

New `WorkspaceApplicationService` supports:

- Create cloud workspace with initial owner membership.
- List the authenticated user's active workspaces.
- Retrieve an individual workspace after membership verification.

### Authenticated workspace API

Workspace endpoints are now available without the previous development-only controller flag.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/v1/workspaces` | Create a workspace |
| GET | `/api/v1/workspaces` | List active workspaces |
| GET | `/api/v1/workspaces/{workspaceId}` | Retrieve an accessible workspace |

Workspace creation requires the authenticated user and a valid CSRF token.

The response now includes:

- Workspace ID.
- Name.
- Storage mode.
- Workspace status.
- Membership role.
- Membership status.
- Creation timestamp.
- Update timestamp.

The API does not accept an owner user ID from the request body.

### Authorization error handling

A new `WorkspacePermissionDeniedException` is introduced.

`ApiExceptionHandler` maps it to:

- HTTP 403
- Error code `WORKSPACE_PERMISSION_DENIED`

Inaccessible workspaces use the existing structured resource-not-found response.

---

## 7. Security considerations

The implementation preserves the existing Spring Security foundation.

- Session authentication remains cookie-based.
- Existing HttpOnly session cookie behavior is unchanged.
- CSRF protection remains enabled.
- Workspace creation requires authentication and CSRF validation.
- Authenticated identities are resolved through the active user record.
- Workspace retrieval requires active membership.
- Workspace listings are restricted to active memberships.
- Another user cannot access a workspace solely by supplying its UUID.

The legacy business REST endpoints remain development-only and disabled by default.

They must not be exposed in production until membership authorization is connected to business creation, listing and retrieval operations.

The `WorkspaceAccessService` provides reusable authorization methods for that subsequent integration.

---

## 8. Tests introduced or updated

### New domain tests

`WorkspaceMembershipTest`

Covers:

- Owner membership creation.
- Member membership creation.
- Owner factory restrictions.
- Viewer read-only permissions.
- Role changes.
- Ownership transfer restriction.
- Suspension and reactivation.
- Removal and reactivation.
- Owner lifecycle restrictions.
- Timestamp validation.

### New API integration tests

`WorkspaceOwnershipApiIntegrationTest`

Uses the actual registration, login, persistent session and CSRF flow.

Covers:

- Anonymous workspace listing rejection.
- Authenticated workspace creation.
- Initial OWNER membership persistence.
- Authenticated workspace listing.
- Owner workspace retrieval.
- Cross-user workspace access prevention.
- Cross-user listing isolation.
- Suspended membership access rejection.
- Anonymous workspace creation rejection.

### Updated tests

- `FlywayMigrationIntegrationTest`
    - Verifies the workspace membership table is created.

- `DevelopmentEndpointGuardIntegrationTest`
    - Verifies the production workspace API is registered and requires authentication.
    - Verifies legacy business development endpoints remain disabled.

- `BusinessApiIntegrationTest`
    - Retains regression coverage for the legacy development business endpoints.

---

## 9. Configuration changes

`application.yml` was updated to clarify that:

`BUSINESSLAB_DEV_API_ENABLED`

controls legacy development business endpoints.

The new workspace API is registered independently and relies on the existing authentication and membership protections.

No additional application dependencies were introduced.

---

## 10. Validation results

The user reported that all local validation passed.

| Validation | Result |
|---|---|
| `./gradlew :apps:api:clean :apps:api:test` | PASSED |
| `./gradlew :apps:api:build` | PASSED |
| PostgreSQL application startup and V4 migration | PASSED |

The test suite includes the new domain, membership, authorization and migration coverage.

The PostgreSQL startup verifies the production Flyway migration, including the database-specific partial unique index.

---

## 11. Known limitations

The following are intentionally deferred:

1. Workspace membership invitation and acceptance.
2. Membership management REST APIs.
3. Dedicated ownership transfer workflow.
4. Workspace renaming and archival APIs.
5. Recovery or assignment of pre-existing workspaces without memberships.
6. Business creation authorization integration.
7. Business listing and retrieval authorization integration.
8. Workspace selection and management in the frontend.

Role-based business creation permissions are represented in the domain and centralized access service, but the legacy business REST controller has not yet been connected to this service.

It remains disabled by default.

---

## 12. Next implementation milestone

### BL-015.4 — Business Operations Authorization Integration

The next slice should connect the existing business domain and REST APIs to workspace membership authorization.

Expected scope:

- Business creation requires active workspace membership and appropriate role.
- Business listing requires active workspace membership.
- Business detail retrieval verifies access through its owning workspace.
- Direct business UUID access cannot bypass workspace authorization.
- Archived workspace restrictions are respected.
- Legacy development-only controller gating can be reconsidered after complete authorization coverage.
- Integration tests cover cross-user business access and VIEWER restrictions.

After backend authorization is complete, the frontend can safely connect to workspace selection and actual business creation.

---

## 13. Milestone outcome

BL-015.3 establishes the initial multi-user workspace ownership and membership foundation.

BusinessLab now has a persisted relationship between users and workspaces, atomic initial owner creation, membership-scoped workspace retrieval and cross-user access prevention.

The architecture is prepared for future membership management and business-level authorization.

**Milestone outcome:** Workspace ownership and membership foundation implemented and local validation passed.
