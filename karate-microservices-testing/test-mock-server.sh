#!/bin/bash
echo "Testing Mock Server Lifecycle with CustomRunnerTest"
echo "===================================================="
echo ""
echo "Running: ./gradlew test --tests '*CustomRunnerTest' -Dkarate.env=qa -Dmock.server.enabled=true --rerun-tasks"
echo ""

./gradlew test --tests '*CustomRunnerTest' -Dkarate.env=qa -Dmock.server.enabled=true --rerun-tasks 2>&1 | \
grep -E "Mock server|Starting|Stopping|✓|Available Mock|Port:" | \
head -20

echo ""
echo "===================================================="
echo "Check full output in: build/test-results/test/"
