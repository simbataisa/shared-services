@echo off
REM Windows batch script to build and run all services

setlocal enabledelayedexpansion

set "ESC="

if /i "%~1"=="--help" goto :showhelp
if /i "%~1"=="-h" goto :showhelp
if /i "%~1"=="help" goto :showhelp
if "%~1"=="/?" goto :showhelp

:showhelp
echo Usage: run-all.bat ^<windows^|linux^|apple-silicon^>
echo If no argument is provided, the script auto-detects or defaults appropriately.
echo Examples:
echo   run-all.bat windows
echo   run-all.bat linux
echo   run-all.bat apple-silicon
if /i "%~1"=="--help" exit /b 0
if /i "%~1"=="-h" exit /b 0
if /i "%~1"=="help" exit /b 0
if "%~1"=="/?" exit /b 0

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

set "TARGET="
if not "%~1"=="" (
    if /i "%~1"=="windows" (
        set "TARGET=windows"
    ) else if /i "%~1"=="linux" (
        set "TARGET=linux"
    ) else if /i "%~1"=="apple-silicon" (
        set "TARGET=apple"
    ) else if /i "%~1"=="apple" (
        set "TARGET=apple"
    ) else if /i "%~1"=="mac" (
        set "TARGET=apple"
    ) else (
        echo [WARNING] Unknown platform argument "%~1"; falling back to auto-detect.
    )
)

if "%TARGET%"=="windows" (
    call gradlew.bat dockerBuildWindows -x test -x jacocoTestReport -x jacocoTestCoverageVerification
) else if "%TARGET%"=="linux" (
    call gradlew.bat dockerBuild -x test -x jacocoTestReport -x jacocoTestCoverageVerification -PjibTargetArch=amd64
) else if "%TARGET%"=="apple" (
    call gradlew.bat dockerBuild -x test -x jacocoTestReport -x jacocoTestCoverageVerification -PjibTargetArch=arm64
) else (
    call gradlew.bat dockerBuildWindows -x test -x jacocoTestReport -x jacocoTestCoverageVerification
)
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

REM Step 3: Build Karate mock server
echo [INFO] Step 3/5: Building Karate mock server in Docker...
echo [WARNING] This may take several minutes on first build (downloading Gatling dependencies)...

docker compose build --progress=plain karate-mock-server
if %errorlevel% neq 0 (
    echo [ERROR] Failed to build Karate mock server Docker image.
    exit /b 1
)

echo [SUCCESS] Karate mock server Docker image built successfully.

REM Step 4: Stop any existing containers
echo [INFO] Step 4/5: Stopping existing containers (if any)...
docker compose --profile observability down 2>nul

REM Step 5: Start all services
echo [INFO] Step 5/5: Starting all services with observability profile...

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
echo   Swagger UI:     http://localhost:8080/swagger-ui/index.html
echo   Jaeger UI:      http://localhost:16686
echo   Kafka UI:       http://localhost:8081
echo   PostgreSQL:     localhost:5432
echo   Mock Server:    http://localhost:8090
echo.
echo [INFO] Mock Server (Running in Docker):
echo   Stripe Mock:        http://localhost:8090/stripe
echo   PayPal Mock:        http://localhost:8090/paypal
echo   Bank Transfer Mock: http://localhost:8090/bank-transfer
echo   Health Check:       http://localhost:8090/stripe/health
echo.
echo [INFO] The Karate mock server is running inside Docker and provides:
echo   - Payment gateway mocks (Stripe, PayPal, Bank Transfer)
echo   - Realistic API responses for testing
echo   - Backend automatically connects to these mocks
echo.
echo [INFO] Alternative: Run mock server on host machine:
echo   cd karate-microservices-testing
echo   gradlew.bat test --tests "*MockRunnerTest" -Dkarate.env=qa -Dmock.block.ms=600000
echo.
echo [INFO] To view logs:
echo   docker compose logs -f
echo.
echo [INFO] To stop all services:
echo   docker compose --profile observability down
echo.

endlocal
