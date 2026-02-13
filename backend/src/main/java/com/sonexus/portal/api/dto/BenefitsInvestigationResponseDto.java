package com.sonexus.portal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitsInvestigationResponseDto {

    private Long id;
    private Long patientId;
    private String patientReferenceId;
    private String patientName;
    private Long programId;
    private String programName;
    private String investigationType;

    // Input fields
    private String payerName;
    private String payerPlanId;
    private String memberId;
    private String patientState;
    private String medicationName;

    // Result fields
    private String coverageStatus;
    private String coverageType;
    private Boolean priorAuthRequired;
    private Boolean deductibleApplies;
    private Boolean specialtyPharmacyRequired;
    private BigDecimal copayAmount;
    private Integer coinsurancePercentage;
    private String notes;
    private Map<String, Object> resultPayload;

    private LocalDateTime expiresAt;
    private Long createdById;
    private String createdByEmail;
    private LocalDateTime createdAt;

    // Computed fields
    private Boolean isExpired;
}
