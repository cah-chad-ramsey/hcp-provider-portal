package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.EnrollmentRequest;
import com.sonexus.portal.api.dto.EnrollmentResponse;
import com.sonexus.portal.api.dto.ProgramResponse;
import com.sonexus.portal.api.dto.ProviderResponse;
import com.sonexus.portal.domain.ports.AuthProvider;
import com.sonexus.portal.infrastructure.persistence.entity.*;
import com.sonexus.portal.infrastructure.persistence.entity.EnrollmentEntity.EnrollmentStatus;
import com.sonexus.portal.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final PatientRepository patientRepository;
    private final ProgramRepository programRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final EnrollmentStatusHistoryRepository statusHistoryRepository;
    private final AuthProvider authProvider;
    private final AuditService auditService;

    @Transactional
    public EnrollmentResponse createOrUpdateEnrollment(Long patientId, EnrollmentRequest request) {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        ProgramEntity program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new RuntimeException("Program not found"));

        UserEntity createdBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProviderEntity prescriber = null;
        if (request.getPrescriberId() != null) {
            prescriber = providerRepository.findById(request.getPrescriberId())
                    .orElseThrow(() -> new RuntimeException("Prescriber not found"));
        }

        // Check for existing draft enrollment
        List<EnrollmentEntity> existingDrafts = enrollmentRepository.findByPatientIdAndStatus(
                patientId, EnrollmentStatus.DRAFT);

        EnrollmentEntity enrollment;
        boolean isNew = existingDrafts.isEmpty();

        if (isNew) {
            enrollment = EnrollmentEntity.builder()
                    .patient(patient)
                    .program(program)
                    .prescriber(prescriber)
                    .status(request.getSubmit() ? EnrollmentStatus.SUBMITTED : EnrollmentStatus.DRAFT)
                    .diagnosisCode(request.getDiagnosisCode())
                    .diagnosisDescription(request.getDiagnosisDescription())
                    .medicationName(request.getMedicationName())
                    .notes(request.getNotes())
                    .createdBy(createdBy)
                    .build();
        } else {
            enrollment = existingDrafts.get(0);
            enrollment.setProgram(program);
            enrollment.setPrescriber(prescriber);
            enrollment.setDiagnosisCode(request.getDiagnosisCode());
            enrollment.setDiagnosisDescription(request.getDiagnosisDescription());
            enrollment.setMedicationName(request.getMedicationName());
            enrollment.setNotes(request.getNotes());

            if (request.getSubmit()) {
                enrollment.setStatus(EnrollmentStatus.SUBMITTED);
                enrollment.setSubmittedAt(LocalDateTime.now());
            }
        }

        if (request.getSubmit()) {
            enrollment.setSubmittedAt(LocalDateTime.now());
        }

        EnrollmentEntity saved = enrollmentRepository.save(enrollment);

        // Record status history if submitted
        if (request.getSubmit()) {
            recordStatusChange(saved, null, EnrollmentStatus.SUBMITTED, "Initial submission", createdBy);

            // Audit log for submission
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("patientId", patientId);
            metadata.put("programId", request.getProgramId());
            auditService.logEvent("ENROLLMENT_SUBMITTED", "ENROLLMENT", saved.getId(), "SUBMIT", null, metadata);

            log.info("Enrollment submitted: id={}, patient={}, program={}", saved.getId(), patientId, request.getProgramId());
        } else {
            log.info("Enrollment saved as draft: id={}, patient={}, program={}", saved.getId(), patientId, request.getProgramId());
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getPatientEnrollments(Long patientId) {
        List<EnrollmentEntity> enrollments = enrollmentRepository.findByPatientId(patientId);
        return enrollments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Long id) {
        EnrollmentEntity enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));
        return mapToResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse updateEnrollmentStatus(Long id, EnrollmentStatus newStatus, String reason) {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        UserEntity changedBy = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        EnrollmentEntity enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        EnrollmentStatus oldStatus = enrollment.getStatus();
        enrollment.setStatus(newStatus);

        EnrollmentEntity updated = enrollmentRepository.save(enrollment);
        recordStatusChange(updated, oldStatus, newStatus, reason, changedBy);

        log.info("Enrollment status updated: id={}, from={}, to={}, by={}", id, oldStatus, newStatus, userId);

        return mapToResponse(updated);
    }

    private void recordStatusChange(EnrollmentEntity enrollment, EnrollmentStatus fromStatus,
                                     EnrollmentStatus toStatus, String reason, UserEntity changedBy) {
        EnrollmentStatusHistoryEntity history = EnrollmentStatusHistoryEntity.builder()
                .enrollment(enrollment)
                .fromStatus(fromStatus != null ? fromStatus.name() : null)
                .toStatus(toStatus.name())
                .reason(reason)
                .changedBy(changedBy)
                .build();

        statusHistoryRepository.save(history);
    }

    private EnrollmentResponse mapToResponse(EnrollmentEntity entity) {
        ProgramResponse programResponse = ProgramResponse.builder()
                .id(entity.getProgram().getId())
                .name(entity.getProgram().getName())
                .description(entity.getProgram().getDescription())
                .active(entity.getProgram().getActive())
                .build();

        ProviderResponse prescriberResponse = null;
        if (entity.getPrescriber() != null) {
            prescriberResponse = ProviderResponse.builder()
                    .id(entity.getPrescriber().getId())
                    .npi(entity.getPrescriber().getNpi())
                    .name(entity.getPrescriber().getName())
                    .specialty(entity.getPrescriber().getSpecialty())
                    .build();
        }

        String patientName = entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName();

        return EnrollmentResponse.builder()
                .id(entity.getId())
                .patientId(entity.getPatient().getId())
                .patientName(patientName)
                .program(programResponse)
                .prescriber(prescriberResponse)
                .status(entity.getStatus().name())
                .diagnosisCode(entity.getDiagnosisCode())
                .diagnosisDescription(entity.getDiagnosisDescription())
                .medicationName(entity.getMedicationName())
                .notes(entity.getNotes())
                .createdById(entity.getCreatedBy().getId())
                .createdByEmail(entity.getCreatedBy().getEmail())
                .submittedAt(entity.getSubmittedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
