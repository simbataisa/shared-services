# Backend Test Plan

## Overview

This document describes the comprehensive testing strategy for the Shared Services Backend application, including unit tests, integration tests, and code coverage requirements.

## Test Framework Stack

### Core Testing Frameworks
- **JUnit 5** - Primary testing framework
- **Mockito** - Mocking framework for unit tests
- **Spring Boot Test** - Spring testing utilities
- **MockMvc** - REST API testing for controllers
- **Testcontainers** - Containerized integration testing
- **Allure** - Test reporting and documentation

### Code Coverage
- **JaCoCo** - Code coverage measurement and reporting
- **Target Coverage**: 80%+ branch coverage for all controllers

## Integration Testing with Testcontainers

### Overview
Integration tests use **Testcontainers** to provide isolated, reproducible test environments with real database instances. This approach eliminates the need for external database dependencies and ensures consistent test behavior across different environments.

### BaseIntegrationTest Configuration

All integration tests extend `BaseIntegrationTest` which provides:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    // PostgreSQL container configuration
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("sharedservices")
            .withUsername("postgres")
            .withPassword("postgres");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
```

### Key Features

1. **Docker-based PostgreSQL**: Uses PostgreSQL 16-alpine Docker image
2. **Automatic Lifecycle Management**: Container starts once per test class and is shared across tests
3. **Dynamic Configuration**: Uses `@DynamicPropertySource` to inject container connection details
4. **Flyway Migrations**: Runs actual database migrations for realistic testing
5. **Isolation**: Each test suite gets a fresh database state

### Benefits

- **Real Database**: Tests run against actual PostgreSQL, not H2 or mocks
- **Consistency**: Same database behavior in CI/CD and local development
- **No Setup Required**: Developers don't need to install PostgreSQL locally
- **Isolation**: Tests don't interfere with development databases
- **CI/CD Friendly**: Works seamlessly in containerized CI environments

## Test Suites

### 1. Unit Tests (Controller Layer)

Unit tests use MockMvc and mock services to test controller logic in isolation.

#### AuthControllerTest
- **Coverage**: 100% instruction, 100% branch
- **Tests**: 8 tests
- **Focus**: Authentication, login, token generation

#### DashboardControllerTest
- **Coverage**: 100% instruction, 100% branch
- **Tests**: 6 tests
- **Focus**: Dashboard statistics and metrics

#### ModuleControllerTest
- **Coverage**: 100% instruction, 100% branch
- **Tests**: Multiple tests covering CRUD operations
- **Focus**: Module management endpoints

#### PaymentControllerTest
- **Coverage**: 100% instruction, 100% branch
- **Tests**: 53 tests
- **Focus**: Payment requests, transactions, refunds, audit logs, statistics
- **Recent Enhancements**:
  - Added 23 new tests for comprehensive branch coverage
  - Tests for all payment request operations (create, update, approve, reject, cancel)
  - Tests for all transaction operations (process, retry, cancel, query by status)
  - Tests for refund operations (create, cancel, query)
  - Tests for audit log retrieval (by request, transaction, refund, user)
  - Error case coverage for all statistics endpoints

#### PermissionControllerTest
- **Coverage**: High instruction and branch coverage
- **Tests**: Multiple tests
- **Focus**: Permission CRUD operations

#### ProductControllerTest
- **Coverage**: 100% instruction, 100% branch
- **Tests**: 12 tests
- **Focus**: Product management

#### RoleControllerTest
- **Coverage**: High instruction and branch coverage
- **Tests**: Multiple tests
- **Focus**: Role management and assignment

#### TenantControllerTest
- **Coverage**: High instruction and branch coverage
- **Tests**: Multiple tests
- **Focus**: Multi-tenancy operations

#### UserControllerTest
- **Coverage**: 99% instruction, 80% branch
- **Tests**: 54 tests
- **Focus**: User management, roles, user groups, status changes
- **Recent Enhancements**:
  - Added 14 new tests for improved branch coverage
  - Tests for creating users with roles and user groups
  - Tests for updating users with role/group assignments
  - Tests for password change validation and error cases
  - Tests for role/user group operations with exceptions
  - Tests for inactive user retrieval with date filtering

#### UserGroupControllerTest
- **Coverage**: High instruction and branch coverage
- **Tests**: Multiple tests
- **Focus**: User group management

### 2. Integration Tests

Integration tests validate complete workflows with real database operations using Testcontainers.

#### AuthControllerIntegrationTest
- **Location**: `src/test/java/com/ahss/integration/`
- **Extends**: `BaseIntegrationTest`
- **Tests**: Authentication flows with database
- **Database**: Uses Testcontainers PostgreSQL

#### DashboardControllerIntegrationTest
- **Tests**: Dashboard data aggregation
- **Database**: Testcontainers PostgreSQL

#### ModuleControllerIntegrationTest
- **Tests**: Module CRUD with persistence
- **Database**: Testcontainers PostgreSQL

#### PaymentControllerIntegrationTest
- **Tests**:
  - Payment request lifecycle
  - Transaction processing
  - Refund workflows
  - Audit log generation
- **Database**: Testcontainers PostgreSQL
- **Special Notes**:
  - Tests handle optional statistics that may return 200 or 500 depending on seed data
  - Comprehensive coverage of payment workflows

#### PermissionControllerIntegrationTest
- **Tests**: Permission CRUD with database validation
- **Database**: Testcontainers PostgreSQL

#### ProductControllerIntegrationTest
- **Tests**: Product management with persistence
- **Database**: Testcontainers PostgreSQL

#### RoleControllerIntegrationTest
- **Tests**: Role CRUD operations
- **Database**: Testcontainers PostgreSQL

#### TenantControllerIntegrationTest
- **Tests**: Tenant management and search
- **Database**: Testcontainers PostgreSQL
- **Focus**: Multi-tenancy validation

#### UserControllerIntegrationTest
- **Tests**:
  - User CRUD operations
  - User search and filtering
  - Username/email existence checks
  - Status-based user queries
- **Database**: Testcontainers PostgreSQL

#### UserGroupControllerIntegrationTest
- **Tests**: User group operations
- **Database**: Testcontainers PostgreSQL
- **Special Handling**: Gracefully handles empty seed data

### 3. Security Integration Tests

#### SecurityConfigIntegrationTest
- **Tests**: Security configuration and access control
- **Database**: Testcontainers PostgreSQL
- **Focus**: Authentication and authorization rules

## Test Execution

### Running All Tests
```bash
./gradlew test
```

### Running Specific Test Class
```bash
./gradlew test --tests UserControllerTest
./gradlew test --tests PaymentControllerIntegrationTest
```

### Running with Coverage Report
```bash
./gradlew clean test jacocoTestReport
```

Coverage report available at: `build/reports/jacoco/test/html/index.html`

### Running with Allure Report
```bash
./gradlew clean test allureGenerate
./gradlew allureServe
```

Allure report opens automatically in browser at: `http://localhost:port`

## Code Coverage Metrics

### Current Coverage Status

| Controller | Instruction Coverage | Branch Coverage | Status |
|-----------|---------------------|-----------------|---------|
| PaymentController | 100% | 100% | ✅ Excellent |
| UserController | 99% | 80% | ✅ Good |
| AuthController | 100% | 100% | ✅ Excellent |
| DashboardController | 100% | 100% | ✅ Excellent |
| ProductController | 100% | 100% | ✅ Excellent |
| ModuleController | High | High | ✅ Good |
| PermissionController | High | High | ✅ Good |
| RoleController | High | High | ✅ Good |
| TenantController | High | High | ✅ Good |
| UserGroupController | High | High | ✅ Good |

### Coverage Goals
- **Minimum Branch Coverage**: 80%
- **Minimum Instruction Coverage**: 90%
- **Minimum Line Coverage**: 90%

## Allure Test Reporting

All tests include comprehensive Allure annotations for enhanced reporting:

### Annotations Used
- `@Epic` - High-level feature grouping
- `@Feature` - Functional area
- `@Story` - Specific test scenario
- `@Severity` - Test importance (BLOCKER, CRITICAL, NORMAL, MINOR, TRIVIAL)
- `@Owner` - Team responsible
- `Allure.step()` - Detailed test step logging
- `Allure.addAttachment()` - Request/response logging

### Example
```java
@Test
@Epic("Integration Tests")
@Feature("Payment Management")
@Story("Create payment request returns 201")
@Severity(SeverityLevel.CRITICAL)
@Owner("backend")
void createPaymentRequest_success() {
    String token = Allure.step("Obtain JWT token", this::obtainToken);
    // Test implementation
}
```

## Test Data Management

### Seed Data
- Initial data loaded via Flyway migrations
- Located in: `src/main/resources/db/migration/`
- Includes: admin user, default roles, permissions, sample tenants

### Test Data Strategy
1. **Unit Tests**: Use mocked data, no database
2. **Integration Tests**: Use seed data + test-specific data
3. **Cleanup**: TestContainers provides fresh database per test class

## Continuous Integration

### CI Pipeline Requirements
1. **Docker Support**: CI environment must support Docker (for Testcontainers)
2. **Java 17+**: Required Java version
3. **Gradle**: Build tool
4. **Memory**: Minimum 2GB for test execution

### Recommended CI Configuration
```yaml
# Example GitHub Actions
- name: Run Tests with TestContainers
  run: ./gradlew clean test
  env:
    TESTCONTAINERS_RYUK_DISABLED: false
```

## Best Practices

### Unit Tests
1. Mock all external dependencies (services, repositories)
2. Use MockMvc for controller testing
3. Test both success and error scenarios
4. Validate request/response structure
5. Use Allure annotations for documentation

### Integration Tests
1. Extend `BaseIntegrationTest` for database access
2. Use `obtainToken()` for authenticated requests
3. Test complete workflows, not just individual operations
4. Verify database state changes
5. Handle cases where seed data may not exist

### Coverage
1. Aim for 100% branch coverage on critical paths
2. Test all error handling branches
3. Test validation logic thoroughly
4. Include edge cases and boundary conditions

### Testcontainers
1. Share container instances across tests in a class (static initialization)
2. Use @DynamicPropertySource for configuration injection
3. Ensure Docker is running before executing integration tests
4. Monitor container lifecycle in CI logs

## Known Limitations

1. **TestContainers Performance**: First test run downloads Docker images (one-time cost)
2. **CI Environment**: Requires Docker support in CI/CD pipeline
3. **Resource Usage**: Integration tests consume more memory than unit tests
4. **PATCH Limitations**: TestRestTemplate has known issues with PATCH method; these paths tested via unit tests

## Future Improvements

1. **Performance Testing**: Add load tests for critical endpoints
2. **Contract Testing**: Implement consumer-driven contract tests
3. **Mutation Testing**: Add PIT mutation testing for coverage quality
4. **E2E Tests**: Selenium/Playwright tests for full system validation
5. **Chaos Engineering**: Testcontainers Toxiproxy for resilience testing

## Troubleshooting

### TestContainers Issues

**Problem**: Tests fail with "Could not find a valid Docker environment"
**Solution**: Ensure Docker Desktop is running

**Problem**: Tests are slow on first run
**Solution**: Docker is downloading PostgreSQL image; subsequent runs will be faster

**Problem**: Port conflicts
**Solution**: Testcontainers automatically assigns random available ports

### Coverage Issues

**Problem**: Coverage report not generated
**Solution**: Run `./gradlew clean test jacocoTestReport`

**Problem**: Low branch coverage
**Solution**: Add tests for error cases, validation failures, and edge conditions

## References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Allure Framework](https://docs.qameta.io/allure/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)

---

**Last Updated**: 2025-11-05
**Maintained By**: Backend Team
**Version**: 1.0
