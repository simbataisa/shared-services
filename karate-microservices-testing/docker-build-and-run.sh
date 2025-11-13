#!/bin/bash

# Quick build and run script for Karate Mock Server Docker image

set -e

# Color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

echo "========================================"
echo "  Karate Mock Server - Docker Build"
echo "========================================"
echo ""

# Step 1: Build the Docker image
print_info "Step 1/3: Building Docker image..."
docker build -t karate-mock-server:latest .

if [ $? -ne 0 ]; then
    print_warning "Docker build failed!"
    exit 1
fi

print_success "Docker image built successfully"
echo ""

# Step 2: Show image info
print_info "Step 2/3: Image information..."
docker images karate-mock-server:latest
echo ""

# Step 3: Start the container
print_info "Step 3/3: Starting mock server..."
print_info "Container will run in background on port 8090"
echo ""

# Stop and remove existing container if it exists
if docker ps -a | grep -q karate-mock-server; then
    print_info "Removing existing container..."
    docker stop karate-mock-server 2>/dev/null || true
    docker rm karate-mock-server 2>/dev/null || true
fi

# Run the container
docker run -d \
    --name karate-mock-server \
    -p 8090:8090 \
    -e MOCK_PORT=8090 \
    -e MOCK_ENV=qa \
    -e MOCK_BLOCK_MS=600000 \
    karate-mock-server:latest

if [ $? -ne 0 ]; then
    print_warning "Failed to start container!"
    exit 1
fi

print_success "Mock server started successfully!"
echo ""

# Wait a bit for the server to start
print_info "Waiting for mock server to be ready..."
sleep 5

# Show logs
print_info "Container logs (showing last 20 lines):"
echo "----------------------------------------"
docker logs --tail 20 karate-mock-server
echo "----------------------------------------"
echo ""

# Test health endpoint
print_info "Testing health endpoint..."
sleep 2
if curl -f http://localhost:8090/stripe/health > /dev/null 2>&1; then
    print_success "Mock server is healthy and ready!"
else
    print_warning "Health check failed - server might still be starting up"
    print_info "Try: curl http://localhost:8090/stripe/health"
fi

echo ""
echo "========================================"
echo "  Mock Server Ready!"
echo "========================================"
echo ""
print_info "Access URLs:"
echo "  Stripe mock:        http://localhost:8090/stripe"
echo "  PayPal mock:        http://localhost:8090/paypal"
echo "  Bank Transfer mock: http://localhost:8090/bank-transfer"
echo ""
print_info "Useful commands:"
echo "  View logs:    docker logs -f karate-mock-server"
echo "  Stop server:  docker stop karate-mock-server"
echo "  Remove:       docker rm karate-mock-server"
echo "  Restart:      docker restart karate-mock-server"
echo ""
print_info "Test health:"
echo "  curl http://localhost:8090/stripe/health"
echo ""
