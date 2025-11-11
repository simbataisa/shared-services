
# Mock Server Runner Guide

## Overview

The `MockRunnerTest` has been successfully refactored to work with Karate 1.5.x API. The test starts a mock server that simulates payment gateway APIs (Stripe, PayPal, Bank Transfer).

## What Was Fixed

### 1. API Migration to Karate 1.5.x
- **Old API** (deprecated): `MockServer.feature().http().build()`
- **New API** (Karate 1.5.x):
  ```java
  Feature feature = Feature.read(featureFile);
  MockHandler handler = new MockHandler(feature);
  HttpServer server = HttpServer.handler(handler).http(port).build();
  ```

### 2. Improved Test Structure
- Added `@BeforeAll` for setup
- Added `@AfterAll` for cleanup
- Added comprehensive logging with SLF4J
- Added assertions to verify server is running
- Added detailed endpoint documentation in logs

### 3. Better Configuration Support
- Configurable port via system property or environment variable
- Configurable runtime duration via `mock.block.ms`
- Support for `karate.env` system property

## How to Use

### Option 1: Run as JUnit Test (Recommended for Testing)

This approach runs the mock server for a short duration and then stops automatically.

```bash
cd karate-microservices-testing

# Run with default settings (10 minutes, port 8090)
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa

# Run with custom port
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.port=9090

# Run for 30 seconds only (useful for quick tests)
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=30000

# Run for 5 minutes
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=300000
```

### Option 2: Run as Standalone Application (For Long-Running Server)

For development/integration testing where you want the mock server to run indefinitely:

```bash
cd karate-microservices-testing

# Compile first
./gradlew compileTestJava

# Run standalone
java -cp "build/classes/java/test:build/resources/test:$(./gradlew -q printTestClasspath)" \
  com.ahss.karate.mocks.MockServerRunner
```

**Note**: This will run until you press Ctrl+C

### Option 3: Run in Background (CI/CD)

For CI/CD pipelines where you need the server to run in the background:

```bash
# Start in background
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=600000 &
MOCK_PID=$!

# Wait for server to start
sleep 5

# Run your integration tests here
cd ../backend
./gradlew test -Dspring.profiles.active=integration

# Kill the mock server when done
kill $MOCK_PID
```

## Configuration

### System Properties

| Property | Default | Description |
|----------|---------|-------------|
| `mock.port` | 8090 | Port to run the mock server on |
| `mock.block.ms` | 600000 | Duration to keep server running (in milliseconds) |
| `karate.env` | qa | Karate environment (qa, dev, prod) |

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MOCK_PORT` | 8090 | Port to run the mock server on |
| `MOCK_BLOCK_MS` | 600000 | Duration to keep server running (in milliseconds) |

**Priority**: System Properties > Environment Variables > Default Values

## Test Output Example

When you run the test, you'll see:

```
=================================================
Setting up Karate Mock Server
=================================================
Karate Environment: qa
Mock Server Port: 8090
Starting mock server with feature: /path/to/mock-server.feature
=================================================
✓ Mock server started successfully
  URL: http://localhost:8090
=================================================
Available Mock Endpoints:
  Stripe:
    - POST http://localhost:8090/v1/tokens
    - POST http://localhost:8090/v1/charges
    - GET  http://localhost:8090/v1/charges/{id}
    - POST http://localhost:8090/v1/refunds

  PayPal:
    - POST http://localhost:8090/v1/oauth2/token
    - POST http://localhost:8090/v2/checkout/orders
    - POST http://localhost:8090/v2/checkout/orders/{id}/capture
    - GET  http://localhost:8090/v2/checkout/orders/{id}

  Bank Transfer:
    - POST http://localhost:8090/api/v1/transfers
    - GET  http://localhost:8090/api/v1/transfers/{id}
    - POST http://localhost:8090/api/v1/accounts/verify
=================================================
Mock server will run for 600000 ms (10 minutes)
Press Ctrl+C to stop the server early
=================================================
```

## Verify Mock Server is Running

In a separate terminal, test the mock server:

```bash
# Test Stripe tokenization endpoint
curl -X POST http://localhost:8090/v1/tokens \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4242424242424242",
    "expiryMonth": "12",
    "expiryYear": "2025",
    "cvv": "123"
  }'

# Expected response:
# {"token":"tok_...","tokenType":"card","success":true}
```

## Use with Backend Integration Tests

Once the mock server is running, start the backend with the integration profile:

```bash
# Terminal 1: Start mock server
cd karate-microservices-testing
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=3600000

# Terminal 2: Start backend
cd ../backend
./gradlew bootRun --args='--spring.profiles.active=integration'

# Terminal 3: Test the integration
curl -X POST http://localhost:8080/api/v1/payments/requests \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Payment",
    "amount": 99.99,
    "currency": "USD",
    "payerEmail": "test@example.com",
    "allowedPaymentMethods": ["CREDIT_CARD"],
    "tenantId": 1
  }'
```

## Troubleshooting

### Port Already in Use

```
Error: Address already in use
```

**Solution**: Change the port or kill the process using port 8090

```bash
# Find process using port 8090
lsof -i :8090

# Kill it
kill -9 <PID>

# Or use different port
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.port=9090
```

### Test Hangs Forever

This is **expected behavior**. The test is designed to keep the server running for the specified duration (`mock.block.ms`).

**Solutions**:
1. Press `Ctrl+C` to stop early
2. Use shorter duration: `-Dmock.block.ms=5000` (5 seconds)
3. Run in background (see Option 3 above)

### Feature File Not Found

```
Error: Feature file not found
```

**Solution**: Make sure you're running from the `karate-microservices-testing` directory:

```bash
cd karate-microservices-testing
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa
```

### ClassNotFoundException

```
Error: Could not find or load main class com.ahss.karate.mocks.MockServerRunner
```

**Solution**: Compile the test classes first:

```bash
./gradlew compileTestJava
```

## Files Modified

1. **`MockRunnerTest.java`**
   - Migrated from `MockServer` to `HttpServer` with `MockHandler`
   - Added proper lifecycle management with `@BeforeAll` and `@AfterAll`
   - Added comprehensive logging and assertions
   - Made configurable via system properties

2. **`MockServerRunner.java`**
   - Migrated to Karate 1.5.x API
   - Added error handling
   - Made compatible with both `Feature.read()` and `MockHandler`

## See Also

- [Mock Server README](src/test/resources/mocks/README.md)
- [Integration Testing Guide](../../INTEGRATION-TESTING-GUIDE.md)
- [Karate Documentation](https://karatelabs.github.io/karate/)
