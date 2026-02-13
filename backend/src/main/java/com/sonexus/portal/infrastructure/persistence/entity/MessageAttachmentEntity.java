package com.sonexus.portal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_attachments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MessageAttachmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private SecureMessageEntity message;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(length = 100)
    private String mimeType;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
