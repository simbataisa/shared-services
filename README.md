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
cd backend && ./gradlew dockerBuild

# Windows (x86_64) - build amd64 image
cd backend && ./gradlew dockerBuildWindows
# Or with property override:
cd backend && ./gradlew dockerBuild -PjibTargetArch=amd64
```

## Windows Setup

Windows users can now use the same simplified Docker Compose command with profiles:

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

````

## 📋 Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher
- **Docker & Docker Compose**
- **Git**

## 🚀 Quick Start

### Option 1: Automated Setup (Recommended)

Use the provided script to build and start all services automatically:

```bash
git clone <repository-url>
cd shared-services
./run-all.sh
```

The script will:
1. ✅ Build backend Docker image
2. ✅ Build frontend Docker image
3. ✅ Start all services with observability profile
4. ✅ Display access URLs and configuration info

### Option 2: Manual Setup

#### 1. Clone the Repository
```bash
git clone <repository-url>
cd shared-services
```

#### 2. Build Images (Optional)

```bash
# Build backend
cd backend && ./gradlew dockerBuild && cd ..

# Build frontend
docker compose build frontend
```

#### 3. Start All Services

```bash
# macOS/Linux
docker compose --profile observability up -d

# Windows
docker compose -f docker-compose.windows.yml --profile observability up -d
```

This starts all required services:

- **PostgreSQL** - Database
- **Kafka** - Message broker
- **OpenTelemetry Collector** - Trace collection
- **Jaeger** - Distributed tracing UI
- **Kafka UI** - Kafka management console
- **Backend** - Spring Boot application
- **Frontend** - React application

### 3. Access the Application

```bash
Frontend:  http://localhost:5173
Backend:   http://localhost:8080
Jaeger:    http://localhost:16686
Kafka UI:  http://localhost:8081
```

### 4. Running with Karate Mock Server (For Testing)

The backend in Docker is configured to connect to payment gateway mocks running on your host machine:

```bash
# In a separate terminal, start the Karate mock server
cd karate-microservices-testing
./gradlew test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=1000
```

The Docker container will reach the mock server at `host.docker.internal:8090`. This allows:
- ✅ Backend in Docker to call mock Stripe, PayPal, BankTransfer APIs
- ✅ Payment gateway integration testing without external API calls
- ✅ Isolated testing environment

**How it works:**
- The backend uses `MOCK_SERVER_HOST=host.docker.internal` environment variable
- Mock URLs: `http://host.docker.internal:8090/stripe/*`, `/paypal/*`, `/bank-transfer/*`
- For local development (not Docker), URLs default to `http://localhost:8090/*`

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

## 🏗️ Development

### Backend Development

```bash
cd backend
./gradlew bootRun
```

### Frontend Development

```bash
cd frontend
npm run dev
```

### Database Migrations

```bash
# Migrations are automatically applied on startup
# Located in: backend/src/main/resources/db/migration/
```

### Build for Production

```bash
# Backend
cd backend
./gradlew build

# Frontend
cd frontend
npm run build
```

## 🧪 Testing

### Backend Testing

```bash
cd backend
./gradlew test
```

### Frontend Testing

```bash
cd frontend
npm run lint
```

## 📁 Project Structure

```
shared-services/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Configuration and migrations
│   └── build.gradle        # Gradle build configuration
├── frontend/               # React frontend
│   ├── src/                # TypeScript source code
│   ├── public/             # Static assets
│   └── package.json        # NPM dependencies
├── .trae/documents/        # Technical documentation
├── docker-compose.yml      # Database setup
└── README.md              # This file
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
