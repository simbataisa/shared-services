# Backend Test Plan

## Current Coverage Snapshot
- Existing automation is limited to `src/test/java/com/ahss/util/BCryptTest.java:1`, which prints BCrypt hashes without assertions; effectively none of the Spring services, controllers, repositories, or entities have regression protection today.

## Testing Goals & Principles
- Establish fast, deterministic unit coverage for service/business logic while keeping database-heavy scenarios in focused integration suites; favour behaviour assertions over implementation details.
- Exercise validation and error handling paths exposed by controllers so API contracts remain stable even as implementations evolve.
- Use expressive test names, data builders, and helpers so that future contributors can extend the suite without duplicating setup.

## Tooling & Infrastructure
- Standardise on JUnit 5 + AssertJ for fluent assertions and Jacoco for code-coverage reporting; enforce minimum module-level coverage gates in CI.
- Prefer Mockito with Spring’s `@MockBean`/`MockitoBean` support to stub collaborators in slice tests, and fall back to plain `@ExtendWith(MockitoExtension.class)` when the Spring container is not required.
- Use MockMvc (with `@WebMvcTest` or `@AutoConfigureMockMvc`) for controller contracts so HTTP behaviour is verified without bootstrapping the full application.
- Run JPA/repository and end-to-end tests against PostgreSQL Testcontainers because entities such as `PaymentRequest` (`src/main/java/com/ahss/entity/PaymentRequest.java:24`), `PaymentTransaction` (`src/main/java/com/ahss/entity/PaymentTransaction.java:33`), and `PaymentRefund` (`src/main/java/com/ahss/entity/PaymentRefund.java:28`) depend on JSONB, enum arrays, and UUID types that H2 cannot emulate reliably. Reuse singleton containers between suites to keep execution time predictable.
- Publish Allure reports from CI for both unit and integration stages so flaky failures and coverage deltas are visible to the team.
- Provide reusable factory utilities for complex aggregates (users with roles, payment requests with transactions) and centralise Flyway baseline data seeding for integration tests.

## Component Isolation Strategy
- Default to plain JUnit + Mockito for pure business services and converters; avoid Spring context startup unless a bean lifecycle or annotation processor is being exercised.
- Apply Spring test slices to keep bean initialisation focused: `@WebMvcTest` + MockMvc for controllers, `@DataJpaTest` + Testcontainers for repositories, `@JsonTest` for Jackson mappings, and `@Import`-scoped configuration for service-level tests that need limited Spring infrastructure.
- Use `@MockBean`/`MockitoBean` for downstream dependencies (e.g., repositories, external clients) within slice tests instead of switching to `@SpringBootTest`.
- Reserve `@SpringBootTest` for the handful of cross-layer flows called out in the coverage map; gate those tests behind a dedicated Gradle task so fast feedback remains available.

## Coverage Map by Domain

### Payment lifecycle (requests, transactions, refunds, audit)
- Unit focus — `PaymentRequestServiceImpl` (`src/main/java/com/ahss/service/impl/PaymentRequestServiceImpl.java:38`), `PaymentTransactionServiceImpl` (`src/main/java/com/ahss/service/impl/PaymentTransactionServiceImpl.java:210`), and `PaymentRefundServiceImpl` (`src/main/java/com/ahss/service/impl/PaymentRefundServiceImpl.java:205`): stub repositories/audit services to verify status transitions (create, partial update, cancel, expire, mark-as-paid, retry, stale-processing), summary calculations, and error propagation when records are missing.
- Entity behaviour — exercise `PaymentTransaction` helpers (`src/main/java/com/ahss/entity/PaymentTransaction.java:305`) and `PaymentRequest` guards (`src/main/java/com/ahss/entity/PaymentRequest.java:212`) to confirm retry, expiry, and payment acceptance rules.
- Repository focus — cover key finder, search, and aggregate queries in `PaymentRequestRepository` (`src/main/java/com/ahss/repository/PaymentRequestRepository.java:16`), `PaymentTransactionRepository` (`src/main/java/com/ahss/repository/PaymentTransactionRepository.java:16`), `PaymentRefundRepository` (`src/main/java/com/ahss/repository/PaymentRefundRepository.java:16`), and `PaymentAuditLogRepository` (`src/main/java/com/ahss/repository/PaymentAuditLogRepository.java:18`) using @DataJpaTest + Testcontainers data sets.
- Controller slice — use MockMvc to assert response envelopes and status codes for `PaymentController` (`src/main/java/com/ahss/controller/PaymentController.java:32`) across success, validation failure, and not-found paths; mock services to focus on HTTP contracts and audit logging exposure.
- Integration threads — craft `@SpringBootTest` flows that create a payment request, process a transaction, issue a refund, and confirm audit trail entries via the exposed REST endpoints to validate wiring, converters, and database mappings end-to-end.

### Authentication & security
- Web tests — `AuthController` (`src/main/java/com/ahss/controller/AuthController.java:35`) needs MockMvc coverage for happy-path login plus invalid credentials, locked accounts, inactive status, and missing fields, asserting JWT payload presence and audit side-effects (`UserService.incrementFailedLoginAttempts`, `resetFailedLoginAttempts`, `updateLastLogin` in `src/main/java/com/ahss/service/impl/UserServiceImpl.java:228`).
- Token & filter — add unit tests for `JwtTokenProvider` (`src/main/java/com/ahss/security/JwtTokenProvider.java:30`) to validate claim composition and expiry, and for `JwtAuthenticationFilter` (`src/main/java/com/ahss/security/JwtAuthenticationFilter.java:25`) to confirm security context population and graceful handling of malformed tokens.
- Configuration — a lightweight smoke test for `SecurityConfig` (`src/main/java/com/ahss/config/SecurityConfig.java:19`) ensures the filter chain permits the documented public routes and keeps session management stateless.

### User & access management
- Service unit tests — cover creation/update/soft-delete and lock/unlock flows in `UserServiceImpl` (`src/main/java/com/ahss/service/impl/UserServiceImpl.java:124`), ensuring native update shortcuts are invoked and role/user-group assignment guards behave; include concurrency edge cases for failed login counters.
- Repository focus — `UserRepository` (`src/main/java/com/ahss/repository/UserRepository.java:16`) queries (search, active filters, native updates) merit @DataJpaTest coverage with representative data (locked vs unlocked users, soft-deleted users).
- Role & permission — test `RoleServiceImpl` (`src/main/java/com/ahss/service/impl/RoleServiceImpl.java:26`) and `PermissionServiceImpl` (`src/main/java/com/ahss/service/impl/PermissionServiceImpl.java:21`) for duplicate detection, active-status enforcement, and the expected `UnsupportedOperationException` thrown by deactivate/activate hooks; surface the mismatch with `PermissionController` to drive future refactor.
- User groups — validate `UserGroupServiceImpl` (`src/main/java/com/ahss/service/impl/UserGroupServiceImpl.java:19`) list and detail handling where counts are derived from lazy collections.
- Controller slice tests should assert payload shapes and error handling for `UserController` (`src/main/java/com/ahss/controller/UserController.java:24`), `RoleController` (`src/main/java/com/ahss/controller/RoleController.java:17`), `PermissionController` (`src/main/java/com/ahss/controller/PermissionController.java:18`), and `UserGroupController` (`src/main/java/com/ahss/controller/UserGroupController.java:16`).

### Product, module, and tenant catalogue
- Unit tests — ensure `ProductServiceImpl` (`src/main/java/com/ahss/service/impl/ProductServiceImpl.java:24`), `ModuleServiceImpl` (`src/main/java/com/ahss/service/impl/ModuleServiceImpl.java:24`), and `TenantServiceImpl` (`src/main/java/com/ahss/service/impl/TenantServiceImpl.java:23`) enforce uniqueness, active-state constraints, and cross-entity validations (e.g., module creation requires active product).
- Repository coverage — validate custom lookups in `ProductRepository` (`src/main/java/com/ahss/repository/ProductRepository.java:16`), `ModuleRepository` (`src/main/java/com/ahss/repository/ModuleRepository.java:17`), and `TenantRepository` (`src/main/java/com/ahss/repository/TenantRepository.java:16`) including case-insensitive searches and status filters.
- Controller slice — add MockMvc suites for `ProductController` (`src/main/java/com/ahss/controller/ProductController.java:17`), `ModuleController` (`src/main/java/com/ahss/controller/ModuleController.java:17`), and `TenantController` (`src/main/java/com/ahss/controller/TenantController.java:21`) covering CRUD, status transitions, and validation feedback.

### Audit, dashboard, and observability
- Service tests — cover aggregation helpers and cleanup logic inside `PaymentAuditLogServiceImpl` (`src/main/java/com/ahss/service/impl/PaymentAuditLogServiceImpl.java:180`), especially `getActionCountBreakdown`, `getUserActionCountBreakdown`, and `cleanupOldAuditLogs`.
- Dashboard — MockMvc tests for `DashboardController` (`src/main/java/com/ahss/controller/DashboardController.java:16`) to assert the static portfolio response and failure handling so future dynamic implementations can refactor safely.

## Cross-Cutting Concerns
- DTO validation — exercise Jakarta validation on request payloads like `CreatePaymentRequestDto` (`src/main/java/com/ahss/dto/request/CreatePaymentRequestDto.java:15`), `UpdateUserRequest` (`src/main/java/com/ahss/dto/request/UpdateUserRequest.java:13`), and `CreateUserGroupRequest` (`src/main/java/com/ahss/dto/request/CreateUserGroupRequest.java:12`) using Validator/MockMvc tests to capture boundary errors.
- Response wrapper — add a micro test for `ApiResponse` (`src/main/java/com/ahss/dto/response/ApiResponse.java:7`) ensuring timestamps and success flags are populated consistently.
- Mapper coverage — verify DTO conversions performed inside services (e.g., `PaymentTransactionServiceImpl.convertToDto` at `src/main/java/com/ahss/service/impl/PaymentTransactionServiceImpl.java:234`) to avoid silent nulls or incorrect metadata.

## Test Data & Utilities
- Create builder classes or factory methods for recurring aggregates (tenant with modules, user with roles, payment request with linked audit logs) and share them via a `test-fixtures` package.
- Provide reusable Testcontainers configuration and Spring `@DynamicPropertySource` utilities so integration suites can spin up PostgreSQL quickly and reuse schema migrations.
- Capture common assertions (ApiResponse helpers, JSON path snippets) to minimise duplication in controller tests.

## Roadmap & Prioritisation
- Phase 0: Introduce Testcontainers base class, fixture utilities, and Gradle profiles (headless unit vs integration).
- Phase 1: Cover payment lifecycle services/controllers/entities, as they hold the most complex state transitions and audit coupling.
- Phase 2: Secure authentication and user management flows, including repository/native query coverage.
- Phase 3: Exercise product/module/tenant catalogue and permission/role management controllers.
- Phase 4: Add remaining dashboard/audit aggregates, cross-cutting DTO validation, and regression tests for known bugs (e.g., permission activation endpoints).

## Risks & Open Questions
- Several service methods are placeholders (`PaymentTransactionServiceImpl.getTransactionCountByPaymentMethod`, `PaymentRefundServiceImpl.getRefundCountByStatus`); tests should document current behaviour and help prioritise implementation work.
- `PermissionController` exposes activate/deactivate endpoints while `PermissionServiceImpl` throws `UnsupportedOperationException`; decide whether to adjust the controller contract or implement the feature before hardening tests.
- Confirm availability of shared PostgreSQL containers in CI and whether datasets can be seeded solely through Flyway or require dedicated fixtures.
