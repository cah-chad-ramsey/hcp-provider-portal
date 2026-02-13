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
public class ProviderAffiliationResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private ProviderResponse provider;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime verifiedAt;
    private String verifiedByEmail;
    private String verificationReason;
}
