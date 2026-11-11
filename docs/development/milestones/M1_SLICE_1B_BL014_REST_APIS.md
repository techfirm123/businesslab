# BusinessLab — M1 Slice 1B: BL-014.3 REST API Layer

**Status:** Implemented and locally validated  
**Milestone:** M1 — Generic Business Foundation  
**Feature branch:** `feature/m1-generic-business-foundation`

---

## 1. Objective

Implement the initial REST API layer for BusinessLab's generic business foundation.

The API enables clients to:

- Discover available business templates.
- Create and retrieve cloud workspaces.
- Create businesses from registered templates.
- Retrieve individual businesses.
- List businesses belonging to a workspace.

The implementation exposes the existing domain, application services, and persistence capabilities through HTTP without introducing industry-specific assumptions into the platform API.

Authentication and workspace authorization are not yet implemented. Consequently, workspace and business endpoints are protected by a development-only feature flag and remain disabled by default.

## 2. API endpoints

Base path: `/api/v1`

| Method | Endpoint | Purpose | Expected status |
|---|---|---|---|
| GET | `/templates` | Discover available business templates | 200 |
| POST | `/workspaces` | Create a cloud workspace | 201 |
| GET | `/workspaces/{workspaceId}` | Retrieve a workspace | 200 |
| POST | `/workspaces/{workspaceId}/businesses` | Create a business | 201 |
| GET | `/workspaces/{workspaceId}/businesses` | List workspace businesses | 200 |
| GET | `/businesses/{businessId}` | Retrieve a business | 200 |

### Template discovery

Returns the registered code-backed template descriptors, including:

- Template identity and version.
- Source type.
- Name and description.
- Industry category.
- Lifecycle status.
- Capabilities.
- Tags.
- Configuration field definitions and validation metadata.

The current template registry exposes the Coffee Shop template as its initial platform implementation.

### Workspace creation

Creates a cloud-backed workspace.

The response includes:

- Workspace UUID.
- Name.
- Storage mode.
- Lifecycle status.
- Creation and update timestamps.

### Business creation

Creates a business using an exact template ID and version.

The request includes:

- Template ID.
- Template version.
- Business name.
- Optional description.
- Configuration values.

Creation performs template resolution and configuration validation before persisting the business.

The response includes the generated business UUID, workspace association, exact template reference, configuration, lifecycle status, and timestamps.

The creation response also provides a `Location` header pointing to the newly created business resource.

## 3. Development endpoint security guard

Workspace and business controllers are conditionally registered using:

`businesslab.api.development-endpoints-enabled`

Configuration:

```yaml
businesslab:
  api:
    development-endpoints-enabled: ${BUSINESSLAB_DEV_API_ENABLED:false}
```

The default is `false`.

For local API development:

```bash
BUSINESSLAB_DEV_API_ENABLED=true ./gradlew :apps:api:bootRun
```

The template discovery endpoint remains available independently of this development-only guard.

**Security limitation:** These endpoints are not ready for public deployment. Authentication, identity propagation, workspace ownership checks, and authorization must be implemented before enabling workspace/business APIs in a production environment.

## 4. Request validation and error handling

The REST layer supports request validation through Jakarta Bean Validation.

The common API error response provides structured error details rather than exposing internal exception information.

The exception handler maps application failures to appropriate HTTP statuses:

| HTTP status | Condition |
|---|---|
| 400 | Invalid request or malformed JSON |
| 404 | Workspace, business, or template version not found |
| 409 | Resource state or persistence conflict |
| 422 | Business configuration validation failure |

The implementation includes dedicated exception handling for request validation, unreadable JSON, resource lookup, configuration validation, and database integrity errors.

## 5. Main implementation components

### Workspace API

- `WorkspaceController`
- `WorkspaceCreateRequest`
- `WorkspaceResponse`

### Business API

- `BusinessController`
- `BusinessCreateRequest`
- `BusinessResponse`

### Template API

- `TemplateController`
- `BusinessTemplateResponse`
- `TemplateConfigurationFieldResponse`

### Application services and shared API support

- `WorkspaceBusinessQueryService`
- `ResourceNotFoundException`
- `ApiErrorDetail`
- `ApiErrorResponse`
- `ApiExceptionHandler`

The existing `BusinessApplicationService` was updated to support the REST layer's resource-not-found semantics.

### Test coverage

- `BusinessApiIntegrationTest`
- `TemplateApiIntegrationTest`
- `DevelopmentEndpointGuardIntegrationTest`
- Updated `BusinessPersistenceIntegrationTest`

The persistence regression test now expects `ResourceNotFoundException` for an unknown template version, consistent with the REST application behavior. It continues to verify transactional workspace rollback.

## 6. Spring Boot 4 MVC testing compatibility

During validation, the test compilation initially failed because MVC test support was not available on the test classpath.

The following dependency was added to `apps/api/build.gradle`:

```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
```

The `AutoConfigureMockMvc` import was updated in the three MVC integration test classes to:

```java
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
```

This aligns the test implementation with Spring Boot 4's modular MVC testing support.

## 7. End-to-end API smoke test results

The following local HTTP operations were executed successfully:

| Operation | Result |
|---|---|
| Template discovery | HTTP 200 |
| Workspace creation | HTTP 201 |
| Workspace retrieval | HTTP 200 |
| Business creation | HTTP 201 |
| Business retrieval | HTTP 200 |
| Workspace business listing | HTTP 200 |

### Verified test data

Workspace:

- ID: `0038cd07-8502-4ec7-9f02-25382766bb0c`
- Name: `My Business Workspace`
- Storage mode: `CLOUD`
- Status: `ACTIVE`

Business:

- ID: `f943ee96-5643-47d9-9bf3-53eec6ee3a69`
- Name: `My First Coffee Shop`
- Template: `coffee-shop`
- Version: `1.0.0`
- Initial capital: `500000`
-
