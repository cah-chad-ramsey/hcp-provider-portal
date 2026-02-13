package com.sonexus.portal.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitsInvestigationRequest {
    private Long patientId;
    private Long programId;
    private String investigationType; // MEDICAL or PHARMACY
    private String payerName;
    private String payerPlanId;
    private String memberId;
    private String patientState;
    private LocalDate patientDob;
    private String medicationName;
}
