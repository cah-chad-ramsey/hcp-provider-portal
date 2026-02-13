# HCP Provider Portal MVP

Healthcare Provider Portal for Sonexus Support that enables office staff, support agents, and administrators to manage patient enrollment, benefits investigation, forms, and secure messaging.

## Project Status

### ✅ Completed Phases

**Phase 1: Infrastructure & Auth Foundation**
- Monorepo structure with Docker Compose
- PostgreSQL + Flyway migrations
- MinIO S3-compatible storage
- JWT authentication & RBAC
- Hexagonal architecture

**Phase 2: Provider Association Flow**
- Provider affiliation requests
- Admin approval workflow
- Access control based on affiliation

**Phase 3: Patient Management & Enrollment**
- Patient CRUD operations
- 7-step enrollment wizard
- Draft enrollment support
- Enrollment status tracking
- Support service enrollment

**Phase 4: Benefits Investigation**
- Rule-based BI engine (YAML rules)
- Medical & pharmacy coverage
- 30-day result expiration
- BI history tracking

**Phase 5: Forms & Resources**
- Admin form upload with MinIO
- Form search and filtering
- Download auditing
- Version tracking

**Phase 6: Secure Messaging**
- Threaded conversations
- File attachments via MinIO
- Read/unread tracking
- Program/patient context

**Phase 7: Admin Console & Auditing**
- Centralized admin dashboard
- Program/service management
- Comprehensive audit log viewer
- Advanced filtering

**Phase 8: Dashboard Next Best Action**
- 4 intelligent business rules
- Priority-based recommendations
- One-click navigation
- Smart action cards

**Phase 9: Testing & Polish** 🚧 In Progress
- RFC 7807 Problem Details error handling
- Correlation ID tracking in logs
- OpenAPI/Swagger documentation
- Comprehensive README
- Test infrastructure

## Quick Start

### Prerequisites

**For Local Development (Recommended):**
- Java 21 - `brew install openjdk@21`
- Maven 3.9+ - `brew install maven`
- PostgreSQL 15 - `brew install postgresql@15`
- Node.js 20+ - `brew install node`

**For Docker:**
- Docker & Docker Compose

### Option 1: Local Development (Recommended)

The easiest way to run the portal locally with full hot-reload capabilities:

**Terminal 1 - Backend:**
```bash
./start-backend.sh
```

**Terminal 2 - Frontend:**
```bash
./start-frontend.sh
```

The scripts will automatically:
- ✅ Check all prerequisites (Java 21, Maven, PostgreSQL, Node.js)
- ✅ Start PostgreSQL if not running
- ✅ Create database and postgres role if needed
- ✅ Grant database permissions
- ✅ Kill conflicting processes on ports 8080/4200
- ✅ Install dependencies (first run only)
- ✅ Set JAVA_HOME to Java 21
- ✅ Start the dev servers

**Access the application:**
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui

**Login credentials:**
- Admin: `admin` / `password` (ADMIN role)
- Staff: `staff` / `password` (OFFICE_STAFF role)
- Agent: `agent` / `password` (SUPPORT_AGENT role)

**First-time setup notes:**
- Maven will download ~150MB of dependencies (5-10 minutes)
- npm will install frontend packages (3-5 minutes)
- If on corporate VPN and Maven fails, disconnect temporarily for initial download

### Option 2: Running with Docker Compose

1. **Clone the repository**
   ```bash
   cd /path/to/cp360-poc
   ```

2. **Start all services**
   ```bash
   docker compose up --build
   ```

   This will start:
   - PostgreSQL (port 5432)
   - MinIO (port 9000, console 9001)
   - MailHog (SMTP 1025, Web UI 8025)
   - Backend (port 8080)
   - Frontend (port 4200)

3. **Access the application**
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - MinIO Console: http://localhost:9001 (minioadmin / minioadmin)
   - MailHog: http://localhost:8025

4. **Login with demo credentials**
   - Admin: `admin@sonexus.com` / `password`
   - Staff: `staff@sonexus.com` / `password`
   - Agent: `agent@sonexus.com` / `password`

### Stopping Services

**Local Development:**
- Press `Ctrl+C` in each terminal window

**Docker:**
```bash
docker compose down
```

To remove volumes (reset database):
```bash
docker compose down -v
```

## Architecture

### Hexagonal Architecture (Ports & Adapters)

The backend follows hexagonal architecture to enable easy migration from local services to cloud services:

**Domain Ports** (interfaces):
- `AuthProvider` - Authentication (JWT local → OIDC cloud)
- `FileStoragePort` - File storage (MinIO → S3/GCS)
- `BenefitsInvestigationPort` - BI (Rule-based → HTTP API)
- `NotificationPort` - Notifications (NoOp → Email)
- `EventBusPort` - Events (In-memory → Kafka)

**Current Adapters** (local implementations):
- `JwtAuthAdapter` - Local JWT authentication
- `MinioFileStorageAdapter` - S3-compatible local storage
- `RuleBasedBenefitsAdapter` - YAML-based BI rules
- `NoOpNotificationAdapter` - No-op notifications
- `InMemoryEventBusAdapter` - Simple in-memory events

### Project Structure

```
/frontend          # Angular 18 + Material
/backend           # Java 17 + Spring Boot 3
/infra             # Docker Compose + scripts
docker-compose.yml # Service orchestration
```

## Development

### Running Locally

Use the provided start scripts for the best development experience:

**Backend (Terminal 1):**
```bash
./start-backend.sh
```
- Automatically sets JAVA_HOME to Java 21
- Starts PostgreSQL if needed
- Creates database and roles automatically
- Hot reload enabled via Spring Boot DevTools
- Logs visible in terminal

**Frontend (Terminal 2):**
```bash
./start-frontend.sh
```
- Automatically installs dependencies if needed
- Kills conflicting processes on port 4200
- Hot reload enabled via Angular CLI
- Opens browser automatically

### Manual Development (Advanced)

If you prefer manual control:

**Backend:**
```bash
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm start
```

### Configuration

**Backend Environment Variables:**
- `DB_HOST` - Database host (default: localhost)
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name (default: hcp_portal)
- `DB_USER` - Database user (default: postgres)
- `DB_PASSWORD` - Database password (default: postgres)

**Frontend Environment:**
- `environment.ts` - Development settings
- `environment.prod.ts` - Production settings

## API Documentation

Once the backend is running, visit:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Database Migrations

Migrations are managed by Flyway and run automatically on startup.

Location: `backend/src/main/resources/db/migration/`

Current migrations:
- `V001__initial_schema.sql` - Users, roles, user_roles tables

## Features

### 🎯 Core Functionality
- **Provider Management** - Request and approve provider affiliations
- **Patient Enrollment** - 7-step wizard with draft support
- **Benefits Investigation** - Automated insurance coverage verification
- **Forms Library** - Upload, search, and download compliance forms
- **Secure Messaging** - HIPAA-compliant messaging with attachments
- **Admin Console** - System configuration and audit logs
- **Smart Dashboard** - AI-powered action recommendations

### 🔐 Security Features
- JWT authentication with role-based access control
- Audit logging for all sensitive operations (IDs only, no PHI)
- Correlation ID tracking across requests
- Input validation and sanitization
- Secure file upload with MIME type validation

### 📊 Business Rules
- **Stale Enrollments**: Alert if submitted > 7 days without update
- **Expired Benefits**: Notify when BI results expire (30 days)
- **Missing Services**: Suggest enrollment for patients without services
- **Unread Messages**: Highlight threads with unread messages

## Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### E2E Tests
```bash
cd frontend
npm run e2e
```

## Troubleshooting

### Start Script Issues

**"Java 21 is not installed"**
```bash
brew install openjdk@21
```

**"PostgreSQL is not installed"**
```bash
brew install postgresql@15
```

**"Maven SSL/Certificate errors" (Corporate VPN)**
- Disconnect from VPN temporarily for first-time Maven dependency download
- Dependencies are cached in `~/.m2/repository` and work on VPN after initial download

**"Port 8080/4200 already in use"**
- The start scripts automatically kill conflicting processes
- Or manually: `lsof -ti:8080 | xargs kill -9`

**"Database 'hcp_portal' does not exist"**
- The backend start script creates it automatically
- Or manually: `createdb hcp_portal`

**"Role 'postgres' does not exist"**
- The backend start script creates it automatically
- Or manually: `psql -d postgres -c "CREATE ROLE postgres WITH LOGIN PASSWORD 'postgres' SUPERUSER;"`

**"Lombok getters not found" / Compilation errors**
- Ensure you're using Java 21 (not Java 25)
- Check: `java -version` should show version 21
- The start script automatically sets JAVA_HOME

**Backend won't start without MinIO**
- MinIO is optional for local development
- File upload features won't work but everything else will
- Warning will be logged but app will start

### Docker Issues

**Port Conflicts**
If you have services running on the default ports, you can modify the ports in `docker-compose.yml`.

**Database Connection Issues**
Ensure PostgreSQL is healthy before backend starts:
```bash
docker compose logs postgres
```

**MinIO Bucket Not Created**
Check the minio-init service logs:
```bash
docker compose logs minio-init
```

### Common Fixes

**Clear Maven cache:**
```bash
rm -rf ~/.m2/repository
```

**Clear npm cache:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

**Reset PostgreSQL database:**
```bash
dropdb hcp_portal
createdb hcp_portal
# Restart backend - Flyway will recreate tables
```

**Check logs:**
```bash
# Backend logs
cd backend
mvn spring-boot:run 2>&1 | tee backend.log

# Frontend logs
cd frontend
npm start 2>&1 | tee frontend.log
```

## License

Proprietary - Sonexus Support

## Support

For issues or questions, contact the development team.
