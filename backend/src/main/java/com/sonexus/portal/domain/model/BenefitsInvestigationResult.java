package com.sonexus.portal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitsInvestigationResult {
    private String coverageStatus; // ACTIVE, INACTIVE, UNKNOWN
    private String coverageType; // MEDICARE, MEDICAID, COMMERCIAL, UNKNOWN
    private Boolean priorAuthRequired;
    private Boolean deductibleApplies;
    private Boolean specialtyPharmacyRequired;
    private String notes;
    private Map<String, Object> additionalData; // Flexible for provider-specific data
}
