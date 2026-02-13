# API Documentation

Complete API reference for the HCP Provider Portal.

> **Interactive Documentation**: http://localhost:8080/swagger-ui.html

## Authentication

All endpoints (except `/api/v1/auth/*`) require JWT authentication.

### Obtain Token

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "staff@sonexus.com",
  "password": "password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400
}
```

### Using Token

```http
GET /api/v1/patients
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Endpoints by Module

### Authentication (`/api/v1/auth`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| POST | `/login` | No | - | Login and get JWT token |
| POST | `/register` | No | - | Register new user |
| GET | `/me` | Yes | All | Get current user info |

### Patients (`/api/v1/patients`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| POST | `/` | Yes | OFFICE_STAFF | Create patient |
| GET | `/` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Search patients (paginated) |
| GET | `/{id}` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Get patient by ID |

**Create Patient:**
```http
POST /api/v1/patients
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1980-01-01",
  "gender": "MALE",
  "phone": "555-0123",
  "email": "john.doe@example.com",
  "addressLine1": "123 Main St",
  "city": "Springfield",
  "state": "IL",
  "zipCode": "62701"
}
```

**Response:**
```json
{
  "id": 1,
  "referenceId": "PAT-ABC123",
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1980-01-01",
  "gender": "MALE",
  "phone": "555-0123",
  "email": "john.doe@example.com",
  "createdAt": "2026-02-13T10:30:00Z"
}
```

### Enrollments (`/api/v1`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| POST | `/patients/{id}/enrollments` | Yes | OFFICE_STAFF | Create enrollment |
| GET | `/enrollments/{id}` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Get enrollment |
| PATCH | `/enrollments/{id}` | Yes | SUPPORT_AGENT, ADMIN | Update enrollment status |
| GET | `/enrollments/{id}/history` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Get status history |

**Create Enrollment:**
```http
POST /api/v1/patients/1/enrollments
Authorization: Bearer <token>
Content-Type: application/json

{
  "programId": 1,
  "prescriberId": 123,
  "diagnosisCode": "E11.9",
  "status": "DRAFT"
}
```

**Update Enrollment Status:**
```http
PATCH /api/v1/enrollments/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "APPROVED",
  "reason": "All requirements met"
}
```

### Benefits Investigation (`/api/v1/patients/{id}/benefits-investigation`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| POST | `/` | Yes | OFFICE_STAFF, SUPPORT_AGENT | Run BI |
| GET | `/latest` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Get latest BI |
| GET | `/{biId}` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Get specific BI |

**Run Benefits Investigation:**
```http
POST /api/v1/patients/1/benefits-investigation
Authorization: Bearer <token>
Content-Type: application/json

{
  "investigationType": "MEDICAL",
  "payerName": "Blue Cross Blue Shield",
  "payerPlanId": "BCBS-001",
  "memberId": "12345678",
  "patientState": "IL",
  "medication": "Example Drug"
}
```

**Response:**
```json
{
  "id": 1,
  "patientId": 1,
  "investigationType": "MEDICAL",
  "coverageStatus": "COVERED",
  "coverageType": "COMMERCIAL",
  "priorAuthRequired": false,
  "deductibleApplies": true,
  "specialtyPharmacyRequired": false,
  "expiresAt": "2026-03-15T10:30:00Z",
  "createdAt": "2026-02-13T10:30:00Z"
}
```

### Forms & Resources (`/api/v1/forms`, `/api/v1/admin/forms`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| GET | `/forms` | Yes | All | Search forms (paginated) |
| GET | `/forms/{id}` | Yes | All | Get form metadata |
| GET | `/forms/{id}/download` | Yes | All | Download form (audited) |
| POST | `/admin/forms` | Yes | ADMIN | Upload form |

**Upload Form (Admin):**
```http
POST /api/v1/admin/forms
Authorization: Bearer <token>
Content-Type: multipart/form-data

title: Patient Consent Form
description: Standard consent form for enrollment
programId: 1
category: CONSENT
file: <binary-data>
```

**Search Forms:**
```http
GET /api/v1/forms?search=consent&page=0&size=20
Authorization: Bearer <token>
```

### Secure Messaging (`/api/v1/messages`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| GET | `/threads` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | List threads |
| POST | `/threads` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Create thread |
| GET | `/threads/{id}` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Get thread with messages |
| POST | `/threads/{id}/messages` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Send message |
| POST | `/attachments` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Upload attachment |
| GET | `/attachments/{id}/download` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Download attachment |

**Create Thread:**
```http
POST /api/v1/messages/threads
Authorization: Bearer <token>
Content-Type: application/json

{
  "subject": "Patient Enrollment Question",
  "programId": 1,
  "patientId": 1
}
```

**Send Message:**
```http
POST /api/v1/messages/threads/1/messages
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "What is the status of this enrollment?",
  "attachmentIds": [1, 2]
}
```

### Dashboard (`/api/v1/dashboard`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| GET | `/next-actions` | Yes | OFFICE_STAFF, SUPPORT_AGENT, ADMIN | Get recommended actions |

**Get Next Actions:**
```http
GET /api/v1/dashboard/next-actions
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "id": "abc-123",
    "title": "Follow up on enrollment",
    "description": "Enrollment for John Doe has been pending for 15 days",
    "actionType": "ENROLLMENT",
    "priority": "HIGH",
    "resourceId": 1,
    "resourceName": "John Doe",
    "actionUrl": "/patients/1",
    "icon": "schedule",
    "daysOverdue": 15
  }
]
```

### Admin - Affiliations (`/api/v1/admin/providers/affiliations`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| GET | `/` | Yes | ADMIN | Get pending affiliations |
| POST | `/{id}/verify` | Yes | ADMIN | Approve/reject affiliation |

**Verify Affiliation:**
```http
POST /api/v1/admin/providers/affiliations/1/verify
Authorization: Bearer <token>
Content-Type: application/json

{
  "approved": true,
  "reason": "Valid NPI and credentials verified"
}
```

### Admin - Programs (`/api/v1/admin/programs`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| POST | `/{id}/services` | Yes | ADMIN | Add service to program |

**Add Service to Program:**
```http
POST /api/v1/admin/programs/1/services
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Copay Assistance",
  "description": "Financial assistance with copayments",
  "serviceType": "COPAY_ASSISTANCE"
}
```

### Admin - Audit Logs (`/api/v1/admin/audit`)

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| GET | `/` | Yes | ADMIN | Get audit logs with filtering |

**Get Audit Logs:**
```http
GET /api/v1/admin/audit?eventType=PATIENT_CREATED&startDate=2026-02-01T00:00:00Z&page=0&size=50
Authorization: Bearer <token>
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "eventType": "PATIENT_CREATED",
      "userId": 2,
      "userName": "Jane Smith",
      "userEmail": "staff@sonexus.com",
      "resourceType": "PATIENT",
      "resourceId": 1,
      "action": "CREATE",
      "correlationId": "abc-123-def",
      "ipAddress": "192.168.1.1",
      "createdAt": "2026-02-13T10:30:00Z"
    }
  ],
  "totalElements": 150,
  "totalPages": 3,
  "number": 0,
  "size": 50
}
```

## Error Responses

All errors follow RFC 7807 Problem Details format.

### 400 Bad Request
```json
{
  "type": "https://api.sonexus.com/errors/validation",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed",
  "timestamp": "2026-02-13T10:30:00Z",
  "errors": {
    "firstName": "First name is required",
    "dateOfBirth": "Date of birth must be in the past"
  }
}
```

### 401 Unauthorized
```json
{
  "type": "https://api.sonexus.com/errors/unauthorized",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "type": "https://api.sonexus.com/errors/access-denied",
  "title": "Access Denied",
  "status": 403,
  "detail": "Access denied",
  "timestamp": "2026-02-13T10:30:00Z"
}
```

### 404 Not Found
```json
{
  "type": "https://api.sonexus.com/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Patient with id 999 not found",
  "timestamp": "2026-02-13T10:30:00Z"
}
```

### 500 Internal Server Error
```json
{
  "type": "https://api.sonexus.com/errors/internal",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "An unexpected error occurred. Please contact support.",
  "timestamp": "2026-02-13T10:30:00Z"
}
```

## Headers

### Request Headers

- `Authorization: Bearer <token>` - Required for authenticated endpoints
- `Content-Type: application/json` - For JSON payloads
- `X-Correlation-ID: <uuid>` - Optional, for request tracing

### Response Headers

- `X-Correlation-ID: <uuid>` - Always present, for request tracing
- `Content-Type: application/json` - For JSON responses

## Pagination

All list endpoints support pagination:

**Query Parameters:**
- `page` - Page number (0-indexed), default: 0
- `size` - Page size, default: 20
- `sort` - Sort field and direction, e.g., `createdAt,desc`

**Response:**
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

## Rate Limiting

- **Limit**: 1000 requests per hour per user
- **Header**: `X-RateLimit-Remaining: 950`
- **Status**: 429 Too Many Requests when exceeded

## CORS

Allowed origins:
- `http://localhost:4200` (development)
- `https://portal.sonexus.com` (production)

## Webhook Events (Future)

Coming soon: Webhook support for:
- Patient enrollment status changes
- Benefits investigation results
- Message notifications

---

**For detailed interactive documentation, visit: http://localhost:8080/swagger-ui.html**
