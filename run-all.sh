#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Show help and exit
if [[ "$1" == "--help" || "$1" == "-h" || "$1" == "help" ]]; then
    echo "Usage: ./run-all.sh [windows|linux|apple-silicon]"
    echo "If no argument is provided, the script auto-detects the platform."
    echo "Examples:"
    echo "  ./run-all.sh windows"
    echo "  ./run-all.sh linux"
    echo "  ./run-all.sh apple-silicon"
    exit 0
fi

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
print_info "Checking prerequisites..."

if ! command_exists docker; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command_exists docker-compose && ! docker compose version >/dev/null 2>&1; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

print_success "All prerequisites are met."

COMPOSE_FILE="docker-compose.yml"

ARG="$1"
if [[ -n "$ARG" ]]; then
    LOWER_ARG=$(echo "$ARG" | tr '[:upper:]' '[:lower:]')
    case "$LOWER_ARG" in
        windows)
            print_info "Using platform override: windows"
            USER_PLATFORM="windows"
            ;;
        linux)
            print_info "Using platform override: linux"
            USER_PLATFORM="linux"
            ;;
        apple|apple-silicon|mac|darwin)
            print_info "Using platform override: apple-silicon"
            USER_PLATFORM="apple"
            ;;
        *)
            print_warning "Unknown platform argument '$ARG'; falling back to auto-detect"
            USER_PLATFORM=""
            ;;
    esac
else
    USER_PLATFORM=""
fi

if [[ -z "$USER_PLATFORM" ]]; then
    PLATFORM=$(uname -s)
    if [[ "$PLATFORM" == "Linux" ]]; then
        ARCH=$(uname -m)
        if [[ "$ARCH" == "x86_64" || "$ARCH" == "amd64" ]]; then
            print_info "Detected platform: Linux (x86_64)"
        else
            print_info "Detected platform: Linux ($ARCH)"
        fi
    elif [[ "$PLATFORM" == "Darwin" ]]; then
        ARCH=$(uname -m)
        if [[ "$ARCH" == "arm64" ]]; then
            print_info "Detected platform: macOS (Apple Silicon)"
        else
            print_info "Detected platform: macOS (Intel)"
        fi
    else
        print_warning "Detected platform: $PLATFORM - using default configuration"
    fi
fi

# Step 1: Build backend service
print_info "Step 1/4: Building backend service..."
cd backend || exit 1

if [[ -n "$USER_PLATFORM" ]]; then
    if [[ "$USER_PLATFORM" == "windows" ]]; then
        BUILD_CMD="./gradlew dockerBuildWindows -x test -x jacocoTestReport -x jacocoTestCoverageVerification"
    elif [[ "$USER_PLATFORM" == "linux" ]]; then
        BUILD_CMD="./gradlew dockerBuild -x test -x jacocoTestReport -x jacocoTestCoverageVerification -PjibTargetArch=amd64"
    elif [[ "$USER_PLATFORM" == "apple" ]]; then
        BUILD_CMD="./gradlew dockerBuild -x test -x jacocoTestReport -x jacocoTestCoverageVerification -PjibTargetArch=arm64"
    fi
else
    PLATFORM=$(uname -s)
    ARCH=$(uname -m)
    if [[ "$PLATFORM" == "Darwin" ]]; then
        if [[ "$ARCH" == "arm64" ]]; then
            BUILD_CMD="./gradlew dockerBuild -x test -x jacocoTestReport -x jacocoTestCoverageVerification -PjibTargetArch=arm64"
        else
            BUILD_CMD="./gradlew dockerBuild -x test -x jacocoTestReport -x jacocoTestCoverageVerification -PjibTargetArch=amd64"
        fi
    elif [[ "$PLATFORM" == "Linux" ]]; then
        if [[ "$ARCH" == "x86_64" || "$ARCH" == "amd64" ]]; then
            BUILD_CMD="./gradlew dockerBuild -x test -x jacocoTestReport -x jacocoTestCoverageVerification -PjibTargetArch=amd64"
        else
            BUILD_CMD="./gradlew dockerBuild -x test -x jacocoTestReport -x jacocoTestCoverageVerification -PjibTargetArch=arm64"
        fi
    else
        BUILD_CMD="./gradlew dockerBuild -x test -x jacocoTestReport -x jacocoTestCoverageVerification"
    fi
fi

if $BUILD_CMD; then
    print_success "Backend Docker image built successfully."
else
    print_error "Failed to build backend Docker image."
    exit 1
fi

cd .. || exit 1

# Step 2: Build frontend service
print_info "Step 2/4: Building frontend service..."

# Set default API URL for containerized environment
export VITE_API_BASE_URL="${VITE_API_BASE_URL:-http://localhost:8080/api/v1}"

if docker compose build frontend; then
    print_success "Frontend Docker image built successfully."
else
    print_error "Failed to build frontend Docker image."
    exit 1
fi

# Step 3: Build Karate mock server
print_info "Step 3/5: Building Karate mock server in Docker..."
print_warning "This may take several minutes on first build (downloading Gatling dependencies)..."

if docker compose build --progress=plain karate-mock-server; then
    print_success "Karate mock server Docker image built successfully."
else
    print_error "Failed to build Karate mock server Docker image."
    exit 1
fi

# Step 4: Stop any existing containers
print_info "Step 4/5: Stopping existing containers (if any)..."
docker compose --profile observability down 2>/dev/null || true

# Step 5: Start all services
print_info "Step 5/5: Starting all services with observability profile..."

if docker compose --profile observability up -d; then
    print_success "All services started successfully!"
else
    print_error "Failed to start services."
    exit 1
fi

# Wait for services to be ready
print_info "Waiting for services to be ready..."
sleep 5

# Check service health
print_info "Checking service status..."
docker compose ps

# Display access information
echo ""
print_success "=========================================="
print_success "   All services are up and running!      "
print_success "=========================================="
echo ""
print_info "Access URLs:"
echo "  Frontend:       http://localhost:5173"
echo "  Backend:        http://localhost:8080"
echo "  Swagger UI:     http://localhost:8080/swagger-ui/index.html"
echo "  Jaeger UI:      http://localhost:16686"
echo "  Kafka UI:       http://localhost:8081"
echo "  PostgreSQL:     localhost:5432"
echo "  Mock Server:    http://localhost:8090 🎭"
echo ""
print_info "Mock Server (Running in Docker):"
echo "  Stripe Mock:        http://localhost:8090/stripe"
echo "  PayPal Mock:        http://localhost:8090/paypal"
echo "  Bank Transfer Mock: http://localhost:8090/bank-transfer"
echo "  Health Check:       http://localhost:8090/stripe/health"
echo ""
print_info "The Karate mock server is running inside Docker and provides:"
echo "  ✓ Payment gateway mocks (Stripe, PayPal, Bank Transfer)"
echo "  ✓ Realistic API responses for testing"
echo "  ✓ Backend automatically connects to these mocks"
echo ""
print_info "Alternative: Run mock server on host machine:"
echo "  cd karate-microservices-testing"
echo "  ./gradlew test --tests \"*MockRunnerTest\" -Dkarate.env=qa -Dmock.block.ms=600000"
echo ""
print_info "To view logs:"
echo "  docker compose logs -f"
echo ""
print_info "To stop all services:"
echo "  docker compose --profile observability down"
echo ""
