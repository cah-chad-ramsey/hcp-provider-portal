package com.sonexus.portal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_audits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "form_resource_id", nullable = false)
    private Long formResourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_resource_id", insertable = false, updatable = false)
    private FormResourceEntity formResource;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "patient_id")
    private Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private PatientEntity patient;

    @Column(name = "downloaded_at", nullable = false)
    private LocalDateTime downloadedAt;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        downloadedAt = LocalDateTime.now();
    }
}
