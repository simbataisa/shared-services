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

# Detect platform
PLATFORM=$(uname -s)
COMPOSE_FILE="docker-compose.yml"

if [[ "$PLATFORM" == "Linux" ]]; then
    ARCH=$(uname -m)
    if [[ "$ARCH" == "x86_64" ]]; then
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

# Step 1: Build backend service
print_info "Step 1/4: Building backend service..."
cd backend || exit 1

if [[ "$PLATFORM" == "Linux" ]] && [[ "$ARCH" == "x86_64" ]]; then
    ./gradlew dockerBuild -PjibTargetArch=amd64
elif ./gradlew dockerBuild; then
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

# Step 3: Stop any existing containers
print_info "Step 3/4: Stopping existing containers (if any)..."
docker compose --profile observability down 2>/dev/null || true

# Step 4: Start all services
print_info "Step 4/4: Starting all services with observability profile..."

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
echo "  Swagger UI:     http://localhost:8080/swagger-ui/swagger-ui/index.html"
echo "  Jaeger UI:      http://localhost:16686"
echo "  Kafka UI:       http://localhost:8081"
echo "  PostgreSQL:     localhost:5432"
echo ""
print_info "Mock Server Configuration:"
echo "  The backend in Docker will connect to mock servers on host machine"
echo "  Make sure to run Karate mock server on port 8090:"
echo "  cd karate-microservices-testing"
echo "  ./gradlew cleanTest test --tests \"*MockRunnerTest\" -Dkarate.env=qa -Dmock.block.ms=1000"
echo "  Run custom tests with mock server:"
echo "  ./gradlew cleanTest test --tests \"*CustomRunnerTest\" -Dkarate.env=qa -Dmock.server.enabled=true -Dmock.port=8090 --info -Dkarate.options=\"classpath:api\""
echo ""
print_info "To view logs:"
echo "  docker compose logs -f"
echo ""
print_info "To stop all services:"
echo "  docker compose --profile observability down"
echo ""
