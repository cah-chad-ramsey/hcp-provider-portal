package com.sonexus.portal.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Content is required")
    private String content;

    private List<Long> attachmentIds = new ArrayList<>();
}
