package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {
    Page<AuditEventEntity> findByResourceTypeAndResourceId(String resourceType, Long resourceId, Pageable pageable);
    Page<AuditEventEntity> findByCorrelationId(String correlationId, Pageable pageable);

    @Query("SELECT a FROM AuditEventEntity a WHERE " +
           "(:eventType IS NULL OR a.eventType = :eventType) AND " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:correlationId IS NULL OR a.correlationId = :correlationId) AND " +
           "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdAt <= :endDate)")
    Page<AuditEventEntity> findAuditEvents(
            @Param("eventType") String eventType,
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("correlationId") String correlationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
