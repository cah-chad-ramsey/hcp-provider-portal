package com.sonexus.portal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextActionResponse {

    private String id;
    private String title;
    private String description;
    private String actionType;  // ENROLLMENT, BENEFITS, SERVICE, MESSAGE, etc.
    private String priority;    // HIGH, MEDIUM, LOW
    private Long resourceId;    // Patient ID, Enrollment ID, etc.
    private String resourceName;
    private String actionUrl;
    private String icon;
    private Integer daysOverdue;
}
