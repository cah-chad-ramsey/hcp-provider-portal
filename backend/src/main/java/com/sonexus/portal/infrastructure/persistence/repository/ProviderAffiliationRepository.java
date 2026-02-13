package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.ProviderAffiliationEntity;
import com.sonexus.portal.infrastructure.persistence.entity.ProviderAffiliationEntity.AffiliationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderAffiliationRepository extends JpaRepository<ProviderAffiliationEntity, Long> {
    List<ProviderAffiliationEntity> findByUserId(Long userId);
    List<ProviderAffiliationEntity> findByStatus(AffiliationStatus status);
    Optional<ProviderAffiliationEntity> findByUserIdAndProviderId(Long userId, Long providerId);
    boolean existsByUserIdAndProviderIdAndStatus(Long userId, Long providerId, AffiliationStatus status);
    List<ProviderAffiliationEntity> findByUserIdAndStatus(Long userId, AffiliationStatus status);
}
