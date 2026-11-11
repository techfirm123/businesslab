# BusinessLab — BL-015.1A: User Identity and Credential Foundation

**Status:** Implemented and locally validated  
**Feature branch:** `feature/m1-identity-access-foundation`  
**Milestone:** M1 — Identity, Access and Application Experience

---

## 1. Objective

Establish the initial trusted user identity foundation for BusinessLab.

This slice introduces:

- A dedicated user identity domain.
- Normalized email addresses.
- User account lifecycle status.
- Separate password credential persistence.
- Secure password hashing.
- User registration API.
- Database migrations for identity persistence.
- Automated registration and migration integration tests.

This is the first step towards authenticated BusinessLab workspaces and user-owned businesses.

## 2. Identity domain

A dedicated `identity` bounded context was introduced.

### User identity

`User` represents the trusted platform user.

Attributes:

- User ID (UUID).
- Normalized email address.
- Display name.
- Account status.
- Creation timestamp.
- Update timestamp.

### Email address

`EmailAddress` is a value object.

Responsibilities:

- Trim surrounding whitespace.
- Normalize email addresses to lowercase.
- Validate basic email format.
- Enforce a maximum length of 320 characters.

Email normalization ensures that differently cased representations of the same email resolve to one account identity.

### Account status

Initial supported statuses:

- ACTIVE
- SUSPENDED

Additional lifecycle states, including pending email verification, can be introduced when the verification workflow is implemented.

## 3. Credential architecture

User identity and authentication credentials are persisted separately.

### app_user

Stores:

- User ID.
- Email.
- Display name.
- Account status.
- Creation and update timestamps.

### user_password_credential

Stores:

- User ID.
- Encoded password hash.
- Credential creation and update timestamps.

The credential table has a one-to-one relationship with the user table and is deleted when the corresponding user is deleted.

Raw passwords are never persisted.

### Password hashing

Spring Security's `PasswordEncoder` abstraction is used.

The configured delegating password encoder stores an algorithm identifier alongside the encoded password, allowing future hash algorithm upgrades.

The initial policy requires:

- At least 12 Unicode characters.
- Maximum 72 UTF-8 bytes for the encoded input.
- Adaptive password hashing.
- No raw password exposure through API responses.

The current default encoder uses BCrypt.

## 4. Database changes

Existing V1 migrations were preserved.

Two new V2 migrations were introduced.

Production:

`apps/api/src/main/resources/db/migration/V2__create_user_identity_foundation.sql`

Test:

`apps/api/src/test/resources/db/test-migration/V2__create_user_identity_foundation.sql`

Both establish:

- `app_user`
- `user_password_credential`

Database constraints enforce email uniqueness, normalized email storage, valid account status, non-empty password hashes, timestamps and referential integrity.

The test schema uses H2-compatible SQL while preserving the relevant production schema constraints.

## 5. Application architecture

### Domain

- `identity/domain/UserId.java`
- `identity/domain/EmailAddress.java`
- `identity/domain/UserStatus.java`
- `identity/domain/User.java`

### Application

- `identity/application/UserRepository.java`
- `identity/application/PasswordCredentialRepository.java`
- `identity/application/UserRegistrationService.java`
- `identity/application/EmailAlreadyRegisteredException.java`

### Infrastructure

- `identity/infrastructure/security/PasswordEncoderConfiguration.java`
- `identity/infrastructure/persistence/JdbcUserRepository.java`
- `identity/infrastructure/persistence/JdbcPasswordCredentialRepository.java`

### API

- `identity/api/UserRegistrationController.java`
- `identity/api/dto/UserRegistrationRequest.java`
- `identity/api/dto/UserRegistrationResponse.java`

The registration service coordinates user creation, password encoding and atomic persistence of the account and its credential.

## 6. Registration API

Endpoint:

`POST /api/v1/auth/register`

Request fields:

- email
- displayName
- password

Successful response:

HTTP 201 Created.

The response includes:

- User ID.
- Normalized email.
- Display name.
- Account status.
- Creation timestamp.

The response never includes the password or encoded credential.

### Duplicate email

HTTP 409 Conflict.

Error code:

`EMAIL_ALREADY_REGISTERED`

Duplicate detection is case-insensitive because email addresses are normalized before lookup and persistence.

## 7. Error handling

The shared `ApiExceptionHandler` was extended to handle duplicate account registration.

Relevant mappings:

| Condition | HTTP status |
|---|---:|
| Invalid request | 400 |
| Invalid email or password | 400 |
| Duplicate email | 409 |
| Database integrity conflict | 409 |

The registration endpoint returns structured API errors consistent with the existing BusinessLab API response convention.

## 8. Dependencies

The backend now includes:

`org.springframework.security:spring-security-crypto`

This introduces password hashing primitives without enabling the complete HTTP security filter chain in this slice.

Full Spring Security authentication, session persistence, and CSRF protection remain part of the next slice.

## 9. Automated tests

Added:

`apps/api/src/test/java/com/businesslab/identity/api/UserRegistrationApiIntegrationTest.java`

Coverage includes:

- Successful user registration.
- Password hash persistence.
- Password hash verification.
- No password exposure in API responses.
- Email normalization.
- Duplicate email rejection.
- Minimum password length.
- Maximum UTF-8 password byte length.
- Invalid email rejection.
- No account creation for rejected registration requests.

Updated:

`apps/api/src/test/java/com/businesslab/platform/database/FlywayMigrationIntegrationTest.java`

The test now also verifies the presence of the two identity tables.

## 10. Local API smoke tests

The following operations were verified:

| Operation | Result |
|---|---|
| Register a new user | HTTP 201 |
| Register the same email again | HTTP 409 |
| Duplicate email structured error | Passed |

The registration response returned the generated UUID, normalized email, display name, ACTIVE status and timestamp.

The duplicate request returned `EMAIL_ALREADY_REGISTERED`.

## 11. Automated validation

The following commands completed successfully:

Targeted tests:

```bash
./gradlew :apps:api:test \
  --tests "com.businesslab.identity.api.UserRegistrationApiIntegrationTest" \
  --tests "com.businesslab.platform.database.FlywayMigrationIntegrationTest"
```

Full backend tests:

```bash
./gradlew :apps:api:clean :apps:api:test
```

Backend build:

```bash
./gradlew :apps:api:build
```

All validation passed in the final local run.

## 12. Security limitations

This slice establishes registration and credential persistence, not a complete authentication system.

The following capabilities remain pending:

- Login and logout.
- Server-side session persistence.
- Session fixation protection.
- CSRF protection.
- Authentication filters and protected endpoints.
-
