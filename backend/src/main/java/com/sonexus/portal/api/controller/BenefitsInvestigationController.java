package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.BenefitsInvestigationRequestDto;
import com.sonexus.portal.api.dto.BenefitsInvestigationResponseDto;
import com.sonexus.portal.service.BenefitsInvestigationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BenefitsInvestigationController {

    private final BenefitsInvestigationService benefitsInvestigationService;

    /**
     * Run a new benefits investigation for a patient
     */
    @PostMapping("/patients/{patientId}/benefits-investigation")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    public ResponseEntity<BenefitsInvestigationResponseDto> runInvestigation(
            @PathVariable Long patientId,
            @Valid @RequestBody BenefitsInvestigationRequestDto request) {

        log.info("POST /api/v1/patients/{}/benefits-investigation - Running {} investigation",
                patientId, request.getInvestigationType());

        BenefitsInvestigationResponseDto response = benefitsInvestigationService.runInvestigation(patientId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all benefits investigations for a patient
     */
    @GetMapping("/patients/{patientId}/benefits-investigation")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    public ResponseEntity<List<BenefitsInvestigationResponseDto>> getPatientInvestigations(
            @PathVariable Long patientId) {

        log.info("GET /api/v1/patients/{}/benefits-investigation - Fetching all investigations", patientId);

        List<BenefitsInvestigationResponseDto> investigations =
                benefitsInvestigationService.getPatientInvestigations(patientId);

        return ResponseEntity.ok(investigations);
    }

    /**
     * Get the latest benefits investigation for a patient by type
     */
    @GetMapping("/patients/{patientId}/benefits-investigation/latest")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    public ResponseEntity<BenefitsInvestigationResponseDto> getLatestInvestigation(
            @PathVariable Long patientId,
            @RequestParam String investigationType) {

        log.info("GET /api/v1/patients/{}/benefits-investigation/latest?investigationType={} - Fetching latest",
                patientId, investigationType);

        BenefitsInvestigationResponseDto investigation =
                benefitsInvestigationService.getLatestInvestigation(patientId, investigationType);

        return ResponseEntity.ok(investigation);
    }

    /**
     * Get a specific benefits investigation by ID
     */
    @GetMapping("/benefits-investigation/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    public ResponseEntity<BenefitsInvestigationResponseDto> getInvestigationById(@PathVariable Long id) {

        log.info("GET /api/v1/benefits-investigation/{} - Fetching investigation", id);

        BenefitsInvestigationResponseDto investigation = benefitsInvestigationService.getInvestigationById(id);

        return ResponseEntity.ok(investigation);
    }
}
