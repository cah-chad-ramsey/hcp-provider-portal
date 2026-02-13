package com.sonexus.portal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenefitsInvestigationRequestDto {

    @NotNull(message = "Program ID is required")
    private Long programId;

    @NotBlank(message = "Investigation type is required (MEDICAL or PHARMACY)")
    private String investigationType;

    @NotBlank(message = "Payer name is required")
    private String payerName;

    private String payerPlanId;

    @NotBlank(message = "Member ID is required")
    private String memberId;

    private String patientState;

    private LocalDate patientDob;

    private String medicationName;
}
