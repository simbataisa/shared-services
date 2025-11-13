# Karate Mock Server - Docker Guide

This guide explains how to use the Dockerized Karate Mock Server for payment gateway testing and API mocking.

## 🎯 Quick Start

### Build the Docker Image

```bash
cd karate-microservices-testing
docker build -t karate-mock-server .
```

### Start the Mock Server

```bash
# Using Docker directly
docker run -p 8090:8090 karate-mock-server

# Using Docker Compose
docker compose up
```

The mock server will be available at `http://localhost:8090`

## 🐳 Docker Image Features

- **Base Image**: Eclipse Temurin 21 JRE (optimized runtime)
- **Multi-Stage Build**: Small final image size (~200MB)
- **Non-Root User**: Runs as `karate` user for security
- **Health Check**: Built-in health check endpoint
- **Volume Support**: Mount custom feature files
- **Configurable**: Environment variables for all settings

## 📋 Available Commands

### 1. Start Mock Server (Default)

Starts the Karate mock server with payment gateway mocks:

```bash
docker run -p 8090:8090 karate-mock-server
# or
docker run -p 8090:8090 karate-mock-server start-mock
```

### 2. Run Custom Tests

Execute custom Karate tests with specific features:

```bash
docker run \
  -e KARATE_OPTIONS='classpath:api/users.feature classpath:api/tenants.feature' \
  karate-mock-server custom-tests
```

### 3. Run API Tests

Run all API tests:

```bash
docker run -e MOCK_ENV=qa karate-mock-server api-tests
```

### 4. List Available Mocks

See all available mock feature files:

```bash
docker run karate-mock-server list-mocks
```

### 5. Show Help

Display usage information:

```bash
docker run karate-mock-server help
```

## ⚙️ Configuration

### Environment Variables

| Variable            | Default                            | Description                          |
| ------------------- | ---------------------------------- | ------------------------------------ |
| `MOCK_PORT`         | `8090`                             | Port for the mock server             |
| `MOCK_ENV`          | `qa`                               | Environment name (qa, dev, staging)  |
| `MOCK_FEATURE_FILE` | `src/test/resources/mocks/mock-server.feature` | Feature file to run as server |
| `MOCK_BLOCK_MS`     | `600000`                           | How long to keep server running (ms) |
| `KARATE_OPTIONS`    | -                                  | Options for custom test runs         |

### Example Configurations

#### Custom Port

```bash
docker run -p 9090:9090 -e MOCK_PORT=9090 karate-mock-server
```

#### Different Environment

```bash
docker run -p 8090:8090 -e MOCK_ENV=dev karate-mock-server
```

#### Longer Running Server

```bash
docker run -p 8090:8090 -e MOCK_BLOCK_MS=3600000 karate-mock-server  # 1 hour
```

## 📂 Volume Mounts

### Mount Custom Mock Files

Create your own mock feature files and mount them:

```bash
# Your custom mocks directory structure
my-custom-mocks/
├── api/
│   ├── custom-service.feature
│   └── another-service.feature
└── external/
    └── third-party-api.feature

# Run with custom mocks
docker run \
  -p 8090:8090 \
  -v $(pwd)/my-custom-mocks:/app/mocks \
  karate-mock-server
```

**Note:** Mounted files in `/app/mocks` will be copied to `src/test/resources/mocks/` and made available as `classpath:mocks/*`

### Save Test Reports

Mount a directory to save Karate test reports:

```bash
docker run \
  -p 8090:8090 \
  -v $(pwd)/test-reports:/app/build/reports \
  karate-mock-server
```

Reports will be saved to `./test-reports` on your host machine.

## 🔧 Docker Compose Usage

### Basic Setup

```yaml
version: "3.9"

services:
  karate-mock:
    image: karate-mock-server:latest
    ports:
      - "8090:8090"
    environment:
      MOCK_PORT: 8090
      MOCK_ENV: qa
    volumes:
      - ./custom-mocks:/app/mocks
      - ./reports:/app/build/reports
```

### Start with Docker Compose

```bash
# Start in foreground
docker compose up

# Start in background
docker compose up -d

# View logs
docker compose logs -f karate-mock-server

# Stop
docker compose down
```

## 🏗️ Integration with Backend Service

### Option 1: Using Docker Networks

```yaml
version: "3.9"

services:
  karate-mock:
    image: karate-mock-server:latest
    ports:
      - "8090:8090"
    networks:
      - app-network

  backend:
    image: your-backend:latest
    environment:
      # Backend can access mock at http://karate-mock:8090
      STRIPE_API_URL: http://karate-mock:8090/stripe
      PAYPAL_API_URL: http://karate-mock:8090/paypal
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

### Option 2: Host Network Mode

```bash
# Run mock server in host network mode
docker run --network host karate-mock-server

# Backend running locally can access at localhost:8090
```

## 🧪 Testing the Mock Server

### Health Check

```bash
curl http://localhost:8090/stripe/health
# Response: {"status":"UP"}
```

### Test Stripe Mock

```bash
# Create a payment intent
curl -X POST http://localhost:8090/stripe/v1/payment_intents \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000,
    "currency": "usd",
    "payment_method_types": ["card"]
  }'
```

### Test PayPal Mock

```bash
# Create an order
curl -X POST http://localhost:8090/paypal/v2/checkout/orders \
  -H "Content-Type: application/json" \
  -d '{
    "intent": "CAPTURE",
    "purchase_units": [{
      "amount": {
        "currency_code": "USD",
        "value": "100.00"
      }
    }]
  }'
```

### Test Bank Transfer Mock

```bash
# Initiate a transfer
curl -X POST http://localhost:8090/bank-transfer/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "USD",
    "account_number": "12345678"
  }'
```

## 📊 Built-in Mock Endpoints

### Stripe Endpoints

- `POST /stripe/v1/payment_intents` - Create payment intent
- `GET /stripe/v1/payment_intents/{id}` - Get payment intent
- `POST /stripe/v1/payment_intents/{id}/confirm` - Confirm payment
- `POST /stripe/v1/refunds` - Create refund
- `GET /stripe/health` - Health check

### PayPal Endpoints

- `POST /paypal/v2/checkout/orders` - Create order
- `GET /paypal/v2/checkout/orders/{id}` - Get order
- `POST /paypal/v2/checkout/orders/{id}/capture` - Capture order
- `POST /paypal/v2/payments/captures/{id}/refund` - Refund capture
- `GET /paypal/health` - Health check

### Bank Transfer Endpoints

- `POST /bank-transfer/v1/transfers` - Initiate transfer
- `GET /bank-transfer/v1/transfers/{id}` - Get transfer status
- `POST /bank-transfer/v1/transfers/{id}/refund` - Refund transfer
- `GET /bank-transfer/health` - Health check

## 🔍 Troubleshooting

### Mock Server Won't Start

**Issue:** Container exits immediately

**Solution:**

```bash
# Check logs
docker logs karate-mock-server

# Run with interactive mode to see errors
docker run -it -p 8090:8090 karate-mock-server
```

### Port Already in Use

**Issue:** `Cannot bind to port 8090`

**Solution:**

```bash
# Use a different port
docker run -p 9090:9090 -e MOCK_PORT=9090 karate-mock-server

# Or stop the process using port 8090
# macOS/Linux
lsof -ti:8090 | xargs kill

# Windows
netstat -ano | findstr :8090
taskkill /PID <PID> /F
```

### Custom Mocks Not Loading

**Issue:** Mounted feature files aren't being used

**Solution:**

```bash
# Verify mount point
docker run -v $(pwd)/mocks:/app/mocks karate-mock-server list-mocks

# Check file permissions (must be readable)
chmod -R 644 mocks/*.feature

# Verify volume is mounted correctly
docker inspect karate-mock-server | grep -A 10 Mounts
```

### Connection Refused from Backend

**Issue:** Backend can't reach mock server

**Solution:**

```bash
# If backend in Docker, use service name not localhost
MOCK_URL=http://karate-mock:8090  # ✅ Correct
MOCK_URL=http://localhost:8090    # ❌ Won't work in Docker

# If backend on host, use host.docker.internal
MOCK_URL=http://host.docker.internal:8090  # ✅ For Docker Desktop

# Or use host network mode
docker run --network host karate-mock-server
```

## 📦 Image Optimization

### Current Image Size

```bash
docker images karate-mock-server
# REPOSITORY            TAG       SIZE
# karate-mock-server    latest    ~200MB
```

### Further Optimization

To reduce image size further:

```bash
# Use Alpine-based JRE
# Edit Dockerfile FROM line:
FROM eclipse-temurin:21-jre-alpine

# Remove unnecessary test resources
# Add to .dockerignore:
build/
test-results/
```

## 🚀 CI/CD Integration

### GitHub Actions Example

```yaml
name: Test with Mock Server

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Build mock server
        run: |
          cd karate-microservices-testing
          docker build -t karate-mock-server .

      - name: Start mock server
        run: |
          docker run -d -p 8090:8090 --name mock karate-mock-server

      - name: Wait for mock server
        run: |
          timeout 30 bash -c 'until curl -f http://localhost:8090/stripe/health; do sleep 1; done'

      - name: Run tests
        run: |
          # Your backend tests here
          ./gradlew test

      - name: Stop mock server
        if: always()
        run: docker stop mock
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    stages {
        stage('Build Mock Server') {
            steps {
                sh 'docker build -t karate-mock-server karate-microservices-testing/'
            }
        }
        stage('Start Mock Server') {
            steps {
                sh 'docker run -d -p 8090:8090 --name mock karate-mock-server'
                sh 'sleep 10'  // Wait for startup
            }
        }
        stage('Run Tests') {
            steps {
                sh './gradlew test'
            }
        }
    }
    post {
        always {
            sh 'docker stop mock || true'
            sh 'docker rm mock || true'
        }
    }
}
```

## 🎓 Advanced Usage

### Multiple Mock Servers

Run multiple mock servers with different configurations:

```bash
# Stripe mock on port 8090
docker run -d --name stripe-mock \
  -p 8090:8090 \
  -e MOCK_FEATURE_FILE='classpath:mocks/payment-gateways/stripe-mock.feature' \
  karate-mock-server

# PayPal mock on port 8091
docker run -d --name paypal-mock \
  -p 8091:8091 \
  -e MOCK_PORT=8091 \
  -e MOCK_FEATURE_FILE='classpath:mocks/payment-gateways/paypal-mock.feature' \
  karate-mock-server
```

### Dynamic Mock Configuration

Use environment files:

```bash
# Create .env file
cat > mock.env << EOF
MOCK_PORT=8090
MOCK_ENV=qa
MOCK_FEATURE_FILE=classpath:mocks
MOCK_BLOCK_MS=3600000
EOF

# Run with env file
docker run --env-file mock.env -p 8090:8090 karate-mock-server
```

### Debug Mode

Run in interactive mode to see detailed logs:

```bash
docker run -it -p 8090:8090 karate-mock-server start-mock
```

## 📚 Additional Resources

- [Karate Documentation](https://github.com/karatelabs/karate)
- [Docker Documentation](https://docs.docker.com/)
- [Main README](../README.md)
- [Karate Module README](./README.md)

## 💡 Tips and Best Practices

1. **Use Docker Compose** for complex setups with multiple services
2. **Mount volumes** for custom mocks instead of rebuilding the image
3. **Use health checks** to ensure mock server is ready before running tests
4. **Set appropriate MOCK_BLOCK_MS** based on your test duration
5. **Use Docker networks** for service-to-service communication
6. **Tag your images** with version numbers for better tracking
7. **Clean up** stopped containers regularly: `docker system prune`

## 🤝 Contributing

To improve the mock server Docker setup:

1. Update `Dockerfile` for image optimizations
2. Enhance `docker-entrypoint.sh` with new features
3. Add new mock endpoints in feature files
4. Update this documentation

---

**Need help?** Check the troubleshooting section or open an issue on GitHub.
