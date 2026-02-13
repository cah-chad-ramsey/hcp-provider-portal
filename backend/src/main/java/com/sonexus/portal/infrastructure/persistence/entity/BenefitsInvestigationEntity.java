package com.sonexus.portal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "benefits_investigations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitsInvestigationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private PatientEntity patient;

    @Column(name = "program_id")
    private Long programId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", insertable = false, updatable = false)
    private ProgramEntity program;

    @Column(name = "investigation_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private InvestigationType investigationType;

    // Input fields
    @Column(name = "payer_name")
    private String payerName;

    @Column(name = "payer_plan_id", length = 100)
    private String payerPlanId;

    @Column(name = "member_id", length = 100)
    private String memberId;

    @Column(name = "patient_state", length = 2)
    private String patientState;

    @Column(name = "medication_name")
    private String medicationName;

    // Result fields
    @Column(name = "coverage_status", nullable = false, length = 50)
    private String coverageStatus;

    @Column(name = "coverage_type", length = 50)
    private String coverageType;

    @Column(name = "prior_auth_required")
    private Boolean priorAuthRequired;

    @Column(name = "deductible_applies")
    private Boolean deductibleApplies;

    @Column(name = "specialty_pharmacy_required")
    private Boolean specialtyPharmacyRequired;

    @Column(name = "copay_amount", precision = 10, scale = 2)
    private BigDecimal copayAmount;

    @Column(name = "coinsurance_percentage")
    private Integer coinsurancePercentage;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_payload", columnDefinition = "jsonb")
    private Map<String, Object> resultPayload;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Audit fields
    @Column(name = "created_by", nullable = false)
    private Long createdById;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private UserEntity createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            // Default expiration: 30 days from now
            expiresAt = LocalDateTime.now().plusDays(30);
        }
    }

    public enum InvestigationType {
        MEDICAL, PHARMACY
    }
}
