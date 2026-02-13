# HCP Provider Portal - Quick Start Guide

## Prerequisites

Before starting, make sure you have:
- ✅ PostgreSQL 15 installed and running
- ✅ Database `hcp_portal` created
- ✅ Maven installed
- ✅ Node.js and npm installed

## Quick Start

### Option 1: Use the Startup Menu (Recommended)

```bash
cd /Users/chad.ramsey/ai-projects/cp360-poc
./start.sh
```

This will show you a menu with options to:
1. Start Backend only
2. Start Frontend only
3. Start Both (automatically opens in separate terminals)
4. Check Prerequisites
5. Exit

### Option 2: Start Individually

**Backend:**
```bash
./start-backend.sh
```

**Frontend (in new terminal):**
```bash
./start-frontend.sh
```

## Access the Application

Once both are running:

- **Frontend UI:** http://localhost:4200
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html

## Login Credentials

All users have password: `password`

- **admin@sonexus.com** - Full admin access
- **staff@sonexus.com** - Office staff (most features)
- **agent@sonexus.com** - Support agent

## First Time Setup

The backend will automatically:
1. Create all database tables (via Flyway)
2. Seed users, providers, and programs
3. Start on port 8080

The frontend will automatically:
1. Install dependencies (first time only - takes 2-3 minutes)
2. Compile the Angular app
3. Open browser to http://localhost:4200

## Troubleshooting

### Check Prerequisites
```bash
./start.sh
# Choose option 4
```

### Backend Won't Start
- Check PostgreSQL is running: `pg_isready`
- Check database exists: `psql -l | grep hcp_portal`
- Check Maven settings: `cat ~/.m2/settings.xml`

### Frontend Won't Start
- Delete node_modules and reinstall: `rm -rf frontend/node_modules && cd frontend && npm install`

### Port Already in Use
- Backend (8080): `lsof -ti:8080 | xargs kill -9`
- Frontend (4200): `lsof -ti:4200 | xargs kill -9`

## What You Can Test

### Phase 1-5 Features (All Working):

✅ **Authentication** - Login with JWT tokens
✅ **Provider Association** - Request and approve affiliations
✅ **Patient Management** - Create, view, list patients
✅ **Benefits Investigation** - Run medical/pharmacy coverage checks
✅ **Forms & Resources** - Upload and download forms (needs MinIO for file storage)

### Test Flows:

**Flow A: Provider Association**
1. Login as staff@sonexus.com
2. Dashboard → Request Provider Affiliation
3. Logout → Login as admin@sonexus.com → Approve
4. Logout → Login as staff again (now approved!)

**Flow B: Patient Enrollment**
1. Login as staff@sonexus.com (with approved affiliation)
2. Manage Patients → Create New Patient
3. View patient → Enroll in Program

**Flow C: Benefits Investigation**
1. From patient detail → Benefits tab
2. Run Benefits Investigation wizard
3. View results for medical and pharmacy

## Stop the Application

Press `Ctrl+C` in each terminal where the services are running.

Or kill processes:
```bash
# Kill backend
lsof -ti:8080 | xargs kill -9

# Kill frontend
lsof -ti:4200 | xargs kill -9
```

## Need Help?

Check the logs in the terminal where each service is running for error messages.
