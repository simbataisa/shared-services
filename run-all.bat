@echo off
REM Windows batch script to build and run all services

setlocal enabledelayedexpansion

REM Function to print colored messages (Windows 10+)
set "ESC="

REM Check prerequisites
echo [INFO] Checking prerequisites...

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not installed. Please install Docker Desktop first.
    exit /b 1
)

docker compose version >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Docker Compose is not available. Please install Docker Desktop with Compose support.
    exit /b 1
)

echo [SUCCESS] All prerequisites are met.

REM Detect platform
echo [INFO] Detected platform: Windows

REM Step 1: Build backend service
echo [INFO] Step 1/4: Building backend service...
cd backend
if %errorlevel% neq 0 (
    echo [ERROR] Failed to navigate to backend directory.
    exit /b 1
)

REM Use gradlew.bat for Windows
call gradlew.bat jibDockerBuild
if %errorlevel% neq 0 (
    echo [ERROR] Failed to build backend Docker image.
    cd ..
    exit /b 1
)

echo [SUCCESS] Backend Docker image built successfully.
cd ..

REM Step 2: Build frontend service
echo [INFO] Step 2/4: Building frontend service...

REM Set default API URL for containerized environment
if not defined VITE_API_BASE_URL (
    set "VITE_API_BASE_URL=http://localhost:8080/api/v1"
)

docker compose build frontend
if %errorlevel% neq 0 (
    echo [ERROR] Failed to build frontend Docker image.
    exit /b 1
)

echo [SUCCESS] Frontend Docker image built successfully.

REM Step 3: Stop any existing containers
echo [INFO] Step 3/4: Stopping existing containers (if any)...
docker compose --profile observability down 2>nul

REM Step 4: Start all services
echo [INFO] Step 4/4: Starting all services with observability profile...

docker compose --profile observability up -d
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
docker compose ps

REM Display access information
echo.
echo ==========================================
echo    All services are up and running!
echo ==========================================
echo.
echo [INFO] Access URLs:
echo   Frontend:       http://localhost:5173
echo   Backend:        http://localhost:8080
echo   Swagger UI:     http://localhost:8080/swagger-ui/swagger-ui/index.html
echo   Jaeger UI:      http://localhost:16686
echo   Kafka UI:       http://localhost:8081
echo   PostgreSQL:     localhost:5432
echo.
echo [INFO] Mock Server Configuration:
echo   The backend in Docker will connect to mock servers on host machine
echo   Make sure to run Karate mock server on port 8090:
echo   cd karate-microservices-testing
echo   gradlew.bat cleanTest test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=1000
echo   Run custom tests with mock server:
echo   gradlew.bat cleanTest test --tests "*CustomRunnerTest" -Dkarate.env=qa -Dmock.server.enabled=true -Dmock.port=8090 --info -Dkarate.options="classpath:api"
echo.
echo [INFO] To view logs:
echo   docker compose logs -f
echo.
echo [INFO] To stop all services:
echo   docker compose --profile observability down
echo.

endlocal
