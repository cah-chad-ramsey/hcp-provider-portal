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
public class MessageAttachmentResponse {

    private Long id;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
