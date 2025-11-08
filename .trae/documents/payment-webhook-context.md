# Webhook Context: Controllers, Parsers, Mapper, Kafka, Orchestrator

## Overview

- Standardizes inbound payment webhooks (Stripe, PayPal) and converts them into a canonical `PaymentCallbackEvent` consumed by the payment orchestrator.
- Shared controller base centralizes common HTTP handling and event dispatch. Parser layer translates raw payloads. Mapper normalizes event types. Kafka bridges controllers and saga orchestration.

## Components

- Controllers
  - `com.ahss.integration.webhook.BaseWebhookController`
  - `com.ahss.integration.webhook.StripeWebhookController`
  - `com.ahss.integration.webhook.PayPalWebhookController`
- Parser Layer
  - `com.ahss.integration.webhook.parser.WebhookMessageParser`
  - `com.ahss.integration.webhook.parser.StripeWebhookMessageParser`
  - `com.ahss.integration.webhook.parser.PayPalWebhookMessageParser`
  - `com.ahss.integration.webhook.parser.WebhookMessageParserFactory`
- Event Type Mapper
  - `com.ahss.integration.webhook.WebhookEventTypeMapper`
- Kafka
  - Consumers: `com.ahss.kafka.consumer.PaymentCallbackConsumer`
  - Producers: `com.ahss.kafka.producer.PaymentCallbackProducer`, `com.ahss.kafka.producer.PaymentEventProducer`
- Orchestrator
  - `com.ahss.saga.PaymentSagaOrchestrator`

## Webhook Controllers

- `BaseWebhookController`
  - Provides a reusable `handleWebhook` pipeline for all gateways.
  - Responsibilities:
    - Parse JSON request body (`ObjectMapper.readTree`).
    - Build `PaymentCallbackEvent` skeleton (correlation, amounts, currency).
    - Attach `gatewayResponse` map for traceability.
    - Enrich event via abstract hooks.
    - Dispatch event to Kafka (`PaymentCallbackProducer`).
    - Return standardized HTTP response with `PaymentCallbackType` and metadata.
  - Abstract hooks to specialize per gateway:
    - `gatewayName()`
    - `extractEventType(JsonNode root)`
    - `mapEventType(String rawType)` → `PaymentCallbackType`
    - `populateEvent(JsonNode root, PaymentCallbackEvent evt)`
    - `metadata(JsonNode root)` → request-scoped metadata map

- `StripeWebhookController` / `PayPalWebhookController`
  - Implement the hooks to extract gateway-specific fields (amount, currency, IDs, errors, refunds).
  - Delegate overall handling to `BaseWebhookController.handleWebhook(...)`.

## Parser Layer

- Purpose: Convert raw webhook payloads into canonical `PaymentCallbackEvent` outside of HTTP context.
- Interfaces and classes:
  - `WebhookMessageParser`: strategy interface with `supports(JsonNode root)` and `parse(JsonNode root)`.
  - `StripeWebhookMessageParser`: handles `Stripe` payloads (`type`, `data.object`, amounts in minor units).
  - `PayPalWebhookMessageParser`: handles `PayPal` payloads (`event_type`, `resource`, ISO timestamps).
  - `WebhookMessageParserFactory`: selects parser based on `supports(...)` predicate.
- Mapping: Both parsers rely on `WebhookEventTypeMapper` to map raw gateway event types to `PaymentCallbackType`.

## Event Type Mapper

- `WebhookEventTypeMapper`
  - Central mapping from gateway raw types to canonical `PaymentCallbackType` (`PAYMENT_SUCCESS`, `PAYMENT_FAILED`, `REFUND_SUCCESS`, etc.).
  - Provides helpers: `mapStripe(String type)`, `mapPayPal(String type)`.
  - Used by controllers and parsers to keep type semantics consistent.

## Kafka Consumers

- `PaymentCallbackConsumer` (`com.ahss.kafka.consumer`)
  - Reads messages from `app.kafka.topics.payment-callbacks`.
  - Current behavior: parses message JSON, detects gateway by shape, builds `PaymentCallbackEvent` and passes to `PaymentSagaOrchestrator.handle(event)`.
  - Ongoing refactor: delegate parsing to `WebhookMessageParserFactory.forPayload(root)` → selected `WebhookMessageParser.parse(...)`, then orchestrate.
  - Base utilities provided by `BaseJsonKafkaConsumer` (JSON read helpers).

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

## Testing Hooks

- Controller tests include Allure attachments for request and event payloads.
- Added cases for success, failure, refund, and mapper fallbacks per gateway.
- Recommendation: add unit tests for `BaseWebhookController` helpers and parser classes; verify mapper coverage.

## Extension Points

- Add new gateway controllers by extending `BaseWebhookController` and implementing hooks.
- Add new parser implementations and register in `WebhookMessageParserFactory`.
- Consider `EventTypeMappingStrategy` interface if mapper grows with more providers.
