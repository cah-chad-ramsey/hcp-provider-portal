package com.sonexus.portal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateThreadRequest {

    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject must be less than 255 characters")
    private String subject;

    private Long programId;

    private Long patientId;
}
