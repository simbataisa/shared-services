# Distributed Tracing (OpenTelemetry) Guide

This guide explains how tracing is wired end-to-end in the Shared Services backend: Spring Boot produces spans via Micrometer Tracing and the OpenTelemetry Java agent, the OpenTelemetry Collector receives and processes them, and Jaeger stores and visualizes traces.

## Architecture Overview
- Backend emits spans via two paths:
  - Micrometer Tracing with OTLP exporter (`management.otlp.tracing.endpoint`).
  - OpenTelemetry Java agent for auto-instrumentation (HTTP server, JDBC/Hibernate, etc.).
- Collector receives OTLP HTTP traffic on `:4318` and exports to Jaeger via OTLP gRPC on `:4317`.
- Jaeger UI is available at `http://localhost:16686` for trace exploration.

## Collector Configuration
The collector config lives at `otel-collector-config.yaml` and is mounted by Docker Compose. Effective configuration:

```yaml
receivers:
  otlp:
    protocols:
      http:

processors:
  batch:
    timeout: 1s

exporters:
  logging:
    loglevel: debug
  otlp:
    endpoint: jaeger:4317
    tls:
      insecure: true

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [logging, otlp]
```

What this means:
- Receiver: accepts OTLP over HTTP from the backend at `http://localhost:4318`.
- Processor: batches spans for efficiency and backpressure.
- Exporters:
  - `logging`: prints spans to collector logs for quick debugging.
  - `otlp`: forwards spans to Jaeger’s OTLP gRPC endpoint (`jaeger:4317`) inside the Docker network.
- Pipeline: only traces are configured; metrics and logs are not in scope here.

## Backend Tracing Configuration
Key dependencies are declared in `backend/build.gradle`:
```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-tracing-bridge-otel:1.3.5'
implementation 'io.opentelemetry:opentelemetry-exporter-otlp'
implementation 'org.springframework.boot:spring-boot-starter-aop'
implementation 'org.springframework.kafka:spring-kafka:3.2.4'
```

Runtime configuration in `backend/src/main/resources/application.yml`:
```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces

logging:
  pattern:
    level: "%5p [traceId=%mdc{traceId}, spanId=%mdc{spanId}]"
```

Environment variables commonly used locally:
- `OTEL_SERVICE_NAME=sharedservices-backend`
- `OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318`
- `OTEL_TRACES_EXPORTER=otlp`
- `OTEL_METRICS_EXPORTER=none` (optional; disables Java agent metrics)

## OpenTelemetry Java Agent (Auto‑Instrumentation)
Purpose: enrich spans with server, JDBC/Hibernate, and library auto-instrumentation without code changes.

Local usage (agent JAR is located at the repo root as `otel-javaagent.jar`):
```bash
cd backend
JAVA_TOOL_OPTIONS="-javaagent:../otel-javaagent.jar"
OTEL_SERVICE_NAME=sharedservices-backend \
OTEL_TRACES_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318 \
OTEL_METRICS_EXPORTER=none \
OTEL_LOGS_EXPORTER=none \
./gradlew bootRun
```

Notes:
- The agent is attached via `JAVA_TOOL_OPTIONS` so it works with `bootRun` without additional Gradle wiring.
- If you see warnings about logs export (HTTP `404` to `:4318`), set `OTEL_LOGS_EXPORTER=none` or add a logs pipeline to the collector.

### Convenience Gradle Task
Run with a dedicated task that attaches the agent automatically:
```bash
cd backend
./gradlew bootRunWithAgent
```

### Containerized Backend with Agent
Use the Docker Compose backend service that mounts the agent and sends traces to the collector:
```bash
# Build the image once (buildpacks)
cd backend && ./gradlew bootBuildImage

# Start collector, jaeger, and backend
docker-compose --profile observability up -d otel-collector jaeger backend

# Stop services
docker-compose --profile observability down
```

## What Gets Instrumented
- HTTP server: incoming requests mapped to controller spans.
- Spring controllers/services: explicit spans via Micrometer Observations.
- JDBC/Hibernate: database operations auto-instrumented by the Java agent beneath the current span.
- HTTP clients (`RestTemplate`/`WebClient`): traced via Micrometer.
- Kafka producers: `traceparent`/`tracestate` injected by `OtelKafkaProducerInterceptor`.

## Verifying End‑to‑End
1. Start collector and Jaeger:
   ```bash
   docker-compose up -d otel-collector jaeger
   ```
2. Start backend with the agent (see command above).
3. Trigger activity, for example:
   ```bash
   curl -X GET 'http://localhost:8080/api/v1/tenants/search?query=demo'
   # or
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H 'Content-Type: application/json' \
     -d '{"username": "admin@ahss.com", "password": "wrong"}'
   ```
4. Check collector logs for spans (`docker-compose logs -f otel-collector`).
5. Open Jaeger UI (`http://localhost:16686`), choose service `sharedservices-backend`, and search traces.

## Operational Tips
- Sampling: tune `management.tracing.sampling.probability` for volume control.
- Troubleshooting:
  - If traces don’t appear, confirm backend sends to `http://localhost:4318/v1/traces` and collector is up.
  - Ensure Docker Compose services are on the same network; the config uses `jaeger:4317` internally.
  - Disable agent metrics via `OTEL_METRICS_EXPORTER=none` to reduce noise.

## Related Code
- Logback MDC pattern: `backend/src/main/resources/logback-spring.xml`.
- Kafka header propagation: `com.ahss.tracing.kafka.OtelKafkaProducerInterceptor`.
- Observation spans: `com.ahss.config.ObservabilityConfig`, `TracingAspect`.
