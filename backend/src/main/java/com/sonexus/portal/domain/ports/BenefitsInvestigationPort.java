package com.sonexus.portal.domain.ports;

import com.sonexus.portal.domain.model.BenefitsInvestigationRequest;
import com.sonexus.portal.domain.model.BenefitsInvestigationResult;

/**
 * Port for benefits investigation operations.
 * Implementations: RuleBasedBenefitsAdapter (local/MVP), HttpBenefitsApiAdapter (cloud/prod)
 */
public interface BenefitsInvestigationPort {

    /**
     * Investigate medical insurance coverage
     */
    BenefitsInvestigationResult investigateMedicalCoverage(BenefitsInvestigationRequest request);

    /**
     * Investigate pharmacy insurance coverage
     */
    BenefitsInvestigationResult investigatePharmacyCoverage(BenefitsInvestigationRequest request);

    /**
     * Check if service is available (for health checks)
     */
    boolean isAvailable();
}
