# Repository Guidelines

## Project Structure & Module Organization
This monorepo hosts a Spring Boot API in `backend/` and a Vite + React client in `frontend/`. Backend domain, DTO, and controller classes live in `backend/src/main/java/com/ahss`, with Flyway SQL migrations under `backend/src/main/resources/db/migration` and shared config in `backend/src/main/resources/application.yml`. The frontend keeps shared UI primitives in `frontend/src/components`, state in `frontend/src/store`, and typed helpers under `frontend/src/lib` and `frontend/src/types`. Use `docker-compose.yml` to provision the local Postgres service; the `database_backup_*.sql` artifact is only for manual restores.

## Build, Test, and Development Commands
- `cd backend && ./gradlew bootRun` starts the API against local Postgres (Java 21 required).
- `cd backend && ./gradlew build` compiles the service, runs unit/integration tests, and packages the JAR.
- `cd frontend && npm install` bootstraps dependencies (run once per lockfile change).
- `cd frontend && npm run dev` launches the Vite dev server at `http://localhost:5173`.
- `cd frontend && npm run build` creates an optimized production bundle.
- `cd frontend && npm run lint` enforces the ESLint TypeScript rule set.

## Coding Style & Naming Conventions
Backend classes follow Spring conventions: 4-space indentation, package prefix `com.ahss`, and `PascalCase` for classes/interfaces with `camelCase` fields. Keep DTOs in `backend/src/main/java/com/ahss/dto` and REST controllers under `.../controller`. Frontend code uses TypeScript with 2-space indentation, `PascalCase` components in `frontend/src/components`, `useCamelCase` hooks in `frontend/src/hooks`, and Tailwind utility classes co-located in JSX. Always run `npm run lint` before committing UI changes and let your IDE keep imports sorted.

## Testing Guidelines
JUnit 5 tests live in `backend/src/test/java`; mirror the package of the class under test and suffix files with `*Test`. Prefer `@DataJpaTest` or `@SpringBootTest` for boundary coverage and stub external calls. Execute `cd backend && ./gradlew test` pre-push; new features should include accompanying tests or rationale for gaps in the PR. Frontend testing is not yet automated—when adding critical UI logic, include lightweight smoke tests and document manual verification steps in the PR.

## Commit & Pull Request Guidelines
Commits follow Conventional Commits (`type(scope): summary`) as seen in recent history; group related changes and keep scope names aligned with top-level folders (`frontend`, `tenant`, `layout`, etc.). Each PR should include a concise summary, screenshots for UI changes, database migration notes, and explicit test results (`./gradlew test`, `npm run lint`). Link to Jira/GitHub issues when available and request reviewers from both backend and frontend when the change spans services.

## Security & Configuration Tips
Store sensitive credentials in environment variables referenced from `application.yml`; never commit developer-specific secrets. Use `docker compose up postgres` to create disposable databases instead of local installs, and drop volumes when rotating credentials. Review Flyway migrations for idempotence before merging, and confirm that generated SQL aligns with the shared schema backup.
