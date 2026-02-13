package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.*;
import com.sonexus.portal.domain.ports.AuthProvider;
import com.sonexus.portal.domain.ports.FileStoragePort;
import com.sonexus.portal.infrastructure.persistence.entity.*;
import com.sonexus.portal.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecureMessageService {

    private final SecureMessageThreadRepository threadRepository;
    private final SecureMessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ProgramRepository programRepository;
    private final PatientRepository patientRepository;
    private final AuthProvider authProvider;
    private final AuditService auditService;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public MessageThreadResponse createThread(CreateThreadRequest request) {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        UserEntity createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SecureMessageThreadEntity.SecureMessageThreadEntityBuilder threadBuilder = SecureMessageThreadEntity.builder()
                .subject(request.getSubject())
                .createdBy(createdBy);

        if (request.getProgramId() != null) {
            ProgramEntity program = programRepository.findById(request.getProgramId())
                    .orElseThrow(() -> new RuntimeException("Program not found"));
            threadBuilder.program(program);
        }

        if (request.getPatientId() != null) {
            PatientEntity patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            threadBuilder.patient(patient);
        }

        SecureMessageThreadEntity thread = threadRepository.save(threadBuilder.build());
        log.info("Message thread created: id={}, subject={}, createdBy={}", thread.getId(), thread.getSubject(), userId);

        auditService.logEvent("MESSAGE_THREAD_CREATED", "THREAD", thread.getId(), "CREATE");

        return mapThreadToResponse(thread, userId);
    }

    @Transactional(readOnly = true)
    public Page<MessageThreadResponse> getThreads(Pageable pageable) {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        Page<SecureMessageThreadEntity> threads = threadRepository.findThreadsByUser(userId, pageable);

        List<MessageThreadResponse> responses = threads.getContent().stream()
                .map(thread -> mapThreadToResponse(thread, userId))
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, threads.getTotalElements());
    }

    @Transactional
    public MessageThreadResponse getThreadById(Long threadId) {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        SecureMessageThreadEntity thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        // Mark unread messages as read
        List<SecureMessageEntity> messages = messageRepository.findByThreadIdOrderBySentAt(threadId);
        messages.stream()
                .filter(m -> m.getReadAt() == null && !m.getSentBy().getId().equals(userId))
                .forEach(m -> m.setReadAt(LocalDateTime.now()));

        if (!messages.isEmpty()) {
            messageRepository.saveAll(messages);
        }

        return mapThreadToResponseWithMessages(thread, messages, userId);
    }

    @Transactional
    public MessageResponse sendMessage(Long threadId, SendMessageRequest request) {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        SecureMessageThreadEntity thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new RuntimeException("Thread not found"));

        UserEntity sentBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SecureMessageEntity message = SecureMessageEntity.builder()
                .thread(thread)
                .content(request.getContent())
                .sentBy(sentBy)
                .build();

        message = messageRepository.save(message);

        // Associate attachments
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            final SecureMessageEntity finalMessage = message;
            List<MessageAttachmentEntity> attachments = attachmentRepository.findAllById(request.getAttachmentIds());
            attachments.forEach(att -> att.setMessage(finalMessage));
            attachmentRepository.saveAll(attachments);
        }

        // Update thread last message timestamp
        thread.setLastMessageAt(message.getSentAt());
        threadRepository.save(thread);

        log.info("Message sent: id={}, threadId={}, sentBy={}", message.getId(), threadId, userId);
        auditService.logEvent("MESSAGE_SENT", "MESSAGE", message.getId(), "CREATE");

        return mapMessageToResponse(message);
    }

    @Transactional
    public MessageAttachmentResponse uploadAttachment(MultipartFile file) throws IOException {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Validate file size (e.g., max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds maximum limit of 10MB");
        }

        // Store file in MinIO
        String fileName = file.getOriginalFilename();
        String fileKey = "attachments/" + UUID.randomUUID() + "/" + fileName;
        fileStoragePort.storeFile(fileKey, file.getContentType(), file.getInputStream(), file.getSize());

        // Create temporary attachment entity (not yet associated with a message)
        MessageAttachmentEntity attachment = MessageAttachmentEntity.builder()
                .filePath(fileKey)
                .fileName(fileName)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();

        attachment = attachmentRepository.save(attachment);

        log.info("Attachment uploaded: id={}, fileName={}, uploadedBy={}", attachment.getId(), fileName, userId);
        auditService.logEvent("ATTACHMENT_UPLOADED", "ATTACHMENT", attachment.getId(), "CREATE");

        return mapAttachmentToResponse(attachment);
    }

    @Transactional(readOnly = true)
    public byte[] downloadAttachment(Long attachmentId) {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        MessageAttachmentEntity attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        // Verify user has access to this attachment's thread
        SecureMessageEntity message = attachment.getMessage();
        if (message == null) {
            throw new RuntimeException("Attachment not associated with a message");
        }

        try {
            InputStream inputStream = fileStoragePort.retrieveFile(attachment.getFilePath());
            byte[] fileContent = inputStream.readAllBytes();

            log.info("Attachment downloaded: id={}, fileName={}, downloadedBy={}", attachment.getId(), attachment.getFileName(), userId);
            auditService.logEvent("ATTACHMENT_DOWNLOADED", "ATTACHMENT", attachment.getId(), "VIEW");

            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read attachment file", e);
        }
    }

    private MessageThreadResponse mapThreadToResponse(SecureMessageThreadEntity thread, Long currentUserId) {
        Long unreadCount = messageRepository.countUnreadMessages(thread.getId(), currentUserId);

        return MessageThreadResponse.builder()
                .id(thread.getId())
                .subject(thread.getSubject())
                .programId(thread.getProgram() != null ? thread.getProgram().getId() : null)
                .programName(thread.getProgram() != null ? thread.getProgram().getName() : null)
                .patientId(thread.getPatient() != null ? thread.getPatient().getId() : null)
                .patientName(thread.getPatient() != null ?
                        thread.getPatient().getFirstName() + " " + thread.getPatient().getLastName() : null)
                .createdBy(thread.getCreatedBy().getId())
                .createdByName(thread.getCreatedBy().getFirstName() + " " + thread.getCreatedBy().getLastName())
                .createdAt(thread.getCreatedAt())
                .lastMessageAt(thread.getLastMessageAt())
                .unreadCount(unreadCount)
                .build();
    }

    private MessageThreadResponse mapThreadToResponseWithMessages(
            SecureMessageThreadEntity thread,
            List<SecureMessageEntity> messages,
            Long currentUserId) {

        MessageThreadResponse response = mapThreadToResponse(thread, currentUserId);
        response.setMessages(messages.stream()
                .map(this::mapMessageToResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private MessageResponse mapMessageToResponse(SecureMessageEntity message) {
        return MessageResponse.builder()
                .id(message.getId())
                .threadId(message.getThread().getId())
                .content(message.getContent())
                .sentBy(message.getSentBy().getId())
                .sentByName(message.getSentBy().getFirstName() + " " + message.getSentBy().getLastName())
                .sentAt(message.getSentAt())
                .readAt(message.getReadAt())
                .attachments(message.getAttachments().stream()
                        .map(this::mapAttachmentToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private MessageAttachmentResponse mapAttachmentToResponse(MessageAttachmentEntity attachment) {
        return MessageAttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .uploadedAt(attachment.getUploadedAt())
                .downloadUrl("/api/v1/messages/attachments/" + attachment.getId() + "/download")
                .build();
    }
}
