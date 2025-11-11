# Karate Microservices Testing

Maintainable, parallel, CI-ready test automation using Karate for a microservices platform. Includes API, integration, contract, performance, and async/Kafka tests with mocks, schemas, and shared utilities.

## Quick Start

- Prereqs: JDK 21, Gradle (or use wrapper if added)
- Copy env file: `cp karate-microservices-testing/.env.example .env` (optional)
- Local dev (auto-mock): `make test env=dev threads=5`
- QA URLs (with mock server):
  1. Start mocks: `make mock` (or run in background)
  2. Run tests pointing to mock as QA: `BASE_URL=http://localhost:8090 AUTH_BASE_URL=http://localhost:8090/auth make test env=qa`

## Project Structure

```
karate-microservices-testing/
├── src/test/java
│   ├── TestRunner.java
│   ├── PerformanceTestRunner.java
│   ├── MockRunner.java
│   └── helpers/
│       ├── KafkaHelper.java
│       ├── ContractVerifier.java
│       └── CustomValidators.java
├── src/test/resources
│   ├── karate-config.js
│   ├── logback-test.xml
│   ├── common/
│   │   ├── auth/oauth2.feature
│   │   ├── headers/common-headers.js
│   │   ├── schemas/{user,error,pagination}.json
│   │   └── utils/{data-generator.js,validators.js,retry-logic.feature}
│   ├── services/
│   │   ├── user-service/{api,contracts,performance,async,mocks,config}
│   │   └── order-service/{api,contracts,performance,async,mocks,config}
│   ├── integration/{user-order-flow.feature,end-to-end-checkout.feature}
│   └── mocks/{mock-server.feature, mock-responses/*.json}
├── Makefile
├── pom.xml
└── .env.example
```

## How to Run

- All tests: `make test env=qa threads=5`
- Smoke-only: `make test-smoke env=qa threads=5`
- Contracts: `make test-contract`
- Performance: `make test-perf`
- Start mock server: `make mock`

## Reports

- Karate HTML: `karate-microservices-testing/target/karate-reports/` (open `karate-summary.html`)
- Cucumber JSON: enabled for performance runner; available under `target/` paths.

## Conventions & Quality Gates

- Tags: `@smoke`, `@regression`, `@contract`, `@perf`, `@async`, `@e2e`, `@ignore`
- Schema-first: shared JSON schemas; inline matchers only for dynamic fields.
- Headers: centralized in `common/headers/common-headers.js`.
- Retries: only for idempotent GET/HEAD.
- Data setup: prefer API factories; namespace by run ID when needed.
- Flakiness: twice in CI -> quarantine with `@ignore` and ticket.

## Troubleshooting

- 401s: ensure `CLIENT_ID`/`CLIENT_SECRET` or use mock server.
- Timeouts: adjust `karate-config.js` `connectTimeout` / `readTimeout`.
- Data collisions: use `utils.randomEmail()` for unique data.
- Kafka: `KafkaHelper` is stubbed; wire real brokers via env vars if needed.

## Contributing

- Naming: `services/<svc>/<area>/<feature>.feature`
- Tag meaningful scenarios; avoid over-tagging.
- Update schemas when payloads change; keep tests DRY with shared utils.