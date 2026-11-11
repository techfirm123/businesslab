# BusinessLab — BL-015.4: Business Operations Authorization Integration

**Milestone:** M1 — BusinessLab Platform Foundation  
**Slice:** BL-015.4  
**Status:** Implementation complete; local test validation passed  
**Branch:** `feature/m1-business-authorization`

---

## 1. Objective

Integrate workspace membership and role-based authorization into all business REST operations and application services.

The previous implementation established:

- User registration and authentication.
- Persistent authenticated sessions and CSRF protection.
- Workspace ownership and membership.
- Workspace roles and lifecycle states.
- Centralized workspace access and permission checks.

However, business operations still used unscoped application-service methods, and business REST endpoints were conditionally enabled through a development-only configuration flag.

This slice closes that gap and makes business operations membership-aware.

## 2. Functional outcome

All business REST operations now require an authenticated user with an appropriate active workspace membership.

### Authorization matrix

| Operation | OWNER | ADMIN | MEMBER | VIEWER |
|---|---|---|---|---|
| List workspace businesses | Allowed | Allowed | Allowed | Allowed |
| Read business by ID | Allowed | Allowed | Allowed | Allowed |
| Create business | Allowed | Allowed | Allowed | Denied |

### Membership enforcement

- Only active workspace memberships grant access.
- Suspended and removed memberships cannot access workspace businesses.
- Users without workspace membership cannot access business records.
- A user cannot access another workspace's businesses by supplying a known business UUID.
- VIEWER members retain read-only business access.
- VIEWER members receive HTTP 403 when attempting business creation.
- Users without active membership receive HTTP 404 to conceal workspace and business existence.

### Workspace lifecycle

- Business creation is restricted to active workspaces.
- Existing archived-workspace creation restrictions remain in place.
- Active members can continue reading businesses associated with an archived workspace.

## 3. Architecture changes

### 3.1 BusinessApplicationService

Business creation now requires:

- Requesting user ID.
- Workspace ID.
- Business template reference.
- Business name and description.
- Configuration values.

Before loading the workspace or creating the business, the service calls:

`WorkspaceAccessService.requireBusinessCreationAccess(...)`

This centralizes role and membership enforcement in the application layer.

The following unscoped operations were removed:

- `createCloudWorkspace(String)`
- `createWorkspaceWithBusiness(...)`
- The previous `createBusiness(...)` overload without a requesting user identity.

Workspace creation and initial owner membership remain the responsibility of `WorkspaceApplicationService`.

### 3.2 WorkspaceBusinessQueryService

Business read operations now require the requesting user's identity.

`getBusinesses(...)`:

1. Requires an active membership in the requested workspace.
2. Retrieves businesses belonging to that workspace.

`getBusiness(...)`:

1. Retrieves the business by its identifier.
2. Resolves its owning workspace.
3. Checks the requesting user's active workspace membership.
4. Returns the business only after authorization succeeds.

Unknown and inaccessible business identifiers receive the same resource-level not-found response.

Template discovery remains public and does not expose workspace-specific business data.

### 3.3 BusinessController

All three business endpoints now resolve the authenticated identity through `CurrentUserService`.

The controller passes the resolved `UserId` to the application services.

The controller does not implement its own membership or role rules. Those remain centralized in the application layer.

### 3.4 Development-only endpoint removal

The business controller no longer depends on the development-only conditional property.

The following configuration was removed:

`businesslab.api.development-endpoints-enabled`

Business endpoints are now registered normally and protected by the existing Spring Security configuration.

Authentication is required for business reads and writes. Existing CSRF protection remains in effect for POST operations.

## 4. API surface

The following endpoints are now authenticated and membership-aware.

| Method | Endpoint | Authorization |
|---|---|---|
| POST | `/api/v1/workspaces/{workspaceId}/businesses` | Active membership with business creation permission |
| GET | `/api/v1/workspaces/{workspaceId}/businesses` | Any active workspace membership |
| GET | `/api/v1/businesses/{businessId}` | Active membership in the business's owning workspace |

### Error behavior

| Scenario | HTTP status | Error code |
|---|---:|---|
| Anonymous request | 401 | `AUTHENTICATION_REQUIRED` |
| Unknown business | 404 | `RESOURCE_NOT_FOUND` |
| No active membership | 404 | `RESOURCE_NOT_FOUND` |
| Suspended membership | 404 | `RESOURCE_NOT_FOUND` |
| Removed membership | 404 | `RESOURCE_NOT_FOUND` |
| VIEWER attempts business creation | 403 | `WORKSPACE_PERMISSION_DENIED` |
| Invalid business configuration | 422 | `BUSINESS_CONFIGURATION_INVALID` |
| Creation in archived workspace | 409 | `RESOURCE_STATE_CONFLICT` |

## 5. Files changed

### Modified

- `apps/api/src/main/java/com/businesslab/business/application/BusinessApplicationService.java`
- `apps/api/src/main/java/com/businesslab/workspace/application/WorkspaceBusinessQueryService.java`
- `apps/api/src/main/java/com/businesslab/business/api/BusinessController.java`
- `apps/api/src/main/resources/application.yml`
- `apps/api/src/test/java/com/businesslab/business/api/BusinessApiIntegrationTest.java`
- `apps/api/src/test/java/com/businesslab/business/application/BusinessPersistenceIntegrationTest.java`
- `apps/api/src/test/java/com/businesslab/business/application/PostgresBusinessPersistenceIntegrationTest.java`
- `apps/api/src/test/java/com/businesslab/platform/api/DevelopmentEndpointGuardIntegrationTest.java`

### Removed

- `apps/api/src/main/java/com/businesslab/business/application/WorkspaceBusinessCreationResult.java`

### Existing shared components reused

No new authorization framework or database schema was introduced.

The implementation reuses:

- `CurrentUserService`
- `WorkspaceAccessService`
- `WorkspaceMembershipRepository`
- `WorkspaceMembership`
- `WorkspaceMembershipRole`
- `WorkspaceMembershipStatus`
- `ResourceNotFoundException`
- `WorkspacePermissionDeniedException`

## 6. Test coverage

The business API integration tests cover:

1. Workspace owner can create, retrieve and list businesses.
2. Active MEMBER can create and read businesses.
3. VIEWER can list and retrieve businesses.
4. VIEWER cannot create businesses.
5. A user without workspace membership cannot list businesses.
6. A user without workspace membership cannot retrieve a business through its UUID.
7. A user without workspace membership cannot create a business in that workspace.
8. Suspended memberships cannot access business operations.
9. Unknown business identifiers return not found.
10. Invalid template configuration continues to return structured validation errors.
11. Anonymous business reads and creation are rejected.
12. Existing business persistence tests use owned workspaces and authenticated user identities.

Test fixtures create memberships through the domain model and repository rather than bypassing the membership model with direct SQL inserts.

## 7. Database and migration impact

No Flyway migration was required.

The existing workspace membership schema already supports the required authorization model.

This slice uses the existing role and membership status definitions without introducing new database concepts.

## 8. Validation

Local validation was performed by the developer.

**Reported result: All tests passed.**

The implementation was validated against the existing backend test suite and the new business authorization integration tests.

## 9. Security considerations

- Business creation cannot be performed through an unscoped application-service operation.
- Business listing requires active membership.
- Direct business-ID retrieval requires membership in the owning workspace.
- Inaccessible resources are concealed through not-found responses.
- Role-based creation checks are centralized.
- Authentication remains enforced by Spring Security.
- CSRF protection remains enabled for state-changing requests.
- Development-only business endpoint gating is no longer needed.

## 10. Scope deliberately deferred

The following capabilities remain outside BL-015.4:

- Workspace member invitation and acceptance workflows.
- Member role-management APIs.
- Ownership transfer.
- Business update and deletion operations.
- Business lifecycle management APIs.
- Fine-grained permissions beyond the current workspace role model.

These can be implemented in subsequent feature slices.

## 11. Completion status

BL-015.4 closes the gap between workspace ownership and business operations.

Business data is now protected by authenticated identity, active workspace membership and role-based permissions at the application-service boundary.

**Milestone status: Implementation complete; test validation passed.**

Next: Commit, push and merge the feature branch through the standard BusinessLab PR workflow.
