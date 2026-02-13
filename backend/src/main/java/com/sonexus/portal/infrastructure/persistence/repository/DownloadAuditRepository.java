package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.DownloadAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DownloadAuditRepository extends JpaRepository<DownloadAuditEntity, Long> {

    /**
     * Find download audits by form resource
     */
    List<DownloadAuditEntity> findByFormResourceIdOrderByDownloadedAtDesc(Long formResourceId);

    /**
     * Find download audits by user
     */
    List<DownloadAuditEntity> findByUserIdOrderByDownloadedAtDesc(Long userId);

    /**
     * Find download audits by patient
     */
    List<DownloadAuditEntity> findByPatientIdOrderByDownloadedAtDesc(Long patientId);

    /**
     * Find download audits by correlation ID
     */
    List<DownloadAuditEntity> findByCorrelationIdOrderByDownloadedAtDesc(String correlationId);

    /**
     * Count downloads for a form
     */
    long countByFormResourceId(Long formResourceId);

    /**
     * Find recent downloads
     */
    List<DownloadAuditEntity> findTop100ByOrderByDownloadedAtDesc();

    /**
     * Find downloads in date range
     */
    List<DownloadAuditEntity> findByDownloadedAtBetweenOrderByDownloadedAtDesc(
            LocalDateTime start, LocalDateTime end);
}
