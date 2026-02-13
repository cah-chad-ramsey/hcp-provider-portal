package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.ProviderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<ProviderEntity, Long> {
    Optional<ProviderEntity> findByNpi(String npi);
    Page<ProviderEntity> findByActiveTrue(Pageable pageable);
    Page<ProviderEntity> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    Page<ProviderEntity> findByActiveTrueAndSpecialtyContainingIgnoreCase(String specialty, Pageable pageable);
}
