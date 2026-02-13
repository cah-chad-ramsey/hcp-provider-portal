ROLE 
You are a senior full‑stack architect + lead engineer. Build an MVP “HCP - Provider Portal” that runs COMPLETELY LOCALLY using Docker Compose + local mocks. No cloud accounts or managed services.

In this project folder, you have access to /Users/chad.ramsey/ai-projects/cp360-poc/Provider Portal Features 1.pdf and /Users/chad.ramsey/ai-projects/cp360-poc/cp360.pen.  Evaluate the PDF and designs to create a plan to implement using the following rules.

DO NOT WRITE ANY CODE YET.  When we are done planning I will review the plan and instruct you to switch to implementation.

PRIMARY GOAL (LOCAL MVP)
Deliver a working monorepo with:
- /frontend Angular (latest LTS) + Angular Material + TypeScript + SCSS (NgRx optional; only if it helps)
- /backend Java 17 + Spring Boot 3 + Spring Security + JPA/Hibernate + Bean Validation + OpenAPI
- /infra docker-compose + scripts
Local services: Postgres + MinIO (+ optional MailHog). One command: `docker compose up --build`.

NON-NEGOTIABLE CONSTRAINTS
1) Local-first: everything runs offline locally via containers.
2) Hexagonal architecture (Ports & Adapters) with these ports ONLY:
   - FileStoragePort (MinIO adapter now; cloud adapter placeholder later)
   - BenefitsInvestigationPort (Rule-based adapter now; HTTP API adapter placeholder later)
   - AuthProvider (Local JWT now; OIDC placeholder behind prod profile)
   - NotificationPort (NoOp or MailHog adapter now)
   - EventBusPort (InProcess in-memory now)
3) Spring profiles:
   - local: JWT auth, MinIO, BI rules stub, seed data
   - test: Testcontainers
   - prod: wiring placeholders ONLY (OIDC, cloud storage, real BI)
4) Security:
   - RBAC on every endpoint
   - Provider affiliation must be VERIFIED before any patient/PHI access
   - Audit sensitive actions (form view/download, enrollment submit, consent update, message access)
   - Never log PHI; add correlationId filter + return header

MVP USER ROLES
- OFFICE_STAFF: manage patients/enrollment/BI/messages/forms download
- SUPPORT_AGENT: update statuses + respond to messages
- ADMIN: verify affiliations, manage programs/services/forms, view audits
(PROVIDER role optional; defer unless easy)

FEATURES (MVP SCOPE)
Implement ONLY what is needed to support these flows end-to-end:
A) Login -> request provider association -> ADMIN approves
B) Create patient -> enrollment wizard (draft + submit) with consents + audit
C) Run Benefits Investigation using RULE-BASED STUB (YAML/JSON rules) + store history
D) Admin uploads a form -> staff views/downloads (audited) via MinIO
E) Create secure message thread -> send message + attachment -> read/unread

UI (MINIMUM SCREENS)
- Login
- Provider Association (request + status)
- Dashboard (simple KPI cards; optional “next best action” using local JSON rules)
- Patients List + Patient Details (tabs: Overview, Enrollment, Benefits, Documents, Messages, Support Services minimal)
- Enrollment Wizard (Angular Material stepper)
- Forms & Resources (search/filter + inline view + download)
- Secure Message Center (threads + compose + attachments)
- Admin Console (affiliations approvals, programs/services config, forms upload, audit viewer)

API CONTRACT (IMPLEMENT EXACTLY)
Base: /api/v1 + OpenAPI + Swagger UI.
Auth (local):
- POST /auth/register (optional)
- POST /auth/login (JWT)
- GET /me
Provider Association:
- POST /providers/associate
- GET /providers/affiliations
Admin:
- POST /admin/providers/affiliations/{id}/verify (approve/reject + reason)
Programs & Services:
- GET /programs
- GET /programs/{id}/services
- POST /admin/programs/{id}/services
Patients & Enrollment:
- GET /patients (paged filters)
- POST /patients
- GET /patients/{id}
- POST /patients/{id}/enrollments (draft or submit)
- PATCH /enrollments/{id} (status updates)
- GET /enrollments/{id}/history
Benefits Investigation (RULE STUB NOW):
- POST /patients/{id}/benefits-investigation
- GET /patients/{id}/benefits-investigation/latest
- GET /benefits-investigation/{id}
Support Services:
- POST /patients/{id}/services/{serviceId}/enroll
- GET /patients/{id}/services
- PATCH /patient-services/{id}
Forms & Resources:
- GET /forms (filters)
- POST /admin/forms (multipart + metadata + complianceApproved)
- GET /forms/{id}/view
- GET /forms/{id}/download
- GET /forms/{id}/versions
Secure Messaging:
- GET /messages/threads (paged)
- POST /messages/threads
- GET /messages/threads/{id}
- POST /messages/threads/{id}/messages
- POST /messages/attachments (multipart -> attachmentId)
- GET /messages/attachments/{id}/download
Auditing:
- GET /admin/audit (paged; limited fields; no PHI)

DOMAIN + DB (NORMALIZED)
Use Postgres + Flyway migrations + seed data. Implement entities needed for the endpoints:
User, Role, UserRole,
Provider, ProviderAffiliation,
Program, SupportService,
Patient (minimal; referenceId for UI),
PatientConsent,
Enrollment,
StatusHistory,
BenefitsInvestigation,
PatientServiceEnrollment,
FormResource, DownloadAudit,
SecureMessageThread, SecureMessage, MessageAttachment,
AuditEvent (or equivalent).
Store files ONLY in MinIO; Postgres stores metadata only.

RULE-BASED BI STUB (MUST WORK)
BenefitsInvestigationPort with RuleBased adapter; rules editable via local YAML/JSON.
Inputs: patientId, programId, type(MEDICAL|PHARMACY), payerName, payerPlanId?, memberId?, state?, dob?
Outputs: coverageStatus(ACTIVE|INACTIVE|UNKNOWN), coverageType(COMMERCIAL|MEDICAID|MEDICARE|CASH|UNKNOWN),
priorAuthRequired(YES|NO|UNKNOWN), deductibleApplies(YES|NO|UNKNOWN), specialtyPharmacyRequired(YES|NO|UNKNOWN),
notes, expiresAt(now+config, default 30d), plus resultPayload JSON + UI-friendly summaryFields.
Deterministic rules:
- payerName contains medicare/cms -> MEDICARE; medicaid -> MEDICAID; blue/aetna/cigna/uhc/anthem -> COMMERCIAL
- payerName blank -> coverageStatus UNKNOWN
- Pharmacy: optum/caremark/express scripts -> specialtyPharmacyRequired YES
- Prior auth: planId starts “PA-” OR payerName contains “hmo” -> priorAuth YES else NO

LOCAL DEV EXPERIENCE (MUST DELIVER)
- docker-compose.yml runs: postgres, minio, (optional mailhog), backend, frontend
- Seed users local: admin/admin (ADMIN), staff/staff (OFFICE_STAFF), agent/agent (SUPPORT_AGENT)
- URLs: FE http://localhost:4200, BE http://localhost:8080, Swagger http://localhost:8080/swagger-ui
- Consistent error format: Problem+JSON
- Pagination on list endpoints
- p95 reads <300ms locally (exclude streaming)

TESTING (MUST RUN LOCALLY)
Backend: JUnit + integration tests using Testcontainers for Postgres + MinIO.
Frontend: unit tests + e2e smoke tests (Cypress or Playwright) covering flows A–E.

OUTPUT REQUIREMENTS (FINAL)
Generate the full working codebase + migrations + seed + OpenAPI + README (run/test) + “Swap to Cloud Later” guide:
- MinIO -> GCS/Azure/S3 adapter swap
- JWT -> OIDC via prod profile
- BI stub -> HTTP API adapter swap

IMPORTANT BEHAVIOR
- Do NOT write long explanations. Produce code + minimal README instructions.
- Prefer simple, clean MVP implementations over overengineering.
- No extra features beyond scope unless required to satisfy flows A–E.
 