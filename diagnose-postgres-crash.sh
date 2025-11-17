#!/bin/bash

# PostgreSQL OOM Crash Diagnostic Script
# Run this on your DigitalOcean droplet to investigate the crash

echo "=========================================="
echo "PostgreSQL OOM Crash Investigation"
echo "=========================================="
echo ""

# 1. Check current container status
echo "=== 1. Current Container Status ==="
docker ps -a | grep postgres
echo ""

# 2. Check memory limit applied to container
echo "=== 2. Docker Memory Limit (should be 3GB = 3221225472) ==="
docker inspect sharedservices-postgres --format='Memory Limit: {{.HostConfig.Memory}} bytes'
echo ""

# 3. Check if it was OOM killed
echo "=== 3. OOM Kill Status ==="
docker inspect sharedservices-postgres --format='OOMKilled: {{.State.OOMKilled}}'
docker inspect sharedservices-postgres --format='Exit Code: {{.State.ExitCode}}'
docker inspect sharedservices-postgres --format='Error: {{.State.Error}}'
echo ""

# 4. Get PostgreSQL container logs
echo "=== 4. PostgreSQL Container Logs (last 100 lines) ==="
docker logs sharedservices-postgres --tail 100
echo ""

# 5. Search for memory/crash related issues in logs
echo "=== 5. Memory/Error Related Log Entries ==="
docker logs sharedservices-postgres 2>&1 | grep -i -E "memory|oom|fatal|panic|error|killed" | tail -50
echo ""

# 6. Check kernel OOM killer logs
echo "=== 6. Kernel OOM Killer Logs ==="
sudo dmesg | grep -i -B 5 -A 10 "oom" | tail -100
echo ""

# 7. Check system memory status
echo "=== 7. Current System Memory ==="
free -h
echo ""

# 8. Check Docker memory usage
echo "=== 8. Current Docker Container Stats ==="
docker stats --no-stream
echo ""

# 9. Check PostgreSQL memory configuration
echo "=== 9. PostgreSQL Memory Configuration ==="
docker exec sharedservices-postgres psql -U postgres -c "SHOW shared_buffers;" 2>/dev/null || echo "Container not running"
docker exec sharedservices-postgres psql -U postgres -c "SHOW work_mem;" 2>/dev/null || echo "Container not running"
docker exec sharedservices-postgres psql -U postgres -c "SHOW maintenance_work_mem;" 2>/dev/null || echo "Container not running"
docker exec sharedservices-postgres psql -U postgres -c "SHOW max_connections;" 2>/dev/null || echo "Container not running"
echo ""

# 10. Check database size
echo "=== 10. Database Size ==="
docker exec sharedservices-postgres psql -U postgres -c "\l+ sharedservices" 2>/dev/null || echo "Container not running"
echo ""

# 11. Check active connections
echo "=== 11. Active Connections ==="
docker exec sharedservices-postgres psql -U postgres -d sharedservices -c \
  "SELECT count(*), state FROM pg_stat_activity GROUP BY state;" 2>/dev/null || echo "Container not running"
echo ""

# 12. Check for long-running queries
echo "=== 12. Current Queries ==="
docker exec sharedservices-postgres psql -U postgres -d sharedservices -c \
  "SELECT pid, usename, state, query_start, query FROM pg_stat_activity WHERE state != 'idle' ORDER BY query_start;" 2>/dev/null || echo "Container not running"
echo ""

# 13. Check container restart count
echo "=== 13. Container Restart Count ==="
docker inspect sharedservices-postgres --format='Restart Count: {{.RestartCount}}'
docker inspect sharedservices-postgres --format='Started At: {{.State.StartedAt}}'
docker inspect sharedservices-postgres --format='Finished At: {{.State.FinishedAt}}'
echo ""

# 14. Check if using Alpine or Debian image
echo "=== 14. PostgreSQL Image Info ==="
docker inspect sharedservices-postgres --format='Image: {{.Config.Image}}'
docker exec sharedservices-postgres cat /etc/os-release 2>/dev/null | head -3 || echo "Cannot read OS info"
echo ""

# 15. Summary
echo "=========================================="
echo "Investigation Complete!"
echo "=========================================="
echo ""
echo "Key things to look for:"
echo "1. OOMKilled should be 'true' if it was memory killed"
echo "2. Exit Code 137 indicates OOM kill"
echo "3. Kernel logs will show which process was killed and why"
echo "4. Check if memory limit is actually 3GB (3221225472 bytes)"
echo "5. Look for any FATAL or PANIC messages in PostgreSQL logs"
echo ""
