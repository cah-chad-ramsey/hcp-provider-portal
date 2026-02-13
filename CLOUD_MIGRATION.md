# Cloud Migration Guide

This guide explains how to migrate the HCP Provider Portal from local development services (MinIO, JWT, rule-based BI) to production cloud services (S3/GCS, OIDC, real BI APIs).

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Migration Strategy](#migration-strategy)
- [File Storage Migration](#file-storage-migration)
- [Authentication Migration](#authentication-migration)
- [Benefits Investigation Migration](#benefits-investigation-migration)
- [Database Migration](#database-migration)
- [Notification Services](#notification-services)
- [Event Bus Migration](#event-bus-migration)
- [Deployment Checklist](#deployment-checklist)

## Architecture Overview

The application uses **Hexagonal Architecture (Ports & Adapters)** to enable seamless migration from local to cloud services without changing domain logic.

### Current Architecture (Local)

```
┌─────────────────────────────────────────┐
│           Domain Layer                   │
│  (Business Logic - No Dependencies)     │
└─────────────────────────────────────────┘
              ↓ (Ports)
┌─────────────────────────────────────────┐
│         Infrastructure Layer             │
│  ┌─────────────────────────────────┐   │
│  │ Local Adapters                  │   │
│  │ - MinioFileStorageAdapter       │   │
│  │ - JwtAuthAdapter                │   │
│  │ - RuleBasedBenefitsAdapter      │   │
│  │ - NoOpNotificationAdapter       │   │
│  │ - InMemoryEventBusAdapter       │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

### Target Architecture (Cloud)

```
┌─────────────────────────────────────────┐
│           Domain Layer                   │
│  (Same - No Changes Required)           │
└─────────────────────────────────────────┘
              ↓ (Same Ports)
┌─────────────────────────────────────────┐
│         Infrastructure Layer             │
│  ┌─────────────────────────────────┐   │
│  │ Cloud Adapters                  │   │
│  │ - S3FileStorageAdapter          │   │
│  │ - OidcAuthAdapter               │   │
│  │ - HttpBenefitsApiAdapter        │   │
│  │ - EmailNotificationAdapter      │   │
│  │ - KafkaEventBusAdapter          │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

**Key Benefit**: Domain logic remains unchanged. Only swap adapter implementations.

## Migration Strategy

### Phase 1: Dual Operation (Recommended)
1. Deploy both local and cloud adapters
2. Use Spring Profiles to switch between them
3. Test cloud adapters in staging
4. Gradually migrate production traffic

### Phase 2: Full Cloud Migration
1. Remove local adapters
2. Update configuration
3. Deploy to production
4. Monitor and optimize

## File Storage Migration

### Current: MinIO (S3-Compatible Local Storage)

**Adapter**: `MinioFileStorageAdapter`
```java
@Profile("local")
@Component
public class MinioFileStorageAdapter implements FileStoragePort {
    private final MinioClient minioClient;
    // Implementation for local MinIO
}
```

### Target: AWS S3, Google Cloud Storage, or Azure Blob

#### Option 1: AWS S3

**1. Add AWS SDK Dependency**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.0</version>
</dependency>
```

**2. Create S3FileStorageAdapter**
```java
package com.sonexus.portal.infrastructure.adapters;

import com.sonexus.portal.domain.ports.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Profile("prod")
@Component
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final String bucketName = "hcp-portal-files-prod";

    @Override
    public void storeFile(String key, byte[] content, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(content));
    }

    @Override
    public byte[] getFile(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    @Override
    public void deleteFile(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(request);
    }

    @Override
    public boolean fileExists(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
```

**3. Configure S3 Client**
```java
package com.sonexus.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile("prod")
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
```

**4. Environment Configuration**
```yaml
# application-prod.yml
spring:
  profiles:
    active: prod

aws:
  s3:
    bucket-name: hcp-portal-files-prod
    region: us-east-1
```

**5. IAM Policy (AWS)**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::hcp-portal-files-prod/*",
        "arn:aws:s3:::hcp-portal-files-prod"
      ]
    }
  ]
}
```

#### Option 2: Google Cloud Storage

**1. Add GCS Dependency**
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-storage</artifactId>
    <version>2.30.0</version>
</dependency>
```

**2. Create GcsFileStorageAdapter**
```java
@Profile("prod")
@Component
@RequiredArgsConstructor
public class GcsFileStorageAdapter implements FileStoragePort {

    private final Storage storage;
    private final String bucketName = "hcp-portal-files-prod";

    @Override
    public void storeFile(String key, byte[] content, String contentType) {
        BlobId blobId = BlobId.of(bucketName, key);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        storage.create(blobInfo, content);
    }

    @Override
    public byte[] getFile(String key) {
        BlobId blobId = BlobId.of(bucketName, key);
        return storage.readAllBytes(blobId);
    }

    // ... other methods
}
```

**3. Configure GCS**
```java
@Configuration
@Profile("prod")
public class GcsConfig {

    @Bean
    public Storage storage() {
        return StorageOptions.getDefaultInstance().getService();
    }
}
```

### Migration Steps

1. **Create S3 Bucket**
   ```bash
   aws s3 mb s3://hcp-portal-files-prod
   aws s3api put-bucket-versioning --bucket hcp-portal-files-prod \
     --versioning-configuration Status=Enabled
   ```

2. **Migrate Existing Files** (if any)
   ```bash
   # Copy from MinIO to S3
   mc alias set local http://localhost:9000 minioadmin minioadmin
   mc alias set prod s3://hcp-portal-files-prod
   mc cp --recursive local/hcp-portal-files/ prod/
   ```

3. **Update Spring Profile**
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   ```

4. **Deploy and Verify**
   - Test file upload
   - Test file download
   - Verify audit logs
   - Check S3 bucket contents

## Authentication Migration

### Current: JWT (Local Tokens)

**Adapter**: `JwtAuthAdapter`
```java
@Profile("local")
@Component
public class JwtAuthAdapter implements AuthProvider {
    // Generate and validate JWT tokens locally
}
```

### Target: OIDC (Okta, Auth0, AWS Cognito, Azure AD)

#### Option 1: Okta OIDC

**1. Add Spring Security OAuth2 Dependency**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**2. Create OidcAuthAdapter**
```java
package com.sonexus.portal.infrastructure.adapters;

import com.sonexus.portal.domain.ports.AuthProvider;
import com.sonexus.portal.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Profile("prod")
@Component
@RequiredArgsConstructor
public class OidcAuthAdapter implements AuthProvider {

    private final UserRepository userRepository;

    @Override
    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String email = jwt.getClaim("email");
            return userRepository.findByEmail(email)
                    .map(this::mapToUser);
        }

        return Optional.empty();
    }

    @Override
    public boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.getRoles().contains(role))
                .orElse(false);
    }

    private User mapToUser(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .roles(entity.getRoles().stream()
                        .map(RoleEntity::getName)
                        .toList())
                .build();
    }
}
```

**3. Configure Spring Security for OIDC**
```java
package com.sonexus.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("prod")
public class OidcSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll()
            );

        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter =
            new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
```

**4. Environment Configuration**
```yaml
# application-prod.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://dev-12345.okta.com/oauth2/default
          jwk-set-uri: https://dev-12345.okta.com/oauth2/default/v1/keys
```

**5. Okta Application Setup**
1. Create new application in Okta
2. Set Application Type: "Single-Page Application"
3. Configure Login redirect URIs: `http://localhost:4200/callback`
4. Add custom claim "roles" with user roles
5. Note Client ID and Issuer URI

**6. Frontend Configuration**
```typescript
// environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://api.sonexus.com/api/v1',
  oidc: {
    issuer: 'https://dev-12345.okta.com/oauth2/default',
    clientId: 'your-client-id',
    redirectUri: window.location.origin + '/callback',
    scopes: ['openid', 'profile', 'email']
  }
};
```

#### Option 2: AWS Cognito

**1. Configure Cognito**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ABCDEFGH
```

**2. Cognito User Pool Setup**
```bash
# Create user pool
aws cognito-idp create-user-pool \
  --pool-name hcp-portal-users \
  --policies "PasswordPolicy={MinimumLength=8,RequireUppercase=true}" \
  --auto-verified-attributes email

# Create app client
aws cognito-idp create-user-pool-client \
  --user-pool-id us-east-1_ABCDEFGH \
  --client-name hcp-portal-app \
  --generate-secret
```

### Migration Steps

1. **Set up OIDC Provider** (Okta/Cognito/Auth0)
2. **Create Application** in OIDC provider
3. **Configure Custom Claims** (add "roles")
4. **Migrate User Accounts** to OIDC provider
5. **Deploy Backend** with prod profile
6. **Update Frontend** with OIDC configuration
7. **Test Authentication Flow**
8. **Decommission JWT** adapter

## Benefits Investigation Migration

### Current: Rule-Based (YAML Rules)

**Adapter**: `RuleBasedBenefitsAdapter`
```java
@Profile("local")
@Component
public class RuleBasedBenefitsAdapter implements BenefitsInvestigationPort {
    // Load rules from YAML file
    // Apply deterministic rules
}
```

### Target: Real BI API (CoverMyMeds, Zelis, Change Healthcare)

**1. Create HttpBenefitsApiAdapter**
```java
package com.sonexus.portal.infrastructure.adapters;

import com.sonexus.portal.domain.ports.BenefitsInvestigationPort;
import com.sonexus.portal.domain.model.BenefitsInvestigationRequest;
import com.sonexus.portal.domain.model.BenefitsInvestigationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Profile("prod")
@Component
@RequiredArgsConstructor
public class HttpBenefitsApiAdapter implements BenefitsInvestigationPort {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.coverymeds.com/v1/benefits";
    private final String apiKey = System.getenv("BI_API_KEY");

    @Override
    public BenefitsInvestigationResult investigate(BenefitsInvestigationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        BenefitsApiRequest apiRequest = mapToApiRequest(request);
        HttpEntity<BenefitsApiRequest> httpRequest = new HttpEntity<>(apiRequest, headers);

        BenefitsApiResponse response = restTemplate.postForObject(
                apiUrl + "/investigate",
                httpRequest,
                BenefitsApiResponse.class
        );

        return mapToResult(response);
    }

    private BenefitsApiRequest mapToApiRequest(BenefitsInvestigationRequest request) {
        return BenefitsApiRequest.builder()
                .payerId(request.getPayerPlanId())
                .memberId(request.getMemberId())
                .patientDob(request.getPatientDob())
                .medication(request.getMedication())
                .build();
    }

    private BenefitsInvestigationResult mapToResult(BenefitsApiResponse response) {
        return BenefitsInvestigationResult.builder()
                .coverageStatus(response.getCoverageStatus())
                .coverageType(response.getCoverageType())
                .priorAuthRequired(response.isPriorAuthRequired())
                .specialtyPharmacyRequired(response.isSpecialtyPharmacyRequired())
                .deductibleApplies(response.isDeductibleApplies())
                .build();
    }
}
```

**2. Configure RestTemplate with Retry Logic**
```java
@Configuration
@Profile("prod")
public class BenefitsApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Add interceptor for retry logic
        restTemplate.setInterceptors(Collections.singletonList(
            new RetryInterceptor()
        ));

        // Add error handler
        restTemplate.setErrorHandler(new BenefitsApiErrorHandler());

        return restTemplate;
    }
}
```

**3. Environment Configuration**
```yaml
# application-prod.yml
benefits:
  api:
    url: https://api.coverymeds.com/v1
    timeout: 30s
    retry:
      max-attempts: 3
      backoff: 2s
```

**4. Secure API Credentials**
```bash
# Store in AWS Secrets Manager
aws secretsmanager create-secret \
  --name hcp-portal/bi-api-key \
  --secret-string "your-api-key-here"

# Retrieve in application
export BI_API_KEY=$(aws secretsmanager get-secret-value \
  --secret-id hcp-portal/bi-api-key \
  --query SecretString --output text)
```

### Migration Steps

1. **Obtain BI API Credentials** from provider
2. **Test API** in staging environment
3. **Map API Responses** to domain model
4. **Implement Error Handling** for API failures
5. **Add Circuit Breaker** for resilience
6. **Deploy with Prod Profile**
7. **Monitor API Usage** and costs
8. **Keep Rule-Based as Fallback** (optional)

## Database Migration

### Development → Production Database

**1. Export Schema**
```bash
# From local PostgreSQL
pg_dump -h localhost -U postgres -d hcp_portal \
  --schema-only > schema.sql
```

**2. AWS RDS PostgreSQL Setup**
```bash
# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier hcp-portal-prod \
  --db-instance-class db.t3.medium \
  --engine postgres \
  --engine-version 15.5 \
  --master-username admin \
  --master-user-password <secure-password> \
  --allocated-storage 100 \
  --backup-retention-period 7 \
  --multi-az
```

**3. Configure Application**
```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://hcp-portal-prod.xxxx.us-east-1.rds.amazonaws.com:5432/hcp_portal
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

**4. Enable SSL**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://...?ssl=true&sslmode=require
```

**5. Migration Checklist**
- [ ] Create RDS instance with Multi-AZ
- [ ] Configure security groups (restrict to application subnet)
- [ ] Enable automated backups (7-30 days retention)
- [ ] Run Flyway migrations
- [ ] Test database connectivity
- [ ] Set up CloudWatch alarms

## Notification Services

### Current: NoOp (No Notifications)

**Adapter**: `NoOpNotificationAdapter`

### Target: Email Notifications (AWS SES, SendGrid)

**1. Create EmailNotificationAdapter**
```java
@Profile("prod")
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements NotificationPort {

    private final SesClient sesClient;
    private final String fromEmail = "noreply@sonexus.com";

    @Override
    public void sendNotification(String to, String subject, String body) {
        SendEmailRequest request = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(to).build())
                .message(Message.builder()
                        .subject(Content.builder().data(subject).build())
                        .body(Body.builder()
                                .html(Content.builder().data(body).build())
                                .build())
                        .build())
                .source(fromEmail)
                .build();

        sesClient.sendEmail(request);
    }
}
```

**2. Configure AWS SES**
```bash
# Verify domain
aws ses verify-domain-identity --domain sonexus.com

# Create configuration set
aws ses create-configuration-set --configuration-set-name hcp-portal
```

## Event Bus Migration

### Current: In-Memory (Single Instance)

**Adapter**: `InMemoryEventBusAdapter`

### Target: Kafka, AWS SNS/SQS, Azure Service Bus

**1. Create KafkaEventBusAdapter**
```java
@Profile("prod")
@Component
@RequiredArgsConstructor
public class KafkaEventBusAdapter implements EventBusPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic = "hcp-portal-events";

    @Override
    public void publish(String eventType, Object event) {
        String payload = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(topic, eventType, payload);
    }
}
```

## Deployment Checklist

### Pre-Deployment

- [ ] Create S3/GCS bucket for file storage
- [ ] Set up OIDC provider (Okta/Cognito)
- [ ] Configure RDS PostgreSQL database
- [ ] Obtain BI API credentials
- [ ] Set up email service (SES/SendGrid)
- [ ] Create Kubernetes cluster or ECS service
- [ ] Configure load balancer and SSL certificates
- [ ] Set up monitoring (CloudWatch/Datadog)
- [ ] Configure logging aggregation
- [ ] Create CI/CD pipeline

### Configuration

- [ ] Update `application-prod.yml` with production values
- [ ] Set environment variables for secrets
- [ ] Configure Spring Profile: `SPRING_PROFILES_ACTIVE=prod`
- [ ] Update CORS origins for production domain
- [ ] Configure rate limiting
- [ ] Set up API keys rotation

### Security

- [ ] Change all default passwords
- [ ] Rotate JWT secret (if keeping JWT)
- [ ] Configure security groups/firewall rules
- [ ] Enable HTTPS everywhere
- [ ] Set up WAF rules
- [ ] Configure DDoS protection
- [ ] Enable database encryption at rest
- [ ] Set up secrets management (AWS Secrets Manager)

### Testing

- [ ] Run integration tests against prod adapters
- [ ] Performance test with production-like data
- [ ] Security scan (OWASP ZAP, SonarQube)
- [ ] Load testing (JMeter, Gatling)
- [ ] Failover testing
- [ ] Backup and restore testing

### Monitoring

- [ ] Set up application metrics (Prometheus/CloudWatch)
- [ ] Configure alerting (PagerDuty/OpsGenie)
- [ ] Enable distributed tracing (Jaeger/X-Ray)
- [ ] Set up log aggregation (ELK/Splunk)
- [ ] Create dashboards (Grafana/CloudWatch)
- [ ] Configure health checks

### Post-Deployment

- [ ] Verify all endpoints working
- [ ] Test file upload/download
- [ ] Test authentication flow
- [ ] Run benefits investigation
- [ ] Send test notifications
- [ ] Review audit logs
- [ ] Check correlation IDs in logs
- [ ] Verify database backups
- [ ] Test disaster recovery procedure

## Cost Optimization

### Estimated Monthly Costs (AWS)

| Service | Configuration | Cost |
|---------|--------------|------|
| RDS PostgreSQL | db.t3.medium, Multi-AZ | $150 |
| S3 Storage | 100GB + requests | $25 |
| ECS Fargate | 2 tasks, 2GB RAM | $90 |
| ALB | Application Load Balancer | $25 |
| CloudWatch | Logs + Metrics | $20 |
| **Total** | | **~$310/month** |

### Cost Saving Tips

1. **Use Reserved Instances** for RDS (30-40% savings)
2. **Enable S3 Lifecycle Policies** (archive old files)
3. **Use Spot Instances** for non-critical workloads
4. **Configure Auto-Scaling** to scale down during off-hours
5. **Set up Budget Alerts** to monitor spending

## Support

For questions about cloud migration:
- Architecture Review: architecture@sonexus.com
- Security Review: security@sonexus.com
- DevOps Support: devops@sonexus.com

---

**Remember**: The beauty of hexagonal architecture is that domain logic never changes during migration. Only swap adapter implementations!
