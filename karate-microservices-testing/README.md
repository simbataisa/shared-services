# Karate Microservices Testing

Maintainable, parallel, CI-ready test automation using Karate for a microservices platform. Includes API, integration, contract, performance, and async/Kafka tests with mocks, schemas, and shared utilities.

## Quick Start

### Option 1: Docker Mock Server (Recommended) 🐳

No Java installation required! Just Docker:

```bash
# Build and run mock server
./docker-build-and-run.sh          # macOS/Linux
docker-build-and-run.bat           # Windows

# Or use Docker Compose
docker compose up

# Or manually
docker build -t karate-mock-server .
docker run -p 8090:8090 karate-mock-server
```

Mock server will be available at `http://localhost:8090`

**See [DOCKER-MOCK-SERVER.md](./DOCKER-MOCK-SERVER.md) for complete guide**

### Option 2: Local Development (Requires JDK 21)

- Prereqs: JDK 21, Gradle (or use wrapper)
- Copy env file: `cp .env.example .env` (optional)
- Local dev (auto-mock): `./gradlew test -Dkarate.env=dev`
- Start mock server: `./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=600000`

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

### Maven Usage

- Full suite (JUnit runners):
  - `mvn -f karate-microservices-testing/pom.xml test -Dkarate.env=qa -Dthreads=5`
- API runner only:
  - `mvn -f karate-microservices-testing/pom.xml test -Dtest='*ApiRunnerTest' -Dkarate.env=qa`
- Custom runner with mock server:
  - `mvn -f karate-microservices-testing/pom.xml test -Dtest='*CustomRunnerTest' -Dkarate.env=qa -Dmock.server.enabled=true -Dmock.port=8090 -Dkarate.options="classpath:api"`
- Integration runner without mocks:
  - `mvn -f karate-microservices-testing/pom.xml test -Dtest='*IntegrationRunnerTest' -Duse.mock=false -Dkarate.env=qa`
- Mock server standalone:
  - `mvn -f karate-microservices-testing/pom.xml test -Dtest='*MockRunnerTest' -Dmock.port=8090 -Dmock.block.ms=300000`
- Gatling performance:
  - `mvn -f karate-microservices-testing/pom.xml gatling:test -Dgatling.simulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:api/users.feature" -Dkarate.env=qa -Dthreads=10`
  - `mvn -f karate-microservices-testing/pom.xml gatling:test -Dgatling.simulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:api --tags @perf" -Dkarate.env=qa -Dthreads=10`

## Reports

- Karate HTML: `karate-microservices-testing/target/karate-reports/` (open `karate-summary.html`)
- Cucumber JSON: enabled for performance runner; available under `target/` paths.
 - Gatling reports: `karate-microservices-testing/target/gatling/`

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