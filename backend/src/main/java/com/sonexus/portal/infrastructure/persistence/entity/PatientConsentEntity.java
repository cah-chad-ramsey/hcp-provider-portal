package com.sonexus.portal.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_consents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientConsentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(nullable = false, length = 50)
    private String consentType;

    @Column(nullable = false)
    private Boolean granted;

    @Column(nullable = false)
    private LocalDateTime grantedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    private UserEntity grantedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
