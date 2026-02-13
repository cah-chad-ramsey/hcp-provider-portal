package com.sonexus.portal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "form_resources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "program_id")
    private Long programId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", insertable = false, updatable = false)
    private ProgramEntity program;

    @Column(length = 100)
    private String category;

    // File storage
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    // Version control
    @Column(nullable = false)
    private Integer version;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private FormResourceEntity parent;

    // Compliance
    @Column(name = "compliance_approved", nullable = false)
    private Boolean complianceApproved;

    // Audit fields
    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedById;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", insertable = false, updatable = false)
    private UserEntity uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (version == null) {
            version = 1;
        }
        if (complianceApproved == null) {
            complianceApproved = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
