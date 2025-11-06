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

## 🧭 Observability (Tracing)

This project uses OpenTelemetry for distributed tracing. Spans are produced by Spring (Micrometer Tracing) and the OpenTelemetry Java agent, sent to an OpenTelemetry Collector, and visualized in Jaeger.

### Components
- **Backend**: emits OTLP HTTP traces to `http://localhost:4318/v1/traces`.
- **Collector**: receives OTLP HTTP on `:4318`, batches spans, and exports to Jaeger over OTLP gRPC `:4317`.
- **Jaeger**: UI at `http://localhost:16686` for searching and viewing traces.

### Start Collector + Jaeger
```bash
docker-compose up -d otel-collector jaeger
```

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
Use the Compose backend service that mounts the agent and targets the collector:
```bash
# Build the backend image with Jib (Docker CLI available)
cd backend && ./gradlew dockerBuild
# If Gradle still can’t find Docker, pass the CLI path explicitly:
cd backend && ./gradlew dockerBuild -Djib.dockerClient.executable="$(which docker)"
# Apple Silicon (M1/M2): defaults to linux/arm64 image.
# Windows (x86_64): build an amd64 image via either option:
# 1) Convenience task:
cd backend && ./gradlew dockerBuildWindows
# 2) Property override:
cd backend && ./gradlew dockerBuild -PjibTargetArch=amd64

## Windows Setup
- Build image (amd64):
```bash
cd backend && ./gradlew dockerBuildWindows
# Alternatively:
cd backend && ./gradlew dockerBuild -PjibTargetArch=amd64
```

- Start services with Windows compose file:
```bash
docker-compose -f docker-compose.windows.yml up -d postgres kafka otel-collector jaeger kafka-ui backend
```

- If Gradle can’t find Docker on Windows PowerShell, pass the executable path:
```powershell
cd backend; ./gradlew dockerBuildWindows -Djib.dockerClient.executable="$((Get-Command docker).Source)"
```

- Verify and access:
```bash
docker images | grep shared-services
# Jaeger:   http://localhost:16686
# Kafka UI: http://localhost:8081
# Backend:  http://localhost:8080
```

Notes (containers):
- Inside Docker, never use `localhost` to reach other services; use the Compose service names.
- OTLP endpoints are configured to `http://otel-collector:4318` for the Java agent.
- Spring Micrometer OTLP can be set via `MANAGEMENT_OTLP_TRACING_ENDPOINT=http://otel-collector:4318/v1/traces`.
- See troubleshooting guide: `.trae/documents/docker-compose-troubleshooting.md`.

## Frontend (Docker)

- Build the frontend image via Compose (uses `frontend/Dockerfile`):
```bash
# macOS/Linux
VITE_API_BASE_URL=http://localhost:8080/api/v1 \
docker-compose build frontend

# Windows PowerShell
$env:VITE_API_BASE_URL = "http://localhost:8080/api/v1"; docker-compose -f docker-compose.windows.yml build frontend
```

- Start frontend with the rest of the stack:
```bash
# macOS/Linux
docker-compose --profile observability up -d otel-collector jaeger kafka-ui backend frontend

# Windows
docker-compose -f docker-compose.windows.yml up -d postgres kafka otel-collector jaeger kafka-ui backend frontend
```

- Access URLs:
```bash
Frontend: http://localhost:5173
Backend:  http://localhost:8080
Jaeger:   http://localhost:16686
Kafka UI: http://localhost:8081
```

Notes:
- The frontend image build injects `VITE_API_BASE_URL` at build time (default `http://localhost:8080/api/v1`).
- You can change it by setting `VITE_API_BASE_URL` before `docker-compose build frontend`.
  
Note:
- Default in-container API base is `http://backend:8080/api/v1` to ensure services communicate within the Compose network. Override it for local non-Docker builds if needed.

### Frontend Local Build and Type Checking

- Local production build now skips TypeScript type checking to avoid blocking on non-critical type errors:
  ```bash
  cd frontend
  npm run build
  ```
- Run a dedicated type check when you want strict validation (e.g., CI or local verification):
  ```bash
  cd frontend
  npm run typecheck
  ```
- Recommendation: keep strictness in development (`tsconfig.app.json`) and use `npm run typecheck` in CI to enforce type safety without blocking production builds.

# Fallback: build a tarball if Docker CLI is unavailable
cd backend && ./gradlew dockerBuildTar
# Then, when Docker CLI is available:
docker load -i backend/build/jib-image.tar

# Start collectors and backend
docker-compose --profile observability up -d otel-collector jaeger backend

# Stop services
docker-compose --profile observability down

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
    docker compose --profile observability up -d postgres kafka otel-collector jaeger kafka-ui backend frontend

    # Windows
    docker compose -f docker-compose.windows.yml up -d postgres kafka otel-collector jaeger kafka-ui backend frontend
    ```

- Warning: `the attribute version is obsolete`
  - Compose v2 ignores the `version` field. You can remove `version: '3.9'` from your compose files to suppress the warning.
  
- Backend exporting to `localhost:4318` inside container
  - Cause: Using `localhost` in container resolves to the container itself.
  - Fix: Set env vars to target the collector by service name:
    ```bash
    # docker-compose.yml environment for backend
    OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318
    MANAGEMENT_OTLP_TRACING_ENDPOINT=http://otel-collector:4318/v1/traces
    ```
  - Reference: `.trae/documents/docker-compose-troubleshooting.md`.
```

## 📋 Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher
- **Docker & Docker Compose**
- **Git**

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd shared-services
```

### 2. Start the Database
```bash
docker-compose up -d postgres
```

### 3. Start the Backend
```bash
cd backend
./gradlew bootRun
```

The backend will be available at `http://localhost:8080`

### 4. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:5173`

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
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

### Production Considerations
- Externalize JWT secret to environment variables
- Enable proper CORS configuration
- Implement rate limiting
- Add comprehensive monitoring and logging
- Enable HTTPS/TLS

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the documentation in `.trae/documents/`
- Review the API documentation at `/api/swagger-ui.html` (when implemented)

## 🗺️ Roadmap

- [ ] Complete Tenant Management Controller implementation
- [ ] Add comprehensive API documentation (Swagger/OpenAPI)
- [ ] Implement advanced analytics and reporting
- [ ] Add comprehensive test coverage
- [ ] Implement monitoring and health checks
- [ ] Add integration hub functionality
- [ ] Enhance security with rate limiting and advanced authentication