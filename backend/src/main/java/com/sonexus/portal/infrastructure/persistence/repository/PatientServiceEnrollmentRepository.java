package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.PatientServiceEnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientServiceEnrollmentRepository extends JpaRepository<PatientServiceEnrollmentEntity, Long> {
    List<PatientServiceEnrollmentEntity> findByPatientId(Long patientId);
    Optional<PatientServiceEnrollmentEntity> findByPatientIdAndServiceId(Long patientId, Long serviceId);
}
