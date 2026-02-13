package com.sonexus.portal.infrastructure.persistence.repository;

import com.sonexus.portal.infrastructure.persistence.entity.MessageAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachmentEntity, Long> {
    List<MessageAttachmentEntity> findByMessageId(Long messageId);
}
