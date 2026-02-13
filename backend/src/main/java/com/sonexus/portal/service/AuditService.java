package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.AuditEventResponse;
import com.sonexus.portal.domain.ports.AuthProvider;
import com.sonexus.portal.infrastructure.persistence.entity.AuditEventEntity;
import com.sonexus.portal.infrastructure.persistence.entity.UserEntity;
import com.sonexus.portal.infrastructure.persistence.repository.AuditEventRepository;
import com.sonexus.portal.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final AuthProvider authProvider;

    @Transactional
    public void logEvent(String eventType, String resourceType, Long resourceId, String action) {
        logEvent(eventType, resourceType, resourceId, action, null, null);
    }

    @Transactional
    public void logEvent(String eventType, String resourceType, Long resourceId, String action,
                         String correlationId, Map<String, Object> metadata) {
        try {
            Long userId = authProvider.getCurrentUser()
                    .map(user -> user.getId())
                    .orElse(null);

            UserEntity userEntity = null;
            if (userId != null) {
                userEntity = userRepository.findById(userId).orElse(null);
            }

            String ipAddress = getClientIpAddress();
            String finalCorrelationId = correlationId != null ? correlationId : UUID.randomUUID().toString();

            AuditEventEntity auditEvent = AuditEventEntity.builder()
                    .eventType(eventType)
                    .user(userEntity)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .action(action)
                    .correlationId(finalCorrelationId)
                    .ipAddress(ipAddress)
                    .metadata(metadata)
                    .build();

            auditEventRepository.save(auditEvent);
            log.info("Audit event logged: type={}, resource={}:{}, action={}, correlationId={}",
                    eventType, resourceType, resourceId, action, finalCorrelationId);
        } catch (Exception e) {
            log.error("Failed to log audit event", e);
            // Don't throw exception - audit logging should not break main flow
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> getAuditEvents(
            String eventType,
            Long userId,
            String action,
            String correlationId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        Page<AuditEventEntity> events = auditEventRepository.findAuditEvents(
                eventType, userId, action, correlationId, startDate, endDate, pageable);

        return events.map(this::mapToResponse);
    }

    private AuditEventResponse mapToResponse(AuditEventEntity entity) {
        return AuditEventResponse.builder()
                .id(entity.getId())
                .eventType(entity.getEventType())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(entity.getUser() != null ?
                        entity.getUser().getFirstName() + " " + entity.getUser().getLastName() : "System")
                .userEmail(entity.getUser() != null ? entity.getUser().getEmail() : null)
                .resourceType(entity.getResourceType())
                .resourceId(entity.getResourceId())
                .action(entity.getAction())
                .correlationId(entity.getCorrelationId())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String xForwardedFor = attributes.getRequest().getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return attributes.getRequest().getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Failed to get client IP address", e);
        }
        return "unknown";
    }
}
