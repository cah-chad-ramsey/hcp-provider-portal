# Quick Start for Local Development

## TL;DR - Start in 2 Commands

**Terminal 1 (Backend):**
```bash
./start-backend.sh
```

**Terminal 2 (Frontend):**
```bash
./start-frontend.sh
```

That's it! Both scripts handle everything automatically.

---

## What the Scripts Do Automatically

### `start-backend.sh`

✅ **Prerequisites Check:**
- Verifies Java 21 is installed
- Checks Maven availability
- Confirms PostgreSQL installation

✅ **PostgreSQL Setup:**
- Starts PostgreSQL if stopped
- Creates `postgres` role if missing
- Creates `hcp_portal` database if missing
- Grants all necessary permissions

✅ **Environment Configuration:**
- Sets `JAVA_HOME` to Java 21 (not Java 25!)
- Sets database connection variables
- Displays configuration summary

✅ **Cleanup:**
- Kills any processes using port 8080

✅ **First-Time Setup:**
- Detects if Maven dependencies need downloading
- Warns about 5-10 minute download time
- Provides VPN tip for corporate environments

✅ **Launch:**
- Starts Spring Boot with hot reload
- Shows access URLs and credentials

### `start-frontend.sh`

✅ **Prerequisites Check:**
- Verifies Node.js is installed
- Checks npm availability

✅ **Cleanup:**
- Kills any processes using port 4200

✅ **Dependency Management:**
- Installs npm packages if `node_modules` missing (first time)
- Verifies Angular CLI availability

✅ **Launch:**
- Starts Angular dev server with hot reload
- Shows access URLs and credentials

---

## Access URLs

After both scripts are running:

**Frontend:**
- 🌐 Application: http://localhost:4200
- 🔄 Hot reload: Enabled (auto-refresh on file save)

**Backend:**
- 🔌 API: http://localhost:8080
- 📖 Swagger UI: http://localhost:8080/swagger-ui
- 🔄 Hot reload: Enabled (auto-restart on Java file save)

---

## Login Credentials

All users have password: `password`

| Username | Role | Permissions |
|----------|------|-------------|
| `admin` | ADMIN | Full system access, user management, audit logs |
| `staff` | OFFICE_STAFF | Patient enrollment, forms, messages |
| `agent` | SUPPORT_AGENT | Support services, patient management |

---

## First-Time Setup

### Initial Run (Once)

**Maven Dependencies (~5-10 minutes):**
- Downloads ~150MB of Spring Boot, PostgreSQL, JWT libraries
- Cached in `~/.m2/repository/`
- Future runs use cache (instant startup)

**npm Dependencies (~3-5 minutes):**
- Downloads ~500MB of Angular, Material, TypeScript packages
- Cached in `frontend/node_modules/`
- Future runs skip this step

**Corporate VPN Issue:**
If Maven fails with SSL certificate errors:
1. Disconnect from VPN temporarily
2. Run `./start-backend.sh`
3. Let Maven download dependencies (5-10 min)
4. Reconnect to VPN
5. Future runs work fine on VPN (dependencies cached)

---

## Stop the Servers

Just press `Ctrl+C` in each terminal window.

**To fully reset:**
```bash
# Reset database
dropdb hcp_portal
# Restart backend script - Flyway recreates tables

# Clear backend cache (optional)
rm -rf ~/.m2/repository

# Clear frontend cache (optional)
cd frontend
rm -rf node_modules package-lock.json
```

---

## Troubleshooting

### "Java 21 is not installed"
```bash
brew install openjdk@21
```

### "PostgreSQL is not installed"
```bash
brew install postgresql@15
```

### "Maven is not installed"
```bash
brew install maven
```

### "Node.js is not installed"
```bash
brew install node
```

### Port Already in Use
The scripts automatically kill conflicting processes. If manual intervention needed:
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Kill process on port 4200
lsof -ti:4200 | xargs kill -9
```

### Compilation Errors (Lombok)
Ensure you're using Java 21, not Java 25:
```bash
java -version  # Should show version 21
```
The start script automatically sets `JAVA_HOME` to Java 21.

### Backend Starts But Frontend API Calls Fail
- Ensure backend finished starting (shows "Started HcpPortalApplication")
- Check backend logs for errors
- Verify http://localhost:8080/swagger-ui loads

### Frontend Builds But Shows Blank Page
- Check browser console for errors (F12)
- Ensure `environment.ts` has correct API URL
- Try clearing browser cache (Cmd+Shift+R)

---

## Development Workflow

### Making Code Changes

**Backend Changes:**
1. Edit Java files in `backend/src/`
2. Save file
3. Spring Boot DevTools auto-restarts (~5 seconds)
4. Refresh browser or re-run API call

**Frontend Changes:**
1. Edit TypeScript/HTML/SCSS files in `frontend/src/`
2. Save file
3. Angular CLI auto-recompiles (~2 seconds)
4. Browser auto-refreshes

**Database Schema Changes:**
1. Create new migration: `backend/src/main/resources/db/migration/V00X__description.sql`
2. Restart backend (Ctrl+C then `./start-backend.sh`)
3. Flyway applies migration automatically

### Viewing Logs

**Backend Logs:**
Displayed directly in Terminal 1 running the backend.

**Frontend Logs:**
Displayed directly in Terminal 2 running the frontend.

**Database Logs:**
```bash
# View PostgreSQL logs
brew services info postgresql@15

# Connect to database
psql -d hcp_portal
```

### Testing Endpoints

**Using Swagger UI:**
1. Go to http://localhost:8080/swagger-ui
2. Click endpoint to expand
3. Click "Try it out"
4. Fill in parameters
5. Click "Execute"

**Using curl:**
```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Use JWT token
curl http://localhost:8080/api/v1/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Pro Tips

💡 **Use separate terminal windows/tabs** - Makes it easy to see both backend and frontend logs simultaneously

💡 **Keep both running** - Hot reload saves massive time during development

💡 **Check logs first** - 90% of issues are visible in terminal logs

💡 **Swagger is your friend** - Test API endpoints before writing frontend code

💡 **Database GUI tools** - Use TablePlus, pgAdmin, or DBeaver to inspect `hcp_portal` database

💡 **Browser DevTools** - F12 → Network tab to see API requests/responses

💡 **Correlation IDs** - Every API response includes `X-Correlation-ID` header for debugging

---

## Next Steps

1. ✅ Run both start scripts
2. ✅ Login at http://localhost:4200
3. ✅ Explore the application
4. ✅ Check out Swagger UI at http://localhost:8080/swagger-ui
5. 📖 Read [TESTING.md](TESTING.md) for testing guide
6. 📖 Read [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for API details
7. 📖 Read [CLOUD_MIGRATION.md](CLOUD_MIGRATION.md) for deployment guide

---

## Questions?

- Check [README.md](README.md) for full documentation
- Check [TESTING.md](TESTING.md) for testing strategies
- Check GitHub issues for known problems
- View logs in terminal for error details
