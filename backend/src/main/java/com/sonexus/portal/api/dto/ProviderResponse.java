package com.sonexus.portal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {
    private Long id;
    private String npi;
    private String name;
    private String specialty;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String phone;
    private String fax;
    private String email;
    private Boolean active;
}
