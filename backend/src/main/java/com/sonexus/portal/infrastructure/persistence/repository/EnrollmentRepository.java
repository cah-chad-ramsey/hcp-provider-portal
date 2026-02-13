package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.EnrollmentEntity;
import com.sonexus.portal.infrastructure.persistence.entity.EnrollmentEntity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    List<EnrollmentEntity> findByPatientId(Long patientId);
    List<EnrollmentEntity> findByStatus(EnrollmentStatus status);
    List<EnrollmentEntity> findByPatientIdAndStatus(Long patientId, EnrollmentStatus status);
}
