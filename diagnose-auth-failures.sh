#!/bin/bash
echo "========================================"
echo "PostgreSQL Authentication Failure Investigation"
echo "========================================"
echo

echo "=== 1. Check PostgreSQL logs for failed authentication attempts ==="
docker logs sharedservices-postgres 2>&1 | grep -i "authentication failed" | tail -20
echo

echo "=== 2. Check what's currently connecting to PostgreSQL ==="
docker exec sharedservices-postgres psql -U postgres -d sharedservices -c "
SELECT
    pid,
    usename,
    application_name,
    client_addr,
    state,
    backend_start,
    query_start,
    state_change,
    wait_event,
    query
FROM pg_stat_activity
WHERE usename != 'postgres' OR application_name != ''
ORDER BY backend_start DESC
LIMIT 20;
"
echo

echo "=== 3. Check backend container logs for database connection errors ==="
docker logs sharedservices-backend 2>&1 | grep -i "pgg_superadmin" | tail -20
echo

echo "=== 4. Check backend environment variables ==="
docker exec sharedservices-backend env | grep -E "(DATASOURCE|DATABASE|DB_|POSTGRES)"
echo

echo "=== 5. Check if there's a Flyway migration history with pgg_superadmin ==="
docker exec sharedservices-postgres psql -U postgres -d sharedservices -c "
SELECT * FROM flyway_schema_history
WHERE script LIKE '%pgg%' OR script LIKE '%superadmin%'
ORDER BY installed_rank DESC;
"
echo

echo "=== 6. Check current database roles ==="
docker exec sharedservices-postgres psql -U postgres -c "\du"
echo

echo "=== 7. Monitor connection attempts in real-time (5 seconds) ==="
echo "Watching for new connection attempts..."
timeout 5 docker exec sharedservices-postgres tail -f /var/lib/postgresql/data/log/postgresql-*.log 2>/dev/null || \
timeout 5 docker logs -f sharedservices-postgres 2>&1 | grep -i "connection\|authentication"
echo

echo "========================================"
echo "Investigation Complete!"
echo "========================================"
