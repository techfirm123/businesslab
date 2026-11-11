# M1 — BL-015.2: BusinessLab Application UI Foundation

**Status:** Implementation complete; local frontend validation passed  
**Branch:** `feature/m1-businesslab-application-ui`  
**Milestone:** M1 — BusinessLab Platform Foundation

---

## 1. Objective

Establish the first functional BusinessLab frontend application, transforming the existing public landing page into an entry point for an authenticated business experimentation platform.

The slice delivers:

- BusinessLab-specific registration and login experiences.
- Integration with the existing Spring Boot authentication APIs.
- Cookie-based session awareness.
- CSRF-protected state-changing requests.
- A protected application shell and navigation.
- A personalized application overview.
- Live discovery of available business templates.

The existing public landing page is retained and enhanced with authentication entry points.

## 2. Architecture and implementation

### 2.1 Frontend technology

The implementation uses the existing frontend stack without introducing additional dependencies:

- Next.js 16.4.5
- React 19.3.1
- TypeScript 6.0.3
- Existing CSS-based design system

No additional frontend packages were required.

### 2.2 Application routes

| Route | Purpose | Access |
|---|---|---|
| `/` | Public BusinessLab landing page | Public |
| `/register` | Account registration | Public |
| `/login` | Account login | Public |
| `/app` | Personalized application overview | Authenticated |
| `/app/discover` | Business template catalog | Authenticated |

The authenticated application uses a dedicated layout and navigation system.

### 2.3 Authentication integration

The frontend integrates with the existing Spring Boot authentication endpoints:

| Endpoint | Method | Purpose |
|---|---|---|
| `/api/v1/auth/csrf` | GET | Obtain the current CSRF token |
| `/api/v1/auth/register` | POST | Register a user |
| `/api/v1/auth/login` | POST | Authenticate a user |
| `/api/v1/auth/me` | GET | Retrieve the authenticated user |
| `/api/v1/auth/logout` | POST | End the authenticated session |
| `/api/v1/templates` | GET | Retrieve available business templates |

All API calls use `credentials: "include"` so the browser sends the session cookie.

The API client also provides:

- JSON request and response handling.
- Structured API error handling.
- Network failure handling.
- HTTP 204 response handling.
- Disabled caching for authenticated API requests.

### 2.4 CSRF handling

State-changing requests retrieve a fresh CSRF token from the backend before submission.

The token is sent using the header name provided by the backend, currently `X-CSRF-TOKEN`.

A fresh token is obtained for registration, login and logout. This accounts for token invalidation associated with session authentication and logout.

The frontend does not store the authenticated session identifier or CSRF token in localStorage or sessionStorage.

The server-managed session remains in the HttpOnly `BUSINESSLAB_SESSION` cookie.

### 2.5 Authentication state

A reusable React authentication provider manages:

- Current authenticated user.
- Session verification.
- Loading, authenticated, unauthenticated and error states.
- Session refresh.
- Logout.

The authenticated application verifies the user through `/api/v1/auth/me` before rendering its content.

The frontend route guard is a user-experience mechanism. Backend authorization remains the actual security boundary.

### 2.6 Application design system

A separate application stylesheet establishes BusinessLab's authenticated product identity.

Design characteristics:

- Forest-green and natural-green palette.
- Warm neutral application background.
- Restrained borders and shadows.
- Custom navigation and account components.
- Business-oriented visual illustrations.
- Responsive layouts for desktop and mobile.
- Accessible form labels and interaction states.
- Reduced-motion support.

The public landing-page stylesheet remains separate from the authenticated application design system.

## 3. Main UI capabilities

### 3.1 Registration

The registration experience includes:

- Display name.
- Email address.
- Password.
- Password confirmation.
- Password visibility control.
- Client-side password confirmation validation.
- Backend error display.
- Registration success feedback on the login screen.

The form connects to the existing backend registration API. Password security validation remains enforced by the backend.

### 3.2 Login

The login experience includes:

- Email and password inputs.
- Password visibility control.
- Submission loading state.
- Backend authentication error feedback.
- Navigation to the authenticated application after successful login.
- Registration completion feedback.

### 3.3 Application shell

The authenticated shell includes:

- BusinessLab brand identity.
- Workspace navigation area.
- Overview and Discover navigation.
- Upcoming capability indicators.
- User identity display.
- Account menu.
- Logout action.
- Session indicator.

My Businesses, Experiment Lab and Flow Studio are presented as upcoming capabilities rather than falsely functional destinations.

### 3.4 Personalized overview

The overview presents:

- Personalized welcome message.
- BusinessLab introductory visual.
- Available template count from the backend.
- Business portfolio empty state.
- Experimentation introduction.
- Discover entry point.

No fabricated revenue, business counts, operational metrics or financial results are displayed.

### 3.5 Template discovery

The Discover screen retrieves the actual catalog from:

`GET /api/v1/templates`

It displays available template information, including:

- Template name.
- Version.
- Category.
- Description.
- Tags.
- Configuration fields.
- Capability count.
- Template status.

The setup action remains disabled until the authenticated workspace and business creation foundations are implemented.

## 4. Environment configuration

New example configuration:

`apps/web/.env.example`

```dotenv
NEXT_PUBLIC_BUSINESSLAB_API_URL=http://localhost:8080
```

For local development, create `apps/web/.env.local` using the same value.

The environment variable allows the frontend to target a different backend without changing application source code.

The existing backend CORS configuration allows `http://localhost:3000` by default.

## 5. Files introduced or modified

### New files

```text
apps/web/.env.example

apps/web/src/app/app/discover/page.tsx
apps/web/src/app/app/layout.tsx
apps/web/src/app/app/page.tsx

apps/web/src/app/application.css
apps/web/src/app/login/page.tsx
apps/web/src/app/register/page.tsx

apps/web/src/components/application/app-icon.tsx
apps/web/src/components/application/application-shell.tsx

apps/web/src/features/auth/auth-api.ts
apps/web/src/features/auth/auth-provider.tsx
apps/web/src/features/auth/auth-shell.tsx
apps/web/src/features/auth/login-form.tsx
apps/web/src/features/auth/register-form.tsx
apps/web/src/features/auth/types.ts

apps/web/src/lib/api/client.ts

apps/web/src/lib/templates/template-api.ts
apps/web/src/lib/templates/template-types.ts
```

### Modified files

```text
apps/web/src/app/layout.tsx
apps/web/src/app/page.tsx
```

No backend implementation or database migrations were required in this slice.

No new frontend dependencies were introduced.

## 6. Validation

The following checks were executed locally and reported as passed:

| Command | Result |
|---|---|
| `pnpm --filter @businesslab/web lint` | PASSED |
| `pnpm --filter @businesslab/web typecheck` | PASSED |
| `pnpm --filter @businesslab/web build` | PASSED |

Lint corrections included:

- Escaping unescaped apostrophes in JSX.
- Replacing effect-based registration query initialization with the Next.js async `searchParams` page API.

These results establish successful frontend static checks, type validation and production compilation.

They should not be interpreted as a substitute for runtime authentication and browser integration testing.

## 7. Security considerations

The implementation preserves the existing backend security architecture:

- Session identifiers remain in HttpOnly cookies.
- The frontend does not read or persist session identifiers.
- State-changing requests use backend-issued CSRF tokens.
- API requests include credentials.
- Authenticated routes verify the session with the backend.
- Backend authentication and authorization remain authoritative.

Production deployment must ensure the frontend/backend origin, CORS, cookie `SameSite`, `Secure` and HTTPS configuration are aligned. A same-origin reverse-proxy deployment is a straightforward option.

## 8. Known limitations

The following capabilities are intentionally not implemented in this slice:

1. Workspace creation and workspace selection.
2. Workspace ownership and membership authorization.
3. Business creation and configuration.
4. Persistent business portfolio listing.
5. Experiment execution and scenario comparison.
6. Business Flow Studio.
7. AI advisory and business intelligence experiences.
8. Email verification, password recovery and registration abuse protection.

Template discovery is connected, but business setup is not yet enabled.

These limitations are part of the planned incremental product development rather than incomplete implementations of this slice.

## 9. Next implementation slice

### BL-015.3 — Workspace Ownership and Membership Foundation

The next backend slice should establish the relationship between authenticated users and workspaces.

It should cover:

- Workspace ownership and membership.
- User-to-workspace authorization.
- Workspace creation by authenticated users.
- Secure workspace access.
- Protection against cross-user workspace access.
- Membership lifecycle and role foundations.

Once this is complete, the frontend can connect to real workspace and business operations.

## 10. Milestone outcome

BL-015.2 establishes the first actual authenticated BusinessLab application experience.

The product now has a public entry point, user registration, login, session-aware navigation, a personalized overview and live template discovery.

The implementation preserves the existing backend security foundation and avoids introducing mock business data or premature business operations.

**Milestone outcome:** BusinessLab's application UI foundation is implemented and local frontend validation has passed.
