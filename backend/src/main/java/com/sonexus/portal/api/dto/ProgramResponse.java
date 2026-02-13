package com.sonexus.portal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private List<SupportServiceResponse> services;
}
