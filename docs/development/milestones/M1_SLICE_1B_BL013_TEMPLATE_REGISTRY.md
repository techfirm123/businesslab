# M1 — Slice 1B — BL-013: Business Template Contract and Registry

**Project:** BusinessLab
**Milestone:** M1 — Generic Platform Foundation
**Slice:** 1B — Generic Business Foundation
**Task:** BL-013 — Business Template Contract and Registry
**Status:** COMPLETED

## 1. Objective

Introduce an extensible business template architecture that allows
BusinessLab to support multiple types of businesses through
independent, versioned template implementations.

The generic platform must discover, resolve and validate templates
without containing industry-specific business logic.

## 2. Template Domain

Implemented:

- BusinessTemplate
- BusinessTemplateReference (introduced in BL-012)
- BusinessTemplateDescriptor
- BusinessTemplateCategory
- BusinessTemplateStatus
- BusinessTemplateCapability
- TemplateConfigurationField
- ConfigurationValueType
- BusinessTemplateValidationError
- BusinessTemplateValidationResult

## 3. Business Template Contract

Every template implements:

- Template metadata retrieval.
- Configuration field definitions.
- Template-specific configuration validation.

Template implementations are independent of the generic
Business aggregate.

Future simulation and operational extensions will be introduced
through additional template contracts.

## 4. Template Versioning

A template is uniquely identified by:

- Template ID
- Template version

Example:

coffee-shop / 1.0.0

The registry resolves exact template versions.

It does not silently substitute the latest version.

Deprecated versions remain resolvable for existing businesses
but are excluded from new-business discovery.

## 5. Configuration Metadata

Implemented typed configuration field definitions.

Supported field types:

- STRING
- INTEGER
- DECIMAL
- BOOLEAN

Metadata includes:

- Field key
- Display label
- Description
- Required flag
- Example value
- Minimum and maximum constraints
- Maximum string length
- Optional validation pattern

The metadata can eventually support dynamically generated
business setup forms.

## 6. Template Registry

Implemented:

InMemoryBusinessTemplateRegistry

Responsibilities:

- Discover templates through Spring dependency injection.
- Register template implementations.
- Resolve templates using exact ID and version.
- Reject duplicate registrations.
- Return available templates for new businesses.
- Exclude deprecated versions from available-template discovery.

The initial registry is in-memory because template definitions
are currently platform-managed static implementations.

## 7. First Registered Template

Implemented:

CoffeeShopBusinessTemplate

Template ID: coffee-shop

Version: 1.0.0

Category: FOOD_AND_BEVERAGE

Status: ACTIVE

Supported capability:

- BUSINESS_SETUP

Initial configuration fields:

- initialCapital
- currency

Configuration validation includes:

- Required fields.
- Numeric initial capital.
- Positive initial capital.
- Three-letter uppercase currency format.
- Unknown field rejection.

This implementation establishes the initial coffee-shop
template definition only.

Operational simulation mechanics have not been implemented.

## 8. Files Created

### Template domain

- apps/api/src/main/java/com/businesslab/templates/domain/BusinessTemplateCategory.java
- apps/api/src/main/java/com/businesslab/templates/domain/BusinessTemplateStatus.java
- apps/api/src/main/java/com/businesslab/templates/domain/BusinessTemplateCapability.java
- apps/api/src/main/java/com/businesslab/templates/domain/ConfigurationValueType.java
- apps/api/src/main/java/com/businesslab/templates/domain/TemplateConfigurationField.java
- apps/api/src/main/java/com/businesslab/templates/domain/BusinessTemplateDescriptor.java
- apps/api/src/main/java/com/businesslab/templates/domain/BusinessTemplateValidationError.java
- apps/api/src/main/java/com/businesslab/templates/domain/BusinessTemplateValidationResult.java
- apps/api/src/main/java/com/businesslab/templates/domain/BusinessTemplate.java

### Template application

- apps/api/src/main/java/com/businesslab/templates/application/BusinessTemplateRegistry.java

### Template infrastructure

- apps/api/src/main/java/com/businesslab/templates/infrastructure/registry/InMemoryBusinessTemplateRegistry.java

### First business template

- apps/api/src/main/java/com/businesslab/industries/coffeeshop/template/CoffeeShopBusinessTemplate.java

### Tests

- apps/api/src/test/java/com/businesslab/industries/coffeeshop/template/CoffeeShopBusinessTemplateTest.java
- apps/api/src/test/java/com/businesslab/templates/domain/BusinessTemplateDescriptorTest.java
- apps/api/src/test/java/com/businesslab/templates/application/InMemoryBusinessTemplateRegistryTest.java
- apps/api/src/test/java/com/businesslab/templates/application/BusinessTemplateRegistryIntegrationTest.java

## 9. Validation Results

Developer-confirmed results:

- Template domain tests: PASSED
- Configuration validation tests: PASSED
- Registry unit tests: PASSED
- Duplicate registration tests: PASSED
- Deprecated template tests: PASSED
- Spring template discovery: PASSED
- Complete backend test suite: PASSED
- Backend build: PASSED

## 10. Architecture Decisions

1. Business templates are independent of the generic Business domain.
2. Template identity includes an exact version.
3. Template registration uses Spring dependency injection.
4. Duplicate template ID/version combinations are rejected.
5. Deprecated template versions remain resolvable.
6. Only active templates appear in new-business discovery.
7. Configuration definitions support dynamic UI generation.
8. Template-specific validation is owned by each template.
9. The initial registry is in-memory.
10. CoffeeShopBusinessTemplate is the first registered template,
    not the foundation of the generic platform.

## 11. Known Limitations

Not implemented:

- Template persistence.
- Public template discovery API.
- Dynamic setup forms.
- Full JSON Schema support.
- Template simulation extensions.
- Template workflow definitions.
- Business creation integration.
- Business persistence.

These will be introduced progressively.

## 12. Next Implementation Task

BL-014 — Generic Business Persistence and APIs.

Objectives:

1. Introduce the first application-domain Flyway migration.
2. Define workspace persistence.
3. Define business persistence.
4. Persist generic business configuration.
5. Implement workspace and business repositories.
6. Implement business creation service.
7. Integrate template resolution and configuration validation.
8. Implement business creation and retrieval APIs.
9. Add database integration tests.
10. Verify persistence across application restart.

## 13. Development Position

M1 Slice 1A — Repository Bootstrap: COMPLETED

BL-011 — Flyway Foundation: COMPLETED

BL-012 — Generic Business Domain Model: COMPLETED

BL-013 — Business Template Contract and Registry: COMPLETED

BL-014 — Generic Business Persistence and APIs: NEXT

---

BusinessLab engineering principle:

The platform provides the generic business infrastructure.
Individual templates define industry-specific behaviour.
