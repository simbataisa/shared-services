#!/bin/bash
set -e

# Karate Mock Server Docker Entrypoint
# This script handles starting the mock server with various configurations

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Function to start mock server
start_mock_server() {
    print_info "Starting Karate Mock Server..."
    print_info "Configuration:"
    print_info "  Port: ${MOCK_PORT}"
    print_info "  Feature File: ${MOCK_FEATURE_FILE:-src/test/resources/mocks/mock-server.feature}"
    print_info "  Block Duration: ${MOCK_BLOCK_MS}ms"

    # Check if custom feature files are mounted
    if [ -d "/app/mocks" ] && [ "$(ls -A /app/mocks 2>/dev/null)" ]; then
        print_info "Found mounted feature files in /app/mocks"
        print_info "Copying to src/test/resources/mocks/"

        # Ensure destination directory exists
        mkdir -p /app/src/test/resources/mocks

        # Copy mounted files
        cp -r /app/mocks/* /app/src/test/resources/mocks/ 2>/dev/null || print_warning "No files to copy"

        # If a custom feature file is copied, update the path
        if [ -f "/app/src/test/resources/mocks/mock-server.feature" ]; then
            export MOCK_FEATURE_FILE="src/test/resources/mocks/mock-server.feature"
        fi
    fi

    print_info "Starting mock server on port ${MOCK_PORT}..."

    # Run the mock server using Gradle test with MockRunnerTest
    FEATURE="${MOCK_FEATURE_FILE:-src/test/resources/mocks/mock-server.feature}"
    if [[ "$FEATURE" == classpath:* || -d "$FEATURE" ]]; then
        FEATURE="src/test/resources/mocks/mock-server.feature"
    fi
    KARATE_PORT="${MOCK_KARATE_PORT:-$MOCK_PORT}"
    if [[ "$KARATE_PORT" != "$MOCK_PORT" ]]; then
        print_info "Starting TCP proxy from 0.0.0.0:${MOCK_PORT} -> 127.0.0.1:${KARATE_PORT}"
        nohup socat TCP-LISTEN:${MOCK_PORT},fork,reuseaddr TCP:127.0.0.1:${KARATE_PORT} >/dev/null 2>&1 &
    fi
    exec ./gradlew test --tests "*MockRunnerTest" \
        -Dkarate.env="${MOCK_ENV}" \
        -Dmock.port="${KARATE_PORT}" \
        -Dmock.feature.file="${FEATURE}" \
        -Dmock.block.ms="${MOCK_BLOCK_MS}" \
        --console=plain \
        --no-daemon
}

# Function to run custom tests
run_custom_tests() {
    print_info "Running custom Karate tests..."

    if [ -z "$KARATE_OPTIONS" ]; then
        print_error "KARATE_OPTIONS environment variable not set"
        print_info "Example: KARATE_OPTIONS='classpath:api/users.feature'"
        exit 1
    fi

    print_info "Test options: ${KARATE_OPTIONS}"
    print_info "Environment: ${MOCK_ENV}"

    exec ./gradlew test --tests "*CustomRunnerTest" \
        -Dkarate.options="${KARATE_OPTIONS}" \
        -Dkarate.env="${MOCK_ENV}" \
        --console=plain \
        --no-daemon
}

# Function to run API tests
run_api_tests() {
    print_info "Running API tests..."
    print_info "Environment: ${MOCK_ENV}"

    exec ./gradlew test --tests "*ApiRunnerTest" \
        -Dkarate.env="${MOCK_ENV}" \
        --console=plain \
        --no-daemon
}

# Function to list available mocks
list_mocks() {
    print_info "Available mock feature files:"
    echo ""

    # List built-in mocks
    if [ -d "/app/src/test/resources/mocks" ]; then
        print_success "Built-in mocks (classpath:mocks/):"
        find /app/src/test/resources/mocks -name "*.feature" -type f | while read file; do
            rel_path=$(echo "$file" | sed 's|/app/src/test/resources/||')
            echo "  - ${rel_path}"
        done
        echo ""
    fi

    # List mounted mocks
    if [ -d "/app/mocks" ] && [ "$(ls -A /app/mocks 2>/dev/null)" ]; then
        print_success "Mounted mocks (/app/mocks/):"
        find /app/mocks -name "*.feature" -type f | while read file; do
            rel_path=$(echo "$file" | sed 's|/app/mocks/||')
            echo "  - ${rel_path}"
        done
        echo ""
    else
        print_warning "No mounted mocks found in /app/mocks/"
        print_info "To mount custom mocks, use: -v /path/to/mocks:/app/mocks"
        echo ""
    fi

    print_info "To start mock server with specific feature file:"
    echo "  docker run -e MOCK_FEATURE_FILE='src/test/resources/mocks/custom-mock.feature' karate-mock-server"
    echo ""
}

# Function to show help
show_help() {
    cat << EOF
Karate Mock Server Docker Container

USAGE:
    docker run [OPTIONS] karate-mock-server [COMMAND]

COMMANDS:
    start-mock       Start the mock server (default)
    custom-tests     Run custom Karate tests
    api-tests        Run API tests
    list-mocks       List available mock feature files
    help             Show this help message

ENVIRONMENT VARIABLES:
    MOCK_PORT           Port for mock server (default: 8090)
    MOCK_ENV            Environment name (default: qa)
    MOCK_FEATURE_FILE   Feature file to load (default: src/test/resources/mocks/mock-server.feature)
    MOCK_BLOCK_MS       How long to keep server running in ms (default: 600000 / 10 minutes)
    KARATE_OPTIONS      Options for custom tests (required for custom-tests command)

EXAMPLES:
    # Start mock server with default settings
    docker run -p 8090:8090 karate-mock-server

    # Start with custom port and environment
    docker run -p 9090:9090 -e MOCK_PORT=9090 -e MOCK_ENV=dev karate-mock-server

    # Mount custom feature files
    docker run -p 8090:8090 -v \$(pwd)/my-mocks:/app/mocks karate-mock-server

    # Run custom tests
    docker run -e KARATE_OPTIONS='classpath:api/users.feature' karate-mock-server custom-tests

    # List available mocks
    docker run karate-mock-server list-mocks

    # Run API tests
    docker run -e MOCK_ENV=qa karate-mock-server api-tests

VOLUME MOUNTS:
    /app/mocks       Mount directory with custom .feature files
    /app/reports     Mount directory to save test reports

MORE INFO:
    See README.md for detailed documentation
    https://github.com/karatelabs/karate

EOF
}

# Main entrypoint logic
case "${1:-start-mock}" in
    start-mock)
        start_mock_server
        ;;
    custom-tests)
        run_custom_tests
        ;;
    api-tests)
        run_api_tests
        ;;
    list-mocks)
        list_mocks
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
