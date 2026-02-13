package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.*;
import com.sonexus.portal.service.AuditService;
import com.sonexus.portal.service.ProgramService;
import com.sonexus.portal.service.ProviderAffiliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "Administrative operations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ProviderAffiliationService affiliationService;
    private final ProgramService programService;
    private final AuditService auditService;

    @GetMapping("/providers/affiliations")
    @Operation(summary = "Get pending affiliations", description = "Get all pending provider affiliation requests")
    public ResponseEntity<List<ProviderAffiliationResponse>> getPendingAffiliations() {
        log.info("Getting pending provider affiliations");
        List<ProviderAffiliationResponse> affiliations = affiliationService.getPendingAffiliations();
        return ResponseEntity.ok(affiliations);
    }

    @PostMapping("/providers/affiliations/{id}/verify")
    @Operation(summary = "Verify affiliation", description = "Approve or reject a provider affiliation request")
    public ResponseEntity<ProviderAffiliationResponse> verifyAffiliation(
            @PathVariable Long id,
            @Valid @RequestBody VerifyAffiliationRequest request) {
        log.info("Verifying provider affiliation: id={}, approved={}", id, request.getApproved());
        ProviderAffiliationResponse response = affiliationService.verifyAffiliation(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/programs/{id}/services")
    @Operation(summary = "Add service to program", description = "Add a new support service to a program")
    public ResponseEntity<SupportServiceResponse> addServiceToProgram(
            @PathVariable Long id,
            @Valid @RequestBody AddServiceRequest request) {
        log.info("Adding service to program: programId={}, serviceName={}", id, request.getName());
        SupportServiceResponse response = programService.addServiceToProgram(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/audit")
    @Operation(summary = "Get audit logs", description = "Get audit logs with filtering")
    public ResponseEntity<Page<AuditEventResponse>> getAuditLogs(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String correlationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        log.info("Getting audit logs: eventType={}, userId={}, action={}", eventType, userId, action);
        Page<AuditEventResponse> auditLogs = auditService.getAuditEvents(
                eventType, userId, action, correlationId, startDate, endDate, pageable);
        return ResponseEntity.ok(auditLogs);
    }
}
