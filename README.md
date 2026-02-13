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

- Docker & Docker Compose
- Java 17 (for local development)
- Node.js 20+ (for local development)
- Maven 3.9+ (for local development)

### Running with Docker Compose

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

### Backend Development

```bash
cd backend
mvn spring-boot:run
```

### Frontend Development

```bash
cd frontend
npm install
npm start
```

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

### Port Conflicts
If you have services running on the default ports, you can modify the ports in `docker-compose.yml`.

### Database Connection Issues
Ensure PostgreSQL is healthy before backend starts:
```bash
docker compose logs postgres
```

### MinIO Bucket Not Created
Check the minio-init service logs:
```bash
docker compose logs minio-init
```

## License

Proprietary - Sonexus Support

## Support

For issues or questions, contact the development team.
