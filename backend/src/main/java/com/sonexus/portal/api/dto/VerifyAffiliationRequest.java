package com.sonexus.portal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyAffiliationRequest {

    @NotNull(message = "Approved status is required")
    private Boolean approved;

    @NotBlank(message = "Reason is required")
    private String reason;
}
