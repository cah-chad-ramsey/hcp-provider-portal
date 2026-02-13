package com.sonexus.portal.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormResourceRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Long programId;

    private String category;

    private Boolean complianceApproved;
}
