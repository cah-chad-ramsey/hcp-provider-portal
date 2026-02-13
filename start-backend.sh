#!/bin/bash

echo "================================"
echo "Starting HCP Portal Backend"
echo "================================"

cd "$(dirname "$0")/backend"

# Check if PostgreSQL is running
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "❌ ERROR: PostgreSQL is not running!"
    echo "Start it with: brew services start postgresql@15"
    exit 1
fi

# Check if database exists
if ! psql -lqt | cut -d \| -f 1 | grep -qw hcp_portal; then
    echo "❌ ERROR: Database 'hcp_portal' does not exist!"
    echo "Create it with: createdb hcp_portal"
    exit 1
fi

echo "✅ PostgreSQL is running"
echo "✅ Database exists"
echo ""
echo "Starting Spring Boot application..."
echo "This will take 2-3 minutes on first run..."
echo ""

# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=hcp_portal
export DB_USER=${USER}
export DB_PASSWORD=""

# Start backend
mvn spring-boot:run
