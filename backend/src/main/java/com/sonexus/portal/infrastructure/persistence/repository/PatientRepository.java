package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.PatientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {
    Optional<PatientEntity> findByReferenceId(String referenceId);

    @Query("SELECT p FROM PatientEntity p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "p.referenceId LIKE CONCAT('%', :search, '%')")
    Page<PatientEntity> searchPatients(@Param("search") String search, Pageable pageable);
}
