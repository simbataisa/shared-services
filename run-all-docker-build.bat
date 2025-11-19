@echo off
REM Windows batch script to build and run all services using Podman multi-stage builds
REM This script builds backend and frontend entirely within Podman containers

setlocal enabledelayedexpansion

REM Check prerequisites
echo [INFO] Checking prerequisites...

where podman >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Podman is not installed. Please install Podman Desktop first.
    exit /b 1
)

podman compose version >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Podman Compose is not available. Please install podman-compose.
    exit /b 1
)

echo [SUCCESS] All prerequisites are met.

REM Detect platform
echo [INFO] Detected platform: Windows

REM Step 1: Build backend service inside Podman
echo [INFO] Step 1/4: Building backend service in Podman (multi-stage build)...
echo [WARNING] This may take a few minutes on first build as dependencies are downloaded...

podman compose -f docker-compose-build.yml build backend
if %errorlevel% neq 0 (
    echo [ERROR] Failed to build backend Podman image.
    exit /b 1
)

echo [SUCCESS] Backend Podman image built successfully.

REM Step 2: Build frontend service inside Podman
echo [INFO] Step 2/4: Building frontend service in Podman (multi-stage build)...

REM Set default API URL for containerized environment
if not defined VITE_API_BASE_URL (
    set "VITE_API_BASE_URL=http://localhost:8080/api/v1"
)

podman compose -f docker-compose-build.yml build frontend
if %errorlevel% neq 0 (
    echo [ERROR] Failed to build frontend Podman image.
    exit /b 1
)

echo [SUCCESS] Frontend Podman image built successfully.

REM Step 3: Build Karate mock server inside Podman
echo [INFO] Step 3/5: Building Karate mock server in Podman (multi-stage build)...
echo [WARNING] This may take several minutes on first build (downloading Gatling dependencies)...

podman compose -f docker-compose-build.yml build --progress=plain karate-mock-server
if %errorlevel% neq 0 (
    echo [ERROR] Failed to build Karate mock server Podman image.
    exit /b 1
)

echo [SUCCESS] Karate mock server Podman image built successfully.

REM Step 4: Stop any existing containers
echo [INFO] Step 4/5: Stopping existing containers (if any)...
podman compose -f docker-compose-build.yml --profile observability down 2>nul

REM Step 5: Start all services
echo [INFO] Step 5/5: Starting all services with observability profile...

podman compose -f docker-compose-build.yml --profile observability up -d
if %errorlevel% neq 0 (
    echo [ERROR] Failed to start services.
    exit /b 1
)

echo [SUCCESS] All services started successfully!

REM Wait for services to be ready
echo [INFO] Waiting for services to be ready...
timeout /t 5 /nobreak >nul

REM Check service health
echo [INFO] Checking service status...
podman compose -f docker-compose-build.yml ps

REM Display access information
echo.
echo ==========================================
echo    All services are up and running!
echo ==========================================
echo.
echo [INFO] Access URLs:
echo   Frontend:       http://localhost:5173
echo   Backend:        http://localhost:8080
echo   Swagger UI:     http://localhost:8080/swagger-ui/index.html
echo   Jaeger UI:      http://localhost:16686
echo   Kafka UI:       http://localhost:8081
echo   PostgreSQL:     localhost:5432
echo   Mock Server:    http://localhost:8090
echo.
echo [INFO] Mock Server (Running in Podman):
echo   Stripe Mock:        http://localhost:8090/stripe
echo   PayPal Mock:        http://localhost:8090/paypal
echo   Bank Transfer Mock: http://localhost:8090/bank-transfer
echo   Health Check:       http://localhost:8090/stripe/health
echo.
echo [INFO] The Karate mock server is running inside Podman and provides:
echo   - Payment gateway mocks (Stripe, PayPal, Bank Transfer)
echo   - Realistic API responses for testing
echo   - Backend automatically connects to these mocks
echo.
echo [INFO] To view logs:
echo   podman compose -f docker-compose-build.yml logs -f
echo.
echo [INFO] To stop all services:
echo   podman compose -f docker-compose-build.yml --profile observability down
echo.
echo [INFO] Build Information:
echo   - Backend built inside Podman (multi-stage build)
echo   - Frontend built inside Podman (multi-stage build)
echo   - Mock server built inside Podman (multi-stage build)
echo   - No local Gradle or npm installation required
echo.

endlocal
