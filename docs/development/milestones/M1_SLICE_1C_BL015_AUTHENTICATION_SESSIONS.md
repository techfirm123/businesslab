# BusinessLab — BL-015.1B: Authentication and Session Security

**Status:** Implemented and locally validated  
**Milestone:** M1 — Identity, Access and Application Experience  
**Feature branch:** `feature/m1-authentication-session-security`  
**Base branch:** `main`

---

## 1. Objective

Implement the first complete authentication and session security layer for BusinessLab.

This slice builds upon BL-015.1A and introduces:

- Spring Security integration.
- Email/password authentication.
- Login and logout APIs.
- Current-user identity endpoint.
- PostgreSQL-backed HTTP sessions.
- Session ID rotation.
- CSRF protection.
- Credentialed CORS with explicit allowed origins.
- Protected workspace and business APIs.
- Structured JSON security error responses.
- Authentication and session integration tests.

The implementation uses server-side sessions rather than browser-stored bearer tokens.

## 2. Authentication architecture

### Authentication mechanism

Email and password authentication uses:

- Existing `UserRepository`.
- Existing `PasswordCredentialRepository`.
- `BusinessLabUserDetailsService`.
- Spring Security `DaoAuthenticationProvider`.
- Existing `PasswordEncoder` configuration.

The user identity and credential data remain separate.

Only active user accounts can authenticate. Suspended accounts are rejected.

### Authentication flow

1. Browser obtains a CSRF token.
2. Browser submits email and password with the CSRF token.
3. Spring Security authenticates the credentials.
4. The session authentication strategy rotates the session identifier.
5. The previous CSRF token is invalidated.
6. The new security context is explicitly saved in the session.
7. The authenticated session is persisted through Spring Session JDBC.
8. Subsequent requests authenticate through the server-side session cookie.

This explicit security-context persistence is important because login is implemented as a JSON REST endpoint rather than Spring Security's standard form-login flow.

## 3. Authentication endpoints

Base path: `/api/v1/auth`

| Method | Endpoint | Purpose | Expected status |
|---|---|---|---|
| POST | `/register` | Register a new user | 201 |
| POST | `/login` | Authenticate a user | 200 |
| POST | `/logout` | Invalidate authenticated session | 204 |
| GET | `/me` | Retrieve current authenticated identity | 200 |
| GET | `/csrf` | Obtain CSRF token | 200 |

### Login

Accepts:

- Email address.
- Password.

Successful response returns the authenticated user's public profile.

Invalid credentials return HTTP 401 with error code:

`INVALID_CREDENTIALS`

### Current user

`GET /api/v1/auth/me`

Requires an authenticated session.

Returns the authenticated user's:

- User ID.
- Normalized email.
- Display name.
- Account status.
- Creation timestamp.

Anonymous requests return HTTP 401 with:

`AUTHENTICATION_REQUIRED`

### Logout

`POST /api/v1/auth/logout`

Requires an authenticated session and valid CSRF token.

Logout:

- Clears authentication.
- Invalidates the HTTP session.
- Deletes the configured session cookie.
- Prevents further use of the invalidated session.

Successful logout returns HTTP 204.

## 4. Session architecture

Spring Session JDBC is used for persistent server-side HTTP sessions.

### Session storage

The session is stored in PostgreSQL using two tables:

- `SPRING_SESSION`
- `SPRING_SESSION_ATTRIBUTES`

The first contains session metadata. The second stores serialized session attributes, including the Spring Security context.

### Session policy

- Session lifetime: 12 hours.
- Session identifier rotation on authentication.
- Server-side session persistence.
- Cookie-only session tracking.
- Session invalidation on logout.

### Session cookie

Name:

`BUSINESSLAB_SESSION`

Configured properties:

- HttpOnly: true.
- SameSite: Lax.
- Path: `/`.
- Secure: controlled using `BUSINESSLAB_SESSION_COOKIE_SECURE`.

The local HTTP profile uses `Secure=false`. HTTPS deployments must explicitly enable the Secure attribute.

## 5. CSRF protection

CSRF protection is enabled.

The implementation uses:

- `HttpSessionCsrfTokenRepository`.
- `XorCsrfTokenRequestAttributeHandler`.
- `X-CSRF-TOKEN` request header.

The `/api/v1/auth/csrf` endpoint supplies the token and header metadata.

The browser must:

1. Obtain the initial CSRF token.
2. Include it in registration, login and other state-changing requests.
3. Obtain a fresh token after authentication.
4. Obtain another fresh token after logout when continuing to use the same browser session.

Login and logout invalidate the previous CSRF token.

Requests containing a missing or invalid token receive HTTP 403 with error code:

`CSRF_VALIDATION_FAILED`

## 6. Authentication and authorization boundary

The Spring Security filter chain protects the API.

Public endpoints:

- Registration.
- Login.
- CSRF token retrieval.
- Template discovery.
- System status.
- Approved actuator health/info endpoints.

All other endpoints require authentication.

Workspace and business controllers retain their additional development-only registration guard:

`businesslab.api.development-endpoints-enabled`

The development flag does not bypass authentication.

Workspace ownership and membership authorization are not yet implemented. Authenticated users must not be treated as authorized to access arbitrary workspace IDs.

## 7. CORS

Credentialed CORS is configured with explicit origins.

Local default:

`http://localhost:3000`

Configuration environment variable:

`BUSINESSLAB_CORS_ALLOWED_ORIGINS`

The implementation:

- Allows credentials.
- Rejects wildcard origins.
- Configures explicit allowed methods.
- Allows the CSRF request header.
- Exposes the Location response header.
- Restricts configuration to API paths.

Production origins must be explicitly configured before deployment.

## 8. Main implementation components

### Identity infrastructure

- `identity/infrastructure/security/BusinessLabUserDetailsService.java`
- `identity/infrastructure/security/SecurityConfiguration.java`
- `identity/infrastructure/security/ApiSecurityErrorHandler.java`
- `identity/infrastructure/security/SessionCookieConfiguration.java`

### Authentication API

- `identity/api/AuthenticationController.java`
- `identity/api/CsrfController.java`
- `identity/api/dto/LoginRequest.java`
- `identity/api/dto/CsrfTokenResponse.java`

### Application exception

- `identity/application/InvalidCredentialsException.java`

### Shared error handling

- Updated `platform/shared/api/ApiExceptionHandler.java`

### Database migrations

Production:

`apps/api/src/main/resources/db/migration/V3__create_persistent_http_sessions.sql`

Test:

`apps/api/src/test/resources/db/test-migration/V3__create_persistent_http_sessions.sql`

### Test coverage

- `identity/api/AuthenticationApiIntegrationTest.java`
- Updated `identity/api/UserRegistrationApiIntegrationTest.java`
- Updated `business/api/BusinessApiIntegrationTest.java`
- Updated `platform/api/DevelopmentEndpointGuardIntegrationTest.java`
- Updated `platform/database/FlywayMigrationIntegrationTest.java`

## 9. Automated validation

The following commands completed successfully:

Focused authentication and registration tests:

```bash
./gradlew :apps:api:test \
  --tests "com.businesslab.identity.api.AuthenticationApiIntegrationTest" \
  --tests "com.businesslab.identity.api.UserRegistrationApiIntegrationTest"
```

Complete backend regression suite:

```bash
./gradlew :apps:api:clean :apps:api:test
```

Backend build:

```bash
./gradlew :apps:api:build
```

All validation passed in the final local run.

## 10. Manual API validation

The local API was tested using the previously registered PostgreSQL user.

Verified operations:

| Operation | HTTP result |
|---|---:|
| Obtain CSRF token | 200 |
| Login | 200 |
| Retrieve current identity | 200 |
| Logout | 204 |
| Retrieve identity after logout | 401 |

Additional verified properties:

- Session cookie was issued.
- Session cookie was HttpOnly.
- Authenticated user profile was returned.
- Current identity endpoint used the authenticated session.
- Logout invalidated the old session.
- The old cookie could not authenticate a subsequent request.
- Structured authentication error response was returned.

The HTTP response confirmed the cookie name `BUSINESSLAB_SESSION` and `SameSite=Lax`.

## 11. Architectural decisions

1. Server-side JDBC sessions are used instead of browser-stored JWTs.
2. Password verification remains delegated to Spring Security.
3. The security context is explicitly persisted after JSON login.
4. Session ID rotation is performed during authentication.
5. CSRF protection remains enabled for state-changing endpoints.
6. Session cookies are HttpOnly and cookie-only.
7. CORS uses exact configured origins and credentialed requests.
8. Authentication is separate from workspace authorization.
9. Workspace/business endpoints require authentication and retain the development-only feature guard.

## 12. Known limitations

The following items are not yet implemented:

- Workspace ownership enforcement.
- Workspace membership and roles.
- Fine-grained authorization.
- Email verification.
- Password recovery and reset.
- Login and registration rate limiting.
- Abuse protection and CAPTCHA.
- Account lockout and suspicious-login detection.
- Multi-factor authentication.
- Device/session management and remote session revocation.
- Production origin and HTTPS deployment configuration.
- Frontend authentication integration.
- Public registration abuse controls.

These remain necessary before public production exposure.

## 13. Outcome

BL-015.1B completes the first server-side authentication lifecycle for BusinessLab.

Users can authenticate with their registered credentials, obtain a persistent server-side session, retrieve their current identity and securely log out.

CSRF protection, session rotation, structured security errors and authenticated API boundaries are now in place.

**BL-015.1B is implemented and locally validated.**

## 14. Next implementation slice

Proceed with:

**BL-015.2 — BusinessLab Application UI Foundation**

Build the BusinessLab-specific frontend experience:

- Registration screen.
- Login screen.
- CSRF-aware API client.
- Session-aware authentication state.
- Authenticated application shell.
- Navigation and account menu.
- Responsive BusinessLab design system.
- Initial overview dashboard.

Follow with workspace ownership and membership authorization before connecting the full workspace and business creation flow.

---

**Document:** `M1_SLICE_1C_BL015_AUTHENTICATION_SESSIONS.md`  
**Milestone:** M1 — Identity, Access and Application Experience  
**Implementation:** BL-015.1B  
**Validation:** Passed locally
