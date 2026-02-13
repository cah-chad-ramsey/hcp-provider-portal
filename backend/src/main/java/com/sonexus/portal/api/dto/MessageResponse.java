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
public class MessageResponse {

    private Long id;
    private Long threadId;
    private String content;
    private Long sentBy;
    private String sentByName;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private List<MessageAttachmentResponse> attachments;
}
