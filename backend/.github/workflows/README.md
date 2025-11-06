# GitHub Actions Workflows

This directory contains GitHub Actions workflows for automated CI/CD of the Java backend.

## Workflows

### 1. Build (`build.yml`)
**Trigger:** On every push and pull request to any branch

A simple workflow that:
- Builds the application
- Runs all tests (including integration tests with Testcontainers)
- Generates coverage reports
- Uploads artifacts (JAR files, test reports, coverage reports)

**Use this for:** Quick validation of builds on feature branches

---

### 2. CI (`ci.yml`)
**Trigger:** On push/PR to main, develop, and feature/** branches

A comprehensive CI workflow with multiple jobs:

#### Jobs:
1. **build** - Compiles the application and uploads JAR artifacts
2. **test** - Runs tests with Testcontainers and generates coverage reports
3. **allure-report** - Generates Allure test reports and publishes to GitHub Pages
4. **code-quality** - Runs SonarCloud analysis (requires SONAR_TOKEN)
5. **docker-build** - Builds and pushes Docker images (only on main/develop)
6. **dependency-check** - Scans for vulnerable dependencies

**Use this for:** Complete CI validation with reporting and quality checks

---

### 3. Release (`release.yml`)
**Trigger:** Manual (workflow_dispatch)

A manual release workflow that:
- Updates version in build.gradle
- Builds and tests the application
- Creates Docker images with version tags
- Pushes to Docker Hub
- Creates a GitHub Release with artifacts

**How to trigger:**
1. Go to Actions tab in GitHub
2. Select "Release" workflow
3. Click "Run workflow"
4. Enter the version number (e.g., 1.0.0)
5. Click "Run workflow"

---

## Required Secrets

Configure these secrets in your GitHub repository settings (Settings → Secrets and variables → Actions):

### Optional Secrets:
- `CODECOV_TOKEN` - For uploading coverage to Codecov.io
- `SONAR_TOKEN` - For SonarCloud code quality analysis
- `DOCKER_USERNAME` - Docker Hub username for pushing images
- `DOCKER_PASSWORD` - Docker Hub password/token

### Automatic Secrets:
- `GITHUB_TOKEN` - Automatically provided by GitHub Actions

---

## Features

### Docker Support
- Uses Testcontainers for integration tests
- GitHub Actions provides Docker daemon automatically
- Jib builds optimized Docker images without Docker daemon

### Caching
- Gradle dependencies are cached using `actions/setup-java@v4`
- SonarCloud packages are cached for faster scans
- Reduces build times significantly

### Test Reports
- JUnit test results uploaded as artifacts
- Allure reports with coverage integration
- JaCoCo coverage reports with badge generation
- Reports retained for 30 days

### Code Coverage
- JaCoCo generates coverage reports
- Coverage uploaded to Codecov (if token provided)
- Coverage badge generated automatically
- Minimum 70% coverage threshold enforced

### Artifacts
- JAR files (7 days retention)
- Test results (30 days retention)
- Coverage reports (30 days retention)
- Allure reports (30 days retention)

---

## GitHub Pages Setup (for Allure Reports)

To enable Allure report publishing to GitHub Pages:

1. Go to repository Settings → Pages
2. Under "Source", select "Deploy from a branch"
3. Select the `gh-pages` branch
4. Click Save

Reports will be available at: `https://<username>.github.io/<repository>/allure-report/<run-number>/`

---

## Gradle Tasks Used

- `./gradlew build` - Compile and build the application
- `./gradlew test` - Run tests with Testcontainers
- `./gradlew jacocoTestReport` - Generate coverage reports
- `./gradlew allureGenerate` - Generate Allure test reports
- `./gradlew jibDockerBuild` - Build Docker image with Jib
- `./gradlew dependencyCheckAnalyze` - Check for vulnerable dependencies
- `./gradlew sonar` - Run SonarCloud analysis

---

## Customization

### Changing Java Version
Update the `JAVA_VERSION` environment variable in each workflow file.

### Modifying Test Coverage Threshold
Edit `build.gradle` → `jacocoTestCoverageVerification` → `minimum` value.

### Adding More Branches for CI
Edit the `on.push.branches` section in `ci.yml`.

### Disabling Jobs
Comment out or remove unwanted jobs from `ci.yml`.

---

## Troubleshooting

### Tests Fail in GitHub Actions but Pass Locally
- Ensure Testcontainers can access Docker (GitHub Actions provides this automatically)
- Check if tests have hardcoded paths or dependencies

### Docker Build Fails
- Verify the Jib configuration in `build.gradle`
- Check Docker Hub credentials are correctly set

### Coverage Report Not Generated
- Ensure tests ran successfully
- Check that `jacocoTestReport` task completed
- Verify `build/reports/jacoco/test/` directory exists

### Allure Report Not Publishing
- Verify GitHub Pages is enabled in repository settings
- Check that `GITHUB_TOKEN` has write permissions
- Ensure `gh-pages` branch exists

---

## Example: Running Workflow Locally

You can test the workflow steps locally:

```bash
# Build and test
./gradlew clean build

# Generate coverage
./gradlew jacocoTestReport

# Generate Allure report
./gradlew allureGenerate

# Build Docker image
./gradlew jibDockerBuild
```
