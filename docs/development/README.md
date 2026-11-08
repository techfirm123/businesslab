# Development Documentation

## Main documents

- BUSINESSLAB_ROADMAP.md
- BUSINESSLAB_IMPLEMENTATION_BACKLOG.md
- LOCAL_SETUP.md

## Milestone documentation

Each completed milestone must have a dedicated Markdown document
under `milestones/`.

Example:

milestones/
├── M1_PLATFORM_FOUNDATION.md
├── M2_SIMULATION_ENGINE.md
├── M3_FIRST_BUSINESS.md
└── ...

## Milestone completion requirements

A milestone must not be marked complete until:

1. All agreed acceptance criteria are satisfied.
2. Relevant automated tests pass.
3. Application builds successfully.
4. Database migrations, if applicable, are validated.
5. Code quality checks pass.
6. Known limitations are documented.
7. The implementation is committed.

## Handoff document contents

Every milestone handoff must include:

- Milestone overview
- Completed requirements
- Architecture and technical decisions
- Files created and modified
- Database changes
- API contracts
- Test and build commands
- Actual validation results
- Known issues and limitations
- Remaining tasks
- Exact next implementation slice

The handoff must be sufficiently detailed to resume work in
a new conversation without reconstructing earlier discussions.
