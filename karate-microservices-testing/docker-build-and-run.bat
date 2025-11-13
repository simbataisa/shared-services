@echo off
REM Quick build and run script for Karate Mock Server Docker image (Windows)

setlocal enabledelayedexpansion

echo ========================================
echo   Karate Mock Server - Docker Build
echo ========================================
echo.

REM Step 1: Build the Docker image
echo [INFO] Step 1/3: Building Docker image...
docker build -t karate-mock-server:latest .

if %errorlevel% neq 0 (
    echo [WARNING] Docker build failed!
    exit /b 1
)

echo [SUCCESS] Docker image built successfully
echo.

REM Step 2: Show image info
echo [INFO] Step 2/3: Image information...
docker images karate-mock-server:latest
echo.

REM Step 3: Start the container
echo [INFO] Step 3/3: Starting mock server...
echo [INFO] Container will run in background on port 8090
echo.

REM Stop and remove existing container if it exists
docker ps -a | findstr karate-mock-server >nul 2>nul
if %errorlevel% equ 0 (
    echo [INFO] Removing existing container...
    docker stop karate-mock-server 2>nul
    docker rm karate-mock-server 2>nul
)

REM Run the container
docker run -d ^
    --name karate-mock-server ^
    -p 8090:8090 ^
    -e MOCK_PORT=8090 ^
    -e MOCK_ENV=qa ^
    -e MOCK_BLOCK_MS=600000 ^
    karate-mock-server:latest

if %errorlevel% neq 0 (
    echo [WARNING] Failed to start container!
    exit /b 1
)

echo [SUCCESS] Mock server started successfully!
echo.

REM Wait a bit for the server to start
echo [INFO] Waiting for mock server to be ready...
timeout /t 5 /nobreak >nul

REM Show logs
echo [INFO] Container logs (showing last 20 lines):
echo ----------------------------------------
docker logs --tail 20 karate-mock-server
echo ----------------------------------------
echo.

REM Test health endpoint
echo [INFO] Testing health endpoint...
timeout /t 2 /nobreak >nul
curl -f http://localhost:8090/stripe/health >nul 2>nul
if %errorlevel% equ 0 (
    echo [SUCCESS] Mock server is healthy and ready!
) else (
    echo [WARNING] Health check failed - server might still be starting up
    echo [INFO] Try: curl http://localhost:8090/stripe/health
)

echo.
echo ========================================
echo   Mock Server Ready!
echo ========================================
echo.
echo [INFO] Access URLs:
echo   Stripe mock:        http://localhost:8090/stripe
echo   PayPal mock:        http://localhost:8090/paypal
echo   Bank Transfer mock: http://localhost:8090/bank-transfer
echo.
echo [INFO] Useful commands:
echo   View logs:    docker logs -f karate-mock-server
echo   Stop server:  docker stop karate-mock-server
echo   Remove:       docker rm karate-mock-server
echo   Restart:      docker restart karate-mock-server
echo.
echo [INFO] Test health:
echo   curl http://localhost:8090/stripe/health
echo.

endlocal
