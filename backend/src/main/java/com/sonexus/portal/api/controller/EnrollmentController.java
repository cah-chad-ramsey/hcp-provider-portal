package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.EnrollmentRequest;
import com.sonexus.portal.api.dto.EnrollmentResponse;
import com.sonexus.portal.infrastructure.persistence.entity.EnrollmentEntity.EnrollmentStatus;
import com.sonexus.portal.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollments", description = "Patient enrollment management")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/patients/{patientId}/enrollments")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT')")
    @Operation(summary = "Create or update enrollment", description = "Create new enrollment or update draft. Set submit=true to submit.")
    public ResponseEntity<EnrollmentResponse> createOrUpdateEnrollment(
            @PathVariable Long patientId,
            @Valid @RequestBody EnrollmentRequest request) {
        log.info("Creating/updating enrollment for patient: patientId={}, submit={}", patientId, request.getSubmit());
        EnrollmentResponse response = enrollmentService.createOrUpdateEnrollment(patientId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patients/{patientId}/enrollments")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get patient enrollments", description = "Get all enrollments for a patient")
    public ResponseEntity<List<EnrollmentResponse>> getPatientEnrollments(@PathVariable Long patientId) {
        log.info("Getting enrollments for patient: patientId={}", patientId);
        List<EnrollmentResponse> enrollments = enrollmentService.getPatientEnrollments(patientId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/enrollments/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get enrollment by ID", description = "Get enrollment details by ID")
    public ResponseEntity<EnrollmentResponse> getEnrollment(@PathVariable Long id) {
        log.info("Getting enrollment: id={}", id);
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollment);
    }

    @PatchMapping("/enrollments/{id}/status")
    @PreAuthorize("hasAnyRole('SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Update enrollment status", description = "Update enrollment status (Support Agent or Admin)")
    public ResponseEntity<EnrollmentResponse> updateEnrollmentStatus(
            @PathVariable Long id,
            @RequestParam EnrollmentStatus status,
            @RequestParam(required = false) String reason) {
        log.info("Updating enrollment status: id={}, status={}", id, status);
        EnrollmentResponse enrollment = enrollmentService.updateEnrollmentStatus(id, status, reason);
        return ResponseEntity.ok(enrollment);
    }
}
