package com.sonexus.portal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_events")
@Data
@lombok.Getter
@lombok.Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false, length = 50)
    private String resourceType;

    @Column(nullable = false)
    private Long resourceId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 100)
    private String correlationId;

    @Column(length = 45)
    private String ipAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
