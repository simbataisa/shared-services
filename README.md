# Shared Services Platform

A comprehensive enterprise-grade platform for managing shared services with multi-tenant support, role-based access control (RBAC), and modern web technologies.

## 🏗️ Architecture Overview

This is a full-stack application built with:

- **Backend**: Java 21 + Spring Boot 3.3.4 + PostgreSQL
- **Frontend**: React 19 + TypeScript + Vite + TailwindCSS
- **Database**: PostgreSQL with Flyway migrations
- **Authentication**: JWT-based authentication with comprehensive permission system

## 🚀 Key Features

### Core Platform (AHSS Core)

- **Dashboard**: Main overview and system monitoring
- **System Configuration**: Platform-wide settings management
- **Audit Logging**: Comprehensive system audit trails

### User Management System

- **User Administration**: Complete user lifecycle management
- **Authentication**: Secure JWT-based authentication
- **Profile Management**: User profiles and preferences
- **User Groups**: Organizational user grouping

### Multi-Tenant Support

- **Tenant Administration**: Multi-tenant organization management
- **Organization Structure**: Hierarchical organization management
- **Tenant Configuration**: Tenant-specific settings

### Role-Based Access Control (RBAC)

- **Role Management**: Comprehensive role definition and management
- **Permission Management**: Granular permission system
- **Advanced RBAC**: Role-based and attribute-based access control

### Payment Management

- **Payment Requests**: Full lifecycle management (DRAFT → APPROVED → PENDING → PROCESSING → COMPLETED/FAILED/CANCELLED; post-payment VOIDED/REFUNDED/PARTIAL_REFUND)
- **State Machine**: Documented transitions and rules ([technical doc](./payment-request-state-machine.md), [PRD](.trae/documents/payment-prd.md))
- **Gateway Integrations**: Modular integrator for Stripe, PayPal, Bank Transfer ([guide](backend/PAYMENT-INTEGRATOR-FACTORY-GUIDE.md))
- **Refunds & Voids**: Supports full and partial refunds, void operations with audit trail
- **Webhooks & Notifications**: Asynchronous processing and callback handling
- **Unique Identifiers**: Standardized payment UUIDs and tokenization ([technical guide](.trae/documents/payment-uuid-technical-guide.md))
- **Testing**: Karate mock server and data‑driven tests ([module](karate-microservices-testing/README.md))

### Additional Systems

- **Analytics & Reporting**: Business intelligence platform
- **Integration Hub**: API gateway and integration services (Draft)
- **Payment Request State Machine**: Documented lifecycle, transitions, and rules ([technical doc](./payment-request-state-machine.md))
- **Microservices Testing**: Karate-based API and performance test suite (`karate-microservices-testing`)

## 🛠️ Technology Stack

### Backend

- **Runtime**: Java 21
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA + Hibernate
- **Migration**: Flyway
- **Build Tool**: Gradle
- **Authentication**: JWT with comprehensive user claims

### Frontend

- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite
- **UI Components**: Shadcn/UI + Radix UI
- **Styling**: TailwindCSS
- **Routing**: React Router DOM v7
- **State Management**: Zustand
- **HTTP Client**: Axios
- **Form Handling**: React Hook Form
- **Validation**: Zod
- **Icons**: Lucide React
- **Date Handling**: date-fns

### Infrastructure

- **Database**: PostgreSQL 16 (Docker)
- **Containerization**: Docker Compose
- **Development**: Hot reload for both frontend and backend

### Testing & QA

- **API Testing**: Karate framework for microservices end-to-end and data-driven tests
- **Mocking**: Built-in mock server and custom runners for isolated scenarios
- **Performance**: Gatling integration via Karate for load and stress testing
- **Project**: `karate-microservices-testing/` module with Gradle tasks and detailed guides

## 🧭 Observability (Tracing)

This project uses OpenTelemetry for distributed tracing. Spans are produced by Spring (Micrometer Tracing) and the OpenTelemetry Java agent, sent to an OpenTelemetry Collector, and visualized in Jaeger.

### Components

- **Backend**: emits OTLP HTTP traces to `http://localhost:4318/v1/traces`.
- **Collector**: receives OTLP HTTP on `:4318`, batches spans, and exports to Jaeger over OTLP gRPC `:4317`.
- **Jaeger**: UI at `http://localhost:16686` for searching and viewing traces.

### Start All Observability Services

```bash
docker compose --profile observability up -d
```

This starts PostgreSQL, Kafka, OpenTelemetry Collector, Jaeger, and Kafka UI.

Collector config (`otel-collector-config.yaml`):

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

### Run Backend with Java Agent

The agent auto-instruments HTTP server and JDBC/Hibernate.

```bash
cd backend
JAVA_TOOL_OPTIONS="-javaagent:../otel-javaagent.jar" \
OTEL_SERVICE_NAME=sharedservices-backend \
OTEL_TRACES_EXPORTER=otlp \
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318 \
OTEL_METRICS_EXPORTER=none \
OTEL_LOGS_EXPORTER=none \
./gradlew bootRun
```

Spring configuration also enables Micrometer Tracing:

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces
```

### Verify Tracing

- Trigger activity:
  ```bash
  curl -X GET 'http://localhost:8080/api/v1/tenants/search?query=demo'
  # or
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H 'Content-Type: application/json' \
    -d '{"username": "admin@ahss.com", "password": "wrong"}'
  ```
- Check collector logs: `docker-compose logs -f otel-collector`.
- Open Jaeger UI (`http://localhost:16686`), select service `sharedservices-backend`, search traces.

Notes:

- If you see agent warnings about logs export (`404` to `:4318`), set `OTEL_LOGS_EXPORTER=none` or add a logs pipeline to the collector.

### Convenience Gradle Task

Run with the agent attached via a dedicated task:

```bash
cd backend
./gradlew bootRunWithAgent
```

### Containerized Backend with Agent

_Build the backend image and start all services:_

```bash
# Build the backend image with Jib
cd backend && ./gradlew dockerBuild

# Start all services (includes backend with agent)
docker compose --profile observability up -d
```

_Platform-specific builds:_

```bash
# Apple Silicon (M1/M2) - defaults to linux/arm64
cd backend && ./gradlew dockerBuild -x test

# Windows (x86_64) - build amd64 image
cd backend && ./gradlew dockerBuildWindows -x test
# Or with property override:
cd backend && ./gradlew dockerBuild -x test -PjibTargetArch=amd64
```

## Windows Setup

### Quick Start (Automated Script)

Windows users can use the automated batch script for one-command setup:

```bash
# Clone and navigate to project
git clone <repository-url>
cd shared-services

# Run the automated setup script
.\run-all.bat
```

The `run-all.bat` script will:

1. Check Docker prerequisites
2. Build backend image using `jibDockerBuild` (faster on Windows)
3. Build frontend image
4. Start all services with observability profile
5. Display access URLs and mock server instructions

### Manual Setup (Advanced)

For manual control, use these commands:

```bash
# Build backend image (amd64)
cd backend && ./gradlew dockerBuildWindows

# Start all services with observability profile
docker compose -f docker-compose.windows.yml --profile observability up -d

# Stop all services
docker compose -f docker-compose.windows.yml --profile observability down
```

Alternative build options:

```bash
# Using property override
cd backend && ./gradlew dockerBuild -PjibTargetArch=amd64

# If Gradle can't find Docker, pass the executable path
cd backend; ./gradlew dockerBuildWindows -Djib.dockerClient.executable="$((Get-Command docker).Source)"
```

**Why `jibDockerBuild` is recommended for Windows:**

- Builds directly to Docker daemon (no intermediate tar file)
- Resolves "stuck at 88%" issue during Docker image creation
- Significantly faster on Windows systems

Access URLs (same as macOS/Linux):

```bash
Frontend: http://localhost:5173
Backend:  http://localhost:8080
Jaeger:   http://localhost:16686
Kafka UI: http://localhost:8081
```

Notes (containers):

- Inside Docker, never use `localhost` to reach other services; use the Compose service names.
- OTLP endpoints are configured to `http://otel-collector:4318` for the Java agent.
- Spring Micrometer OTLP can be set via `MANAGEMENT_OTLP_TRACING_ENDPOINT=http://otel-collector:4318/v1/traces`.
- See troubleshooting guide: `.trae/documents/docker-compose-troubleshooting.md`.

## Docker Compose Troubleshooting

- Error: `failed to set up container networking: network <id> not found`

  - Cause: Named containers (e.g., `sharedservices-backend`, `sharedservices-frontend`, `sharedservices-kafka-ui`) may reference a deleted network.
  - Fix:

    ```bash
    # Stop and remove resources and orphans
    docker compose down --remove-orphans

    # Prune dangling networks
    docker network prune -f

    # Remove stale named containers
    docker rm -f sharedservices-backend sharedservices-frontend sharedservices-kafka-ui

    # Recreate services (macOS/Linux)
    docker compose --profile observability up -d

    # Windows
    docker compose -f docker-compose.windows.yml --profile observability up -d
    ```

## 📝 Recent Improvements

### ✅ Completed (Latest Updates)

**PostgreSQL Stability Fix (NEW):**

- ✅ Switched from `postgres:16-alpine` to `postgres:16` (Debian-based)
- ✅ Resolved Alpine Linux memory accounting incompatibility with Docker cgroup limits
- ✅ Fixed false OOM kills that occurred despite low memory usage (30MB of 3GB)
- ✅ PostgreSQL now running stable with proper memory management
- ✅ Updated all 4 docker-compose files for consistency

**Docker Build Support (NEW):**

- ✅ Added multi-stage Dockerfile for backend - build inside Docker with no local Java required
- ✅ Created `run-all-docker-build.sh` and `run-all-docker-build.bat` scripts
- ✅ New `docker-compose-build.yml` for Docker-based builds
- ✅ **Zero local dependencies** - only Docker required, no Java or Node.js installation needed
- ✅ Consistent builds across all platforms (Windows/macOS/Linux)
- ✅ Faster builds with Docker layer caching
- ✅ Production-ready multi-stage builds with optimized runtime images

**Windows Compatibility:**

- ✅ Added `run-all.bat` script for Windows users
- ✅ Uses `gradlew.bat` and `jibDockerBuild` for optimal Windows performance
- ✅ Resolves Docker build hanging issues on Windows (stuck at 88%)
- ✅ Full feature parity with Linux/macOS `run-all.sh` script
- ✅ Windows-specific `docker-compose-build.windows.yml` for Docker builds

**Backend Test Fixes:**

- ✅ Fixed JWT Authentication Filter to properly create `UserPrincipal` objects
- ✅ Fixed `JwtAuthenticationFilterTest` ClassCastException - now correctly accesses principal
- ✅ Fixed `PaymentRefundServiceImplTest` authentication requirement - added security context setup
- ✅ Fixed `PayPalIntegratorTest` refund response handling - proper PayPalAmount structure and status mapping
- ✅ **All 438 backend tests now passing** (was 435/438)

**Karate Testing Module Improvements:**

- ✅ Upgraded Gradle from 8.14 to 9.1.0 for consistency with backend module
- ✅ Fixed JUnit Platform launcher dependency issue (required for Gradle 9.x)
- ✅ Enabled configuration cache for 90%+ faster subsequent builds (3s → 322ms)
- ✅ Added performance optimizations in `gradle.properties`
- ✅ Cross-platform consistency - same behavior on Windows, macOS, and Linux

**Karate Mock Server Fixes:**

- ✅ Fixed Bank Transfer mock server JavaScript syntax error
- ✅ Resolved reserved keyword `error` usage in ternary operators
- ✅ Refund flow now working correctly with proper response structure

**Key Bug Fixes:**

- ✅ `JwtAuthenticationFilter.java:64` - Changed from String principal to UserPrincipal object
- ✅ `PaymentRefundServiceImpl` - Now properly validates authenticated users
- ✅ PayPal integration - Fixed COMPLETED status handling for successful refunds
- ✅ Mock server - Fixed JavaScript evaluation errors in Karate DSL
- ✅ Karate tests - Added `junit-platform-launcher` dependency for Gradle 9.x compatibility

## 📋 Prerequisites

### For Option 1B (Docker Build - Recommended for Training)

- **Docker & Docker Compose** (required)
- **Git** (required)

### For Option 1 (Local Build - Development)

- **Java 21** or higher
- **Node.js 18** or higher
- **Docker & Docker Compose**
- **Git**

> **💡 Tip:** If you're new to this project or attending training, we recommend using **Option 1B** (Docker Build) which only requires Docker and Git.

### Install Java 21

#### Windows

- Using Winget (recommended):

```bash
winget install --id EclipseAdoptium.Temurin.21.JDK -e
```

- Using Chocolatey (alternative):

```bash
choco install temurin21
```

- Manual download (alternative):

  - Download the Temurin 21 JDK MSI from: https://adoptium.net/temurin/releases/?version=21
  - Run the installer and follow the prompts.

- Set `JAVA_HOME` and update `PATH` (if not set by installer):

```powershell
# Adjust path if your install directory differs
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21"
setx PATH "%PATH%;%JAVA_HOME%\bin"
```

- Verify installation:

```powershell
java -version
# Expected: openjdk version "21" ...
```

#### macOS / Linux (SDKMAN)

- Install SDKMAN:

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

- List available Java 21 distributions and choose Temurin:

```bash
sdk list java
```

- Install Temurin 21 (replace with the latest identifier from the list):

```bash
# Example identifier; use the latest 21.x-tem shown by `sdk list java`
sdk install java 21.0.5-tem
sdk default java 21.0.5-tem
```

- Verify installation:

```bash
java -version
# Expected: openjdk version "21" ...
```

Notes:

- On macOS with Homebrew, an alternative is `brew install openjdk@21` and adding `export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"` to your shell init file.
- Ensure your terminal is restarted after changing environment variables.

## 🚀 Quick Start

> **📚 For Training Participants:**
> If you're attending a training session and want to avoid Java/Node.js installation issues, **skip to Option 1B below** and use the `run-all-docker-build` scripts instead. This requires only Docker and works reliably across all platforms, especially Windows.

### Option 1: Automated Setup with Local Build (Recommended for Development)

Use the provided script to build locally using Gradle/npm and run in Docker:

#### macOS/Linux

```bash
git clone <repository-url>
cd shared-services
./run-all.sh
```

#### Windows

```bash
git clone <repository-url>
cd shared-services
.\run-all.bat
```

**Requirements:** Java 21, Node.js 18+, Docker

**Note:** If you encounter build issues on Windows, use Option 1B instead.

The script will:

1. ✅ Check prerequisites (Docker, Docker Compose)
2. ✅ Build backend Docker image using Jib (local Gradle build)
3. ✅ Build frontend Docker image
4. ✅ Stop any existing containers
5. ✅ Start all services with observability profile
6. ✅ Wait for services to be ready
7. ✅ Display service status and access URLs
8. ✅ Show mock server configuration instructions

#### Platform Overrides & Help

Use an argument to force the backend container image architecture, or show help:

```bash
# macOS/Linux
./run-all.sh --help
./run-all.sh windows        # linux/amd64 image
./run-all.sh linux          # linux/amd64 image
./run-all.sh apple-silicon  # linux/arm64 image

# Windows
run-all.bat --help
run-all.bat windows         # linux/amd64 image
run-all.bat linux           # linux/amd64 image
run-all.bat apple-silicon   # linux/arm64 image
```

No argument provided:

- macOS Apple Silicon → builds `linux/arm64`
- macOS Intel → builds `linux/amd64`
- Linux x86_64 → builds `linux/amd64`
- Linux arm64/aarch64 → builds `linux/arm64`
- Windows batch script defaults to `linux/amd64` (`dockerBuildWindows`)

Advanced overrides (equivalent without using script args):

```bash
cd backend
./gradlew dockerBuild -x test -PjibTargetArch=amd64   # force amd64
./gradlew dockerBuild -x test -PjibTargetArch=arm64   # force arm64
```

### Option 1B: Automated Setup with Docker Build (Recommended for Training & Windows)

> **✅ Recommended for:**
> - Training participants (bank trainees)
> - Windows users
> - Users who don't want to install Java 21 or Node.js
> - Consistent, reliable builds across all platforms

Build everything inside Docker containers - **no local Java or Node.js installation required**:

#### macOS/Linux

```bash
git clone <repository-url>
cd shared-services
./run-all-docker-build.sh
```

#### Windows

```bash
git clone <repository-url>
cd shared-services
.\run-all-docker-build.bat
```

**Requirements:** Docker only (no Java or Node.js needed)

The script will:

1. ✅ Check prerequisites (Docker only)
2. ✅ Build backend inside Docker using multi-stage build
3. ✅ Build frontend inside Docker using multi-stage build
4. ✅ Stop any existing containers
5. ✅ Start all services with observability profile
6. ✅ Display service status and access URLs

**Key Benefits:**

- 🚀 No local Java or Node.js installation required
- 🐳 Consistent builds across all platforms
- 📦 Isolated build environment
- ⚡ Cached Docker layers for faster subsequent builds
- 🎯 **Especially reliable on Windows machines**
- 👥 **Perfect for training environments**

> 📚 **For detailed comparison and best practices**, see [DOCKER-BUILD-GUIDE.md](./DOCKER-BUILD-GUIDE.md)

### Option 2: Manual Setup

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd shared-services
```

#### 2. Build Images

##### Option A: Local Build (Requires Java 21 + Node.js)

```bash
# Build backend using local Gradle
cd backend && ./gradlew dockerBuild && cd ..

# Build frontend
docker compose build frontend
```

##### Option B: Docker Build (Docker Only - No Local Tools)

```bash
# Build both backend and frontend inside Docker
docker compose -f docker-compose-build.yml build

# Or build individually
docker compose -f docker-compose-build.yml build backend
docker compose -f docker-compose-build.yml build frontend
```

#### 3. Start All Services

##### If you used Local Build (Option A):

```bash
# macOS/Linux
docker compose --profile observability up -d

# Windows
docker compose -f docker-compose.windows.yml --profile observability up -d
```

##### If you used Docker Build (Option B):

```bash
# macOS/Linux
docker compose -f docker-compose-build.yml --profile observability up -d

# Windows
docker compose -f docker-compose-build.windows.yml --profile observability up -d
```

This starts all required services:

- **PostgreSQL** - Database
- **Kafka** - Message broker (port 9092 for host, 29092 for container-to-container)
- **OpenTelemetry Collector** - Trace collection
- **Jaeger** - Distributed tracing UI
- **Kafka UI** - Kafka management console
- **Backend** - Spring Boot application
- **Frontend** - React application

**Note on Kafka Configuration:**

- Host applications (running on your machine) connect to Kafka at `localhost:9092` (default in `application.yml`)
- Docker containers connect to Kafka at `kafka:29092` (overridden by `SPRING_KAFKA_BOOTSTRAP_SERVERS` environment variable)
- This dual-listener setup allows both host and containerized services to communicate with Kafka properly

**Configuration Details:**

```yaml
# application.yml (default for local development)
spring:
  kafka:
    bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

# docker-compose.yml (override for containers)
environment:
  SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
```

### 3. Access the Application

```bash
Frontend:  http://localhost:5173
Backend:   http://localhost:8080
Jaeger:    http://localhost:16686
Kafka UI:  http://localhost:8081
```

### 4. Running with Karate Mock Server (For Testing)

The backend in Docker is configured to connect to payment gateway mocks running on your host machine:

#### macOS/Linux

```bash
# In a separate terminal, start the Karate mock server
cd karate-microservices-testing
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=1000
```

#### Windows

```bash
# In a separate terminal, start the Karate mock server
cd karate-microservices-testing
.\gradlew.bat test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=1000
```

The Docker container will reach the mock server at `host.docker.internal:8090`. This allows:

- ✅ Backend in Docker to call mock Stripe, PayPal, BankTransfer APIs
- ✅ Payment gateway integration testing without external API calls
- ✅ Isolated testing environment

**How it works:**

- The backend uses `MOCK_SERVER_HOST=host.docker.internal` environment variable
- Mock URLs: `http://host.docker.internal:8090/stripe/*`, `/paypal/*`, `/bank-transfer/*`
- For local development (not Docker), URLs default to `http://localhost:8090/*`

**Mock Server Features:**

- **Stripe Mock**: Payment creation, capture, refund with realistic responses
- **PayPal Mock**: Order creation, capture, refund with COMPLETED/CREATED statuses
- **Bank Transfer Mock**: Transfer initiation and refund with state validation
- **Configurable Delays**: Simulate network latency with `-Dmock.block.ms` parameter
- **Error Scenarios**: Test failure cases with proper error responses

**Troubleshooting:**

If you encounter mock server errors:

```bash
# Check if mock server is running
curl http://localhost:8090/stripe/health

# View mock server logs
# The mock server runs in the terminal where you started it

# Restart mock server if needed
# Ctrl+C to stop, then run the gradlew command again
```

### 5. Stop All Services

```bash
# macOS/Linux
docker compose --profile observability down

# Windows
docker compose -f docker-compose.windows.yml --profile observability down
```

## 🗄️ Database Schema

### Core Entities

- **Product**: Main product/service definitions
- **Module**: Product components and features
- **Role**: System roles with hierarchical structure
- **Permission**: Granular permission system
- **User**: System users with profiles
- **UserGroup**: User organization and grouping
- **Tenant**: Multi-tenant support
- **Organization**: Hierarchical organization structure

### Key Features

- **Audit Fields**: All entities include created/updated timestamps and user tracking
- **Status Management**: Comprehensive status system (ACTIVE, INACTIVE, DRAFT, etc.)
- **Soft Delete**: Safe deletion with recovery options
- **Multi-tenant**: Full multi-tenant architecture support

## 🔐 Authentication & Authorization

### JWT Authentication

- **Login Endpoint**: `POST /api/v1/auth/login`
- **Token Claims**: Includes user info, roles, permissions, and admin flags
- **Expiration**: 24 hours (configurable)

### Permission System

- **Format**: `{resource}:{action}` (e.g., `users:read`, `tenants:create`)
- **Resources**: users, tenants, roles, permissions, modules, products, audit, system
- **Actions**: read, create, update, delete, admin

### Sample Permissions

```
users:read, users:create, users:update, users:delete, users:admin
tenants:read, tenants:create, tenants:update, tenants:delete, tenants:admin
roles:read, roles:create, roles:update, roles:delete, roles:admin
products:read, products:create, products:update, products:delete
```

## 📚 API Documentation

### Base URLs

- **Authentication**: `/api/v1/auth`
- **Users**: `/api/v1/users`
- **User Groups**: `/api/v1/user-groups`
- **Products**: `/api/products`
- **Modules**: `/api/modules`
- **Roles**: `/api/roles`
- **Permissions**: `/api/permissions`
- **Dashboard**: `/api/v1/dashboard`

### Sample API Calls

#### Authentication

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin@ahss.com", "password": "password"}'
```

#### Get Dashboard Stats

```bash
curl -X GET http://localhost:8080/api/v1/dashboard/stats \
  -H "Authorization: Bearer <jwt-token>"
```

## 🧪 Testing

### Backend Testing

The backend has comprehensive test coverage with 438+ unit tests covering:

- **Integration Tests**: Payment gateway integrations (Stripe, PayPal, Bank Transfer)
- **Security Tests**: JWT authentication, authorization, UserPrincipal handling
- **Service Tests**: Payment requests, refunds, tenant management, user management
- **Repository Tests**: Database operations and JPA queries
- **Mock Tests**: External API interactions with proper mocking

```bash
cd backend

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "PayPalIntegratorTest"

# Run tests with specific tags
./gradlew test --tests "*Integration*"

# View test reports
# Open: backend/build/reports/tests/test/index.html
```

**Test Results:**

- Total Tests: 438
- Passed: 438 ✅
- Failed: 0
- Skipped: 0

**Key Test Coverage:**

- ✅ JWT Authentication Filter with UserPrincipal
- ✅ Payment Gateway Integration (Stripe, PayPal, Bank Transfer)
- ✅ Payment Refund Service with Security Context
- ✅ Multi-tenant Operations
- ✅ Role-Based Access Control (RBAC)
- ✅ Audit Trail Functionality

### Karate API Testing

End-to-end API and integration tests using Karate framework:

```bash
cd karate-microservices-testing

# Run all API tests
./gradlew test

# Run specific feature tests
./gradlew test --tests "*PaymentRefundFlowTest"

# Run with mock server
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=1000

# Run custom tests with mock server enabled
./gradlew cleanTest test --tests "*CustomRunnerTest" \
  -Dkarate.env=qa \
  -Dmock.server.enabled=true \
  -Dmock.port=8090 \
  --info \
  -Dkarate.options="classpath:api"
```

**Mock Server Configuration:**

- Mock endpoints for Stripe, PayPal, and Bank Transfer
- Configurable response delays for testing timeout scenarios
- Support for both success and error scenarios
- Stateful refund processing simulation

### Frontend Testing

```bash
cd frontend
npm run lint
```

## 🔧 Configuration

### Environment Variables

#### Backend

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sharedservices
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
JWT_SECRET=your-secret-key
```

#### Frontend

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## 🚀 Deployment

### Docker Deployment

```bash
# Start all services with observability
docker compose --profile observability up -d

# View logs
docker compose logs -f

# Stop all services
docker compose --profile observability down
```

## 🧰 Docker Operations

Common commands for inspecting and debugging services.

### List Running Services

```bash
# macOS/Linux
docker compose ps

# Windows
docker compose -f docker-compose.windows.yml ps
```

### Tail Logs (Follow)

```bash
# Specific service via Compose (recommended)
docker compose logs -f --tail=200 backend

# All services
docker compose logs -f --tail=100

# Filter by timeframe
docker compose logs -f --since=10m backend

# By container name (if needed)
docker logs -f --tail=200 sharedservices-backend
```

### Inspect Processes in Containers

```bash
# Show running processes per service
docker compose top
```

### Exec Into a Container

```bash
# macOS/Linux
docker compose exec backend sh

# If bash is available
docker compose exec backend bash

# Windows
docker compose -f docker-compose.windows.yml exec backend sh
```

### Restart or Recreate Services

```bash
# Restart a single service
docker compose restart backend

# Recreate a service (useful after config changes)
docker compose up -d --force-recreate backend

# Recreate all services
docker compose up -d --force-recreate
```

### Stop, Remove, and Clean Up

```bash
# Stop and remove services and networks
docker compose down --remove-orphans

# Also remove volumes (DANGEROUS: deletes data)
docker compose down -v --remove-orphans

# Prune dangling networks
docker network prune -f
```

## 🔧 Common Issues and Solutions

### PostgreSQL Container Crashing with OOM Kills (FIXED)

**Problem:** PostgreSQL container crashes and restarts, showing OOMKilled: true and Exit Code 137, even when using very low memory (e.g., 30MB of 3GB).

**Root Cause:** Alpine Linux (postgres:16-alpine) has memory accounting incompatibility with Docker's cgroup limits, causing false OOM kills.

**Solution (FIXED in latest version):** Switched to Debian-based PostgreSQL image (`postgres:16`). The Debian version properly respects Docker memory limits.

```bash
# Verify you're using the Debian image:
docker inspect sharedservices-postgres --format='{{.Config.Image}}'
# Should show: postgres:16 (NOT postgres:16-alpine)

# Check OOM status:
docker inspect sharedservices-postgres --format='OOMKilled: {{.State.OOMKilled}}'
# Should show: OOMKilled: false
```

If you're still on Alpine, pull the latest code and recreate the container:
```bash
git pull
docker compose down
docker compose --profile observability up -d
```

### Windows Docker Build Stuck at 88%

**Problem:** Docker build hangs at 88% with message "taking some time" during `jibDockerBuild`.

**Solution:** Use the `run-all.bat` script which uses `jibDockerBuild` instead of `dockerBuild`. The `jibDockerBuild` task builds directly to Docker daemon without creating intermediate tar files.

```bash
# Instead of manual build, use:
.\run-all.bat

# Or directly:
cd backend
.\gradlew.bat jibDockerBuild
```

### Backend Unit Tests Failing

**Problem:** Tests fail with authentication or ClassCastException errors.

**Solution:** All 438 tests should pass. If you encounter failures:

1. Ensure you're using Java 21
2. Clean and rebuild:
   ```bash
   cd backend
   ./gradlew clean test
   ```
3. Check for proper security context setup in service tests
4. Verify UserPrincipal usage instead of String for JWT principal

### Mock Server JavaScript Errors

**Problem:** Karate mock server returns 500 errors with "js failed" message.

**Solution:** This has been fixed in the latest version. Ensure you have the latest code where reserved JavaScript keywords (like `error`) are properly handled.

```bash
# Update your repository
git pull origin main

# Restart mock server
cd karate-microservices-testing
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=1000
```

### PayPal Refund Test Failures

**Problem:** PayPal integration tests fail with "expected: <true> but was: <false>".

**Solution:** This has been fixed. The PayPal mock now properly returns:

- `COMPLETED` status for successful refunds
- Proper `PayPalAmount` object structure with `currencyCode` and `value`

### JWT Authentication Not Working

**Problem:** API calls fail with 401 Unauthorized even with valid token.

**Solution:** Ensure the JWT filter is creating `UserPrincipal` objects correctly:

```java
// Correct usage (fixed in latest version)
UserPrincipal userPrincipal = new UserPrincipal(userId, username);
var auth = new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);

// Incorrect usage (old version)
// var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
```

### Docker Container Can't Connect to Mock Server

**Problem:** Backend in Docker can't reach mock server on host machine.

**Solution:**

1. Ensure mock server is running on host: `http://localhost:8090`
2. Backend should use `host.docker.internal:8090` (configured automatically)
3. Verify environment variable: `MOCK_SERVER_HOST=host.docker.internal`

```bash
# Check mock server is accessible
curl http://localhost:8090/stripe/health

# Check backend logs
docker compose logs -f backend
```

### Database Connection Issues

**Problem:** Backend can't connect to PostgreSQL.

**Solution:**

1. Ensure PostgreSQL is running:
   ```bash
   docker compose ps postgres
   ```
2. Check connection settings in `application.yml`
3. Verify Docker network connectivity

### Port Already in Use

**Problem:** Can't start services because port is already in use.

**Solution:**

```bash
# Find process using the port (e.g., 8080)
# macOS/Linux
lsof -i :8080

# Windows
netstat -ano | findstr :8080

# Kill the process or stop conflicting containers
docker compose down
```

### Need Help?

For additional support:

1. Check the detailed documentation in `.trae/documents/`
2. Review test examples in `backend/src/test/`
3. Examine Karate feature files in `karate-microservices-testing/src/test/resources/`
4. Review Docker Compose troubleshooting guide: `.trae/documents/docker-compose-troubleshooting.md`

## Maven Usage

- Backend build and tests:
  - `mvn -f backend/pom.xml clean verify`
  - Allure report: `backend/target/allure-report/index.html`
  - Serve report: `mvn -f backend/pom.xml -Pallure-serve verify`
- Backend container build:
  - `mvn -f backend/pom.xml jib:dockerBuild`
  - Windows Docker arch: `mvn -f backend/pom.xml -Pwindows-docker jib:dockerBuild`
- Karate tests (module `karate-microservices-testing`):
  - Run full suite: `mvn -f karate-microservices-testing/pom.xml test -Dkarate.env=qa -Dthreads=5`
  - API runner: `mvn -f karate-microservices-testing/pom.xml test -Dtest='*ApiRunnerTest' -Dkarate.env=qa`
  - Custom runner with mock server: `mvn -f karate-microservices-testing/pom.xml test -Dtest='*CustomRunnerTest' -Dkarate.env=qa -Dmock.server.enabled=true -Dmock.port=8090 -Dkarate.options="classpath:api"`
  - Integration runner without mocks: `mvn -f karate-microservices-testing/pom.xml test -Dtest='*IntegrationRunnerTest' -Duse.mock=false -Dkarate.env=qa`
  - Mock server only: `mvn -f karate-microservices-testing/pom.xml test -Dtest='*MockRunnerTest' -Dmock.port=8090 -Dmock.block.ms=300000`
  - Gatling perf: `mvn -f karate-microservices-testing/pom.xml gatling:test -Dgatling.simulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:api/users.feature" -Dkarate.env=qa -Dthreads=10`
  - Gatling perf (tagged): `mvn -f karate-microservices-testing/pom.xml gatling:test -Dgatling.simulationClass=performance.simulations.KaratePerformanceSimulation -Dkarate.options="classpath:api --tags @perf" -Dkarate.env=qa -Dthreads=10`
