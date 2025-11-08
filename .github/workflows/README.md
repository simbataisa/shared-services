# GitHub Actions Workflows

This directory contains GitHub Actions workflows for the Shared Services project. All workflows are configured to run from the repository root and properly handle the monorepo structure.

## 📋 Available Workflows

### 1. Backend Build (`backend-build.yml`)

**Quick build validation workflow (no integration tests)**

- **Triggers**: Pull requests only (all branches)
- **Purpose**: Fast feedback on compilation and basic unit tests
- **Jobs**:
  - Build without running tests (`./gradlew clean build -x test`)
  - Optionally run quick unit tests (non-Testcontainers tests)
  - Upload JAR artifacts
  - Upload test reports if available

**When to use**: Runs automatically on PRs for quick validation. Skips integration tests that require Docker/Testcontainers to keep builds fast.

**Note**: This workflow intentionally skips integration tests. Full test suite runs in `backend-ci.yml`.

---

### 2. Backend CI (`backend-ci.yml`)

**Comprehensive CI pipeline with full test coverage and quality checks**

- **Triggers**:
  - Push to: `main`, `develop`, `feature/**`
  - PRs to: `main`, `develop`
- **Jobs**:
  1. **build** - Compile code and create JAR artifacts
  2. **test** - Run **ALL tests including integration tests** with Testcontainers, generate JaCoCo coverage, upload to Codecov
  3. **allure-report** - Generate and publish Allure test reports to GitHub Pages
  4. **code-quality** - Run SonarCloud analysis
  5. **docker-build** - Build and push Docker images (main/develop only)
  6. **dependency-check** - Scan dependencies for vulnerabilities

**Test Infrastructure**:
- **PostgreSQL**: Uses Testcontainers to spin up PostgreSQL 16 containers
- **Kafka**: Uses `@EmbeddedKafka` from Spring Kafka Test (in-memory broker)
- **Tracing**: OpenTelemetry/Jaeger disabled for tests (`management.tracing.enabled=false`)
- **Docker**: GitHub Actions `ubuntu-latest` runners have Docker pre-installed
- **Environment variables**:
  - `TESTCONTAINERS_RYUK_DISABLED=false` - Enable resource cleanup
  - `TESTCONTAINERS_CHECKS_DISABLE=false` - Enable Docker checks
  - `DOCKER_HOST=unix:///var/run/docker.sock` - Docker socket path

**Required Secrets**:
- `CODECOV_TOKEN` - For uploading coverage to Codecov (optional)
- `SONAR_TOKEN` - For SonarCloud integration (optional)
- `DOCKER_USERNAME` - For Docker Hub push (optional)
- `DOCKER_PASSWORD` - For Docker Hub push (optional)

**When to use**: Automatically runs on feature branches and PRs for comprehensive validation including integration tests.

---

### 3. Backend Qodana (`backend-qodana.yml`)

**JetBrains Qodana code quality analysis**

- **Triggers**:
  - Manual: `workflow_dispatch`
  - All PRs
  - Push to: `main`, `feature/github-action-wfl`
- **Features**:
  - Full code quality analysis
  - PR comments with findings
  - Caching for faster runs
  - Annotations in code view

**Required Secrets**:

- `QODANA_TOKEN` - JetBrains Qodana token

**When to use**: Automatically runs on PRs, or manually trigger for deep code analysis.

---

### 4. Backend Release (`backend-release.yml`)

**Manual release workflow for creating versioned releases**

- **Triggers**: Manual only (`workflow_dispatch`)
- **Input**: `version` - Release version (e.g., 1.0.0)
- **Steps**:
  1. Update version in build.gradle
  2. Build release artifacts
  3. Run tests
  4. Build Docker image
  5. Tag and push to Docker Hub
  6. Create GitHub release with JAR artifacts

**Required Secrets**:

- `DOCKER_USERNAME` - For Docker Hub push
- `DOCKER_PASSWORD` - For Docker Hub push

**How to trigger**:

1. Go to Actions tab on GitHub
2. Select "Backend Release" workflow
3. Click "Run workflow"
4. Enter version number (e.g., 1.0.0)

---

## 🗂️ Project Structure

All workflows operate from the repository root and reference the backend service:

```
shared-services/
├── .github/
│   └── workflows/          # All workflows must be here
│       ├── backend-build.yml
│       ├── backend-ci.yml
│       ├── backend-release.yml
│       └── README.md
├── backend/
│   ├── gradlew            # Referenced as: backend/gradlew
│   ├── build.gradle
│   └── src/
└── frontend/
```

**Important**: GitHub Actions only detects workflows in `.github/workflows/` at the repository root. Workflows in subdirectories (e.g., `backend/.github/`) will NOT be detected.

---

## 🚀 Workflow Triggers Matrix

| Workflow        | main | develop | feature/\*\* | All PRs | Manual |
| --------------- | ---- | ------- | ------------ | ------- | ------ |
| backend-build   | ✅   | ✅      | ✅           | ✅      | ❌     |
| backend-ci      | ✅   | ✅      | ✅           | ✅\*    | ❌     |
| backend-qodana  | ✅   | ❌      | ✅\*\*       | ✅      | ✅     |
| backend-release | ❌   | ❌      | ❌           | ❌      | ✅     |

\* Only PRs to `main` or `develop`
\*\* Only `feature/github-action-wfl` branch currently

---

## 🔧 Setup Instructions

### 1. Initial Setup

No setup required for basic functionality. Workflows will run with default settings.

### 2. Optional Integrations

#### Codecov Setup

```bash
# 1. Sign up at https://codecov.io
# 2. Add repository
# 3. Copy token
# 4. Add secret: CODECOV_TOKEN
```

#### SonarCloud Setup

```bash
# 1. Sign up at https://sonarcloud.io
# 2. Import repository
# 3. Copy token
# 4. Add secret: SONAR_TOKEN
```

#### Docker Hub Setup

```bash
# 1. Create Docker Hub account
# 2. Create access token
# 3. Add secrets: DOCKER_USERNAME, DOCKER_PASSWORD
```

#### Qodana Setup

```bash
# 1. Sign up at https://qodana.cloud
# 2. Connect repository
# 3. Copy token
# 4. Add secret: QODANA_TOKEN
```

### 3. Adding GitHub Secrets

Go to: `Settings > Secrets and variables > Actions > New repository secret`

---

## 📊 Artifacts

Workflows generate artifacts that are available for download after runs:

| Workflow      | Artifact                  | Retention | Description             |
| ------------- | ------------------------- | --------- | ----------------------- |
| backend-build | `jar-file`                | 7 days    | Built JAR files (no tests) |
| backend-build | `test-report`             | 7 days    | Unit test reports (if available) |
| backend-ci    | `build-libs`              | 7 days    | Built JAR files         |
| backend-ci    | `jacoco-report`           | 30 days   | JaCoCo coverage HTML    |
| backend-ci    | `test-results`            | 30 days   | JUnit test results      |
| backend-ci    | `allure-results`          | 30 days   | Allure test results     |
| backend-ci    | `allure-report`           | 30 days   | Allure HTML report      |
| backend-ci    | `dependency-check-report` | 30 days   | OWASP dependency report |

---

## 🐛 Troubleshooting

### Workflow not appearing

- Ensure file is in `.github/workflows/` at repository root
- Ensure file has `.yml` or `.yaml` extension
- Check YAML syntax is valid
- Push the commit containing the workflow file

### Workflow not triggering

- Check the `on:` section matches your branch/event
- For PRs: workflow must exist in the base branch
- For new workflows: may need to push a new commit to trigger

### Integration tests failing with connection errors

**For PostgreSQL/Testcontainers failures:**
- Ensure Docker is available (GitHub Actions `ubuntu-latest` has it pre-installed)
- Check Testcontainers configuration in workflow
- Verify `DOCKER_HOST` environment variable is set
- For local development, ensure Docker Desktop is running
- Integration tests require:
  - Docker daemon running
  - Sufficient resources (memory, disk space)
  - Network access for pulling PostgreSQL images

**For Kafka connection failures:**
- Tests use `@EmbeddedKafka` (in-memory broker) - no external Kafka needed
- Check that `spring-kafka-test` dependency is present
- Ensure `@EmbeddedKafka` annotation is on `BaseIntegrationTest`

**For Jaeger/OpenTelemetry failures:**
- Tracing is disabled in tests via `management.tracing.enabled=false`
- No external Jaeger instance required
- If still seeing issues, add to `@DynamicPropertySource`:
  ```java
  registry.add("management.tracing.enabled", () -> false);
  ```

### Gradle permission denied

```bash
# Fixed in workflows with:
chmod +x backend/gradlew
```

### Build failures

- Check Java version (should be 21)
- Verify gradlew wrapper exists in backend/
- Check all paths include `backend/` prefix
- Review workflow logs on GitHub Actions tab

### Secrets not working

- Verify secret names match exactly (case-sensitive)
- Secrets are not available for fork PRs
- Use `${{ secrets.SECRET_NAME }}` syntax

---

## 🔄 Workflow Execution Flow

### Build Pipeline (backend-ci.yml)

```
build (parallel)
  ↓
test → allure-report → code-quality
  ↓
docker-build (main/develop only)

dependency-check (parallel)
```

### Simple Build (backend-build.yml)

```
build-and-test
  ├── Build
  ├── Test
  └── Upload artifacts
```

---

## 📝 Maintenance

### Updating Dependencies

- GitHub Actions versions are pinned (e.g., `@v4`)
- Review and update quarterly
- Test in feature branch before merging

### Adding New Workflows

1. Create `.yml` file in this directory
2. Define `name`, `on`, and `jobs`
3. Test in feature branch
4. Document in this README
5. Merge to main

### Modifying Existing Workflows

1. Create feature branch
2. Modify workflow file
3. Push and test
4. Update this README if triggers/jobs change
5. Create PR for review

---

## 📚 References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/reference/workflow-syntax-for-github-actions)
- [GitHub Actions Marketplace](https://github.com/marketplace?type=actions)
- [Gradle GitHub Actions](https://github.com/gradle/actions)

---

## 🤝 Contributing

When adding or modifying workflows:

1. Test in a feature branch first
2. Ensure all paths reference `backend/` correctly
3. Add appropriate error handling
4. Update this README with changes
5. Keep secrets documented but never commit actual values

---

_Last updated: 2025-11-06_
