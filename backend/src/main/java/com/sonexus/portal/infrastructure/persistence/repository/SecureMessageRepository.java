package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.SecureMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecureMessageRepository extends JpaRepository<SecureMessageEntity, Long> {

    @Query("SELECT m FROM SecureMessageEntity m " +
           "LEFT JOIN FETCH m.attachments " +
           "LEFT JOIN FETCH m.sentBy " +
           "WHERE m.thread.id = :threadId " +
           "ORDER BY m.sentAt ASC")
    List<SecureMessageEntity> findByThreadIdOrderBySentAt(@Param("threadId") Long threadId);

    @Query("SELECT COUNT(m) FROM SecureMessageEntity m " +
           "WHERE m.thread.id = :threadId AND m.readAt IS NULL " +
           "AND m.sentBy.id != :currentUserId")
    Long countUnreadMessages(@Param("threadId") Long threadId, @Param("currentUserId") Long currentUserId);
}
