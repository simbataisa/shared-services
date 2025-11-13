# Multi-stage Dockerfile for Karate Mock Server
# Stage 1: Build the Karate test module
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build JAR and compile test classes without running tests
# assemble: creates JAR, testClasses: compiles test code and processes test resources
RUN ./gradlew assemble testClasses --no-daemon

# Stage 2: Runtime image for mock server
# Use JDK instead of JRE since we need to run Gradle at runtime
FROM eclipse-temurin:21-jdk AS runner
WORKDIR /app

# Create a non-root user with home directory and install required tools
RUN groupadd -r karate && useradd -r -g karate -m -d /home/karate karate && \
    apt-get update && apt-get install -y --no-install-recommends socat curl && \
    rm -rf /var/lib/apt/lists/*

# Copy built artifacts from builder (maintain Gradle's expected directory structure)
COPY --from=builder /app/build/libs build/libs
COPY --from=builder /app/build/classes build/classes
COPY --from=builder /app/build/resources build/resources

# Copy source code (needed for Karate to find feature files)
COPY --from=builder /app/src src

# Copy Gradle wrapper for running tests
COPY --from=builder /app/gradlew .
COPY --from=builder /app/gradle gradle
COPY --from=builder /app/build.gradle .
COPY --from=builder /app/settings.gradle .
COPY --from=builder /app/gradle.properties .

# Copy Gradle home directory from builder (includes downloaded Gradle distribution)
COPY --from=builder /root/.gradle /home/karate/.gradle

# Copy entrypoint script
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Create directories with proper permissions
RUN mkdir -p /app/mocks && \
    chown -R karate:karate /app /home/karate

# Switch to non-root user
USER karate

# Expose default mock server port
EXPOSE 8090

# Environment variables for configuration
ENV MOCK_PORT=8090
ENV MOCK_ENV=qa
ENV MOCK_FEATURE_FILE="src/test/resources/mocks/mock-server.feature"
ENV MOCK_BLOCK_MS=600000
ENV MOCK_KARATE_PORT=8091

# Set entrypoint
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["start-mock"]
