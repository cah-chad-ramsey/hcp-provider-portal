package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.SecureMessageThreadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SecureMessageThreadRepository extends JpaRepository<SecureMessageThreadEntity, Long> {

    @Query("SELECT DISTINCT t FROM SecureMessageThreadEntity t " +
           "LEFT JOIN FETCH t.messages m " +
           "LEFT JOIN FETCH m.sentBy " +
           "WHERE t.createdBy.id = :userId OR EXISTS (" +
           "  SELECT 1 FROM SecureMessageEntity msg WHERE msg.thread = t AND msg.sentBy.id = :userId" +
           ")")
    Page<SecureMessageThreadEntity> findThreadsByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM SecureMessageThreadEntity t " +
           "WHERE t.patient.id = :patientId " +
           "ORDER BY t.lastMessageAt DESC")
    Page<SecureMessageThreadEntity> findByPatientId(@Param("patientId") Long patientId, Pageable pageable);

    @Query("SELECT t FROM SecureMessageThreadEntity t " +
           "WHERE t.program.id = :programId " +
           "ORDER BY t.lastMessageAt DESC")
    Page<SecureMessageThreadEntity> findByProgramId(@Param("programId") Long programId, Pageable pageable);
}
