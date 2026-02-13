package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.SupportServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportServiceRepository extends JpaRepository<SupportServiceEntity, Long> {
    List<SupportServiceEntity> findByProgramIdAndActiveTrue(Long programId);
}
