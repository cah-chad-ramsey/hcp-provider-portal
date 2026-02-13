package com.sonexus.portal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment_status_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private EnrollmentEntity enrollment;

    @Column(length = 20)
    private String fromStatus;

    @Column(nullable = false, length = 20)
    private String toStatus;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private UserEntity changedBy;

    @Column(nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();
}
