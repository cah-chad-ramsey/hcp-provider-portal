package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.*;
import com.sonexus.portal.service.SecureMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Secure Messaging", description = "Secure message management")
public class SecureMessageController {

    private final SecureMessageService messageService;

    @GetMapping("/threads")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get message threads", description = "Get all message threads for current user")
    public ResponseEntity<Page<MessageThreadResponse>> getThreads(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting message threads");
        Page<MessageThreadResponse> threads = messageService.getThreads(pageable);
        return ResponseEntity.ok(threads);
    }

    @PostMapping("/threads")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Create thread", description = "Create a new message thread")
    public ResponseEntity<MessageThreadResponse> createThread(
            @Valid @RequestBody CreateThreadRequest request) {
        log.info("Creating message thread: subject={}", request.getSubject());
        MessageThreadResponse response = messageService.createThread(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/threads/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get thread by ID", description = "Get thread details with all messages")
    public ResponseEntity<MessageThreadResponse> getThread(@PathVariable Long id) {
        log.info("Getting thread: id={}", id);
        MessageThreadResponse thread = messageService.getThreadById(id);
        return ResponseEntity.ok(thread);
    }

    @PostMapping("/threads/{id}/messages")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Send message", description = "Send a message in a thread")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody SendMessageRequest request) {
        log.info("Sending message in thread: threadId={}", id);
        MessageResponse response = messageService.sendMessage(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attachments")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Upload attachment", description = "Upload an attachment for a message")
    public ResponseEntity<MessageAttachmentResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Uploading attachment: fileName={}", file.getOriginalFilename());
        MessageAttachmentResponse response = messageService.uploadAttachment(file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attachments/{id}/download")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Download attachment", description = "Download an attachment")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) {
        log.info("Downloading attachment: id={}", id);
        byte[] fileContent = messageService.downloadAttachment(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileContent);
    }
}
