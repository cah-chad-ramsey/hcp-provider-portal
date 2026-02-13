package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.ProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<ProgramEntity, Long> {
    List<ProgramEntity> findByActiveTrue();
}
