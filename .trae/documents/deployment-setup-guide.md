# Deployment and Setup Guide

## Overview

This guide provides comprehensive instructions for setting up, developing, and deploying the AHSS Shared Services application, which consists of a Spring Boot backend and React frontend.

## Prerequisites

### System Requirements
- **Java**: OpenJDK 21 or higher
- **Node.js**: 18.x or higher
- **npm**: 9.x or higher (comes with Node.js)
- **Docker**: Latest version (for PostgreSQL)
- **Git**: Latest version

### Development Tools (Recommended)
- **IDE**: IntelliJ IDEA, VS Code, or similar
- **Database Client**: pgAdmin, DBeaver, or similar
- **API Testing**: Postman, Insomnia, or curl
- **Terminal**: Modern terminal with shell support

## Project Structure

```
shared-services/
├── backend/                 # Spring Boot application
│   ├── src/
│   ├── build.gradle
│   └── settings.gradle
├── frontend/               # React application
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
├── docker-compose.yml      # PostgreSQL database
└── .trae/
    └── documents/          # Technical documentation
```

## Local Development Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd shared-services
```

### 2. Database Setup

#### Option A: PostgreSQL with Docker (Recommended)

```bash
# Start PostgreSQL container
docker-compose up -d postgres

# Verify PostgreSQL is running
docker ps
docker logs sharedservices-postgres

# Connect to database (optional)
docker exec -it sharedservices-postgres psql -U postgres -d sharedservices
```

**Database Configuration:**
- **Host**: localhost
- **Port**: 5432
- **Database**: sharedservices
- **Username**: postgres
- **Password**: postgres

#### Option B: H2 In-Memory Database (Development Only)

Update `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
  h2:
    console:
      enabled: true
  flyway:
    enabled: false
```

### 3. Backend Setup

```bash
cd backend

# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Alternative: Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Backend will be available at:** `http://localhost:8080`

#### Verify Backend Setup

```bash
# Health check
curl http://localhost:8080/api/products

# Should return: []

# Test authentication
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password"}'
```

### 4. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

**Frontend will be available at:** `http://localhost:5173`

#### Verify Frontend Setup

1. Open browser to `http://localhost:5173`
2. Navigate to Login page
3. Enter any username/password and click "Sign In"
4. Should redirect to User Groups page
5. Click "New Group" to test API integration

## Environment Configuration

### Backend Environment Variables

Create `backend/src/main/resources/application-dev.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/sharedservices}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: ${DDL_AUTO:validate}
    show-sql: ${SHOW_SQL:false}

jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}
  expiration: ${JWT_EXPIRATION:86400000}

logging:
  level:
    com.ahss: ${LOG_LEVEL:INFO}
    org.springframework.security: INFO
```

### Frontend Environment Variables

Create `frontend/.env.local`:

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api/v1

# Development settings
VITE_NODE_ENV=development
```

Create `frontend/.env.production`:

```bash
# Production API URL
VITE_API_BASE_URL=https://your-api-domain.com/api/v1
```

## Production Deployment

### Backend Deployment (Render/Heroku)

#### 1. Prepare for Deployment

Create `backend/Procfile`:
```
web: java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

Create `backend/system.properties`:
```
java.runtime.version=21
```

#### 2. Environment Variables (Production)

Set these environment variables in your deployment platform:

```bash
# Database
DATABASE_URL=jdbc:postgresql://your-db-host:5432/your-db-name
DATABASE_USERNAME=your-db-username
DATABASE_PASSWORD=your-db-password

# Security
JWT_SECRET=your-very-secure-secret-key-at-least-256-bits
JWT_EXPIRATION=86400000

# Application
SPRING_PROFILES_ACTIVE=prod
DDL_AUTO=validate
SHOW_SQL=false

# Logging
LOG_LEVEL=WARN
```

#### 3. Deploy to Render

1. Connect your GitHub repository to Render
2. Create a new Web Service
3. Configure build and start commands:
   - **Build Command**: `cd backend && ./gradlew build`
   - **Start Command**: `cd backend && java -jar build/libs/backend-0.0.1-SNAPSHOT.jar`
4. Set environment variables
5. Deploy

#### 4. Deploy to Heroku

```bash
# Install Heroku CLI
# Login to Heroku
heroku login

# Create Heroku app
heroku create your-app-name

# Set buildpack
heroku buildpacks:set heroku/gradle

# Set environment variables
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set JWT_SECRET=your-secret-key
# ... other environment variables

# Deploy
git subtree push --prefix=backend heroku main
```

### Frontend Deployment (Vercel/Netlify)

#### 1. Build Configuration

Ensure `frontend/package.json` has correct build script:

```json
{
  "scripts": {
    "build": "tsc -b && vite build",
    "preview": "vite preview"
  }
}
```

#### 2. Deploy to Vercel

```bash
# Install Vercel CLI
npm i -g vercel

# Login and deploy
cd frontend
vercel

# Set environment variables in Vercel dashboard
# VITE_API_BASE_URL=https://your-backend-url.com/api/v1
```

#### 3. Deploy to Netlify

1. Connect GitHub repository to Netlify
2. Configure build settings:
   - **Base directory**: `frontend`
   - **Build command**: `npm run build`
   - **Publish directory**: `frontend/dist`
3. Set environment variables in Netlify dashboard
4. Deploy

### Database Migration (Production)

#### PostgreSQL Setup

```sql
-- Create database
CREATE DATABASE sharedservices;

-- Create user (optional)
CREATE USER sharedservices_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE sharedservices TO sharedservices_user;
```

#### Flyway Migration

```bash
# Run migrations manually (if needed)
cd backend
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo
```

## Docker Deployment

### 1. Backend Dockerfile

Create `backend/Dockerfile`:

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy gradle files
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Build application
RUN ./gradlew build -x test

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "build/libs/backend-0.0.1-SNAPSHOT.jar"]
```

### 2. Frontend Dockerfile

Create `frontend/Dockerfile`:

```dockerfile
FROM node:18-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build application
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy built files
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 3. Docker Compose (Full Stack)

Create `docker-compose.prod.yml`:

```yaml
version: '3.9'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: sharedservices
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - app-network

  backend:
    build: ./backend
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/sharedservices
      DATABASE_USERNAME: postgres
      DATABASE_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - postgres
    networks:
      - app-network
    ports:
      - "8080:8080"

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - app-network

volumes:
  postgres_data:

networks:
  app-network:
    driver: bridge
```

### 4. Deploy with Docker

```bash
# Set environment variables
export POSTGRES_PASSWORD=secure_password
export JWT_SECRET=your-secure-jwt-secret

# Build and run
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f
```

## Monitoring and Maintenance

### Health Checks

#### Backend Health Check

```bash
# Basic health check
curl http://localhost:8080/api/products

# Database connectivity check
curl http://localhost:8080/api/v1/auth/login \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"username": "test", "password": "test"}'
```

#### Frontend Health Check

```bash
# Check if frontend is serving
curl http://localhost:5173

# Check if build is successful
cd frontend && npm run build
```

### Log Monitoring

#### Backend Logs

```bash
# Development
./gradlew bootRun

# Production (Docker)
docker-compose logs -f backend

# Production (Heroku)
heroku logs --tail -a your-app-name
```

#### Frontend Logs

```bash
# Development
npm run dev

# Production build logs
npm run build

# Server logs (Nginx)
docker-compose logs -f frontend
```

### Database Maintenance

#### Backup

```bash
# PostgreSQL backup
docker exec sharedservices-postgres pg_dump -U postgres sharedservices > backup.sql

# Restore
docker exec -i sharedservices-postgres psql -U postgres sharedservices < backup.sql
```

#### Migration Management

```bash
# Check migration status
./gradlew flywayInfo

# Migrate to latest
./gradlew flywayMigrate

# Rollback (if needed)
./gradlew flywayUndo
```

## Troubleshooting

### Common Issues

#### Backend Issues

**Issue**: Application fails to start
```bash
# Check Java version
java -version

# Check database connectivity
docker ps
docker logs sharedservices-postgres

# Check application logs
./gradlew bootRun --debug
```

**Issue**: Database connection errors
```bash
# Verify PostgreSQL is running
docker exec sharedservices-postgres pg_isready

# Check connection string
# Verify username/password in application.yml
```

**Issue**: 403 Forbidden errors
```bash
# Check security configuration
# Verify JWT token is being sent
# Check CORS settings
```

#### Frontend Issues

**Issue**: Build failures
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Check Node.js version
node --version
npm --version
```

**Issue**: API connection errors
```bash
# Verify backend is running
curl http://localhost:8080/api/products

# Check environment variables
echo $VITE_API_BASE_URL

# Check browser network tab for CORS errors
```

**Issue**: Routing problems
```bash
# Verify React Router configuration
# Check browser console for errors
# Ensure proper base URL configuration
```

### Performance Optimization

#### Backend Optimization

```yaml
# application.yml optimizations
spring:
  jpa:
    hibernate:
      jdbc:
        batch_size: 20
    properties:
      hibernate:
        order_inserts: true
        order_updates: true
        jdbc:
          batch_versioned_data: true

# Connection pool optimization
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

#### Frontend Optimization

```bash
# Analyze bundle size
npm run build
npx vite-bundle-analyzer dist

# Optimize images
# Implement code splitting
# Add service worker for caching
```

## Security Checklist

### Backend Security

- [ ] Change default JWT secret
- [ ] Enable HTTPS in production
- [ ] Implement proper CORS configuration
- [ ] Add rate limiting
- [ ] Enable SQL injection protection
- [ ] Implement proper error handling
- [ ] Add request validation
- [ ] Enable security headers

### Frontend Security

- [ ] Sanitize user inputs
- [ ] Implement Content Security Policy
- [ ] Use HTTPS for API calls
- [ ] Validate API responses
- [ ] Implement proper error boundaries
- [ ] Add XSS protection
- [ ] Secure token storage

### Database Security

- [ ] Use strong passwords
- [ ] Enable SSL connections
- [ ] Implement backup encryption
- [ ] Regular security updates
- [ ] Access control and auditing
- [ ] Network security (VPC/firewall)

## Maintenance Schedule

### Daily
- Monitor application logs
- Check system resources
- Verify backup completion

### Weekly
- Review security logs
- Update dependencies (dev environment)
- Performance monitoring review

### Monthly
- Security patches and updates
- Database maintenance
- Backup testing and verification
- Performance optimization review

### Quarterly
- Full security audit
- Dependency updates (production)
- Infrastructure review
- Disaster recovery testing