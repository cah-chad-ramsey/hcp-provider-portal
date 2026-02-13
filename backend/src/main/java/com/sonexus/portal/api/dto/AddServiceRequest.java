package com.sonexus.portal.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddServiceRequest {

    @NotBlank(message = "Service name is required")
    @Size(max = 255, message = "Service name must be less than 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotNull(message = "Service type is required")
    private String serviceType;
}
