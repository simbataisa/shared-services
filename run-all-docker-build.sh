#!/bin/bash

# Bash script to build and run all services using Docker multi-stage builds
# This script builds backend and frontend entirely within Docker containers

set -e  # Exit on error

# Color codes for output
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

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check prerequisites
print_info "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

if ! docker compose version &> /dev/null; then
    print_error "Docker Compose is not available. Please install Docker with Compose support."
    exit 1
fi

print_success "All prerequisites are met."

# Detect platform
PLATFORM=$(uname -s)
print_info "Detected platform: $PLATFORM"

# Step 1: Build backend service inside Docker
print_info "Step 1/4: Building backend service in Docker (multi-stage build)..."
print_warning "This may take a few minutes on first build as dependencies are downloaded..."

docker compose -f docker-compose-build.yml build backend

if [ $? -ne 0 ]; then
    print_error "Failed to build backend Docker image."
    exit 1
fi

print_success "Backend Docker image built successfully."

# Step 2: Build frontend service inside Docker
print_info "Step 2/4: Building frontend service in Docker (multi-stage build)..."

# Set default API URL for containerized environment
export VITE_API_BASE_URL=${VITE_API_BASE_URL:-http://localhost:8080/api/v1}

docker compose -f docker-compose-build.yml build frontend

if [ $? -ne 0 ]; then
    print_error "Failed to build frontend Docker image."
    exit 1
fi

print_success "Frontend Docker image built successfully."

# Step 3: Build Karate mock server inside Docker
print_info "Step 3/5: Building Karate mock server in Docker (multi-stage build)..."
print_warning "This may take several minutes on first build (downloading Gatling dependencies)..."

docker compose -f docker-compose-build.yml build --progress=plain karate-mock-server

if [ $? -ne 0 ]; then
    print_error "Failed to build Karate mock server Docker image."
    exit 1
fi

print_success "Karate mock server Docker image built successfully."

# Step 4: Stop any existing containers
print_info "Step 4/5: Stopping existing containers (if any)..."
docker compose -f docker-compose-build.yml --profile observability down 2>/dev/null || true

# Step 5: Start all services
print_info "Step 5/5: Starting all services with observability profile..."

docker compose -f docker-compose-build.yml --profile observability up -d

if [ $? -ne 0 ]; then
    print_error "Failed to start services."
    exit 1
fi

print_success "All services started successfully!"

# Wait for services to be ready
print_info "Waiting for services to be ready..."
sleep 5

# Check service health
print_info "Checking service status..."
docker compose -f docker-compose-build.yml ps

# Display access information
echo ""
echo "=========================================="
echo "   All services are up and running!"
echo "=========================================="
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
print_info "To view logs:"
echo "  docker compose -f docker-compose-build.yml logs -f"
echo ""
print_info "To stop all services:"
echo "  docker compose -f docker-compose-build.yml --profile observability down"
echo ""
print_info "Build Information:"
echo "  ✅ Backend built inside Docker (multi-stage build)"
echo "  ✅ Frontend built inside Docker (multi-stage build)"
echo "  ✅ Mock server built inside Docker (multi-stage build)"
echo "  ✅ No local Gradle or npm installation required"
echo ""
