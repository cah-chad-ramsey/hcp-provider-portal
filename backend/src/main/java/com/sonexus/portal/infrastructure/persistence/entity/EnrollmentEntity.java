package com.sonexus.portal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "program_id", nullable = false)
    private ProgramEntity program;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prescriber_id")
    private ProviderEntity prescriber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.DRAFT;

    @Column(length = 20)
    private String diagnosisCode;

    @Column(columnDefinition = "TEXT")
    private String diagnosisDescription;

    @Column(length = 255)
    private String medicationName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    private LocalDateTime submittedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum EnrollmentStatus {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        APPROVED,
        DENIED,
        WITHDRAWN
    }
}
