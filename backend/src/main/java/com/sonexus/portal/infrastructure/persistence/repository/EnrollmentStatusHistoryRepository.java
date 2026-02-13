package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.EnrollmentStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentStatusHistoryRepository extends JpaRepository<EnrollmentStatusHistoryEntity, Long> {
    List<EnrollmentStatusHistoryEntity> findByEnrollmentIdOrderByChangedAtDesc(Long enrollmentId);
}
