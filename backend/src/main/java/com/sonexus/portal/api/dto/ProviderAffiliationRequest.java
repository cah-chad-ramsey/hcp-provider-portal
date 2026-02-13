package com.sonexus.portal.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderAffiliationRequest {

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    private String notes;
}
