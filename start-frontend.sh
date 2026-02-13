#!/bin/bash

set -e  # Exit on error

echo "================================"
echo "HCP Portal Frontend Startup"
echo "================================"
echo ""

# Change to frontend directory
cd "$(dirname "$0")/frontend"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check for Node.js
echo "🔍 Checking prerequisites..."
echo ""

if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ ERROR: Node.js is not installed!${NC}"
    echo "Install with: brew install node"
    exit 1
fi

NODE_VERSION=$(node -v)
echo -e "${GREEN}✅ Node.js found: $NODE_VERSION${NC}"

# Check for npm
if ! command -v npm &> /dev/null; then
    echo -e "${RED}❌ ERROR: npm is not installed!${NC}"
    echo "Install with: brew install node"
    exit 1
fi

NPM_VERSION=$(npm -v)
echo -e "${GREEN}✅ npm found: v$NPM_VERSION${NC}"
echo ""

# Kill any existing process on port 4200
echo "🔍 Checking for processes on port 4200..."
FRONTEND_PIDS=$(lsof -ti:4200 2>/dev/null || echo "")
if [ ! -z "$FRONTEND_PIDS" ]; then
    echo -e "${YELLOW}⚠️  Killing existing processes on port 4200: $FRONTEND_PIDS${NC}"
    kill -9 $FRONTEND_PIDS 2>/dev/null || true
    sleep 2
    echo -e "${GREEN}✅ Port 4200 is now available${NC}"
else
    echo -e "${GREEN}✅ Port 4200 is available${NC}"
fi
echo ""

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "================================"
    echo "📦 Installing Dependencies"
    echo "================================"
    echo ""
    echo -e "${YELLOW}First-time setup detected!${NC}"
    echo "Installing npm packages (this may take 3-5 minutes)..."
    echo ""

    npm install

    if [ $? -ne 0 ]; then
        echo ""
        echo -e "${RED}❌ ERROR: npm install failed!${NC}"
        echo ""
        echo "Common fixes:"
        echo "  1. Delete node_modules and package-lock.json, try again"
        echo "  2. Clear npm cache: npm cache clean --force"
        echo "  3. Check network connection"
        exit 1
    fi

    echo ""
    echo -e "${GREEN}✅ Dependencies installed successfully${NC}"
    echo ""
else
    echo -e "${GREEN}✅ Dependencies already installed${NC}"
    echo ""
fi

# Verify Angular CLI is available
if [ ! -f "node_modules/.bin/ng" ]; then
    echo -e "${RED}❌ ERROR: Angular CLI not found in node_modules${NC}"
    echo "Try running: npm install"
    exit 1
fi

echo "================================"
echo "🚀 Starting Angular Dev Server"
echo "================================"
echo ""
echo "Frontend will be available at:"
echo -e "${GREEN}   📍 http://localhost:4200${NC}"
echo ""
echo "Make sure the backend is running at:"
echo -e "${BLUE}   📍 http://localhost:8080${NC}"
echo ""
echo "Credentials:"
echo "   👤 admin/password (ADMIN)"
echo "   👤 staff/password (OFFICE_STAFF)"
echo "   👤 agent/password (SUPPORT_AGENT)"
echo ""
echo -e "${BLUE}Press Ctrl+C to stop${NC}"
echo ""

# Wait a moment for terminal to be ready
sleep 1

# Start frontend
npm start
