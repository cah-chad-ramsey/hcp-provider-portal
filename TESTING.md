# Testing Guide

Comprehensive testing documentation for the HCP Provider Portal MVP.

## Table of Contents

- [Testing Strategy](#testing-strategy)
- [Backend Testing](#backend-testing)
- [Frontend Testing](#frontend-testing)
- [E2E Testing](#e2e-testing)
- [Manual Testing](#manual-testing)
- [Performance Testing](#performance-testing)

## Testing Strategy

The project follows a multi-layered testing approach:

1. **Unit Tests** - Test individual components/services in isolation
2. **Integration Tests** - Test interaction between components
3. **E2E Tests** - Test complete user workflows
4. **Manual Testing** - UAT scenarios for business validation

### Test Coverage Goals

- Backend: 80% code coverage
- Frontend: 70% code coverage
- Critical paths: 100% coverage

## Backend Testing

### Running Tests

```bash
cd backend

# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Run integration tests
mvn verify -P integration-tests

# Run specific test class
mvn test -Dtest=DashboardServiceTest

# Run tests in specific package
mvn test -Dtest=com.sonexus.portal.service.*
```

### Test Structure

```
backend/src/test/java/
├── unit/
│   ├── service/           # Service layer tests
│   ├── domain/            # Domain logic tests
│   └── adapter/           # Adapter implementation tests
└── integration/
    ├── api/               # Controller integration tests
    ├── repository/        # Repository tests with Testcontainers
    └── security/          # Security and RBAC tests
```

### Unit Test Examples

#### Service Layer Test

```java
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AuthProvider authProvider;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void shouldDetectStaleEnrollments() {
        // Given
        LocalDateTime eightDaysAgo = LocalDateTime.now().minusDays(8);
        EnrollmentEntity staleEnrollment = createStaleEnrollment(eightDaysAgo);
        when(enrollmentRepository.findAll()).thenReturn(List.of(staleEnrollment));

        // When
        List<NextActionResponse> actions = dashboardService.getNextActions();

        // Then
        assertThat(actions).hasSize(1);
        assertThat(actions.get(0).getPriority()).isEqualTo("MEDIUM");
        assertThat(actions.get(0).getTitle()).contains("Follow up");
    }
}
```

#### Rule-Based BI Engine Test

```java
@Test
void shouldIdentifyMedicareCoverage() {
    // Given
    BIRequest request = BIRequest.builder()
        .payerName("Medicare Part D")
        .build();

    // When
    BIResult result = biAdapter.investigate(request);

    // Then
    assertThat(result.getCoverageType()).isEqualTo("MEDICARE");
    assertThat(result.getCoverageStatus()).isEqualTo("COVERED");
}
```

### Integration Test Examples

#### Controller Test with TestContainers

```java
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class PatientControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "OFFICE_STAFF")
    void shouldCreatePatient() throws Exception {
        mockMvc.perform(post("/api/v1/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "firstName": "John",
                        "lastName": "Doe",
                        "dateOfBirth": "1980-01-01"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.referenceId").exists());
    }
}
```

#### RBAC Security Test

```java
@Test
@WithMockUser(roles = "OFFICE_STAFF")
void shouldDenyAccessToAdminEndpoint() throws Exception {
    mockMvc.perform(get("/api/v1/admin/audit"))
            .andExpect(status().isForbidden());
}

@Test
@WithMockUser(roles = "ADMIN")
void shouldAllowAccessToAdminEndpoint() throws Exception {
    mockMvc.perform(get("/api/v1/admin/audit"))
            .andExpect(status().isOk());
}
```

### Test Data Builders

```java
public class TestDataBuilder {

    public static PatientEntity createPatient(String firstName, String lastName) {
        return PatientEntity.builder()
            .referenceId("PAT-" + UUID.randomUUID().toString().substring(0, 8))
            .firstName(firstName)
            .lastName(lastName)
            .dateOfBirth(LocalDate.of(1980, 1, 1))
            .gender("MALE")
            .build();
    }

    public static EnrollmentEntity createEnrollment(PatientEntity patient) {
        return EnrollmentEntity.builder()
            .patient(patient)
            .status("DRAFT")
            .build();
    }
}
```

## Frontend Testing

### Running Tests

```bash
cd frontend

# Run all tests
npm test

# Run tests with coverage
npm run test:coverage

# Run tests in watch mode
npm test -- --watch

# Run specific test file
npm test -- --include='**/dashboard.component.spec.ts'

# Run E2E tests
npm run e2e
```

### Test Structure

```
frontend/src/
├── app/
│   ├── core/
│   │   └── services/*.spec.ts      # Service tests
│   └── features/
│       └── */components/*.spec.ts  # Component tests
└── e2e/
    └── *.e2e.ts                    # E2E tests
```

### Unit Test Examples

#### Service Test

```typescript
describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DashboardService]
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should fetch next actions', () => {
    const mockActions: NextAction[] = [
      {
        id: '1',
        title: 'Follow up on enrollment',
        priority: 'HIGH',
        actionUrl: '/patients/1'
      }
    ];

    service.getNextActions().subscribe(actions => {
      expect(actions.length).toBe(1);
      expect(actions[0].priority).toBe('HIGH');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/dashboard/next-actions`);
    expect(req.request.method).toBe('GET');
    req.flush(mockActions);
  });

  afterEach(() => {
    httpMock.verify();
  });
});
```

#### Component Test

```typescript
describe('NextActionsComponent', () => {
  let component: NextActionsComponent;
  let fixture: ComponentFixture<NextActionsComponent>;
  let dashboardService: jasmine.SpyObj<DashboardService>;

  beforeEach(() => {
    const dashboardSpy = jasmine.createSpyObj('DashboardService', ['getNextActions']);

    TestBed.configureTestingModule({
      imports: [NextActionsComponent, HttpClientTestingModule],
      providers: [
        { provide: DashboardService, useValue: dashboardSpy }
      ]
    });

    dashboardService = TestBed.inject(DashboardService) as jasmine.SpyObj<DashboardService>;
    fixture = TestBed.createComponent(NextActionsComponent);
    component = fixture.componentInstance;
  });

  it('should load actions on init', () => {
    const mockActions: NextAction[] = [
      { id: '1', title: 'Test Action', priority: 'HIGH', actionUrl: '/test' }
    ];
    dashboardService.getNextActions.and.returnValue(of(mockActions));

    fixture.detectChanges();

    expect(component.actions.length).toBe(1);
    expect(component.loading).toBeFalse();
  });

  it('should navigate on action click', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl');

    const action: NextAction = {
      id: '1',
      title: 'Test',
      priority: 'HIGH',
      actionUrl: '/patients/1'
    };

    component.handleAction(action);

    expect(router.navigateByUrl).toHaveBeenCalledWith('/patients/1');
  });
});
```

## E2E Testing

### Playwright Configuration

```typescript
// playwright.config.ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  use: {
    baseURL: 'http://localhost:4200',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  webServer: {
    command: 'npm start',
    port: 4200,
    reuseExistingServer: true,
  },
});
```

### E2E Test Examples

#### Flow A: Provider Association

```typescript
test('should complete provider association flow', async ({ page }) => {
  // Login as office staff
  await page.goto('/auth/login');
  await page.fill('[formControlName="email"]', 'staff@sonexus.com');
  await page.fill('[formControlName="password"]', 'password');
  await page.click('button[type="submit"]');

  // Navigate to provider association
  await page.click('text=Provider Association');

  // Fill association form
  await page.fill('[formControlName="npi"]', '1234567890');
  await page.click('button:has-text("Request Affiliation")');

  // Verify success message
  await expect(page.locator('.success-message')).toBeVisible();

  // Login as admin
  await page.click('text=Logout');
  await page.fill('[formControlName="email"]', 'admin@sonexus.com');
  await page.fill('[formControlName="password"]', 'password');
  await page.click('button[type="submit"]');

  // Approve affiliation
  await page.goto('/admin/affiliations');
  await page.click('button:has-text("Approve")');

  // Verify approval
  await expect(page.locator('text=APPROVED')).toBeVisible();
});
```

#### Flow B: Patient Enrollment

```typescript
test('should create patient and submit enrollment', async ({ page }) => {
  // Login
  await page.goto('/auth/login');
  await page.fill('[formControlName="email"]', 'staff@sonexus.com');
  await page.fill('[formControlName="password"]', 'password');
  await page.click('button[type="submit"]');

  // Create patient
  await page.goto('/patients');
  await page.click('text=Create Patient');

  await page.fill('[formControlName="firstName"]', 'John');
  await page.fill('[formControlName="lastName"]', 'Doe');
  await page.fill('[formControlName="dateOfBirth"]', '01/01/1980');
  await page.click('button:has-text("Create")');

  // Start enrollment wizard
  await page.click('text=Enroll Patient');

  // Step 1: Select Product
  await page.click('mat-card:has-text("Sonextra Assistance")');
  await page.click('button:has-text("Next")');

  // Step 2: Patient Information (pre-filled)
  await page.click('button:has-text("Next")');

  // Step 3-7: Complete remaining steps
  // ... (fill required fields)

  // Submit enrollment
  await page.click('button:has-text("Submit Enrollment")');

  // Verify success
  await expect(page.locator('text=Enrollment submitted')).toBeVisible();
});
```

## Manual Testing

### UAT Test Scenarios

#### Scenario 1: Office Staff Enrollment Flow
**Prerequisites**: Approved provider affiliation

1. Login as staff@sonexus.com
2. Navigate to Patients
3. Click "Create Patient"
4. Fill patient information
5. Submit patient
6. Click "Enroll Patient"
7. Complete 7-step wizard:
   - Select Sonextra Assistance program
   - Verify patient information
   - Enter contact details
   - Select prescriber
   - Enter diagnosis code
   - Patient authorization checkbox
   - HIPAA consent checkbox
8. Click "Submit Enrollment"
9. Verify enrollment appears in patient detail

**Expected Result**: Enrollment submitted with status "SUBMITTED"

#### Scenario 2: Benefits Investigation
**Prerequisites**: Patient exists

1. Navigate to patient detail
2. Click "Run Benefits Investigation"
3. Enter insurance information:
   - Payer: "Blue Cross Blue Shield"
   - Member ID: "12345"
   - Plan ID: "BCBS-PLAN-001"
4. Click "Run Investigation"
5. Review Medical Results tab
6. Review Pharmacy Results tab
7. Check patient authorization
8. Click "Complete"

**Expected Result**: BI results saved, expires in 30 days

#### Scenario 3: Secure Messaging
**Prerequisites**: None

1. Navigate to Messages
2. Click "New Message"
3. Enter subject: "Test Message"
4. Select program (optional)
5. Select patient (optional)
6. Click "Create Thread"
7. Type message content
8. Click "Attach Files"
9. Upload test file
10. Click "Send Reply"

**Expected Result**: Message sent with attachment

### Test Data

#### User Accounts
- Admin: admin@sonexus.com / password
- Staff: staff@sonexus.com / password
- Agent: agent@sonexus.com / password

#### Test Patients
- John Doe (DOB: 01/01/1980)
- Jane Smith (DOB: 05/15/1985)
- Mary Johnson (DOB: 12/20/1975)

#### Insurance Test Data
- **Medicare**: Payer="Medicare Part D", Expected=MEDICARE coverage
- **Medicaid**: Payer="Medicaid", Expected=MEDICAID coverage
- **Commercial**: Payer="Blue Cross", Expected=COMMERCIAL coverage
- **Specialty Pharmacy**: Payer="Optum Rx", Expected=specialty_pharmacy_required=YES

## Performance Testing

### Performance Goals

- API Response Time: p95 < 300ms (local)
- Page Load Time: < 2 seconds
- Form Submission: < 1 second
- File Upload (5MB): < 3 seconds

### Testing with Apache Benchmark

```bash
# Test login endpoint
ab -n 1000 -c 10 -p login.json -T application/json \
   http://localhost:8080/api/v1/auth/login

# Test patient list
ab -n 1000 -c 10 -H "Authorization: Bearer $TOKEN" \
   http://localhost:8080/api/v1/patients

# Test dashboard next actions
ab -n 1000 -c 10 -H "Authorization: Bearer $TOKEN" \
   http://localhost:8080/api/v1/dashboard/next-actions
```

### Database Query Performance

```sql
-- Check slow queries (>100ms)
SELECT query, mean_exec_time, calls
FROM pg_stat_statements
WHERE mean_exec_time > 100
ORDER BY mean_exec_time DESC
LIMIT 10;

-- Verify indexes are used
EXPLAIN ANALYZE
SELECT * FROM enrollments
WHERE status = 'SUBMITTED'
  AND submitted_at < NOW() - INTERVAL '7 days';
```

## Continuous Integration

### GitHub Actions Workflow

```yaml
name: CI

on: [push, pull_request]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: cd backend && mvn test

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: cd frontend && npm ci && npm test

  e2e-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: docker compose up -d
      - run: cd frontend && npm run e2e
```

## Test Reporting

### Coverage Reports

```bash
# Backend coverage
cd backend
mvn test jacoco:report
open target/site/jacoco/index.html

# Frontend coverage
cd frontend
npm run test:coverage
open coverage/index.html
```

### Test Results

- **JUnit XML**: `backend/target/surefire-reports/`
- **Jest JSON**: `frontend/coverage/coverage-summary.json`
- **Playwright HTML**: `frontend/playwright-report/`

## Best Practices

1. **Write Tests First** - TDD approach for new features
2. **Keep Tests Fast** - Mock external dependencies
3. **Test Behavior, Not Implementation** - Focus on outcomes
4. **Use Descriptive Names** - `shouldRejectEnrollmentWhenPatientHasNoAffiliation`
5. **Arrange-Act-Assert** - Clear test structure
6. **One Assertion Per Test** - Easier to debug failures
7. **Clean Up After Tests** - Reset state, delete test data
8. **Use Test Data Builders** - Reusable test fixtures

## Troubleshooting Tests

### Backend Tests Fail
```bash
# Clean and rebuild
mvn clean install -DskipTests
mvn test

# Check Testcontainers Docker
docker ps | grep testcontainers
```

### Frontend Tests Timeout
```bash
# Increase Jasmine timeout
// spec file
jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
```

### E2E Tests Flaky
```bash
# Add explicit waits
await page.waitForSelector('text=Success', { timeout: 5000 });

# Disable animations
await page.emulateMedia({ reducedMotion: 'reduce' });
```

---

**For questions or issues, contact the development team.**
