package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.PatientRequest;
import com.sonexus.portal.api.dto.PatientResponse;
import com.sonexus.portal.domain.ports.AuthProvider;
import com.sonexus.portal.infrastructure.persistence.entity.PatientEntity;
import com.sonexus.portal.infrastructure.persistence.entity.UserEntity;
import com.sonexus.portal.infrastructure.persistence.repository.PatientRepository;
import com.sonexus.portal.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final ProviderAffiliationService affiliationService;
    private final AuthProvider authProvider;
    private final AuditService auditService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        // Validate user has approved affiliation
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        if (!affiliationService.hasApprovedAffiliation(userId)) {
            throw new RuntimeException("User must have an approved provider affiliation before creating patients");
        }

        UserEntity createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate reference ID
        String referenceId = generateReferenceId();

        PatientEntity patient = PatientEntity.builder()
                .referenceId(referenceId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .createdBy(createdBy)
                .build();

        PatientEntity saved = patientRepository.save(patient);
        log.info("Patient created: id={}, referenceId={}, createdBy={}", saved.getId(), saved.getReferenceId(), userId);

        auditService.logEvent("PATIENT_CREATED", "PATIENT", saved.getId(), "CREATE");

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> searchPatients(String search, Pageable pageable) {
        // Validate affiliation
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        if (!affiliationService.hasApprovedAffiliation(userId)) {
            throw new RuntimeException("User must have an approved provider affiliation to view patients");
        }

        Page<PatientEntity> patients;
        if (search == null || search.isBlank()) {
            patients = patientRepository.findAll(pageable);
        } else {
            patients = patientRepository.searchPatients(search, pageable);
        }

        return patients.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        // Validate affiliation
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        if (!affiliationService.hasApprovedAffiliation(userId)) {
            throw new RuntimeException("User must have an approved provider affiliation to view patients");
        }

        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return mapToResponse(patient);
    }

    private String generateReferenceId() {
        Long nextVal = jdbcTemplate.queryForObject("SELECT nextval('patient_reference_seq')", Long.class);
        return "PT" + String.format("%06d", nextVal);
    }

    private PatientResponse mapToResponse(PatientEntity entity) {
        return PatientResponse.builder()
                .id(entity.getId())
                .referenceId(entity.getReferenceId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .createdById(entity.getCreatedBy().getId())
                .createdByEmail(entity.getCreatedBy().getEmail())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
