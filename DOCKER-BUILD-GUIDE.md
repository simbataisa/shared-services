# Docker Build Guide

This guide explains the different ways to build and run the Shared Services platform.

## 🎯 Quick Decision Guide

Choose the right approach for your needs:

| Scenario | Recommended Approach | Script |
|----------|---------------------|--------|
| **First time setup (no Java/Node.js)** | Docker Build | `./run-all-docker-build.sh` or `.\run-all-docker-build.bat` |
| **CI/CD pipelines** | Docker Build | `docker compose -f docker-compose-build.yml build` |
| **Active development** | Local Build | `./run-all.sh` or `.\run-all.bat` |
| **Quick testing** | Local Build | `./run-all.sh` or `.\run-all.bat` |
| **Windows (no Gradle issues)** | Docker Build | `.\run-all-docker-build.bat` |

## 📦 Build Approaches

### 1. Docker Build (Multi-Stage) - Zero Local Dependencies

**What it does:**
- Builds backend JAR inside Docker container using Gradle
- Builds frontend inside Docker container using npm
- Creates optimized runtime images
- No local Java or Node.js installation required

**Requirements:**
- Docker Desktop (or Docker + Docker Compose)
- That's it! 🎉

**When to use:**
- ✅ First time setup on a new machine
- ✅ CI/CD pipelines for consistent builds
- ✅ Production deployments
- ✅ When you don't want to install Java 21 or Node.js locally
- ✅ Windows users who experience Gradle build issues

**How to use:**

```bash
# Linux/macOS
./run-all-docker-build.sh

# Windows
.\run-all-docker-build.bat

# Or manually
docker compose -f docker-compose-build.yml build
docker compose -f docker-compose-build.yml --profile observability up -d
```

**Pros:**
- 🚀 No local Java or Node.js needed
- 🐳 Consistent builds across all platforms
- 📦 Docker layer caching speeds up subsequent builds
- 🔒 Isolated build environment
- ✅ Production-ready approach

**Cons:**
- ⏱️ First build takes longer (downloads dependencies in Docker)
- 💾 Requires more disk space (Docker images)
- 🔄 Slower iteration for active development

**Build time:**
- First build: 5-10 minutes (downloads all dependencies)
- Subsequent builds: 2-3 minutes (with Docker cache)

---

### 2. Local Build with Jib - Fast Development

**What it does:**
- Builds backend JAR locally using Gradle
- Uses Jib to create Docker image from local JAR
- Builds frontend inside Docker container
- Leverages local Gradle and npm caches

**Requirements:**
- Java 21 (JDK)
- Docker Desktop
- (Frontend build still happens in Docker)

**When to use:**
- ✅ Active development with frequent rebuilds
- ✅ When you have Java 21 already installed
- ✅ Faster iteration cycles
- ✅ Debugging build issues locally

**How to use:**

```bash
# Linux/macOS
./run-all.sh

# Windows
.\run-all.bat

# Or manually
cd backend && ./gradlew jibDockerBuild && cd ..
docker compose build frontend
docker compose --profile observability up -d
```

**Pros:**
- ⚡ Faster builds for active development
- 🔍 Easier to debug build issues
- 💾 Uses local Gradle cache
- 🛠️ More control over build process

**Cons:**
- ❌ Requires Java 21 installation
- 🖥️ Platform-dependent builds
- ⚙️ May have different build results on different machines

**Build time:**
- First build: 3-5 minutes
- Subsequent builds: 30-60 seconds (with Gradle cache)

---

## 🔍 Detailed Comparison

### Architecture Differences

#### Docker Build (Multi-Stage)

```
┌─────────────────────────────────────┐
│  Stage 1: Builder                   │
│  - Base: eclipse-temurin:21-jdk     │
│  - Copy source code                 │
│  - Run ./gradlew bootJar            │
│  - Output: app.jar                  │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Stage 2: Runtime                   │
│  - Base: eclipse-temurin:21-jre     │
│  - Copy app.jar from Stage 1        │
│  - Optimized runtime image          │
└─────────────────────────────────────┘
```

**Dockerfile location:** `backend/Dockerfile`

#### Local Build with Jib

```
┌─────────────────────────────────────┐
│  Local Machine                      │
│  - Run ./gradlew bootJar            │
│  - Uses local Gradle cache          │
│  - Output: build/libs/*.jar         │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  Jib (no Dockerfile)                │
│  - Reads JAR from local build       │
│  - Creates optimized layers         │
│  - Pushes to Docker daemon          │
└─────────────────────────────────────┘
```

**Gradle task:** `jibDockerBuild`

---

## 📂 Files and Scripts

### Docker Build Files

| File | Purpose |
|------|---------|
| `backend/Dockerfile` | Multi-stage build for backend |
| `backend/.dockerignore` | Excludes unnecessary files from build context |
| `docker-compose-build.yml` | Docker Compose for Docker-based builds |
| `docker-compose-build.windows.yml` | Windows-specific Docker Compose |
| `run-all-docker-build.sh` | Automated script for Linux/macOS |
| `run-all-docker-build.bat` | Automated script for Windows |

### Local Build Files

| File | Purpose |
|------|---------|
| `backend/build.gradle` | Gradle build configuration with Jib plugin |
| `docker-compose.yml` | Docker Compose for pre-built images |
| `docker-compose.windows.yml` | Windows-specific Docker Compose |
| `run-all.sh` | Automated script for Linux/macOS |
| `run-all.bat` | Automated script for Windows |

---

## 🛠️ Manual Build Commands

### Docker Build Approach

```bash
# Build backend in Docker
docker compose -f docker-compose-build.yml build backend

# Build frontend in Docker
docker compose -f docker-compose-build.yml build frontend

# Build both
docker compose -f docker-compose-build.yml build

# Start services
docker compose -f docker-compose-build.yml --profile observability up -d

# View logs
docker compose -f docker-compose-build.yml logs -f backend

# Stop services
docker compose -f docker-compose-build.yml --profile observability down
```

### Local Build Approach

```bash
# Build backend locally
cd backend
./gradlew clean jibDockerBuild
cd ..

# Build frontend in Docker
docker compose build frontend

# Start services
docker compose --profile observability up -d

# View logs
docker compose logs -f backend

# Stop services
docker compose --profile observability down
```

---

## 🔧 Troubleshooting

### Docker Build Issues

**Problem:** Docker build fails with "no space left on device"

**Solution:**
```bash
# Clean up Docker
docker system prune -a
docker volume prune
```

**Problem:** Build is very slow

**Solution:**
- Ensure Docker has enough resources (CPU/Memory)
- Check Docker Desktop settings
- Use Docker layer caching
- Ensure good internet connection for first build

### Local Build Issues

**Problem:** "JAVA_HOME is not set"

**Solution:**
```bash
# macOS/Linux
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Windows
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-21"
```

**Problem:** Gradle build fails

**Solution:**
```bash
# Clean Gradle cache
./gradlew clean build --refresh-dependencies
```

---

## 💡 Best Practices

### For Development

1. Use **Local Build** for faster iteration
2. Keep Docker Desktop running
3. Use `docker compose logs -f` to monitor services
4. Restart only affected services: `docker compose restart backend`

### For Production

1. Use **Docker Build** for consistent deployments
2. Build images in CI/CD pipeline
3. Tag images with version numbers
4. Use multi-stage builds to minimize image size
5. Scan images for vulnerabilities

### For CI/CD

```yaml
# Example GitHub Actions workflow
- name: Build Docker images
  run: |
    docker compose -f docker-compose-build.yml build
    docker tag shared-services:0.0.1-SNAPSHOT myregistry/shared-services:${{ github.sha }}
```

---

## 📊 Resource Usage

| Approach | Disk Space | Build Time (First) | Build Time (Cached) | RAM Usage |
|----------|------------|-------------------|---------------------|-----------|
| Docker Build | ~2-3 GB | 5-10 min | 2-3 min | 4-6 GB |
| Local Build | ~1-2 GB | 3-5 min | 30-60 sec | 2-4 GB |

---

## 🎓 Learning Resources

- [Docker Multi-Stage Builds](https://docs.docker.com/build/building/multi-stage/)
- [Jib Documentation](https://github.com/GoogleContainerTools/jib)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Gradle Docker Plugin](https://docs.gradle.org/current/userguide/building_java_projects.html)

---

## ❓ FAQ

**Q: Which approach is faster for development?**
A: Local Build is faster for development due to Gradle caching.

**Q: Which approach should I use for CI/CD?**
A: Docker Build ensures consistent builds across environments.

**Q: Can I switch between approaches?**
A: Yes! Both approaches create compatible Docker images.

**Q: Do I need to install Java for Docker Build?**
A: No! Docker Build only requires Docker installed.

**Q: Which files do I need to commit to Git?**
A: Commit all Dockerfiles, docker-compose files, and build scripts. Don't commit built JARs or Docker images.

**Q: How do I clean up old builds?**
A: Run `docker system prune -a` to clean up old images and `./gradlew clean` for local builds.
