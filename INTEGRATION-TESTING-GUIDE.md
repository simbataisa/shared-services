# Integration Testing Guide with Mock Payment Gateways

This guide explains how to run the backend service with mock payment gateways for integration testing without calling real Stripe, PayPal, or Bank Transfer APIs.

## Overview

The integration testing setup includes:
- **Karate Mock Server** - Simulates payment gateway APIs (Stripe, PayPal, Bank Transfer)
- **Backend Integration Profile** - Configures backend to use mock URLs
- **PostgreSQL Database** - Real database from docker-compose.yml
- **Kafka** - Real Kafka broker from docker-compose.yml

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Integration Test Setup                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐    HTTP      ┌────────────────────────┐   │
│  │   Backend    │─────────────>│   Karate Mock Server   │   │
│  │ (Port 8080)  │              │     (Port 8090)        │   │
│  │              │<─────────────│                        │   │
│  │              │    Mocked    │  • Stripe Mock         │   │
│  │              │   Responses  │  • PayPal Mock         │   │
│  └──────────────┘              │  • Bank Transfer Mock  │   │
│         │                      └────────────────────────┘   │
│         │                                                    │
│         ├─> PostgreSQL (Docker)                             │
│         └─> Kafka (Docker)                                  │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## Quick Start

### Step 0: Start PostgreSQL and Kafka

From the root directory, start the required Docker services:

```bash
docker-compose up -d postgres kafka
```

Verify the services are running:

```bash
docker ps --filter "name=sharedservices-postgres"
docker ps --filter "name=sharedservices-kafka"
```

### Step 1: Start the Karate Mock Server

From the `karate-microservices-testing` directory:

```bash
cd karate-microservices-testing

# Option A: Using Java
java -cp "build/libs/*:build/resources/test" com.ahss.karate.mocks.MockServerRunner

# Option B: Using Gradle (add mockServer task to build.gradle)
./gradlew mockServer
```

You should see:
```
=================================================
Starting Karate Mock Server for Payment Gateways
=================================================
Port: 8090
...
Available Mock Endpoints:
  Stripe (prefix: /stripe):
    - POST http://localhost:8090/stripe/v1/tokens (Tokenization)
    - POST http://localhost:8090/stripe/v1/charges (Process Payment)
  PayPal (prefix: /paypal):
    - POST http://localhost:8090/paypal/v1/oauth2/token
    - POST http://localhost:8090/paypal/v2/checkout/orders
  Bank Transfer (prefix: /bank-transfer):
    - POST http://localhost:8090/bank-transfer/api/v1/transfers
  ...
=================================================
```

### Step 2: Start the Backend with Integration Profile

From the `backend` directory:

```bash
cd backend

# Option 1: Using --args (Recommended)
./gradlew bootRun --args='--spring.profiles.active=integration'

# Option 2: Using JVM system property
./gradlew bootRun -Dspring.profiles.active=integration

# Option 3: With OpenTelemetry agent
./gradlew bootRunWithAgent --args='--spring.profiles.active=integration'

# Option 4: Using environment variable (set before running)
SPRING_PROFILES_ACTIVE=integration ./gradlew bootRun
```

**Note**: Do NOT set `spring.profiles.active` in the `application-integration.yml` file itself - this will cause an error. The profile must be activated externally.

The backend will:
- Connect to PostgreSQL database (localhost:5432/sharedservices)
- Connect to Kafka broker (localhost:9092)
- Route all payment gateway calls to http://localhost:8090 (mock server)

### Step 3: Test the Integration

#### 3.1 Login to get JWT token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Save the token
export TOKEN="<token_from_response>"
```

#### 3.2 Create a payment request

```bash
curl -X POST http://localhost:8080/api/v1/payments/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "title": "Test Payment #1",
    "amount": 99.99,
    "currency": "USD",
    "payerName": "Jane Doe",
    "payerEmail": "jane@example.com",
    "allowedPaymentMethods": ["CREDIT_CARD"],
    "preSelectedPaymentMethod": "CREDIT_CARD",
    "tenantId": 1,
    "metadata": {
      "source": "integration-test"
    }
  }'

# Save the payment token from response
export PAYMENT_TOKEN="<token_from_response>"
```

#### 3.3 Process the payment (will use Stripe mock)

```bash
curl -X POST http://localhost:8080/api/v1/payments/transactions/process \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "paymentToken": "'$PAYMENT_TOKEN'",
    "paymentMethod": "CREDIT_CARD",
    "paymentMethodDetails": {
      "cardNumber": "4242424242424242",
      "expiryMonth": "12",
      "expiryYear": "2025",
      "cvv": "123",
      "cardHolderName": "Jane Doe"
    }
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "id": "...",
    "status": "SUCCESS",
    "amount": 99.99,
    "currency": "USD",
    "gatewayName": "Stripe",
    "message": "Payment processed successfully"
  }
}
```

## Configuration Files

### 1. Backend Integration Profile

**File:** `backend/src/main/resources/application-integration.yml`

Key settings:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sharedservices
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  flyway:
    enabled: true
    baseline-on-migrate: true

  kafka:
    bootstrap-servers: localhost:9092

payment:
  gateways:
    stripe:
      tokenizationApiUrl: http://localhost:8090/stripe/v1/tokens
      paymentApiUrl: http://localhost:8090/stripe/v1/charges
      apiKey: mock_stripe_key

    paypal:
      baseUrl: http://localhost:8090/paypal
      tokenUrl: http://localhost:8090/paypal/v1/oauth2/token
      ordersUrl: http://localhost:8090/paypal/v2/checkout/orders
      clientId: mock_paypal_client_id
      clientSecret: mock_paypal_secret

    bankTransfer:
      baseUrl: http://localhost:8090/bank-transfer
      transfersUrl: http://localhost:8090/bank-transfer/api/v1/transfers
      verifyUrl: http://localhost:8090/bank-transfer/api/v1/accounts/verify
      apiKey: mock_bank_api_key
```

### 2. Karate Mock Features

**Main orchestrator:** `karate-microservices-testing/src/test/resources/mocks/mock-server.feature`

**Gateway-specific mocks:**
- `mocks/payment-gateways/stripe-mock.feature`
- `mocks/payment-gateways/paypal-mock.feature`
- `mocks/payment-gateways/bank-transfer-mock.feature`

## Mock Gateway Behaviors

### Stripe Mock

- **Tokenization:** Always returns success with generated token
- **Charge:** Returns `AUTHORIZED` status with transaction ID
- **Refund:** Returns `REFUNDED` status
- **Auth:** Checks for `Authorization: Bearer` header (mock_stripe_key)

### PayPal Mock

- **OAuth:** Returns access token valid for 32400 seconds
- **Create Order:** Returns order ID with `CREATED` status
- **Capture:** Returns `AUTHORIZED` status with capture ID
- **Refund:** Returns `REFUNDED` status with refund ID

### Bank Transfer Mock

- **Initiate:** Returns `PENDING` status
- **Check Status:** Progresses through PENDING → PROCESSING → COMPLETED
- **Verify Account:** Validates account number length (min 8 digits)
- **Cancel:** Only works if status is PENDING or PROCESSING

## Environment Variables

You can customize mock server and backend behavior:

```bash
# Mock Server
export MOCK_SERVER_URL=http://localhost:8090

# Individual Gateway URLs
export STRIPE_MOCK_URL=http://localhost:8090
export PAYPAL_MOCK_URL=http://localhost:8090
export BANK_TRANSFER_MOCK_URL=http://localhost:8090

# Database
export INTEGRATION_DB_URL=jdbc:h2:mem:testdb

# Kafka
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Running Integration Tests with Karate

### Full End-to-End Test

```bash
cd karate-microservices-testing

# Run the payment end-to-end test
./gradlew test --tests "PaymentE2ETest"
```

### Custom Karate Test

```gherkin
Feature: Payment Integration Test

Background:
  * url 'http://localhost:8080'
  * def mockUrl = 'http://localhost:8090'

  # Login
  * path '/api/v1/auth/login'
  * request { username: 'admin', password: 'admin123' }
  * method post
  * status 200
  * def token = response.data.token

Scenario: Process credit card payment successfully
  # Create payment request
  * path '/api/v1/payments/requests'
  * header Authorization = 'Bearer ' + token
  * request
    """
    {
      "title": "Test Payment",
      "amount": 50.00,
      "currency": "USD",
      "payerEmail": "test@example.com",
      "allowedPaymentMethods": ["CREDIT_CARD"],
      "tenantId": 1
    }
    """
  * method post
  * status 201
  * def paymentToken = response.paymentToken

  # Process payment (will use Stripe mock)
  * path '/api/v1/payments/transactions/process'
  * header Authorization = 'Bearer ' + token
  * request
    """
    {
      "paymentToken": "#(paymentToken)",
      "paymentMethod": "CREDIT_CARD"
    }
    """
  * method post
  * status 201
  * match response.success == true
  * match response.data.status == 'SUCCESS'
```

## Debugging

### Check Mock Server Logs

The mock server outputs all requests it receives:

```
2025-11-10 00:30:15.123  INFO  Request received:
127.0.0.1 - POST /v1/charges

Accept: [application/json]
Content-Type: [application/json]
Authorization: [Bearer mock_stripe_key]
...
```

### Check Backend Logs

Enable debug logging for integrators:

```yaml
logging:
  level:
    com.ahss.integration: TRACE
    org.springframework.web.client.RestTemplate: DEBUG
```

### Verify Mock Server is Running

```bash
# Check if Stripe mock is up
curl http://localhost:8090/stripe/v1/tokens -v

# Check if PayPal mock is up
curl http://localhost:8090/paypal/v1/oauth2/token -v

# Check if Bank Transfer mock is up
curl http://localhost:8090/bank-transfer/api/v1/transfers -v

# Should return 200 or 400 (not connection refused)
```

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Connection refused to localhost:8090 | Mock server not running | Start MockServerRunner |
| 401 Unauthorized from mock | Missing auth header | Check stripe.apiKey in config |
| Backend uses real Stripe | Wrong profile | Ensure `--spring.profiles.active=integration` |
| Connection refused to PostgreSQL | Docker containers not running | Run `docker-compose up -d postgres kafka` |
| Tests fail with NPE | Missing test data | Create users/tenants via Flyway migrations or API |

## Production vs Integration Profiles

| Aspect | Production (`application.yml`) | Integration (`application-integration.yml`) |
|--------|-------------------------------|---------------------------------------------|
| Database | PostgreSQL (production) | PostgreSQL (Docker) |
| Kafka | Kafka cluster (production) | Kafka (Docker) |
| Stripe | https://api.stripe.com | http://localhost:8090 (mock) |
| PayPal | https://api.paypal.com | http://localhost:8090 (mock) |
| Bank Transfer | Real bank API | http://localhost:8090 (mock) |
| Flyway | Enabled | Enabled |
| Tracing | Enabled (Jaeger) | Disabled |

## CI/CD Integration

To run in CI pipelines:

```yaml
# .github/workflows/integration-tests.yml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      # Start mock server in background
      - name: Start Mock Server
        run: |
          cd karate-microservices-testing
          java -cp "build/libs/*:build/resources/test" \
            com.ahss.karate.mocks.MockServerRunner &
          sleep 5

      # Run backend with integration profile
      - name: Run Integration Tests
        run: |
          cd backend
          ./gradlew test -Dspring.profiles.active=integration
```

## Advanced Topics

### Custom Mock Responses

Edit the gateway mock features to add custom scenarios:

```gherkin
# In stripe-mock.feature
Scenario: pathMatches('/v1/charges') && request.amount > 10000
  * def response = {
      error: {
        code: 'amount_too_large',
        message: 'Amount exceeds merchant limit'
      }
    }
  * def responseStatus = 400
```

### Simulating Network Delays

Add delays to mock responses:

```gherkin
Scenario: pathMatches('/v1/charges') && methodIs('post')
  * karate.pause(2000)  # 2 second delay
  * def response = { ... }
```

### State Management

Mock servers maintain in-memory state:

```javascript
// In Background section
* def transactions = {}

// In scenario
* eval transactions[txnId] = txnData
* def existingTxn = transactions[txnId]
```

## See Also

- [Mock Server README](karate-microservices-testing/src/test/resources/mocks/README.md)
- [Backend Integration Profile](backend/src/main/resources/application-integration.yml)
- [Karate Documentation](https://karatelabs.github.io/karate/)
- [Payment Integration Context](../.trae/documents/payment-integration-context.md)
