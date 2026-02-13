package com.sonexus.portal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {

    private Long id;
    private String eventType;
    private Long userId;
    private String userName;
    private String userEmail;
    private String resourceType;
    private Long resourceId;
    private String action;
    private String correlationId;
    private String ipAddress;
    private LocalDateTime createdAt;
}
