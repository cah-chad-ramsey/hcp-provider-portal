#!/bin/bash

set -e  # Exit on error

echo "================================"
echo "HCP Portal Backend Startup"
echo "================================"
echo ""

# Change to backend directory
cd "$(dirname "$0")/backend"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check for Java
echo "🔍 Checking prerequisites..."
echo ""

if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ ERROR: Java is not installed!${NC}"
    echo "Install with: brew install openjdk@21"
    exit 1
fi

# Check for Java 21 specifically
JAVA_21_HOME=$(/usr/libexec/java_home -v 21 2>/dev/null || echo "")
if [ -z "$JAVA_21_HOME" ]; then
    echo -e "${RED}❌ ERROR: Java 21 is not installed!${NC}"
    echo "Install with: brew install openjdk@21"
    exit 1
fi

# Set JAVA_HOME to Java 21
export JAVA_HOME=$JAVA_21_HOME
echo -e "${GREEN}✅ Java 21 found at: $JAVA_HOME${NC}"

# Check for Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ ERROR: Maven is not installed!${NC}"
    echo "Install with: brew install maven"
    exit 1
fi
echo -e "${GREEN}✅ Maven found: $(mvn -version | head -n 1)${NC}"

# Check for PostgreSQL
if ! command -v psql &> /dev/null; then
    echo -e "${RED}❌ ERROR: PostgreSQL is not installed!${NC}"
    echo "Install with: brew install postgresql@15"
    exit 1
fi
echo -e "${GREEN}✅ PostgreSQL found${NC}"
echo ""

# Check if PostgreSQL is running
echo "🔍 Checking PostgreSQL status..."
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  PostgreSQL is not running. Starting it...${NC}"
    brew services start postgresql@15

    # Wait for PostgreSQL to start (max 30 seconds)
    for i in {1..30}; do
        if pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
            echo -e "${GREEN}✅ PostgreSQL started successfully${NC}"
            break
        fi
        if [ $i -eq 30 ]; then
            echo -e "${RED}❌ ERROR: PostgreSQL failed to start after 30 seconds${NC}"
            exit 1
        fi
        sleep 1
    done
else
    echo -e "${GREEN}✅ PostgreSQL is running${NC}"
fi
echo ""

# Check if postgres role exists, create if not
echo "🔍 Checking PostgreSQL roles..."
if ! psql -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='postgres'" | grep -q 1; then
    echo -e "${YELLOW}⚠️  Creating 'postgres' role...${NC}"
    psql -d postgres -c "CREATE ROLE postgres WITH LOGIN PASSWORD 'postgres' SUPERUSER;"
    echo -e "${GREEN}✅ 'postgres' role created${NC}"
else
    echo -e "${GREEN}✅ 'postgres' role exists${NC}"
fi
echo ""

# Check if database exists, create if not
echo "🔍 Checking database..."
if ! psql -lqt | cut -d \| -f 1 | grep -qw hcp_portal; then
    echo -e "${YELLOW}⚠️  Database 'hcp_portal' does not exist. Creating...${NC}"
    createdb hcp_portal
    echo -e "${GREEN}✅ Database 'hcp_portal' created${NC}"
else
    echo -e "${GREEN}✅ Database 'hcp_portal' exists${NC}"
fi
echo ""

# Grant permissions to postgres role
echo "🔍 Ensuring database permissions..."
psql -d hcp_portal -c "GRANT ALL PRIVILEGES ON DATABASE hcp_portal TO postgres;" > /dev/null 2>&1
psql -d hcp_portal -c "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;" > /dev/null 2>&1
psql -d hcp_portal -c "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;" > /dev/null 2>&1
echo -e "${GREEN}✅ Database permissions granted${NC}"
echo ""

# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=hcp_portal
export DB_USER=postgres
export DB_PASSWORD=postgres

echo "🔧 Environment Configuration:"
echo "   JAVA_HOME: $JAVA_HOME"
echo "   DB_HOST: $DB_HOST"
echo "   DB_PORT: $DB_PORT"
echo "   DB_NAME: $DB_NAME"
echo "   DB_USER: $DB_USER"
echo ""

# Check for Maven dependencies (warn if .m2 is empty)
if [ ! -d "$HOME/.m2/repository/org/springframework" ]; then
    echo -e "${YELLOW}⚠️  First-time setup detected!${NC}"
    echo "   Maven will download dependencies (~150MB)"
    echo "   This may take 5-10 minutes..."
    echo ""
    echo -e "${BLUE}💡 TIP: If you're on a corporate VPN and Maven fails with SSL errors,${NC}"
    echo -e "${BLUE}   disconnect from VPN temporarily for the initial download.${NC}"
    echo ""
    read -p "Press Enter to continue..."
    echo ""
fi

# Kill any existing process on port 8080
echo "🔍 Checking for processes on port 8080..."
BACKEND_PIDS=$(lsof -ti:8080 2>/dev/null || echo "")
if [ ! -z "$BACKEND_PIDS" ]; then
    echo -e "${YELLOW}⚠️  Killing existing processes on port 8080: $BACKEND_PIDS${NC}"
    kill -9 $BACKEND_PIDS 2>/dev/null || true
    sleep 2
fi
echo ""

echo "================================"
echo "🚀 Starting Spring Boot Backend"
echo "================================"
echo ""
echo "Backend will be available at:"
echo -e "${GREEN}   📍 http://localhost:8080${NC}"
echo -e "${GREEN}   📖 http://localhost:8080/swagger-ui${NC}"
echo ""
echo "Credentials:"
echo "   👤 admin/password (ADMIN)"
echo "   👤 staff/password (OFFICE_STAFF)"
echo "   👤 agent/password (SUPPORT_AGENT)"
echo ""
echo -e "${BLUE}Press Ctrl+C to stop${NC}"
echo ""

# Start backend
mvn spring-boot:run
