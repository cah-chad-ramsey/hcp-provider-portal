package com.sonexus.portal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportServiceResponse {
    private Long id;
    private Long programId;
    private String name;
    private String description;
    private String serviceType;
    private Boolean active;
}
