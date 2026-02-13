package com.sonexus.portal.infrastructure.adapters;

import com.sonexus.portal.domain.model.BenefitsInvestigationRequest;
import com.sonexus.portal.domain.model.BenefitsInvestigationResult;
import com.sonexus.portal.domain.ports.BenefitsInvestigationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Rule-based benefits investigation adapter for MVP/local development.
 * Uses deterministic rules instead of external API calls.
 * In production, this will be replaced with HttpBenefitsApiAdapter.
 */
@Slf4j
@Service
@Profile({"default", "local", "test"})
public class RuleBasedBenefitsAdapter implements BenefitsInvestigationPort {

    @Override
    public BenefitsInvestigationResult investigateMedicalCoverage(BenefitsInvestigationRequest request) {
        log.info("Investigating medical coverage for patient {} with payer {}",
                request.getPatientId(), request.getPayerName());

        String coverageType = determineCoverageType(request.getPayerName());
        boolean priorAuthRequired = determineIfPriorAuthRequired(request.getPayerName(), request.getPayerPlanId());

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("investigationType", "MEDICAL");
        additionalData.put("payerName", request.getPayerName());
        additionalData.put("memberId", request.getMemberId());

        return BenefitsInvestigationResult.builder()
                .coverageStatus("ACTIVE")
                .coverageType(coverageType)
                .priorAuthRequired(priorAuthRequired)
                .deductibleApplies(true)
                .specialtyPharmacyRequired(false)
                .notes(generateNotes(coverageType, priorAuthRequired, "MEDICAL"))
                .additionalData(additionalData)
                .build();
    }

    @Override
    public BenefitsInvestigationResult investigatePharmacyCoverage(BenefitsInvestigationRequest request) {
        log.info("Investigating pharmacy coverage for patient {} with payer {}",
                request.getPatientId(), request.getPayerName());

        String coverageType = determineCoverageType(request.getPayerName());
        boolean specialtyPharmacyRequired = determineIfSpecialtyPharmacyRequired(request.getPayerName());
        boolean priorAuthRequired = determineIfPriorAuthRequired(request.getPayerName(), request.getPayerPlanId());

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("investigationType", "PHARMACY");
        additionalData.put("payerName", request.getPayerName());
        additionalData.put("memberId", request.getMemberId());
        additionalData.put("medicationName", request.getMedicationName());

        return BenefitsInvestigationResult.builder()
                .coverageStatus("ACTIVE")
                .coverageType(coverageType)
                .priorAuthRequired(priorAuthRequired)
                .deductibleApplies(true)
                .specialtyPharmacyRequired(specialtyPharmacyRequired)
                .notes(generateNotes(coverageType, priorAuthRequired, "PHARMACY"))
                .additionalData(additionalData)
                .build();
    }

    @Override
    public boolean isAvailable() {
        return true; // Rule-based adapter is always available
    }

    /**
     * Determine coverage type based on payer name keywords
     */
    private String determineCoverageType(String payerName) {
        if (payerName == null || payerName.isBlank()) {
            return "UNKNOWN";
        }

        String lowerPayerName = payerName.toLowerCase();

        // Medicare
        if (lowerPayerName.contains("medicare") || lowerPayerName.contains("cms")
            || lowerPayerName.contains("part d") || lowerPayerName.contains("part b")) {
            return "MEDICARE";
        }

        // Medicaid
        if (lowerPayerName.contains("medicaid")) {
            return "MEDICAID";
        }

        // Commercial insurers
        if (lowerPayerName.contains("blue") || lowerPayerName.contains("aetna")
            || lowerPayerName.contains("cigna") || lowerPayerName.contains("uhc")
            || lowerPayerName.contains("united healthcare") || lowerPayerName.contains("anthem")
            || lowerPayerName.contains("humana")) {
            return "COMMERCIAL";
        }

        return "UNKNOWN";
    }

    /**
     * Determine if prior authorization is required
     */
    private boolean determineIfPriorAuthRequired(String payerName, String payerPlanId) {
        if (payerPlanId != null && payerPlanId.toUpperCase().startsWith("PA-")) {
            return true;
        }

        if (payerName != null && payerName.toLowerCase().contains("hmo")) {
            return true;
        }

        return false;
    }

    /**
     * Determine if specialty pharmacy is required
     */
    private boolean determineIfSpecialtyPharmacyRequired(String payerName) {
        if (payerName == null) {
            return false;
        }

        String lowerPayerName = payerName.toLowerCase();

        return lowerPayerName.contains("optum") || lowerPayerName.contains("caremark")
                || lowerPayerName.contains("express scripts") || lowerPayerName.contains("accredo")
                || lowerPayerName.contains("cvs specialty") || lowerPayerName.contains("walgreens specialty");
    }

    /**
     * Generate human-readable notes based on investigation results
     */
    private String generateNotes(String coverageType, boolean priorAuthRequired, String investigationType) {
        StringBuilder notes = new StringBuilder();
        notes.append(investigationType).append(" coverage determined as ").append(coverageType).append(". ");

        if (priorAuthRequired) {
            notes.append("Prior authorization is required. ");
        } else {
            notes.append("No prior authorization required. ");
        }

        notes.append("This is a rule-based determination for MVP purposes.");

        return notes.toString();
    }
}
