# Kafka Distributed Tracing Guide

## Problem

When running the backend application from IntelliJ (or via `./gradlew bootRun`), the traceId from Kafka producers is not propagated to consumers.

**Producer log:**
```
INFO [traceId=e5fd0cf4a919077aadb8373f61f6447e, spanId=3be3f3b64cd271a7] ... Sending JSON payload to topic
```

**Consumer log (WRONG):**
```
INFO [traceId=, spanId=] ... Payment callback received
```

**Consumer log (CORRECT with agent):**
```
INFO [traceId=e5fd0cf4a919077aadb8373f61f6447e, spanId=a2c3f4e8d5b91c7a] ... Payment callback received
```

## Root Cause

The issue occurs because Kafka consumer message processing happens on a **separate thread pool** managed by Spring Kafka. While our code extracts the trace context from Kafka message headers and makes it current, the context propagation across thread boundaries requires either:

1. **OpenTelemetry Java Agent** - Automatically propagates context across all thread boundaries (RECOMMENDED)
2. **Manual Scope Management** - Requires complex integration with Spring Kafka's lifecycle (NOT RECOMMENDED)

## Why the Java Agent is Required

### What We Implemented

In `KafkaConfig.java`, we have:

```java
// Extract context from Kafka headers
Context extractedContext = GlobalOpenTelemetry.getPropagators()
    .getTextMapPropagator()
    .extract(Context.current(), record.headers(), KAFKA_HEADER_GETTER);

// Make it current
extractedContext.makeCurrent();
```

### The Problem

1. `makeCurrent()` returns a `Scope` object that must be closed when processing completes
2. The record interceptor in `kafkaListenerContainerFactory` doesn't have a cleanup hook
3. Without closing the scope, context leaks occur
4. Even if we close it immediately, the context won't propagate to the `@KafkaListener` method running on a different thread

### How the Java Agent Fixes This

The OpenTelemetry Java agent provides **automatic context propagation** by:
- Instrumenting Java's `Executor`, `ExecutorService`, and thread pools
- Wrapping `Runnable` and `Callable` tasks to capture and restore context
- Automatically propagating context across async boundaries
- Managing scope lifecycle without manual intervention

## Solution: Running with Java Agent

### Option 1: Run from Command Line (Recommended for Local Testing)

```bash
./gradlew bootRunWithAgent --args='--spring.profiles.active=integration'
```

This task is configured in `build.gradle` (lines 319-331):
```gradle
tasks.register('bootRunWithAgent', org.springframework.boot.gradle.tasks.run.BootRun) {
    jvmArgs "-javaagent:${file("${rootDir}/../otel-javaagent.jar")}"
    environment 'OTEL_SERVICE_NAME', 'sharedservices-backend'
    environment 'OTEL_TRACES_EXPORTER', 'otlp'
    environment 'OTEL_EXPORTER_OTLP_ENDPOINT', 'http://localhost:4318'
    environment 'OTEL_METRICS_EXPORTER', 'none'
    environment 'OTEL_LOGS_EXPORTER', 'none'
}
```

### Option 2: Configure IntelliJ Run Configuration

1. **Open Run/Debug Configurations** in IntelliJ
2. **Select your Spring Boot application configuration**
3. **Add to VM Options field:**
   ```
   -javaagent:/Users/dennis.dao/workspace/repo/sample/otel-javaagent.jar
   ```
4. **Add Environment Variables (optional - for sending traces to OTLP collector):**
   ```
   OTEL_SERVICE_NAME=sharedservices-backend
   OTEL_TRACES_EXPORTER=otlp
   OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
   OTEL_METRICS_EXPORTER=none
   OTEL_LOGS_EXPORTER=none
   ```
5. **Apply and Run**

### Option 3: Set as Default for All IntelliJ Runs

Add to `.idea/workspace.xml` (or create a shared run configuration):

```xml
<component name="RunManager">
  <configuration default="true" type="SpringBootApplicationConfigurationType">
    <option name="VM_PARAMETERS" value="-javaagent:/Users/dennis.dao/workspace/repo/sample/otel-javaagent.jar" />
    <envs>
      <env name="OTEL_SERVICE_NAME" value="sharedservices-backend" />
      <env name="OTEL_TRACES_EXPORTER" value="otlp" />
      <env name="OTEL_EXPORTER_OTLP_ENDPOINT" value="http://localhost:4318" />
      <env name="OTEL_METRICS_EXPORTER" value="none" />
      <env name="OTEL_LOGS_EXPORTER" value="none" />
    </envs>
  </configuration>
</component>
```

## Verification

After running with the Java agent:

1. **Start the backend:**
   ```bash
   ./gradlew bootRunWithAgent --args='--spring.profiles.active=integration'
   ```

2. **Send a payment request:**
   ```bash
   curl -X POST http://localhost:8080/api/payments \
     -H "Content-Type: application/json" \
     -d '{
       "amount": 100.00,
       "currency": "USD",
       "gatewayName": "Stripe",
       "description": "Test payment"
     }'
   ```

3. **Check the logs:**

   **Producer:**
   ```
   INFO [traceId=e5fd0cf4a919077aadb8373f61f6447e, spanId=3be3f3b64cd271a7]
        Sending JSON payload to topic payment-callbacks
   ```

   **Consumer (should now show the SAME traceId):**
   ```
   INFO [traceId=e5fd0cf4a919077aadb8373f61f6447e, spanId=a2c3f4e8d5b91c7a]
        Payment callback received: {...}
   ```

The traceId should match between producer and consumer!

## Architecture Details

### Producer Side (Already Working)

1. `OtelKafkaProducerInterceptor` (configured in `KafkaConfig.java` lines 65-90)
2. Injects W3C trace context (`traceparent`, `tracestate`) into Kafka message headers
3. Works without the Java agent

### Consumer Side (Requires Java Agent)

1. `OtelKafkaConsumerInterceptor` (configured in `KafkaConfig.java` lines 100-129)
   - Extracts trace context from Kafka message headers
   - Stores in ThreadLocal for access

2. Record Interceptor (in `kafkaListenerContainerFactory` lines 140-157)
   - Extracts context using `TextMapPropagator`
   - Makes context current

3. **Java Agent (CRITICAL)**
   - Propagates the context from interceptor thread to consumer thread
   - Ensures `@KafkaListener` methods see the correct trace context
   - Handles scope lifecycle automatically

## Alternative Approach (Advanced - Not Recommended)

If you absolutely cannot use the Java agent, you would need to:

1. Create a custom `KafkaListenerContainerFactory` with observation customization
2. Implement a custom `ObservationConvention` for Kafka
3. Integrate with Spring's `ObservationRegistry`
4. Manually manage scope lifecycle with proper cleanup hooks

This is significantly more complex and error-prone. **We strongly recommend using the OpenTelemetry Java agent instead.**

## Summary

**For distributed tracing to work across Kafka:**

✅ **DO**: Run with `./gradlew bootRunWithAgent`
✅ **DO**: Add `-javaagent` VM option to IntelliJ run configuration
✅ **DO**: Use the OpenTelemetry Java agent in production

❌ **DON'T**: Run with `./gradlew bootRun` (no agent)
❌ **DON'T**: Run from IntelliJ without `-javaagent` VM option
❌ **DON'T**: Expect manual context propagation to work reliably

The Java agent is the industry-standard solution for context propagation in complex async environments like Kafka consumers.
