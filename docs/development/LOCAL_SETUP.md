# BusinessLab Local Development Setup

## Required software

| Component | Version |
|---|---|
| Java | 21 |
| Gradle Wrapper | 9.9.0 |
| Node.js | 24.18.0 |
| pnpm | 11.22.0 |
| PostgreSQL | 18 |

## 1. Verify the environment

```bash
java -version
node -v
pnpm -v
psql --version
```

## 2. Initialise PostgreSQL

Connect to your local PostgreSQL instance:

```bash
psql --dbname postgres
```

Run:

```sql
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname = 'businesslab'
    ) THEN
        CREATE ROLE businesslab
            WITH LOGIN
            PASSWORD 'businesslab-local-only';
    ELSE
        ALTER ROLE businesslab
            WITH LOGIN
            PASSWORD 'businesslab-local-only';
    END IF;
END
$$;

SELECT 'CREATE DATABASE businesslab OWNER businesslab'
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = 'businesslab'
)
\gexec

ALTER DATABASE businesslab OWNER TO businesslab;
```

Exit:

```sql
\q
```

The default local connection is:

- Host: localhost
- Port: 5432
- Database: businesslab
- Username: businesslab
- Password: businesslab-local-only

These credentials are strictly for local development.

Never reuse them in production.

## 3. Initialise the Gradle Wrapper

BusinessLab uses Gradle Wrapper. A global Gradle installation
is not required.

```bash
GRADLE_VERSION=9.9.0
GRADLE_CACHE="$HOME/.cache/businesslab/gradle"

mkdir -p "$GRADLE_CACHE"

curl -fL \
  "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
  -o "$GRADLE_CACHE/gradle-${GRADLE_VERSION}-bin.zip"

unzip -q -o \
  "$GRADLE_CACHE/gradle-${GRADLE_VERSION}-bin.zip" \
  -d "$GRADLE_CACHE"
```

Generate the wrapper from the repository root:

```bash
"$GRADLE_CACHE/gradle-${GRADLE_VERSION}/bin/gradle" \
  wrapper \
  --gradle-version "$GRADLE_VERSION" \
  --distribution-type bin
```

Verify:

```bash
./gradlew --version
```

Commit all generated Gradle Wrapper files.

## 4. Install frontend dependencies

From the repository root:

```bash
pnpm install
```

This generates the shared pnpm lockfile.

Commit `pnpm-lock.yaml`.

## 5. Start the frontend

```bash
pnpm dev:web
```

URL:

http://localhost:3000

## 6. Start the backend

```bash
./gradlew :apps:api:bootRun
```

URL:

http://localhost:8080

## 7. Validate the backend

```bash
./gradlew :apps:api:test
./gradlew :apps:api:build
```

## 8. Validate the frontend

```bash
pnpm lint:web
pnpm typecheck:web
pnpm build:web
```

## 9. API verification

Health endpoint:

http://localhost:8080/actuator/health

System status endpoint:

http://localhost:8080/api/v1/system/status
