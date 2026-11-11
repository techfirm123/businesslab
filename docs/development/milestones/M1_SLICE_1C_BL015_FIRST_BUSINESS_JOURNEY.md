# BusinessLab — BL-015.5: First Business Journey

**Milestone:** M1 — BusinessLab Platform Foundation  
**Slice:** BL-015.5  
**Status:** Implementation complete; local validation passed  
**Branch:** `feature/m1-first-business-journey`

---

## 1. Objective

Connect the authenticated BusinessLab application to the existing workspace and business APIs.

The objective was to move beyond the authenticated application shell and deliver the first functional user journey:

1. Create a workspace.
2. Select and manage the active workspace.
3. Discover available business templates.
4. Configure a business using template-defined fields.
5. Create a business through the secured backend API.
6. View the resulting business portfolio.

This slice establishes the first complete business creation experience without introducing simulation functionality prematurely.

## 2. Functional outcome

### 2.1 Workspace onboarding

New authenticated users without a workspace now receive a guided onboarding experience.

Users can:

- Create their first workspace.
- Provide a workspace name.
- See workspace creation errors.
- Retry failed requests without losing their entered name.
- Continue into the application after successful creation.

Workspace creation uses the existing authenticated backend endpoint.

### 2.2 Workspace selection

Users can belong to multiple workspaces.

The application now provides:

- A workspace selector in the application sidebar.
- Active workspace information.
- Membership role visibility.
- Workspace switching.
- Creation of additional workspaces from the overview.

The selected workspace ID is retained in localStorage as a UI preference.

Workspace data is always retrieved from the backend. The browser preference does not grant access or establish authorization.

### 2.3 Business discovery

The existing template catalog remains connected to:

`GET /api/v1/templates`

The Discover screen now enables business setup for active templates when the selected workspace permits business creation.

Creation actions are disabled when:

- There is no selected workspace.
- The user has a VIEWER role.
- The workspace is archived.
- The template is not active.

### 2.4 Dynamic business configuration

A new business setup page uses the selected template's configuration metadata to generate its form.

Supported configuration types:

- STRING
- INTEGER
- DECIMAL
- BOOLEAN

The generated fields use the template's metadata for required values, examples, minimums, maximums, maximum string lengths and patterns.

The form also captures:

- Business name.
- Business description.
- Exact template ID.
- Exact template version.

Numeric values are converted to appropriate JavaScript numbers before submission.

Empty optional fields are omitted from the configuration payload.

The backend remains authoritative for business configuration validation.

### 2.5 Business creation

Business creation is connected to:

`POST /api/v1/workspaces/{workspaceId}/businesses`

The frontend:

- Sends the authenticated session cookie.
- Retrieves a backend-issued CSRF token.
- Sends the CSRF token using the response-provided header name.
- Submits the selected template version and configuration.
- Handles structured backend validation errors.
- Navigates to the business portfolio after successful creation.

The backend's existing membership and role authorization remain authoritative.

### 2.6 My Businesses

A new route is available:

`/app/businesses`

It displays actual businesses retrieved from:

`GET /api/v1/workspaces/{workspaceId}/businesses`

The portfolio includes:

- Business name.
- Template ID and version.
- Business status.
- Description.
- Creation date.
- Business identifier.

The portfolio is scoped to the currently selected workspace.

VIEWER users can read businesses but do not receive business creation actions.

### 2.7 Application overview

The overview now uses real backend data instead of placeholder portfolio metrics.

It includes:

- Active workspace information.
- Workspace creation.
- Available active template count.
- Actual business count.
- Recent business preview.
- Business portfolio navigation.
- Workspace switching support.

Experiment information remains explicitly marked as upcoming functionality.

## 3. Architecture and implementation

### 3.1 API client

The shared API client now provides:

`apiPostWithCsrf(...)`

This helper:

1. Retrieves a fresh CSRF token from `/api/v1/auth/csrf`.
2. Uses the header name and token returned by the server.
3. Performs the requested POST operation.
4. Includes credentials through the existing API request mechanism.
5. Avoids storing the CSRF token in browser storage.

Existing authentication API behavior is preserved.

### 3.2 Workspace feature

A dedicated workspace frontend feature was introduced.

Components:

- `Workspace` and workspace role types.
- `workspaceApi`.
- `WorkspaceProvider`.
- `useWorkspaces`.

The provider loads the authenticated user's workspaces, resolves the preferred workspace, manages switching and supports workspace creation.

### 3.3 Business feature

A dedicated business frontend feature was introduced.

Components:

- Business response types.
- Business creation request type.
- `businessApi.list(...)`.
- `businessApi.create(...)`.

Business operations are separated from page components.

### 3.4 Application integration

The authenticated application layout now provides workspace state after session verification.

The application shell includes the active workspace selector and a functional My Businesses navigation destination.

### 3.5 Dynamic template-driven form

The business creation form reads template metadata from the backend instead of hardcoding Coffee Shop configuration fields.

This preserves BusinessLab's generic platform architecture.

New business types can introduce their own configuration fields through the template registry without requiring a dedicated frontend form for each business type.

### 3.6 React 19 lint corrections

The initial lint run identified React `set-state-in-effect` violations in several components.

The following changes were made:

- Workspace initial loading now performs state updates after asynchronous request completion.
- Portfolio loading is associated with the selected workspace and request key.
- Template discovery no longer synchronously resets loading state from its effect.
- Business setup uses Next.js `useSearchParams()` instead of copying URL parameters into state through an effect.
- Configuration drafts are derived from the selected template and updated through user interactions.
- The unused overview state was removed.

These changes preserve the intended behavior while conforming to the React lint rules.

## 4. API integration

The frontend now integrates with these existing backend endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/workspaces` | Load active workspaces |
| POST | `/api/v1/workspaces` | Create a workspace |
| GET | `/api/v1/templates` | Discover templates |
| POST | `/api/v1/workspaces/{workspaceId}/businesses` | Create a business |
| GET | `/api/v1/workspaces/{workspaceId}/businesses` | List workspace businesses |

No backend API contract changes were required.

## 5. Files introduced

```text
apps/web/src/features/workspaces/types.ts
apps/web/src/features/workspaces/workspace-api.ts
apps/web/src/features/workspaces/workspace-provider.tsx

apps/web/src/features/businesses/business-types.ts
apps/web/src/features/businesses/business-api.ts

apps/web/src/app/app/businesses/page.tsx
apps/web/src/app/app/create/page.tsx

apps/web/src/app/application-workspace.css
```

## 6. Files modified

```text
apps/web/src/lib/api/client.ts

apps/web/src/app/app/layout.tsx
apps/web/src/app/app/page.tsx
apps/web/src/app/app/discover/page.tsx

apps/web/src/app/layout.tsx

apps/web/src/components/application/application-shell.tsx
```

## 7. Technology and database impact

The existing technology stack is retained:

- Next.js 16.4.5
- React 19.3.1
- TypeScript 6.0.3
- Existing CSS-based design system
- Existing Spring Boot backend APIs

No new frontend dependencies were introduced.

No backend source changes were required.

No database migration was required.

No changes were made to the authentication or authorization architecture.

## 8. Validation

The initial lint run identified five React lint errors and one unused-state warning. These were corrected.

The developer subsequently confirmed that all requested validation passed.

| Validation | Reported result |
|---|---|
| `pnpm --filter @businesslab/web lint` | PASSED |
| `pnpm --filter @businesslab/web typecheck` | PASSED |
| `pnpm --filter @businesslab/web build` | PASSED |
| Manual first-business journey verification | PASSED as reported |

The implementation was also designed to preserve existing backend API contracts and role-based restrictions.

## 9. Security considerations

- Session identifiers remain in HttpOnly cookies.
- The browser does not use the selected workspace ID as an authorization mechanism.
- Workspace access is established by backend membership.
- Business creation retains backend authorization checks.
- POST operations use fresh CSRF tokens.
- Template configuration is validated again by the backend.
- VIEWER members cannot create businesses.
- Workspace isolation is enforced by the backend's membership-aware endpoints.

## 10. Known limitations

The following capabilities remain outside this slice:

1. Business detail and operational dashboard pages.
2. Business updates and deletion.
3. Business simulation execution.
4. Financial and operational metrics.
5. Experiment creation and comparison.
6. Business Flow Studio.
7. AI business advisory.
8. Workspace invitation and member-management UI.

The current business portfolio provides real business data, but business operation and simulation experiences are future milestones.

The active workspace preference is browser-local rather than synchronized across devices.

## 11. Next implementation milestone

The first functional business journey is now established.

The next major engineering foundation should be the **deterministic simulation engine**.

Proposed scope:

- Simulation domain and lifecycle.
- Simulation clock and controlled time progression.
- Versioned simulation state.
- Deterministic event processing.
- Business template simulation contracts.
- Reproducible execution.
- Simulation history and persistence.
- First executable Coffee Shop simulation.

AI advisory must remain separate from authoritative simulation calculations.

## 12. Completion status

BL-015.5 connects workspace management, template discovery, dynamic business configuration and business creation into one functional user journey.

BusinessLab now supports the first real progression from an authenticated account to a workspace containing a created virtual business.

**Milestone status: Implementation complete; local frontend validation passed.**
