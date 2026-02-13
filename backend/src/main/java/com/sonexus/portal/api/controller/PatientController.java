package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.PatientRequest;
import com.sonexus.portal.api.dto.PatientResponse;
import com.sonexus.portal.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Patients", description = "Patient management")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasRole('OFFICE_STAFF')")
    @Operation(summary = "Create patient", description = "Create a new patient (requires approved affiliation)")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        log.info("Creating patient: {} {}", request.getFirstName(), request.getLastName());
        PatientResponse response = patientService.createPatient(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Search patients", description = "Search patients by name or reference ID")
    public ResponseEntity<Page<PatientResponse>> searchPatients(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Searching patients: search={}", search);
        Page<PatientResponse> patients = patientService.searchPatients(search, pageable);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get patient by ID", description = "Get patient details by ID")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable Long id) {
        log.info("Getting patient: id={}", id);
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }
}
