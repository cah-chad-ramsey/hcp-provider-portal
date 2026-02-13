package com.sonexus.portal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_service_enrollments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientServiceEnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = false)
    private SupportServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private EnrollmentEntity enrollment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceEnrollmentStatus status = ServiceEnrollmentStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrolled_by", nullable = false)
    private UserEntity enrolledBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum ServiceEnrollmentStatus {
        ACTIVE,
        INACTIVE,
        COMPLETED,
        CANCELLED
    }
}
