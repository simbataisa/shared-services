# Karate Test Runners and Gradle Usage

This guide explains how to run individual or multiple Karate feature files using the provided JUnit runners and Gradle tasks in `karate-microservices-testing`.

## Module and Paths

- Module root: `karate-microservices-testing/`
- Features: `src/test/resources/`
- JUnit runners: `src/test/java/com/ahss/automation/runners/**`
- Gatling simulations: `src/test/scala/performance/simulations/**`

You can run Gradle from the repository root using the module wrapper:

- `./karate-microservices-testing/gradlew -p karate-microservices-testing <task> [system properties]`

Or from inside the module directory:

- `cd karate-microservices-testing && ./gradlew <task> [system properties]`

## Key System Properties

- `-Dkarate.env=<env>`: environment (defaults to `qa` or runner-specific defaults)
- `-Dthreads=<n>`: number of threads for parallel execution (used by main runners and Gradle tasks)
- `-Dinclude.tags=<tag1,tag2>`: tags to include (used by `TestRunner`)
- `-Dexclude.tags=<tag1,tag2>`: tags to exclude (used by `TestRunner`)
- `-Dkarate.options="<paths and options>"`: flexible options for `CustomRunnerTest` and Gradle tests
// Gatling-specific (Simulation) load configuration:
- `-Dinjection=<atOnce|constant|ramp>`: selects the injection pattern
- `-DusersPerSec=<n>`: rate for `constant`/`ramp` injections (defaults to `threads`)
- `-DdurationSeconds=<n>`: duration in seconds for `constant`/`ramp` injections

### `karate.options` syntax (supported by CustomRunnerTest)

The runner supports parsing `karate.options` with:

- Paths: any token not starting with `--` (e.g., `classpath:api/users.feature`)
- `--tags <csv>`: e.g., `--tags @smoke,~@ignore`
- `--env <value>` or `--karate.env <value>`: e.g., `--env qa`
- `--threads <n>`: used by CLI/main runners; Gradle test uses `-Dthreads=<n>`
- `--cucumberJson` or `--outputCucumberJson`: enable Cucumber JSON output in CLI/main runners

Examples:

- Single feature: `-Dkarate.options="classpath:api/users.feature"`
- Multiple features: `-Dkarate.options="classpath:api/users.feature classpath:api/user-groups.feature"`
- Paths with tags: `-Dkarate.options="classpath:api --tags @smoke,~@ignore"`
- Set env: `-Dkarate.options="classpath:api --env qa"`
// Gatling load examples:
- At once users: add `-Dthreads=5` (defaults to `atOnce` if no duration is set)
- Constant rate: `-Dinjection=constant -DusersPerSec=2 -DdurationSeconds=120`
- Ramp rate: `-Dinjection=ramp -DusersPerSec=5 -DdurationSeconds=300`

## JUnit Runners

### 1) CustomRunnerTest

- File: `src/test/java/com/ahss/automation/runners/CustomRunnerTest.java`
- Purpose: Flexible runner that consumes `-Dkarate.options` to target specific features or tags.

Run via Gradle with test filter (recommended):

- Single feature:
  - `./gradlew test --tests "*CustomRunnerTest" -Dkarate.options="classpath:api/users.feature" -Dkarate.env=qa`
- Multiple features:
  - `./gradlew test --tests "*CustomRunnerTest" -Dkarate.options="classpath:api/users.feature classpath:api/user-groups.feature" -Dkarate.env=qa`
- Paths with tags:
  - `./gradlew test --tests "*CustomRunnerTest" -Dkarate.options="classpath:api --tags @smoke,~@ignore" -Dkarate.env=qa`
- From repository root using module wrapper:
  - `./karate-microservices-testing/gradlew -p karate-microservices-testing test --tests "*CustomRunnerTest" -Dkarate.options="classpath:api/users.feature" -Dkarate.env=qa`
    -- Threads (for CLI main only): use `-Dthreads=<n>` in combination with CLI main; Gradle `test` task is JUnit-based and uses the property for runners that read it.

### 2) TestRunner (services + integration)

- File: `src/test/java/com/ahss/automation/runners/TestRunner.java`
- Purpose: Runs `classpath:services` and `classpath:integration`; supports include/exclude tags.

Run all services/integration:

- `./gradlew test -Dinclude.tags=@smoke -Dexclude.tags=~@ignore -Dkarate.env=qa`

CLI main with parallel and reports:

- `java -cp build/classes/java/test:build/resources/test:<deps> com.ahss.automation.runners.TestRunner`
- Or use Gradle test with default settings: `./gradlew test`

### 3) Gatling Performance Simulation (Scala)

- File: `src/test/scala/performance/simulations/KaratePerformanceSimulation.scala`
- Purpose: Gatling Simulation that runs Karate performance features; consumes `-Dkarate.options` for paths/tags and `-Dthreads` for user injection.

Run a specific Simulation (non-interactive):

- From module directory:
  - `./gradlew gatlingRun -PgatlingSimulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:services/user-service/performance" -Dkarate.env=qa -Dthreads=5`
- From repository root using module wrapper:
  - `./karate-microservices-testing/gradlew -p karate-microservices-testing gatlingRun -PgatlingSimulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:services/user-service/performance" -Dkarate.env=qa -Dthreads=5`

Run multiple performance features:

- `./gradlew gatlingRun -PgatlingSimulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:services/user-service/performance classpath:services/payments-service/performance" -Dkarate.env=qa -Dthreads=10`

Run a whole services directory with tags:

- `./gradlew gatlingRun -PgatlingSimulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:services --tags @perf" -Dkarate.env=qa -Dthreads=10`

Longer runs (steady or ramped):

- Constant 2 rps for 2 minutes:
  - `./gradlew gatlingRun -PgatlingSimulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:api/users.feature" -Dkarate.env=qa -Dinjection=constant -DusersPerSec=2 -DdurationSeconds=120`
- Ramp from 0 to 5 rps over 5 minutes:
  - `./gradlew gatlingRun -PgatlingSimulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:api/users.feature" -Dkarate.env=qa -Dinjection=ramp -DusersPerSec=5 -DdurationSeconds=300`

Notes:

- Use `-PgatlingSimulationClass=<fully.qualified.Simulation>` to select a simulation non-interactively.
- If `-PgatlingSimulationClass` is not provided, `gatlingRun` may prompt interactively depending on plugin configuration.
- The plugin source layout is configured to use `src/test/scala` and `src/test/resources` for simulations and assets.
- Gatling OSS does not provide a live web UI during execution. Progress is shown in the terminal. The HTML report is generated at the end of the run.

### 4) MockRunner

- File: `src/test/java/com/ahss/automation/runners/MockRunner.java`
- Purpose: Starts the mock server runner for local or CI scenarios.

Run via Gradle task:

- `./gradlew mockStart -Dmock.block.ms=600000`

### 5) UsersApiRunner

- File: `src/test/java/com/ahss/automation/runners/api/UsersApiRunner.java`
- Purpose: Runs `classpath:api/users.feature`.

Run just this test class:

- `./gradlew test --tests "*UsersApiRunner"`

### 6) PaymentsApiRunner

- File: `src/test/java/com/ahss/automation/runners/api/PaymentsApiRunner.java`
- Purpose: Runs `classpath:api/payments.feature`.

Run just this test class:

- `./gradlew test --tests "*PaymentsApiRunner"`

## Running Individual or Multiple Features with Gradle

Use `karate.options` with the `CustomRunnerTest` test filter (backed by JUnit runners):

- Single feature:
  - `./gradlew test --tests "*CustomRunnerTest" -Dkarate.options="classpath:api/users.feature" -Dkarate.env=qa`
- Multiple features:
  - `./gradlew test --tests "*CustomRunnerTest" -Dkarate.options="classpath:api/users.feature classpath:api/user-groups.feature" -Dkarate.env=qa`
- Directory:
  - `./gradlew test --tests "*CustomRunnerTest" -Dkarate.options="classpath:api" -Dkarate.env=qa`
- Tags:
  - `./gradlew test --tests "*CustomRunnerTest" -Dkarate.options="classpath:api --tags @smoke,~@ignore" -Dkarate.env=qa`

Performance features with Gatling:

- Single service performance folder:
  - `./gradlew gatlingRun -PgatlingSimulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:services/user-service/performance" -Dkarate.env=qa -Dthreads=5`
- Multiple services:
  - `./gradlew gatlingRun -PgatlingSimulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:services/user-service/performance classpath:services/payments-service/performance" -Dkarate.env=qa -Dthreads=10`

## Convenience Makefile Targets

From the repository root, you can also use:

- All tests: `make test env=qa threads=5`
- Smoke-only: `make test-smoke env=qa threads=5`
- Contracts: `make test-contract env=qa`
- Performance (Gatling): use `gatlingRun -PgatlingSimulationClass=performance.simulations.KaratePerformanceSimulation` commands above; the legacy `testPerf` task is deprecated.
- Mock server: `make mock`

## Reports

- Karate HTML: `karate-microservices-testing/build/karate-reports/karate-summary.html`
- Cucumber JSON: enabled by some runners; outputs under `build/` directories.
- Gatling HTML: `karate-microservices-testing/build/reports/gatling/<simulation-name>-<timestamp>/index.html`

## Tips

- Ensure `BASE_URL` and `AUTH_BASE_URL` align with your environment; `karate-config.js` computes defaults based on `karate.env` and optional mock server.
- JWT login and dynamic headers are wired via `login.feature` and `common-headers.js`; failures like `403` often indicate missing or invalid tokens.
- Use `--tags ~@ignore` to exclude ignored scenarios if not already set.
- For Gatling, pass `-Dkarate.options` and injection properties (e.g., `-Dinjection`, `-DusersPerSec`, `-DdurationSeconds`) to shape load; use `gatlingRun -PgatlingSimulationClass=<FQN>` to avoid interactive prompts.
