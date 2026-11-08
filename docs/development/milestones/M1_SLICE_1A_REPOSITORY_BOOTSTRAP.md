# M1 — Slice 1A: Repository and Application Bootstrap

**Project:** BusinessLab  
**Milestone:** M1 — Generic Platform Foundation  
**Slice:** 1A — Repository and Application Bootstrap  
**Status:** Completed  
**Version:** 1.0

## 1. Overview

Established the initial BusinessLab monorepo and bootstrapped the
frontend and backend applications.

The purpose of this slice was to establish a working development
environment before implementing business-domain functionality.

## 2. Technology Stack

| Component | Version |
|---|---|
| Java | 21.0.11 |
| Spring Boot | 4.1.2 |
| Gradle | 9.9.0 |
| Gradle DSL | Groovy |
| Node.js | 24.18.0 |
| pnpm | 11.22.0 |
| Next.js | 16.4.5 |
| React | 19.3.1 |
| TypeScript | 6.0.3 |
| PostgreSQL | 18.6 |

## 3. Repository Structure

businesslab/
├── .github/
│   └── workflows/
│       └── ci.yml
├── apps/
│   ├── api/
│   │   └── src/
│   │       ├── main/
│   │       └── test/
│   └── web/
│       └── src/
│           └── app/
├── docs/
│   ├── product/
│   ├── architecture/
│   ├── simulation/
│   ├── ux/
│   ├── ai/
│   └── development/
│       └── milestones/
├── gradle/
├── scripts/
├── settings.gradle
├── build.gradle
├── gradle.properties
├── package.json
├── pnpm-workspace.yaml
├── pnpm-lock.yaml
└── README.md

## 4. Backend Implementation

Established the Spring Boot application.

Implemented:

- Spring Boot application entry point.
- REST API foundation.
- System status endpoint.
- Actuator health endpoint.
- PostgreSQL JDBC dependency.
- H2 test database configuration.
- Gradle Wrapper.
- Gradle native BOM dependency management.

### API endpoints

GET /api/v1/system/status

GET /actuator/health

### Backend build

The backend uses Gradle Groovy DSL.

The project is configured as a Gradle multi-project build with
the API located at apps/api.

## 5. Frontend Implementation

Established the Next.js application.

Implemented:

- Next.js App Router.
- TypeScript configuration.
- ESLint configuration.
- Responsive landing page.
- Initial BusinessLab visual identity.
- Product positioning and capability sections.
- Responsive layouts.
- Accessibility-conscious markup.
- Reduced-motion support.

The landing page introduces BusinessLab as a generic business
experimentation platform.

It does not position the product exclusively as a coffee-shop
application.

## 6. Database Environment

PostgreSQL 18.6 was installed through Homebrew.

Created:

Database: businesslab

Role: businesslab

Local development credentials are configured through
environment-overridable Spring Boot datasource properties.

Verified the database connection independently through psql.

No application domain tables or Flyway migrations have been
implemented yet.

## 7. CI Foundation

Created the initial GitHub Actions workflow.

Frontend checks:

- Dependency installation
- ESLint
- TypeScript
- Production build

Backend checks:

- Gradle Wrapper validation
- Java 21 setup
- Gradle build
- Automated tests

## 8. Validation Results

The developer confirmed that all final checks passed.

Frontend:

- ESLint: PASSED
- TypeScript: PASSED
- Next.js production build: PASSED

Backend:

- Gradle project configuration: PASSED
- Backend automated tests: PASSED
- Backend build: PASSED

Runtime:

- Spring Boot application startup: VERIFIED
- Actuator health endpoint: UP
- System status endpoint: UP

Database:

- PostgreSQL server: VERIFIED
- BusinessLab database: VERIFIED
- BusinessLab role: VERIFIED
- Database identity and version query: PASSED

Note: A database-backed application operation is still required
to explicitly verify end-to-end datasource connectivity.

## 9. Important Technical Decisions

1. Java 21 is the backend language.
2. Spring Boot is the backend framework.
3. Gradle Groovy DSL is the build configuration language.
4. Gradle Wrapper is committed to the repository.
5. Next.js and TypeScript power the frontend.
6. pnpm manages the frontend workspace.
7. PostgreSQL is the primary application database.
8. Backend architecture starts as a modular monolith.
9. Business-specific logic must not leak into generic platform
   modules.
10. AI must not become the authoritative simulation engine.

## 10. Known Limitations

The following functionality has not been implemented:

- User authentication.
- Generic business domain model.
- Business template registry.
- Business creation.
- Business persistence.
- Flyway migrations.
- Simulation engine.
- Simulation clock.
- Simulation events.
- Workflow engine.
- Business Flow Studio.
- Experimentation engine.
- AI agents.

These are intentionally deferred to subsequent implementation slices.

## 11. Next Implementation Slice

M1 — Slice 1B: Generic Business Foundation

Objectives:

1. Establish backend package and module conventions.
2. Introduce Flyway database migrations.
3. Implement the generic Business domain model.
4. Establish business lifecycle states.
5. Implement the Business Template contract.
6. Implement the Business Template Registry.
7. Register the initial Coffee Shop template.
8. Implement business creation and retrieval APIs.
9. Persist business configuration and state.
10. Verify database-backed business creation and retrieval.

Acceptance criteria:

- A business can be created from a registered template.
- Business identity is independent of the selected template.
- Business configuration is persisted.
- A business can be retrieved after application restart.
- Invalid templates and invalid requests are handled properly.
- Automated tests cover business creation and retrieval.
- All quality checks pass.

## 12. Current Development Position

M0 — Product Foundation: COMPLETED

M1 — Slice 1A: Repository and Application Bootstrap: COMPLETED

M1 — Slice 1B: Generic Business Foundation: NEXT

---

**BusinessLab engineering principle:**

Build the generic platform first. Introduce individual business
models progressively as independent, extensible templates.
