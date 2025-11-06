# Docker Compose Troubleshooting Guide

This guide covers common Docker Compose issues and reliable fixes when running the Shared Services stack.

## Symptoms

- Error: `failed to set up container networking: network <id> not found`
- Warning: `the attribute version is obsolete`
- Services fail to start while others are healthy (e.g., `backend`, `frontend`, `kafka-ui`).

## Root Causes

- Named containers (e.g., `sharedservices-backend`, `sharedservices-frontend`) can persist across runs and may reference stale network IDs after cleanup.
- Orphaned resources from switching profiles or compose files without full teardown.
- Legacy `version` field in compose files triggers warnings in Compose v2 (ignored but noisy).

## Collector Endpoint Inside Containers

- Do not use `localhost` for inter-service communication in containers; use Compose service names.
- Backend should export OTLP to the collector service:

```yaml
# docker-compose.yml (backend service environment)
environment:
  OTEL_EXPORTER_OTLP_ENDPOINT: http://otel-collector:4318
  MANAGEMENT_OTLP_TRACING_ENDPOINT: http://otel-collector:4318/v1/traces
```

- When running backend locally (outside Docker), `localhost:4318` is correct. The error occurs only inside containers.

## Quick Fix

Use the following commands to reset networks and containers and then bring services back up.

```bash
# From repository root
cd /path/to/shared-services

# Stop and remove resources and orphans
docker compose down --remove-orphans

# Prune dangling networks (confirm with -f)
docker network prune -f

# Remove stale named containers that may reference deleted networks
docker rm -f sharedservices-backend sharedservices-frontend sharedservices-kafka-ui || true

# Rebuild images if you changed Dockerfiles or build args
docker compose build frontend

# Recreate services (macOS/Linux)
docker compose --profile observability up -d \
  postgres kafka otel-collector jaeger kafka-ui backend frontend
```

Windows PowerShell:

```powershell
cd C:\path\to\shared-services

docker compose -f docker-compose.windows.yml down --remove-orphans
docker network prune -f
docker rm -f sharedservices-backend sharedservices-frontend sharedservices-kafka-ui
docker compose -f docker-compose.windows.yml build frontend
docker compose -f docker-compose.windows.yml up -d \
  postgres kafka otel-collector jaeger kafka-ui backend frontend
```

## Preventive Practices

- Prefer `docker compose down --remove-orphans` when switching profiles or compose files.
- Keep `container_name` only if you need stable names; otherwise, let Compose manage them to reduce stale references.
- Remove the legacy `version: '3.9'` line to silence the “obsolete” warning (Compose v2 ignores it).

## Network Verification

```bash
# List project network (should exist and be bridge type)
docker network ls

# Inspect services status
docker compose ps
```

## Frontend API Base URL

- In-container default uses service discovery: `http://backend:8080/api/v1`.
- For local, non-Docker builds, use `http://localhost:8080/api/v1` or set `VITE_API_BASE_URL` explicitly.
- To build the frontend image with a custom API base:

```bash
# macOS/Linux
VITE_API_BASE_URL=http://localhost:8080/api/v1 \
docker compose build frontend

# Windows PowerShell
$env:VITE_API_BASE_URL = "http://localhost:8080/api/v1"; \
docker compose -f docker-compose.windows.yml build frontend
```

## Health Checks

```bash
# Backend health
curl http://localhost:8080/actuator/health

# Jaeger UI
open http://localhost:16686

# Kafka UI
open http://localhost:8081

# Frontend
open http://localhost:5173
```
