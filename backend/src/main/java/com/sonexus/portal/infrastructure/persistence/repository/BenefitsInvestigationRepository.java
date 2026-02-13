package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.BenefitsInvestigationEntity;
import com.sonexus.portal.infrastructure.persistence.entity.BenefitsInvestigationEntity.InvestigationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BenefitsInvestigationRepository extends JpaRepository<BenefitsInvestigationEntity, Long> {

    /**
     * Find all investigations for a patient, ordered by creation date descending
     */
    List<BenefitsInvestigationEntity> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    /**
     * Find the latest investigation for a patient by type
     */
    Optional<BenefitsInvestigationEntity> findFirstByPatientIdAndInvestigationTypeOrderByCreatedAtDesc(
            Long patientId, InvestigationType investigationType);

    /**
     * Find all non-expired investigations for a patient
     */
    @Query("SELECT b FROM BenefitsInvestigationEntity b WHERE b.patientId = :patientId " +
           "AND b.expiresAt > :now ORDER BY b.createdAt DESC")
    List<BenefitsInvestigationEntity> findNonExpiredByPatientId(
            @Param("patientId") Long patientId,
            @Param("now") LocalDateTime now);

    /**
     * Count investigations by patient
     */
    long countByPatientId(Long patientId);
}
