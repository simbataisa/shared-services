# Payment Gateway Mock Server

This directory contains Karate mock server feature files that simulate external payment gateway APIs for integration testing.

## Overview

The mock server provides realistic API responses for:
- **Stripe** - Credit/Debit card processing
- **PayPal** - Digital wallet payments
- **Bank Transfer** - ACH/Wire transfers

## Mock Server Structure

```
mocks/
├── mock-server.feature              # Main mock server orchestrator
├── payment-gateways/
│   ├── stripe-mock.feature          # Stripe API mock
│   ├── paypal-mock.feature          # PayPal API mock
│   └── bank-transfer-mock.feature   # Bank Transfer API mock
└── README.md                        # This file
```

## Quick Start

### 1. Start the Mock Server

You have three options to start the mock server:

#### Option A: Using Standalone Runner (Recommended for Development)

From the karate-microservices-testing directory:

```bash
java -cp "build/libs/*:build/resources/test" com.ahss.karate.mocks.MockServerRunner
```

#### Option B: Using MockRunnerTest (For Short-Duration Testing)

```bash
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=600000
```

#### Option C: Automatic with CustomRunnerTest (Recommended for Integration Tests)

The CustomRunnerTest can automatically start and stop the mock server when running integration tests:

```bash
# Enable mock server with integration tests
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.env=qa \
  -Dmock.server.enabled=true

# With custom port
./gradlew test --tests "*CustomRunnerTest" \
  -Dkarate.env=qa \
  -Dmock.server.enabled=true \
  -Dmock.port=8090

# Using environment variables
MOCK_SERVER_ENABLED=true MOCK_PORT=8090 ./gradlew test --tests "*CustomRunnerTest"
```

The server will start on `http://localhost:8090` (or the port you specify)

### 2. Start Backend with Integration Profile

From the backend directory:

```bash
./gradlew bootRun --args='--spring.profiles.active=integration'
```

The backend will automatically use mock URLs configured in `application-integration.yml`.

### 3. Test the Integration

```bash
# Create a payment request
curl -X POST http://localhost:8080/api/v1/payments/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Test Payment",
    "amount": 99.99,
    "currency": "USD",
    "tenantId": 1,
    "allowedPaymentMethods": ["CREDIT_CARD"]
  }'

# Process the payment (will use Stripe mock)
curl -X POST http://localhost:8080/api/v1/payments/transactions/process \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "paymentToken": "tok_xxx",
    "paymentMethod": "CREDIT_CARD"
  }'
```

## Mock Endpoints

### Stripe Mock (`stripe-mock.feature`)

#### Tokenize Card
```http
POST http://localhost:8090/v1/tokens
Content-Type: application/json

{
  "cardNumber": "4242424242424242",
  "expiryMonth": "12",
  "expiryYear": "2025",
  "cvv": "123",
  "cardHolderName": "John Doe"
}

Response:
{
  "token": "tok_abc123...",
  "tokenType": "card",
  "success": true
}
```

#### Process Payment (Create Charge)
```http
POST http://localhost:8090/v1/charges
Content-Type: application/json
Authorization: Bearer mock_stripe_key

{
  "token": "tok_abc123...",
  "amount": 99.99,
  "currency": "USD",
  "description": "Test Payment"
}

Response:
{
  "id": "ch_abc123...",
  "status": "AUTHORIZED",
  "amount": 99.99,
  "currency": "USD",
  "success": true
}
```

#### Retrieve Charge
```http
GET http://localhost:8090/v1/charges/{id}
Authorization: Bearer mock_stripe_key
```

#### Create Refund
```http
POST http://localhost:8090/v1/refunds
Content-Type: application/json
Authorization: Bearer mock_stripe_key

{
  "charge": "ch_abc123...",
  "amount": 50.00
}
```

### PayPal Mock (`paypal-mock.feature`)

#### Get OAuth Token
```http
POST http://localhost:8090/v1/oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic <base64(clientId:clientSecret)>

grant_type=client_credentials

Response:
{
  "access_token": "A21AAL...",
  "token_type": "Bearer",
  "expires_in": 32400
}
```

#### Create Order
```http
POST http://localhost:8090/v2/checkout/orders
Content-Type: application/json
Authorization: Bearer A21AAL...

{
  "intent": "CAPTURE",
  "purchase_units": [{
    "amount": {
      "currency_code": "USD",
      "value": "99.99"
    }
  }]
}

Response:
{
  "id": "PAYPAL-ORDER-...",
  "status": "CREATED",
  "links": [...]
}
```

#### Capture Order (Process Payment)
```http
POST http://localhost:8090/v2/checkout/orders/{id}/capture
Content-Type: application/json
Authorization: Bearer A21AAL...

Response:
{
  "id": "PAYPAL-ORDER-...",
  "status": "AUTHORIZED",
  "amount": 99.99,
  "currency": "USD",
  "success": true,
  "externalTransactionId": "CAPTURE-..."
}
```

#### Get Order Details
```http
GET http://localhost:8090/v2/checkout/orders/{id}
Authorization: Bearer A21AAL...
```

#### Refund Capture
```http
POST http://localhost:8090/v2/payments/captures/{id}/refund
Content-Type: application/json
Authorization: Bearer A21AAL...

{
  "amount": {
    "currency_code": "USD",
    "value": "50.00"
  }
}
```

### Bank Transfer Mock (`bank-transfer-mock.feature`)

#### Initiate Transfer
```http
POST http://localhost:8090/api/v1/transfers
Content-Type: application/json
Authorization: Bearer mock_bank_api_key

{
  "amount": 500.00,
  "currency": "USD",
  "accountNumber": "12345678",
  "routingNumber": "021000021",
  "accountHolderName": "Jane Smith",
  "bankName": "Test Bank"
}

Response:
{
  "id": "BT-...",
  "status": "PENDING",
  "amount": 500.00,
  "currency": "USD",
  "success": true,
  "externalTransactionId": "REF-...",
  "message": "Bank transfer initiated successfully"
}
```

#### Check Transfer Status
```http
GET http://localhost:8090/api/v1/transfers/{id}
Authorization: Bearer mock_bank_api_key

Response:
{
  "id": "BT-...",
  "referenceNumber": "REF-...",
  "status": "PROCESSING",  // Progresses: PENDING -> PROCESSING -> COMPLETED
  "amount": 500.00,
  "currency": "USD",
  "bankName": "Test Bank",
  "createdAt": "2025-11-10T00:00:00Z"
}
```

#### Verify Account
```http
POST http://localhost:8090/api/v1/accounts/verify
Content-Type: application/json
Authorization: Bearer mock_bank_api_key

{
  "accountNumber": "12345678",
  "routingNumber": "021000021"
}

Response:
{
  "valid": true,
  "accountNumber": "12345678",
  "routingNumber": "021000021",
  "bankName": "Test Bank",
  "accountType": "CHECKING"
}
```

#### Cancel Transfer
```http
POST http://localhost:8090/api/v1/transfers/{id}/cancel
Authorization: Bearer mock_bank_api_key
```

## Configuration

### Backend Configuration

The backend uses the `integration` profile for mock testing. Configuration is in:
```
backend/src/main/resources/application-integration.yml
```

Key properties:
```yaml
payment:
  gateways:
    mock:
      enabled: true
      baseUrl: http://localhost:8090

    stripe:
      tokenizationApiUrl: http://localhost:8090/v1/tokens
      paymentApiUrl: http://localhost:8090/v1/charges
      apiKey: mock_stripe_key

    paypal:
      baseUrl: http://localhost:8090
      clientId: mock_paypal_client_id
      clientSecret: mock_paypal_secret

    bankTransfer:
      baseUrl: http://localhost:8090
      apiKey: mock_bank_api_key
```

### Environment Variables

You can override defaults using environment variables:

```bash
export MOCK_SERVER_URL=http://localhost:8090
export STRIPE_MOCK_URL=http://localhost:8090
export PAYPAL_MOCK_URL=http://localhost:8090
export BANK_TRANSFER_MOCK_URL=http://localhost:8090
```

## Mock Behavior

### Success Scenarios
- All mocks return successful responses by default
- Stripe: Returns `status: "AUTHORIZED"`, `success: true`
- PayPal: Returns `status: "AUTHORIZED"`, captures complete successfully
- Bank Transfer: Returns `status: "PENDING"`, progresses to `COMPLETED` on subsequent checks

### Error Scenarios

#### Missing Authorization
If Authorization header is missing or invalid:
```http
401 Unauthorized
{
  "error": {
    "message": "Missing or invalid authentication token"
  }
}
```

#### Invalid Resource
If requesting non-existent resource:
```http
404 Not Found
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Resource not found"
  }
}
```

### Stateful Behavior
- Tokens, orders, transfers are stored in memory during the mock server session
- Restart the server to reset state
- Bank Transfer status progresses: PENDING → PROCESSING → COMPLETED

## Testing with Karate

You can also call the mocks directly from Karate tests:

```gherkin
Feature: Payment Integration Test

Background:
  * url mockServerUrl
  * def mockServerUrl = 'http://localhost:8090'

Scenario: Create Stripe payment
  Given path '/v1/charges'
  And request { token: 'tok_test', amount: 100, currency: 'USD' }
  And header Authorization = 'Bearer mock_stripe_key'
  When method post
  Then status 200
  And match response.success == true
  And match response.status == 'AUTHORIZED'
```

## Troubleshooting

### Mock server not starting
- Check if port 8090 is already in use: `lsof -i :8090`
- Change port in `MockServerRunner.java` if needed

### Backend not connecting to mock
- Verify mock server is running: `curl http://localhost:8090/v1/tokens`
- Check backend profile: `--spring.profiles.active=integration`
- Check logs for connection errors

### Mock returns unexpected responses
- Check request format matches examples above
- Verify Content-Type headers
- Check Authorization headers if required

## Extending the Mocks

To add new mock scenarios:

1. Edit the appropriate feature file (`stripe-mock.feature`, etc.)
2. Add a new `Scenario:` with matching conditions
3. Define the response structure
4. Restart the mock server

Example:
```gherkin
# In stripe-mock.feature
Scenario: pathMatches('/v1/charges') && request.amount > 10000
  * def response = { error: { code: 'amount_too_large', message: 'Amount exceeds limit' } }
  * def responseStatus = 400
```

## Production vs Mock

| Feature | Mock | Production |
|---------|------|------------|
| Auth | Optional/Mock keys | Real API keys required |
| Rate Limits | None | Applies |
| Webhooks | Not supported | Supported |
| Network Latency | ~1ms | ~100-500ms |
| Error Scenarios | Configurable | Real errors |
| Cost | Free | Transaction fees |

## See Also

- [Karate Documentation](https://karatelabs.github.io/karate/)
- [Stripe API Docs](https://stripe.com/docs/api)
- [PayPal API Docs](https://developer.paypal.com/api/rest/)
