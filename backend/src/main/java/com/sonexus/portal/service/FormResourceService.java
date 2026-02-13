package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.FormResourceRequestDto;
import com.sonexus.portal.api.dto.FormResourceResponseDto;
import com.sonexus.portal.domain.ports.FileStoragePort;
import com.sonexus.portal.infrastructure.persistence.entity.DownloadAuditEntity;
import com.sonexus.portal.infrastructure.persistence.entity.FormResourceEntity;
import com.sonexus.portal.infrastructure.persistence.entity.UserEntity;
import com.sonexus.portal.infrastructure.persistence.repository.DownloadAuditRepository;
import com.sonexus.portal.infrastructure.persistence.repository.FormResourceRepository;
import com.sonexus.portal.infrastructure.persistence.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormResourceService {

    private final FormResourceRepository formResourceRepository;
    private final DownloadAuditRepository downloadAuditRepository;
    private final UserRepository userRepository;
    private final FileStoragePort fileStoragePort;
    private final AuditService auditService;

    @Transactional
    public FormResourceResponseDto uploadForm(
            FormResourceRequestDto requestDto,
            MultipartFile file) {

        log.info("Uploading form: {} ({})", requestDto.getTitle(), file.getOriginalFilename());

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        validateFileType(file.getContentType());
        validateFileSize(file.getSize());

        try {
            // Store file in MinIO
            String filePath = fileStoragePort.storeFile(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getInputStream(),
                    file.getSize()
            );

            // Create form resource entity
            FormResourceEntity entity = FormResourceEntity.builder()
                    .title(requestDto.getTitle())
                    .description(requestDto.getDescription())
                    .programId(requestDto.getProgramId())
                    .category(requestDto.getCategory())
                    .filePath(filePath)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .complianceApproved(requestDto.getComplianceApproved() != null ?
                            requestDto.getComplianceApproved() : false)
                    .uploadedById(user.getId())
                    .build();

            FormResourceEntity saved = formResourceRepository.save(entity);

            // Audit log
            auditService.logEvent(
                    "FORM_UPLOADED",
                    "FORM_RESOURCE",
                    saved.getId(),
                    "CREATE"
            );

            log.info("Form uploaded successfully with id: {}", saved.getId());

            return mapToResponseDto(saved);

        } catch (Exception e) {
            log.error("Error uploading form", e);
            throw new RuntimeException("Failed to upload form: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<FormResourceResponseDto> searchForms(
            Long programId,
            String category,
            String searchTerm,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<FormResourceEntity> entities = formResourceRepository.findAllWithFilters(
                programId, category, searchTerm, pageable
        );

        return entities.map(this::mapToResponseDto);
    }

    @Transactional(readOnly = true)
    public FormResourceResponseDto getFormById(Long id) {
        FormResourceEntity entity = formResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));

        return mapToResponseDto(entity);
    }

    @Transactional
    public InputStream downloadForm(Long id, Long patientId, HttpServletRequest request) {
        FormResourceEntity entity = formResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get correlation ID from request header
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        // Get client IP
        String ipAddress = getClientIpAddress(request);

        // Create download audit
        DownloadAuditEntity audit = DownloadAuditEntity.builder()
                .formResourceId(id)
                .userId(user.getId())
                .patientId(patientId)
                .correlationId(correlationId)
                .ipAddress(ipAddress)
                .build();

        downloadAuditRepository.save(audit);

        // Audit log
        auditService.logEvent(
                "FORM_DOWNLOADED",
                "FORM_RESOURCE",
                id,
                "READ"
        );

        log.info("Form downloaded: {} by user: {}", entity.getFileName(), userEmail);

        // Retrieve file from MinIO
        return fileStoragePort.retrieveFile(entity.getFilePath());
    }

    @Transactional(readOnly = true)
    public List<FormResourceResponseDto> getFormVersions(Long formId) {
        List<FormResourceEntity> versions = formResourceRepository.findAllVersions(formId);

        return versions.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteForm(Long id) {
        FormResourceEntity entity = formResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));

        // Delete file from storage
        fileStoragePort.deleteFile(entity.getFilePath());

        // Delete entity
        formResourceRepository.delete(entity);

        // Audit log
        auditService.logEvent(
                "FORM_DELETED",
                "FORM_RESOURCE",
                id,
                "DELETE"
        );

        log.info("Form deleted: {}", entity.getFileName());
    }

    private FormResourceResponseDto mapToResponseDto(FormResourceEntity entity) {
        Long downloadCount = downloadAuditRepository.countByFormResourceId(entity.getId());

        return FormResourceResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .programId(entity.getProgramId())
                .programName(entity.getProgram() != null ? entity.getProgram().getName() : null)
                .category(entity.getCategory())
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .mimeType(entity.getMimeType())
                .version(entity.getVersion())
                .parentId(entity.getParentId())
                .complianceApproved(entity.getComplianceApproved())
                .uploadedById(entity.getUploadedById())
                .uploadedByEmail(entity.getUploadedBy() != null ? entity.getUploadedBy().getEmail() : null)
                .uploadedAt(entity.getUploadedAt())
                .updatedAt(entity.getUpdatedAt())
                .downloadCount(downloadCount)
                .build();
    }

    private void validateFileType(String contentType) {
        List<String> allowedTypes = List.of(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "image/jpeg",
                "image/png"
        );

        if (!allowedTypes.contains(contentType)) {
            throw new RuntimeException("File type not allowed: " + contentType);
        }
    }

    private void validateFileSize(long size) {
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (size > maxSize) {
            throw new RuntimeException("File size exceeds maximum allowed size of 50MB");
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
