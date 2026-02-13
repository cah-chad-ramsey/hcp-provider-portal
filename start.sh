#!/bin/bash

echo "╔════════════════════════════════════════════╗"
echo "║     HCP Provider Portal - Startup Menu    ║"
echo "╚════════════════════════════════════════════╝"
echo ""
echo "Choose an option:"
echo "  1) Start Backend only"
echo "  2) Start Frontend only"
echo "  3) Start Both (in separate terminals)"
echo "  4) Check Prerequisites"
echo "  5) Exit"
echo ""
read -p "Enter choice [1-5]: " choice

case $choice in
    1)
        echo ""
        ./start-backend.sh
        ;;
    2)
        echo ""
        ./start-frontend.sh
        ;;
    3)
        echo ""
        echo "Opening Backend in new terminal..."
        osascript -e 'tell application "Terminal" to do script "cd '"$(pwd)"' && ./start-backend.sh"'

        echo "Opening Frontend in new terminal..."
        osascript -e 'tell application "Terminal" to do script "cd '"$(pwd)"' && ./start-frontend.sh"'

        echo ""
        echo "✅ Backend and Frontend starting in separate terminals"
        echo ""
        echo "📍 URLs:"
        echo "   Frontend: http://localhost:4200"
        echo "   Backend:  http://localhost:8080"
        echo "   Swagger:  http://localhost:8080/swagger-ui.html"
        echo ""
        echo "🔐 Login Credentials:"
        echo "   staff@sonexus.com / password"
        echo "   admin@sonexus.com / password"
        echo "   agent@sonexus.com / password"
        ;;
    4)
        echo ""
        echo "Checking prerequisites..."
        echo ""

        # Check PostgreSQL
        if command -v psql &> /dev/null; then
            echo "✅ PostgreSQL installed"
            if pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
                echo "✅ PostgreSQL is running"
            else
                echo "❌ PostgreSQL is NOT running"
                echo "   Start with: brew services start postgresql@15"
            fi

            if psql -lqt | cut -d \| -f 1 | grep -qw hcp_portal; then
                echo "✅ Database 'hcp_portal' exists"
            else
                echo "❌ Database 'hcp_portal' does NOT exist"
                echo "   Create with: createdb hcp_portal"
            fi
        else
            echo "❌ PostgreSQL not installed"
            echo "   Install with: brew install postgresql@15"
        fi

        echo ""

        # Check Maven
        if command -v mvn &> /dev/null; then
            echo "✅ Maven installed ($(mvn --version | head -n1))"
        else
            echo "❌ Maven not installed"
            echo "   Install with: brew install maven"
        fi

        echo ""

        # Check Node/npm
        if command -v node &> /dev/null; then
            echo "✅ Node.js installed ($(node --version))"
        else
            echo "❌ Node.js not installed"
            echo "   Install with: brew install node"
        fi

        if command -v npm &> /dev/null; then
            echo "✅ npm installed ($(npm --version))"
        else
            echo "❌ npm not installed"
        fi

        echo ""

        # Check Maven settings
        if [ -f ~/.m2/settings.xml ]; then
            echo "✅ Maven settings.xml exists"
        else
            echo "⚠️  Maven settings.xml not found"
            echo "   This may cause SSL certificate issues"
        fi

        echo ""
        ;;
    5)
        echo "Exiting..."
        exit 0
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac
