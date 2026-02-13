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
public class FormResourceResponseDto {

    private Long id;
    private String title;
    private String description;
    private Long programId;
    private String programName;
    private String category;

    // File information
    private String fileName;
    private Long fileSize;
    private String mimeType;

    // Version control
    private Integer version;
    private Long parentId;

    // Compliance
    private Boolean complianceApproved;

    // Audit fields
    private Long uploadedById;
    private String uploadedByEmail;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

    // Statistics
    private Long downloadCount;
}
