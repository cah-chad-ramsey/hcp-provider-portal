package com.sonexus.portal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private ProgramResponse program;
    private ProviderResponse prescriber;
    private String status;
    private String diagnosisCode;
    private String diagnosisDescription;
    private String medicationName;
    private String notes;
    private Long createdById;
    private String createdByEmail;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
