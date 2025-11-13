# CustomRunnerTest with Mock Server Integration

## Overview

The `CustomRunnerTest` has been enhanced to support automatic mock server lifecycle management. This allows you to run integration tests with the mock server automatically started before tests and stopped after tests complete.

## Features

- **Automatic Mock Server Management**: Mock server starts before tests and stops after all tests finish
- **Optional Activation**: Mock server is disabled by default and can be enabled via system properties or environment variables
- **Configurable Port**: Customize the mock server port
- **No Manual Management**: No need to manually start/stop the mock server in separate terminals

## Usage

### Basic Usage (Mock Server Disabled)

By default, the mock server is **disabled** and tests run without it:

```bash
./gradlew test --tests "*CustomRunnerTest" -Dkarate.env=qa
```

You'll see:
```
Mock server is disabled. Set mock.server.enabled=true or MOCK_SERVER_ENABLED=true to enable it.
```

### Enable Mock Server

Enable the mock server using system properties:

```bash
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.env=qa \
  -Dmock.server.enabled=true
```

### Custom Mock Server Port

Specify a custom port (default is 8090):

```bash
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.env=qa \
  -Dmock.server.enabled=true \
  -Dmock.port=8090
```

### Using Environment Variables

You can also use environment variables:

```bash
MOCK_SERVER_ENABLED=true MOCK_PORT=8090 ./gradlew test --tests "*CustomRunnerTest" -Dkarate.env=qa
```

### Run Specific Tests with Mock Server

```bash
# Run specific feature file with mock server
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.env=qa \
  -Dmock.server.enabled=true \
  -Dkarate.options="classpath:integration/payment-end-to-end-success.feature"

# Run with specific tags
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.env=qa \
  -Dmock.server.enabled=true \
  -Dkarate.options="--tags @smoke"
```

### Rerun Tests Without Cache

Gradle may cache test tasks and skip execution on subsequent runs. Force a full re-run and disable the build cache:

```bash
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.options="classpath:integration" \
  -Dkarate.env=qa \
  --info \
  --no-build-cache \
  --rerun-tasks
```

## Configuration Options

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `mock.server.enabled` | `MOCK_SERVER_ENABLED` | `false` | Enable/disable mock server |
| `mock.port` | `MOCK_PORT` | `8090` | Mock server port |
| `karate.env` | - | `qa` | Karate environment |

**Priority**: System Properties > Environment Variables > Default Values

## Mock Server Output

When enabled, you'll see output like:

```
=================================================
Starting Karate Mock Server
=================================================
Mock Server Port: 8090
✓ Mock server started successfully
  URL: http://localhost:8090
=================================================
Available Mock Endpoints:
  Stripe (prefix: /stripe):
    - POST http://localhost:8090/stripe/v1/tokens
    - POST http://localhost:8090/stripe/v1/charges
  PayPal (prefix: /paypal):
    - POST http://localhost:8090/paypal/v1/oauth2/token
    - POST http://localhost:8090/paypal/v2/checkout/orders
  Bank Transfer (prefix: /bank-transfer):
    - POST http://localhost:8090/bank-transfer/api/v1/transfers
=================================================

... running tests ...

=================================================
Stopping mock server...
✓ Mock server stopped
=================================================
```

## Use Cases

### 1. Local Development

Run integration tests locally with mock server:

```bash
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.env=qa \
  -Dmock.server.enabled=true
```

### 2. CI/CD Pipeline

Add to your CI/CD pipeline (e.g., `.github/workflows/integration-tests.yml`):

```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'

      - name: Start PostgreSQL and Kafka
        run: docker-compose up -d postgres kafka

      - name: Wait for services
        run: sleep 10

      - name: Start Backend
        run: |
          cd backend
          ./gradlew bootRun --args='--spring.profiles.active=integration' &
          sleep 15

      - name: Run Integration Tests with Mock Server
        run: |
          cd karate-microservices-testing
          ./gradlew test --tests "*CustomRunnerTest" \
            -Dkarate.env=qa \
            -Dmock.server.enabled=true
```

### 3. Manual Testing

For manual testing where you want the backend running separately:

**Terminal 1** - Start mock server standalone:
```bash
cd karate-microservices-testing
java -cp "build/libs/*:build/resources/test" com.ahss.karate.mocks.MockServerRunner
```

**Terminal 2** - Start backend:
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=integration'
```

**Terminal 3** - Run tests without built-in mock server:
```bash
cd karate-microservices-testing
./gradlew test --tests "*CustomRunnerTest" -Dkarate.env=qa
# Mock server already running, so don't enable it
```

## Comparison: CustomRunnerTest vs MockRunnerTest

| Feature | CustomRunnerTest | MockRunnerTest |
|---------|-----------------|----------------|
| Primary Purpose | Run Karate integration tests | Run mock server for testing |
| Mock Server | Optional, managed automatically | Always runs, blocks for specified duration |
| Use Case | Integration testing | Standalone mock server |
| Lifecycle | Starts/stops with tests | Runs for fixed duration |
| Best For | CI/CD pipelines, automated testing | Development, manual testing |

## Troubleshooting

### Port Already in Use

If you get "Address already in use" error:

```bash
# Find and kill process using port 8090
lsof -i :8090
kill -9 <PID>

# Or use a different port
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.env=qa \
  -Dmock.server.enabled=true \
  -Dmock.port=9090
```

### Mock Server Feature File Not Found

Ensure you're running from the correct directory:

```bash
cd karate-microservices-testing
./gradlew test --tests "*CustomRunnerTest" -Dmock.server.enabled=true
```

### Tests Fail to Connect to Mock Server

Check if the backend is configured correctly in `application-integration.yml`:

```yaml
payment:
  gateways:
    stripe:
      tokenizationApiUrl: http://localhost:8090/stripe/v1/tokens
      paymentApiUrl: http://localhost:8090/stripe/v1/charges
```

## Implementation Details

The `CustomRunnerTest` uses JUnit 5 lifecycle annotations:

- `@BeforeAll` - Starts mock server before any tests run (if enabled)
- `@AfterAll` - Stops mock server after all tests complete
- `@Karate.Test` - Runs the actual Karate tests

The mock server lifecycle is managed automatically, ensuring clean startup and shutdown.

## See Also

- [Mock Server README](src/test/resources/mocks/README.md)
- [Mock Server Runner Guide](MOCK-SERVER-RUNNER-GUIDE.md)
- [Integration Testing Guide](../INTEGRATION-TESTING-GUIDE.md)
