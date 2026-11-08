# BusinessLab

**Build. Operate. Experiment. Understand.**

BusinessLab is an interactive business experimentation platform where
people can create virtual businesses, operate them in persistent simulated
environments, experiment with decisions and understand their consequences.

## Product direction

BusinessLab is a generic business simulation platform.

Individual business models are introduced progressively through reusable
business templates.

The first playable business template will be a coffee shop. The platform
itself must not be coupled to this particular business.

## Core capabilities

- Generic business creation and lifecycle management
- Persistent business simulation
- Deterministic simulation engine
- Visual Business Flow Studio
- Business policies and workflow automation
- Business intelligence and explainable outcomes
- Isolated business experiments and scenario comparison
- AI-powered business advisory agents
- Extensible business template catalogue

## Technology stack

| Component | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1.2 |
| Backend build | Gradle 9.9.0 |
| Frontend | Next.js 16.4.5, React 19.3.1 |
| Frontend language | TypeScript 6.0.3 |
| Package manager | pnpm 11.22.0 |
| Database | PostgreSQL 18 |

## Repository structure

- `apps/api` - Spring Boot backend
- `apps/web` - Next.js frontend
- `docs` - Product, architecture, simulation and development documentation
- `gradle` - Gradle Wrapper configuration
- `scripts` - Local development utilities

## Prerequisites

- JDK 21
- Node.js 24.18.0
- pnpm 11.22.0
- PostgreSQL 18
- Git

Gradle does not need to be installed globally. Use the committed
Gradle Wrapper.

## Getting started

### 1. Install frontend dependencies

From the repository root:

```bash
pnpm install
```

### 2. Start the frontend

```bash
pnpm dev:web
```

Open http://localhost:3000.

### 3. Prepare PostgreSQL

Follow the instructions in `docs/development/LOCAL_SETUP.md`.

The default local credentials are:

- Database: `businesslab`
- Username: `businesslab`
- Password: `businesslab-local-only`

These are development-only credentials.

### 4. Start the backend

After generating the Gradle Wrapper:

```bash
./gradlew :apps:api:bootRun
```

The API runs on http://localhost:8080.

Health endpoint:

http://localhost:8080/actuator/health

System status:

http://localhost:8080/api/v1/system/status

## Useful commands

### Frontend

```bash
pnpm dev:web
pnpm lint:web
pnpm typecheck:web
pnpm build:web
```

### Backend

```bash
./gradlew :apps:api:bootRun
./gradlew :apps:api:test
./gradlew :apps:api:build
```

## Engineering principles

1. Keep the core platform independent of individual business templates.
2. Maintain deterministic and reproducible simulation outcomes.
3. Persist business state and simulation history.
4. Treat user-created workflows as versioned definitions.
5. Keep AI advisory separate from authoritative simulation calculations.
6. Add new business workflows incrementally.
7. Test every business rule and simulation outcome.
8. Document every completed feature and milestone.

## Documentation

See the `docs` directory for product and engineering documentation.

## Development status

Initial repository bootstrap.

See `docs/development/` for milestones, roadmap and implementation progress.
