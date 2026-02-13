package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.FormResourceRequestDto;
import com.sonexus.portal.api.dto.FormResourceResponseDto;
import com.sonexus.portal.service.FormResourceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FormResourceController {

    private final FormResourceService formResourceService;

    /**
     * Upload a new form resource (ADMIN only)
     */
    @PostMapping(value = "/admin/forms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormResourceResponseDto> uploadForm(
            @RequestPart("metadata") @Valid FormResourceRequestDto metadata,
            @RequestPart("file") MultipartFile file) {

        log.info("POST /api/v1/admin/forms - Uploading form: {}", metadata.getTitle());

        FormResourceResponseDto response = formResourceService.uploadForm(metadata, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Search and list form resources
     */
    @GetMapping("/forms")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    public ResponseEntity<Page<FormResourceResponseDto>> searchForms(
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/v1/forms - Searching forms with programId={}, category={}, searchTerm={}",
                programId, category, searchTerm);

        Page<FormResourceResponseDto> forms = formResourceService.searchForms(
                programId, category, searchTerm, page, size
        );

        return ResponseEntity.ok(forms);
    }

    /**
     * Get a specific form by ID
     */
    @GetMapping("/forms/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    public ResponseEntity<FormResourceResponseDto> getFormById(@PathVariable Long id) {

        log.info("GET /api/v1/forms/{} - Fetching form", id);

        FormResourceResponseDto form = formResourceService.getFormById(id);

        return ResponseEntity.ok(form);
    }

    /**
     * View form inline (audited)
     */
    @GetMapping("/forms/{id}/view")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    public void viewForm(
            @PathVariable Long id,
            @RequestParam(required = false) Long patientId,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("GET /api/v1/forms/{}/view - Viewing form", id);

        try {
            FormResourceResponseDto form = formResourceService.getFormById(id);
            InputStream fileStream = formResourceService.downloadForm(id, patientId, request);

            // Set response headers for inline viewing
            response.setContentType(form.getMimeType());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + form.getFileName() + "\"");
            response.setContentLengthLong(form.getFileSize());

            // Copy file stream to response
            StreamUtils.copy(fileStream, response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            log.error("Error viewing form", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Download form (audited)
     */
    @GetMapping("/forms/{id}/download")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    public void downloadForm(
            @PathVariable Long id,
            @RequestParam(required = false) Long patientId,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("GET /api/v1/forms/{}/download - Downloading form", id);

        try {
            FormResourceResponseDto form = formResourceService.getFormById(id);
            InputStream fileStream = formResourceService.downloadForm(id, patientId, request);

            // Set response headers for download
            response.setContentType(form.getMimeType());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + form.getFileName() + "\"");
            response.setContentLengthLong(form.getFileSize());

            // Copy file stream to response
            StreamUtils.copy(fileStream, response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            log.error("Error downloading form", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all versions of a form
     */
    @GetMapping("/forms/{id}/versions")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    public ResponseEntity<List<FormResourceResponseDto>> getFormVersions(@PathVariable Long id) {

        log.info("GET /api/v1/forms/{}/versions - Fetching form versions", id);

        List<FormResourceResponseDto> versions = formResourceService.getFormVersions(id);

        return ResponseEntity.ok(versions);
    }

    /**
     * Delete a form (ADMIN only)
     */
    @DeleteMapping("/admin/forms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteForm(@PathVariable Long id) {

        log.info("DELETE /api/v1/admin/forms/{} - Deleting form", id);

        formResourceService.deleteForm(id);

        return ResponseEntity.noContent().build();
    }
}
