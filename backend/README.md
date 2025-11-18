# Shared Services Backend

Java 21 Spring Boot service with Kafka, JPA, OpenTelemetry, and Allure reporting.

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker (for container builds)

## Build & Test

- Run full build and tests:
  - `mvn clean verify`
- Test reports:
  - Allure HTML: `target/allure-report/index.html`
  - JaCoCo: included in Allure under `jacoco/` with a quick-link button

## Allure Report

- Generate during verify: `mvn verify`
- Serve locally (opens the generated report):
  - `mvn -Pallure-serve verify`

## Container Image (Jib)

- Build image to local Docker:
  - `mvn jib:dockerBuild`
- Windows Docker arch (linux/amd64):
  - `mvn -Pwindows-docker jib:dockerBuild`
- Image name: `shared-services:0.0.1-SNAPSHOT`

## Run Locally

- From the `backend/` directory:
  - `mvn spring-boot:run`
- Key endpoints:
  - `http://localhost:8080/api/v1/users`
  - `http://localhost:8080/api/v1/payments`

## Observability

- OTLP HTTP endpoint default: `http://localhost:4318/v1/traces`
- Configure via `application.yml` `management.otlp.tracing.endpoint`

## Configuration

- `src/main/resources/application.yml`
- `src/main/resources/application-integration.yml`