package com.sonexus.portal.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "Program ID is required")
    private Long programId;

    private Long prescriberId;

    private String diagnosisCode;

    private String diagnosisDescription;

    private String medicationName;

    private String notes;

    private Boolean submit = false; // If true, submit; if false, save as draft

    private List<ConsentRequest> consents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentRequest {
        private String consentType;
        private Boolean granted;
        private String notes;
    }
}
