# Backend Test Plan

## Current Coverage Snapshot
- Existing automation includes multiple JUnit 5 suites across controllers, security, and utilities. Notable tests include `AuthControllerTest`, `ModuleControllerTest`, `TenantControllerTest`, `DashboardControllerTest`, `PermissionControllerTest`, `UserControllerTest`, `RoleControllerTest`, `ProductControllerTest`, `UserGroupControllerTest`, `PaymentControllerTest`, `JwtTokenProviderTest`, `JwtAuthenticationFilterTest`, and `BCryptTest`. Allure reporting is enabled and writes results to `backend/build/allure-results`.

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

## Allure Reporting
- Adapter: `allure-junit5` is included and JUnit 5 extension auto-detection is enabled via `junit.jupiter.extensions.autodetection.enabled=true`.
- Results: Allure writes to `backend/build/allure-results` through the `allure.results.directory` system property.
- CLI tasks: Gradle provides `unzipAllure`, `allureGenerate`, and `allureServe` tasks; the CLI runs from the dynamically detected `allure-*` distribution extracted under `backend/build/allure-commandline/`.
- Local workflow:
  - Run tests: `./gradlew clean test`
  - Generate report: `./gradlew allureGenerate`
  - Serve interactively: `./gradlew allureServe` (opens a local server)
- Static HTML: View `backend/build/allure-report/index.html` for the generated report without the server.
- CI notes: Archive `backend/build/allure-results` as an artifact and publish a report using the Allure CLI or a CI action (e.g., GitHub Pages). Consider separate jobs for unit and integration, then combine results before publishing.
- Optional: Add AspectJ agent if using `@Step` and `@Attachment` for richer reporting.

### Allure Report Structure
- **Behaviors Tab**: Shows test organization by Epic → Feature → Story hierarchy using `@Epic`, `@Feature`, and `@Story` annotations. This organizes tests by business functionality and is visible for all tests regardless of pass/fail status.
- **Suites Tab**: Shows tests organized by package and class structure.
- **Categories Tab**: Classifies test failures by type (e.g., product defects, test defects, infrastructure issues). Categories are configured in `src/test/resources/categories.json` and **only appear when tests fail, break, or are skipped**. An empty Categories section indicates all tests passed.
- **Overview**: Displays environment properties including code coverage metrics and links to the Jacoco report.

### Jacoco Coverage Integration
- Coverage metrics are automatically written to the Allure environment section after test execution.
- The full Jacoco HTML report is copied to `backend/build/allure-report/jacoco/` during report generation.
- A clickable landing page is created at `backend/build/allure-report/coverage.html` that auto-redirects to the Jacoco report.
- Access the coverage report:
  - Interactive: Run `./gradlew allureServe` and navigate to `http://localhost:<port>/coverage.html`
  - Static: Open `backend/build/allure-report/coverage.html` in a browser
  - Direct: Open `backend/build/allure-report/jacoco/index.html`
- The Environment section shows coverage percentages for INSTRUCTION, LINE, COMPLEXITY, METHOD, CLASS, and BRANCH metrics with a reference to `coverage.html`.

### Allure Annotations & Usage Guidelines
- Prefer annotations over runtime label calls to avoid lifecycle errors and keep metadata declarative.
  - Class-level: `@Epic("…")`, `@Feature("…")`, `@Owner("backend")`
  - Method-level: `@Story("…")`, `@Severity(SeverityLevel.CRITICAL|NORMAL|MINOR|TRIVIAL)`
- Use direct APIs for steps and attachments:
  - Steps: `Allure.step("Description", () -> { /* code */ });`
  - Attachments: `Allure.addAttachment("Name", MediaType.APPLICATION_JSON_VALUE, jsonString);`
- Naming patterns:
  - Epic = top domain (e.g., `Security`, `Catalogue`, `Payment Lifecycle`).
  - Feature = component or slice (e.g., `Authentication Filter`, `Tenant`, `Module`).
  - Story = test intent written as behaviour (e.g., `Get tenant by ID returns 404 when missing`).
  - Severity = impact of failure on product or CI gates.
- Suppression toggle:
  - Runtime Allure calls are suppressed by default to prevent noisy lifecycle errors.
  - Enable full reporting per run with `-Dallure.suppress=false`.
- Example (controller slice):
  ```java
  @WebMvcTest(controllers = TenantController.class)
  @AutoConfigureMockMvc(addFilters = false)
  @Epic("Catalogue")
  @Feature("Tenant")
  @Owner("backend")
  class TenantControllerTest {
      @Test
      @Story("Get tenant by ID returns 404 when missing")
      @Severity(SeverityLevel.MINOR)
      void get_tenant_by_id_not_found_returns_404() throws Exception {
          Allure.step("Stub service to return empty for id=99", () ->
              when(tenantService.getTenantById(99L)).thenReturn(Optional.empty())
          );
          var result = Allure.step("GET /api/v1/tenants/99", () ->
              mockMvc.perform(get("/api/v1/tenants/99"))
                    .andExpect(status().isNotFound())
                    .andReturn()
          );
          Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
              result.getResponse().getContentAsString());
      }
  }
  ```
- Example (security/unit):
  ```java
  @Epic("Security")
  @Feature("JWT")
  @Owner("backend")
  class JwtTokenProviderTest {
      @Test
      @Story("JWT includes user info, roles, and permissions")
      @Severity(SeverityLevel.CRITICAL)
      void generateTokenWithUserInfo_includes_user_claims_roles_permissions() {
          String token = Allure.step("Generate JWT with user info",
              () -> JwtTokenProvider.generateTokenWithUserInfo(sampleUser()));
          Allure.addAttachment("JWT Token", MediaType.TEXT_PLAIN_VALUE, token);
          Claims claims = Allure.step("Parse JWT", () -> JwtTokenProvider.parse(token));
          Allure.addAttachment("JWT Claims", MediaType.APPLICATION_JSON_VALUE,
              new ObjectMapper().valueToTree(claims).toString());
          // assertions...
      }
  }
  ```
- Do not call runtime label helpers (e.g., `labelStory`, `labelSeverity`) inside `@BeforeEach`; annotations should express metadata, and steps/attachments should run inside test methods.

### Allure Categories Configuration
- Categories classify test failures to help with triage and root cause analysis.
- Configuration file: `src/test/resources/categories.json` (automatically copied to `build/allure-results` during test execution)
- Categories defined:
  - **Ignored tests**: Matches tests with status `skipped`
  - **Product defects**: Failed tests with "assertion" in error message (e.g., `AssertionError`)
  - **Test defects**: Broken tests with "runtime" in error message (e.g., `NullPointerException`)
  - **Infrastructure problems**: Broken tests with "Connection", "Socket", or "Timeout" in error message
- **Important**: Categories only appear when tests fail, break, or are skipped. An empty Categories section means all tests passed (this is expected and good).
- Categories answer "What went wrong?" while Behaviors answer "What was tested?"

### Current Test Coverage by Epic
Based on the latest test run (all passing):
- **Catalogue**: 36 tests (Module: 13, Product: 13, Tenant: 10)
- **IAM**: 23 tests (Role Management: 8, User Management: 6, Permission Management: 6, User Group Management: 3)
- **Payment Lifecycle**: 11 tests (Payment Requests: 11)
- **Security**: 7 tests (Authentication Filter: 3, JWT: 2, BCrypt: 2)
- **Authentication**: 6 tests (Login: 6)
- **Total**: 83 tests organized into 5 Epics and 12 Features with 85 unique Stories

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

## Allure Usage in Spring Tests

### Annotations
- Class-level: `@Epic("…")`, `@Feature("…")`, `@Owner("backend")` to organise the Behaviors view.
- Method-level: `@Story("…")`, `@Severity(SeverityLevel.CRITICAL|NORMAL|MINOR|TRIVIAL)` to describe test intent and impact.
- Prefer annotations over runtime label calls; avoid calling label helpers in setup blocks.

### Steps and Attachments
- Use `Allure.step("Description", () -> { /* action + assertions */ })` around meaningful operations:
  - Stubbing collaborators (e.g., service mocks).
  - Executing HTTP requests with `MockMvc`.
  - Parsing and attaching request/response bodies.
- Add attachments for readability: `Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE, body)`. Attach requests as well when helpful.
- Toggle suppression per run: `-Dallure.suppress=false` to emit full step/attachment lifecycle locally.

### DTO and Entity Initialisation in Steps
- Controllers typically exchange DTOs; services return `UserDto`. Initialise only the fields required for a scenario to keep tests focused.
- Example (request DTO in a step):

```java
CreateUserRequest req = Allure.step("Build CreateUserRequest DTO", () -> {
    CreateUserRequest r = new CreateUserRequest();
    r.setUsername("jdoe");
    r.setEmail("jdoe@example.com");
    r.setPassword("Str0ngP@ss!");
    r.setFirstName("John");
    r.setLastName("Doe");
    return r;
});
String json = new ObjectMapper().writeValueAsString(req);
Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);
```

- Example (service return DTO in a step):

```java
com.ahss.dto.UserDto dto = Allure.step("Create service UserDto stub", () -> {
    com.ahss.dto.UserDto d = new com.ahss.dto.UserDto();
    d.setId(123L);
    d.setUsername("jdoe");
    d.setEmail("jdoe@example.com");
    return d;
});
```

- If a unit/service test requires entities (not controllers), initialise the JPA entity similarly inside a step. For controller slice tests, prefer DTOs to match conversions.

## Spring Test Setup with Beans and Mockito

### Controller Slice (recommended)
- Use `@WebMvcTest(controllers = UserController.class)` plus `@AutoConfigureMockMvc(addFilters = false)`.
- Inject `MockMvc` and `ObjectMapper` into the test; mock downstream dependencies with `@MockBean` (or `@MockitoBean` if your setup includes that extension).
- Keep security filters disabled unless explicitly exercising authentication paths.

### Example Skeleton with Allure and MockMvc
```java
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Epic("IAM")
@Feature("User Management")
@Owner("backend")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @Story("Create user returns 201 when successful")
    @Severity(SeverityLevel.NORMAL)
    void create_user_success_returns_201() throws Exception {
        // Build DTO inside a step
        CreateUserRequest req = Allure.step("Build CreateUserRequest DTO", () -> {
            CreateUserRequest r = new CreateUserRequest();
            r.setUsername("newuser");
            r.setEmail("newuser@example.com");
            r.setPassword("Str0ngP@ss!");
            r.setFirstName("New");
            r.setLastName("User");
            return r;
        });

        String json = mapper.writeValueAsString(req);
        Allure.addAttachment("Request Body (DTO)", MediaType.APPLICATION_JSON_VALUE, json);

        // Stub service return DTO inside a step
        com.ahss.dto.UserDto dto = Allure.step("Stub service createUser -> dto", () -> {
            com.ahss.dto.UserDto d = new com.ahss.dto.UserDto();
            d.setId(100L);
            d.setUsername("newuser");
            d.setEmail("newuser@example.com");
            when(userService.createUser(org.mockito.ArgumentMatchers.any())).thenReturn(d);
            return d;
        });

        var result = Allure.step("POST /api/v1/users", () ->
            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username", org.hamcrest.Matchers.is("newuser")))
                .andReturn()
        );

        Allure.addAttachment("Response Body", MediaType.APPLICATION_JSON_VALUE,
                result.getResponse().getContentAsString());
    }
}
```

### Service/Unit Tests (when Spring isn’t needed)
- Prefer plain JUnit 5 with `@ExtendWith(MockitoExtension.class)`, but if the test interacts with Spring components, switch to `@SpringBootTest` or a targeted slice.
- Mock collaborators with Mockito and initialise DTOs/entities inside `Allure.step` blocks to keep behaviour readable.

### General Guidelines
- Keep each test self-contained, initialise only what’s necessary for its behaviour.
- Use expressive `@Story` names and small steps to highlight intent.
- Attach request/response JSON for HTTP tests; attach DTO/entity state for service tests when it aids debugging.
