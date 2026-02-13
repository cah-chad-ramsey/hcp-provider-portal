package com.sonexus.portal.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageThreadResponse {

    private Long id;
    private String subject;
    private Long programId;
    private String programName;
    private Long patientId;
    private String patientName;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private List<MessageResponse> messages;
}
