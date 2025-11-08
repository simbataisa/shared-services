# Webhook Context: Controllers, Parsers, Mapper, Kafka, Orchestrator

## Overview

- Standardizes inbound payment webhooks (Stripe, PayPal) and converts them into a canonical `PaymentCallbackEvent` consumed by the payment orchestrator.
- Shared controller base centralizes common HTTP handling and event dispatch. Parser layer translates raw payloads. Mapper normalizes event types. Kafka bridges controllers and saga orchestration.

## Components

### Webhook Controllers
- `com.ahss.integration.webhook.BaseWebhookController` - Abstract base controller with template method pattern
- `com.ahss.integration.stripe.StripeWebhookController` - Stripe webhook endpoint at `/api/integrations/webhooks/stripe`
- `com.ahss.integration.paypal.PayPalWebhookController` - PayPal webhook endpoint at `/api/integrations/webhooks/paypal`
- `com.ahss.integration.bank.BankTransferWebhookController` - Bank Transfer webhook endpoint at `/api/integrations/webhooks/bank-transfer`

### Message Parser Layer
- `com.ahss.integration.MessageParser` - Strategy interface with `supports(JsonNode)` and `parse(JsonNode)` methods
- `com.ahss.integration.MessageParserFactory` - Factory with static `forPayload(JsonNode)` method
- `com.ahss.integration.stripe.StripeMessageParser` - Handles Stripe payloads (identifies by `type` + `data` fields)
- `com.ahss.integration.paypal.PayPalMessageParser` - Handles PayPal payloads (identifies by `event_type` + `resource` fields)
- `com.ahss.integration.bank.BankTransferMessageParser` - Handles Bank Transfer payloads (identifies by `status` field)

### Event Type Mapping
- `com.ahss.integration.mapper.PaymentChannelIntegrationEventTypeMapper` - Central mapper using `EnumMap` for performance
- `com.ahss.integration.stripe.StripeWebhookEventType` - Enum for Stripe event types with `fromValue(String)` factory
- `com.ahss.integration.paypal.PayPalWebhookEventType` - Enum for PayPal event types with `fromValue(String)` factory
- `com.ahss.integration.bank.BankTransferWebhookEventType` - Enum for Bank Transfer event types with `toCallbackType()` method

### Payment Integrators (Outbound)
- `com.ahss.integration.PaymentIntegrator` - Interface for outbound payment operations (initiate, refund, tokenize)
- `com.ahss.integration.PaymentIntegratorFactory` - Spring-managed factory using `List<PaymentIntegrator>` injection
- `com.ahss.integration.stripe.StripeIntegrator` - Stripe payment operations via REST API
- `com.ahss.integration.paypal.PayPalIntegrator` - PayPal payment operations via REST API
- `com.ahss.integration.bank.BankTransferIntegrator` - Bank transfer operations

### Response Adaptation
- `com.ahss.integration.PaymentResponseAdapter` - Converts `PaymentResponseDto` (outbound) to `PaymentCallbackEvent` (canonical)

### Kafka Components
- Consumers: `com.ahss.kafka.consumer.PaymentCallbackConsumer` - Consumes from `payment-callbacks` topic
- Producers:
  - `com.ahss.kafka.producer.PaymentCallbackProducer` - Publishes `PaymentCallbackEvent` to Kafka
  - `com.ahss.kafka.producer.PaymentEventProducer` - Publishes domain events

### Orchestration
- `com.ahss.saga.PaymentSagaOrchestrator` - Saga coordinator for multi-step payment workflows

## Webhook Controllers Implementation

### `BaseWebhookController` (Template Method Pattern)

**Location**: `com.ahss.integration.webhook.BaseWebhookController`

**Core Pipeline** (`handleWebhook` method):
```java
protected ResponseEntity<ApiResponse<Void>> handleWebhook(
    String body, Map<String, String> headers, String path, String successMessage)
```

**Pipeline Steps**:
1. Parse JSON body to `JsonNode` via `ObjectMapper`
2. Extract event type via `extractEventType(JsonNode)` hook
3. Create `PaymentCallbackEvent` skeleton
4. Map event type via `mapEventType(String)` hook → `PaymentCallbackType`
5. Set gateway name, received timestamp
6. Populate event fields via `populateEvent(event, root)` hook
7. Attach metadata via `metadata(root, headers)` hook
8. Attach gateway response via `attachGatewayResponse(event, root)` (full payload as Map)
9. Send to Kafka via `callbackProducer.send(event)`
10. Return HTTP 200 with `ApiResponse.ok()`
11. On error: Return HTTP 400 with error details

**Utility Methods**:
- `text(JsonNode)` - Safe text extraction with null handling
- `parseUuid(JsonNode)` - UUID parsing with exception handling
- `minorUnitsToMajor(long)` - Converts cents to dollars (divides by 100)
- `toMap(JsonNode)` - Converts JsonNode to `Map<String, Object>` using `TypeReference`

**Abstract Hooks** (implemented by subclasses):
- `String gatewayName()` - Returns gateway identifier
- `String extractEventType(JsonNode)` - Extracts event type field from payload
- `PaymentCallbackType mapEventType(String)` - Maps raw type to canonical type
- `void populateEvent(PaymentCallbackEvent, JsonNode)` - Extracts gateway-specific fields
- `Map<String, Object> metadata(JsonNode, Map<String, String>)` - Builds metadata from payload and headers

### `StripeWebhookController`

**Endpoint**: `POST /api/integrations/webhooks/stripe`
**Header**: `Stripe-Signature` (optional)

**Implementation Details**:
- **Event Type Extraction**: `root.get("type")` (e.g., `payment_intent.succeeded`)
- **Data Path**: `root.path("data").path("object")` contains payment details
- **Metadata Extraction**: `dataObject.path("metadata")` contains custom fields:
  - `correlationId`, `paymentToken`, `requestCode`
  - `paymentRequestId`, `paymentTransactionId`, `paymentRefundId` (as UUIDs)
- **Amount Handling**: Tries `amount_received` first, falls back to `amount` (both in minor units)
- **Currency**: Extracted from data object
- **Refund ID**: Extracts from `dataObject.path("refunds").path("data")[0].get("id")`
- **Error Handling**: Checks for `last_payment_error.code`/`message`, falls back to `failure_code`/`failure_message`
- **Metadata**: Stores `stripeSignature` from request header

### `PayPalWebhookController`

**Endpoint**: `POST /api/integrations/webhooks/paypal`
**Headers**: `PayPal-Transmission-Sig`, `PayPal-Transmission-Id` (optional)

**Implementation Details**:
- **Event Type Extraction**: `root.get("event_type")` (e.g., `PAYMENT.CAPTURE.COMPLETED`)
- **Data Path**: `root.path("resource")` contains transaction details
- **Amount Handling**: `resource.path("amount").get("value")` as decimal string, `currency_code`
- **Correlation**: Uses `custom_id` for correlation, `invoice_id` for payment token
- **Timestamp**: Parses ISO 8601 `create_time` field
- **Error Handling**: Extracts `resource.get("reason")` for denied payments
- **Metadata**: Stores `transmissionSig` and `transmissionId` from headers

### `BankTransferWebhookController`

**Endpoint**: `POST /api/integrations/webhooks/bank-transfer`
**Headers**: `X-Bank-Signature`, `X-Bank-Request-Id` (optional)

**Implementation Details**:
- **Event Type Extraction**: Multi-strategy: tries `event_type`, then `status`, then `event`
- **Flexible Amount Parsing**:
  - If `amount` is number: direct conversion
  - If `amount` is object: extracts `amount.value` as string
- **Currency**: Tries `currency` field, falls back to `amount.currency`
- **Transaction ID**: Tries `transaction_id`, falls back to `id`
- **Error Handling**: Extracts `error.code` and `error.message` when present
- **Metadata Structure**: Nested `headers` map plus `reference` field and `rawEventType`
- **Event Type Mapping**: Uses `BankTransferWebhookEventType` enum with `toCallbackType()` method

## Parser Layer Implementation

**Purpose**: Convert raw webhook payloads into canonical `PaymentCallbackEvent` outside of HTTP context (e.g., when consuming from Kafka topics).

### `MessageParser` Interface

**Location**: `com.ahss.integration.MessageParser`

```java
boolean supports(JsonNode root);  // Determines if this parser handles the payload
PaymentCallbackEvent parse(JsonNode root);  // Parses payload to canonical event
```

### `MessageParserFactory`

**Location**: `com.ahss.integration.MessageParserFactory`

**Implementation**:
- Static list of parsers: `StripeMessageParser`, `PayPalMessageParser`, `BankTransferMessageParser`
- Factory method: `MessageParser forPayload(JsonNode root)`
- Selection Strategy: Iterates parsers, returns first where `supports(root)` is true
- Returns `null` if no parser matches (graceful degradation)

### `StripeMessageParser`

**Support Detection**: Payload has both `type` and `data` fields

**Parsing Logic**:
- **Correlation ID**: Uses `root.get("id")` (Stripe event ID)
- **Event Type**: Extracted from `root.get("type")`
- **Data Object**: Navigates to `root.path("data").path("object")`
- **Amount**: Tries `amount_received`, falls back to `amount` (converts minor units to major)
- **Currency**: From `object.get("currency")`
- **External TX ID**: From `object.get("id")`
- **Timestamp**: Converts Unix epoch `root.get("created")` to `LocalDateTime` (UTC)
- **Error Handling**: Extracts `object.path("last_payment_error").get("code"/"message")`
- **Refund Handling**: Extracts first refund ID from `object.path("refunds").path("data")[0].get("id")`
- **Gateway Response**: Stores full payload as nested Map using recursive `jsonNodeToJava()` conversion
- **Type Mapping**: Uses `PaymentChannelIntegrationEventTypeMapper.mapStripe()`, defaults to `PAYMENT_FAILED` if null

**Utilities**: Custom `text()`, `longVal()`, `toMap()`, and `jsonNodeToJava()` methods for safe parsing

### `PayPalMessageParser`

**Support Detection**: Payload has both `event_type` and `resource` fields

**Parsing Logic**:
- **Event Type**: Extracted from `root.get("event_type")`
- **Data Object**: Navigates to `root.path("resource")`
- **Correlation ID**: From `resource.get("custom_id")` or `resource.get("id")`
- **Amount**: Parses `resource.path("amount").get("value")` as `BigDecimal` string
- **Currency**: From `resource.path("amount").get("currency_code")`
- **Timestamp**: Parses ISO 8601 `resource.get("create_time")` to `LocalDateTime`
- **Error Handling**: Extracts `resource.get("reason_code"/"reason")` for failures
- **Type Mapping**: Uses `PaymentChannelIntegrationEventTypeMapper.mapPayPal()`

### `BankTransferMessageParser`

**Support Detection**: Payload has `status` field (with or without `event_type`)

**Parsing Logic**:
- **Event Type**: Multi-field extraction (tries `event_type`, `status`, `event`)
- **Amount**: Flexible parsing (handles numeric or object with `value` field)
- **Currency**: Tries top-level `currency`, falls back to `amount.currency`
- **Transaction ID**: From `transaction_id` or `id`
- **Error**: Extracts `error.code` and `error.message` when present
- **Type Mapping**: Uses `BankTransferWebhookEventType.fromValue().toCallbackType()`

## Event Type Mapper Implementation

### `PaymentChannelIntegrationEventTypeMapper`

**Location**: `com.ahss.integration.mapper.PaymentChannelIntegrationEventTypeMapper`

**Architecture**: Utility class with static methods and `EnumMap` for O(1) lookup performance

**Static Maps**:
- `PAYPAL_MAP`: `EnumMap<PayPalWebhookEventType, PaymentCallbackType>`
- `STRIPE_MAP`: `EnumMap<StripeWebhookEventType, PaymentCallbackType>`

**Stripe Mappings** (via `mapStripe(String eventType)`):
- `PAYMENT_INTENT_SUCCEEDED` → `PAYMENT_SUCCESS`
- `PAYMENT_INTENT_PAYMENT_FAILED` → `PAYMENT_FAILED`
- `PAYMENT_INTENT_CANCELED` → `PAYMENT_FAILED`
- `CHARGE_FAILED` → `PAYMENT_FAILED`
- `CHARGE_REFUNDED` → `REFUND_SUCCESS`
- `REFUND_CREATED` → `REFUND_SUCCESS`
- `REFUND_UPDATED` → `REFUND_FAILED`
- Unknown types → `PAYMENT_SUCCESS` (optimistic default)

**PayPal Mappings** (via `mapPayPal(String eventType)`):
- `PAYMENT_SALE_COMPLETED`, `CHECKOUT_ORDER_APPROVED`, `PAYMENT_CAPTURE_COMPLETED` → `PAYMENT_SUCCESS`
- `PAYMENT_SALE_DENIED`, `PAYMENT_CAPTURE_DENIED`, `PAYMENT_CAPTURE_FAILED` → `PAYMENT_FAILED`
- `PAYMENT_SALE_REFUNDED`, `PAYMENT_CAPTURE_REFUNDED`, `PAYMENT_REFUND_COMPLETED` → `REFUND_SUCCESS`
- `PAYMENT_REFUND_DENIED` → `REFUND_FAILED`
- Unknown types → `PAYMENT_SUCCESS` (optimistic default)

**Generic Status Mapping** (via `mapGenericStatus(String status)`):
- Used by `PaymentResponseAdapter` for outbound integrator responses
- `AUTHORIZED`, `CAPTURED`, `TOKENIZED` → `PAYMENT_SUCCESS`
- `REFUNDED` → `REFUND_SUCCESS`
- `null` or unknown → `PAYMENT_SUCCESS` (conservative default)

**Enum Helpers**:
- `StripeWebhookEventType.fromValue(String)` - Factory method for safe parsing
- `PayPalWebhookEventType.fromValue(String)` - Factory method for safe parsing
- `BankTransferWebhookEventType.fromValue(String).toCallbackType()` - Direct conversion method

## Payment Integrators (Outbound Operations)

### `PaymentIntegrator` Interface

**Location**: `com.ahss.integration.PaymentIntegrator`

**Methods**:
```java
boolean supports(PaymentMethodType type);
PaymentResponseDto initiatePayment(PaymentRequestDto, PaymentTransactionDto);
PaymentResponseDto processRefund(PaymentTransactionDto, BigDecimal refundAmount);
PaymentResponseDto tokenizeCard(Object cardDetails);
```

**Purpose**: Abstraction for outbound payment operations (initiate, refund, tokenize) to external payment gateways

### `PaymentIntegratorFactory`

**Location**: `com.ahss.integration.PaymentIntegratorFactory`

**Implementation**: Spring `@Component` with constructor injection of `List<PaymentIntegrator>`

**Method**: `PaymentIntegrator getIntegrator(PaymentMethodType type)`
- Uses Stream API to filter integrators by `supports(type)`
- Throws `NoSuchElementException` if no integrator found
- Spring auto-discovers all `PaymentIntegrator` beans and injects as list

### `StripeIntegrator`

**Location**: `com.ahss.integration.stripe.StripeIntegrator`

**Supported Types**: `CREDIT_CARD`, `DEBIT_CARD`

**Configuration**:
- `@Value("${stripe.tokenizationApiUrl}")` - Default: `https://api.stripe.com/v1/tokens`
- `@Value("${stripe.paymentApiUrl}")` - Default: `https://api.stripe.com/v1/charges`
- Uses `RestTemplate` for HTTP communication

**Operations**:
- `initiatePayment()`: Converts internal DTOs to Stripe API format, posts to charges endpoint
- `processRefund()`: Handles Stripe refund API calls
- `tokenizeCard()`: Tokenizes card details via Stripe tokens API

### `PayPalIntegrator`, `BankTransferIntegrator`

Similar structure with gateway-specific API URLs and request/response conversions

### `PaymentResponseAdapter`

**Location**: `com.ahss.integration.PaymentResponseAdapter`

**Purpose**: Converts outbound `PaymentResponseDto` (from integrators) to canonical `PaymentCallbackEvent` (consumed by orchestrator)

**Method**: `PaymentCallbackEvent toCallbackEvent(PaymentResponseDto resp)`

**Conversion Logic**:
1. Maps `resp.isSuccess()` → `PAYMENT_FAILED` if false
2. Uses `PaymentChannelIntegrationEventTypeMapper.mapGenericStatus(resp.getStatus())` for success types
3. Copies fields: `paymentRequestId`, `paymentTransactionId`, `externalTransactionId`, etc.
4. Sets `receivedAt` to `LocalDateTime.now()`
5. Preserves `gatewayResponse`, `errorCode`, `errorMessage`, `metadata`

**Use Case**: Allows outbound integrator responses to flow through the same orchestration pipeline as inbound webhooks

## Kafka Consumers

### `PaymentCallbackConsumer`

**Location**: `com.ahss.kafka.consumer.PaymentCallbackConsumer`

**Topic**: Configured via `app.kafka.topics.payment-callbacks`

**Behavior**:
- Reads JSON messages from Kafka topic
- Detects gateway by payload structure using `MessageParserFactory.forPayload(JsonNode)`
- Selected parser's `parse()` method creates canonical `PaymentCallbackEvent`
- Passes event to `PaymentSagaOrchestrator.handle(event)` for processing
- Inherits JSON utilities from `BaseJsonKafkaConsumer`

**Error Handling**: Catches exceptions; production systems should forward to Dead Letter Topic (DLT)

## Kafka Producers

- `PaymentCallbackProducer`
  - Publishes canonical `PaymentCallbackEvent` to `payment-callbacks` topic from controllers.
  - Ensures downstream consumers operate on a normalized event.

- `PaymentEventProducer`
  - Publishes domain events related to payments (e.g., state transitions) to internal topics.
  - Used by orchestrator or service layers for broader event-driven workflows.

## PaymentSagaOrchestrator

- `PaymentSagaOrchestrator`
  - Coordinates multi-step payment flows across services.
  - Consumes `PaymentCallbackEvent` and performs actions such as updating payment status, triggering notifications, and scheduling follow-ups.
  - Encapsulates state machine transitions aligned with `payment-request-state-machine.md`.

## Data Flow

1. Gateway → Controller
   - HTTP webhook request arrives at `StripeWebhookController` or `PayPalWebhookController`.
   - Controller uses `BaseWebhookController.handleWebhook(...)` to parse, enrich, and publish.

2. Controller → Kafka
   - `PaymentCallbackProducer` sends `PaymentCallbackEvent` to `payment-callbacks` topic.

3. Kafka → Orchestrator
   - `PaymentCallbackConsumer` reads the event.
   - Uses parser (planned via `WebhookMessageParserFactory`) when needed for raw payload normalization.
   - Calls `PaymentSagaOrchestrator.handle(event)`.

4. Orchestrator → Domain
   - Updates payment records, triggers downstream actions, emits internal events via `PaymentEventProducer` when applicable.

## Error Handling & Observability

- Controllers
  - Standardized error responses; default unknown types map to `PAYMENT_FAILED` to avoid false positives.
  - Attach `gatewayResponse` to `PaymentCallbackEvent` for audit.

- Parsers
  - Extract error codes/messages (`Stripe.last_payment_error`, `PayPal.reason_code`).
  - Safe parsing with null checks; conservative fallbacks on unknown types.

- Kafka
  - Consumer catches exceptions; real systems would forward to DLT.
  - Logs and metrics via Spring Kafka; tracing via OTEL guides (`spring-otel-tracing-guide.md`).

## Testing Strategy and Infrastructure

### Test Infrastructure (`com.ahss.integration.BaseIntegrationTest`)

- **Framework**: Spring Boot integration tests with Testcontainers
- **Database**: PostgreSQL 16 Alpine container (`sharedservices` database)
- **Environment**: `@SpringBootTest` with `RANDOM_PORT` for full integration testing
- **Authentication**: Provides `obtainToken()` helper that authenticates as `admin/admin123` and returns JWT
- **Lifecycle**: `PER_CLASS` test instance to share container across tests
- **Configuration**:
  - Dynamic properties via `@DynamicPropertySource` for container URLs
  - Flyway migrations enabled
  - JPA validation mode (`ddl-auto=validate`)

### Webhook Controller Tests

All webhook controllers extend `BaseIntegrationTest` and use Allure reporting with structured test annotations.

#### Common Test Patterns

- **Allure Annotations**:
  - `@Epic("Payment Channel Integration")`
  - `@Feature("<Gateway> Integration")` (Stripe, PayPal, BankTransfer)
  - `@Story("<Test Scenario>")` describing the test case
  - `@DisplayName("<Human-readable test name>")`

- **Test Structure**:
  1. Construct JSON payload using gateway-specific schema
  2. Add gateway-specific headers (signatures, transmission IDs)
  3. Attach request payload to Allure report
  4. Send HTTP POST to webhook endpoint
  5. Verify HTTP 200 response
  6. Capture `PaymentCallbackEvent` sent to Kafka via `ArgumentCaptor`
  7. Attach captured event JSON to Allure report
  8. Assert event fields (type, gateway, amounts, metadata, errors)

- **Mock Kafka Producer**: `@MockBean PaymentCallbackProducer` to verify event publishing without actual Kafka

#### Stripe Webhook Tests (`StripeWebhookControllerTest`)

- **Test Cases**:
  1. `handle_stripe_payment_intent_succeeded_sends_event`:
     - Payload: `payment_intent.succeeded` with full metadata
     - Verifies: `PAYMENT_SUCCESS` type, amount conversion (12345 cents → $123.45), metadata extraction
     - Checks: `stripeSignature` in metadata, `gatewayResponse` attachment

  2. `handle_stripe_payment_intent_failed_with_last_error`:
     - Payload: `payment_intent.payment_failed` with `last_payment_error` object
     - Verifies: `PAYMENT_FAILED` type, error code/message extraction (`card_declined`, `Declined`)

  3. `handle_stripe_charge_refunded_sets_refund_id`:
     - Payload: `charge.refunded` with refunds array
     - Verifies: `REFUND_SUCCESS` type, `externalRefundId` extraction, amount via `amount` field

- **Headers**: `Stripe-Signature` header attached to metadata

#### PayPal Webhook Tests (`PayPalWebhookControllerTest`)

- **Test Cases**:
  1. `handle_paypal_payment_capture_completed_sends_event`:
     - Payload: `PAYMENT.CAPTURE.COMPLETED` with `resource.amount` object
     - Verifies: `PAYMENT_SUCCESS` type, amount/currency extraction, ISO timestamp parsing
     - Checks: `transmissionSig`, `transmissionId` in metadata

  2. `handle_paypal_payment_capture_denied_sets_error`:
     - Payload: `PAYMENT.CAPTURE.DENIED` with `resource.reason`
     - Verifies: `PAYMENT_FAILED` type, error message (`Insufficient funds`)

  3. `handle_paypal_payment_capture_refunded_no_amount_branch`:
     - Payload: `PAYMENT.CAPTURE.REFUNDED` without amount field
     - Verifies: `REFUND_SUCCESS` type, `null` amount and currency (edge case coverage)

- **Headers**: `PayPal-Transmission-Sig`, `PayPal-Transmission-Id`

#### Bank Transfer Webhook Tests (`BankTransferWebhookControllerTest`)

- **Test Cases**:
  1. `handle_bank_transfer_completed_sends_event`:
     - Payload: `TRANSFER.COMPLETED` with `amount.value` structure
     - Verifies: `PAYMENT_SUCCESS` type, headers in nested metadata structure
     - Checks: `metadata.headers` map contains `X-Bank-Signature`, `X-Bank-Request-Id`
     - Validates: `reference` field and `rawEventType` preserved in metadata

  2. `handle_bank_transfer_failed_sets_error`:
     - Payload: `TRANSFER.FAILED` with `error.code` and `error.message`
     - Verifies: `PAYMENT_FAILED` type, error extraction (`ERR42`, `Bank declined`)

  3. `handle_bank_transfer_refund_completed_type_mapping`:
     - Payload: `TRANSFER.REFUND.COMPLETED`
     - Verifies: `REFUND_SUCCESS` type mapping

- **Headers**: `X-Bank-Signature`, `X-Bank-Request-Id` nested in `metadata.headers`

### Payment Controller Integration Tests (`PaymentControllerIntegrationTest`)

Tests authenticated endpoints for payment domain management:

- **Authentication**: All tests use JWT obtained via `obtainToken()` helper
- **Allure Steps**: Each HTTP request and assertion wrapped in `Allure.step()`
- **Test Coverage**:
  - `getAllPaymentRequests()`: Paginated list endpoint
  - `getAllTransactions()`: Paginated transaction list
  - `getAllRefunds()`: Paginated refund list
  - `getAllAuditLogs()`: Paginated audit log retrieval
  - `getPaymentRequestStats()`: Aggregate statistics (allows 200 or 500 for edge cases)
  - `getTransactionStats()`: Transaction-level statistics
  - `getRefundStats()`: Refund-level statistics (allows 200 or 500)

- **Response Validation**: Verifies `success: true`, `data.content` array structure

### Parser Tests

#### Message Parser Factory Tests (`MessageParserFactoryTest`)

- **Test Cases**:
  1. `selects_stripe_parser()`: Recognizes `{ "type": "...", "data": { "object": {} } }`
  2. `selects_paypal_parser()`: Recognizes `{ "event_type": "...", "resource": {} }`
  3. `selects_bank_transfer_parser()`: Recognizes `{ "status": "...", "amount": ... }`
  4. `returns_null_for_unknown_shape()`: Handles unrecognized payloads gracefully

- **Factory Pattern**: Validates parser selection via `supports(JsonNode)` predicate

### Integrator Tests

#### Stripe Integrator Unit Tests (`StripeIntegratorTest`)

- **Payment Method Support**:
  - `supports()`: Returns `true` for `CREDIT_CARD`, `DEBIT_CARD`
  - Rejects: `PAYPAL`, `BANK_TRANSFER`

- **Operation Tests**:
  1. `initiatePayment_returnsAuthorizedResponse()`:
     - Mocks `RestTemplate` to verify HTTP call
     - Validates: `success=true`, `status=AUTHORIZED`, gateway name, IDs, timestamps

  2. `tokenizeCard_returnsTokenizedResponse()`:
     - Verifies: `status=TOKENIZED`, gateway metadata

- **Mock Usage**: `@MockBean RestTemplate` to avoid real HTTP calls

### Test Organization

- **Package Structure**:
  - `com.ahss.integration`: Base test class and domain controller tests
  - `com.ahss.integration.stripe`: Stripe-specific webhook, integrator, parser, WireMock tests
  - `com.ahss.integration.paypal`: PayPal-specific webhook, integrator, parser, WireMock tests
  - `com.ahss.integration.bank`: Bank Transfer webhook, integrator, parser, WireMock tests
  - `com.ahss.integration.parser`: Parser factory and contract tests

- **Naming Conventions**:
  - Integration tests: `*IntegrationTest` (extends `BaseIntegrationTest`)
  - Controller tests: `*ControllerTest`, `*ControllerIntegrationTest`
  - Unit tests: `*Test` (standalone, no Spring context)
  - WireMock tests: `*WireMockIT` (integration with mock HTTP server)

### Observability and Reporting

- **Allure Attachments**:
  - Request payloads (JSON)
  - Captured event payloads (JSON)
  - Labeled by scenario ("Stripe Request Payload", "Bank Transfer Event Payload (Failed)")

- **Step-by-Step Reporting**:
  - `Allure.step("description", lambda)` wraps actions and assertions
  - Enables granular failure diagnosis in Allure HTML reports

### Coverage and Edge Cases

- **Success Paths**: Standard payment success, capture completed, transfer completed
- **Failure Paths**: Payment failed, capture denied, transfer failed with error enrichment
- **Refund Scenarios**: Full refunds, partial refunds, missing amount edge cases
- **Mapper Fallbacks**: Unknown event types default to `PAYMENT_FAILED` (conservative)
- **Error Enrichment**: Validates `errorCode`, `errorMessage` extraction from gateway-specific structures
- **Metadata Preservation**: Headers, signatures, raw event types attached for audit trails

### Testcontainers Integration

- **PostgreSQL Container**: Shared across all integration tests via static initialization
- **Database Schema**: Flyway migrations applied on container startup
- **Isolation**: Each test runs in a transaction (implicit via Spring Test)
- **Cleanup**: Container lifecycle managed by Testcontainers (auto-cleanup)

### Recommendations for Test Enhancement

1. Add contract tests for parser implementations using parameterized tests
2. Verify `BaseWebhookController` abstract hooks with dedicated unit tests
3. Increase mapper coverage with edge cases (malformed types, null fields)
4. Add WireMock tests for integrator HTTP resilience (timeouts, retries, 5xx errors)
5. Consider adding mutation testing to verify test quality
6. Add performance tests for high-volume webhook scenarios

## Extension Points

- Add new gateway controllers by extending `BaseWebhookController` and implementing hooks.
- Add new parser implementations and register in `WebhookMessageParserFactory`.
- Consider `EventTypeMappingStrategy` interface if mapper grows with more providers.

## Updates and Refactor Summary

- Added Bank Transfer support across layers:
  - Controller: `com.ahss.integration.bank.BankTransferWebhookController` using `BaseWebhookController` pipeline.
  - Parser: `com.ahss.integration.bank.BankTransferMessageParser` handling `event_type`/`status`/`event` shapes.
  - Security: `WebhookSecurityPermitConfig` now permits `/api/integrations/webhooks/bank-transfer` (alongside Stripe and PayPal).

- Centralized event type mapping:
  - Replaced `WebhookEventTypeMapper` usage with `com.ahss.integration.mapper.PaymentChannelIntegrationEventTypeMapper` in controllers and parsers.
  - Introduced `mapGenericStatus(String status)` and refactored `PaymentResponseAdapter.resolveCallbackType(...)` to reuse it, eliminating duplicated status logic (`AUTHORIZED`, `CAPTURED`, `TOKENIZED` → success; `REFUNDED` → refund success; default → success).

- Parser factory is available and used by consumers:
  - `com.ahss.integration.MessageParserFactory` selects among `StripeMessageParser`, `PayPalMessageParser`, and `BankTransferMessageParser` based on payload shape.
  - Aligns `PaymentCallbackConsumer` behavior with the standardized parsing strategy.

- Clarified responsibilities: Webhook controllers vs integrator classes
  - Webhook controllers (Stripe, PayPal, Bank Transfer) handle inbound notifications and publish canonical `PaymentCallbackEvent` to Kafka.
  - Integrators (e.g., `PayPalIntegrator`, `BankTransferIntegrator`) perform outbound API calls (initiate, refund, tokenize) and return `PaymentResponseDto`, which is adapted via `PaymentResponseAdapter` into canonical events using the mapper.

- Tests updated
  - Added integration and unit tests for Bank Transfer controller and parser, covering success, failure (error enrichment), and refund scenarios.
