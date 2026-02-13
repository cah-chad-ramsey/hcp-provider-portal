#!/bin/bash

echo "================================"
echo "Starting HCP Portal Frontend"
echo "================================"

cd "$(dirname "$0")/frontend"

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies (first time only)..."
    echo "This will take 2-3 minutes..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ ERROR: npm install failed!"
        exit 1
    fi
fi

echo "✅ Dependencies installed"
echo ""
echo "Starting Angular dev server..."
echo "Frontend will be available at: http://localhost:4200"
echo ""

# Start frontend
npm start
