package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.BenefitsInvestigationRequestDto;
import com.sonexus.portal.api.dto.BenefitsInvestigationResponseDto;
import com.sonexus.portal.domain.model.BenefitsInvestigationRequest;
import com.sonexus.portal.domain.model.BenefitsInvestigationResult;
import com.sonexus.portal.domain.ports.BenefitsInvestigationPort;
import com.sonexus.portal.infrastructure.persistence.entity.BenefitsInvestigationEntity;
import com.sonexus.portal.infrastructure.persistence.entity.PatientEntity;
import com.sonexus.portal.infrastructure.persistence.entity.ProgramEntity;
import com.sonexus.portal.infrastructure.persistence.entity.UserEntity;
import com.sonexus.portal.infrastructure.persistence.repository.BenefitsInvestigationRepository;
import com.sonexus.portal.infrastructure.persistence.repository.PatientRepository;
import com.sonexus.portal.infrastructure.persistence.repository.ProgramRepository;
import com.sonexus.portal.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BenefitsInvestigationService {

    private final BenefitsInvestigationPort benefitsInvestigationPort;
    private final BenefitsInvestigationRepository investigationRepository;
    private final PatientRepository patientRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final ProviderAffiliationService affiliationService;
    private final AuditService auditService;

    @Transactional
    public BenefitsInvestigationResponseDto runInvestigation(Long patientId, BenefitsInvestigationRequestDto requestDto) {
        log.info("Running benefits investigation for patient {}", patientId);

        // Verify user has approved affiliation
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!affiliationService.hasApprovedAffiliation(user.getId())) {
            throw new RuntimeException("User must have approved affiliation to run benefits investigation");
        }

        // Verify patient exists
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

        // Verify program exists
        ProgramEntity program = programRepository.findById(requestDto.getProgramId())
                .orElseThrow(() -> new RuntimeException("Program not found with id: " + requestDto.getProgramId()));

        // Build domain request
        BenefitsInvestigationRequest domainRequest = BenefitsInvestigationRequest.builder()
                .patientId(patientId)
                .programId(requestDto.getProgramId())
                .investigationType(requestDto.getInvestigationType())
                .payerName(requestDto.getPayerName())
                .payerPlanId(requestDto.getPayerPlanId())
                .memberId(requestDto.getMemberId())
                .patientState(requestDto.getPatientState())
                .patientDob(requestDto.getPatientDob())
                .medicationName(requestDto.getMedicationName())
                .build();

        // Call benefits investigation port (adapter)
        BenefitsInvestigationResult result;
        if ("MEDICAL".equalsIgnoreCase(requestDto.getInvestigationType())) {
            result = benefitsInvestigationPort.investigateMedicalCoverage(domainRequest);
        } else if ("PHARMACY".equalsIgnoreCase(requestDto.getInvestigationType())) {
            result = benefitsInvestigationPort.investigatePharmacyCoverage(domainRequest);
        } else {
            throw new RuntimeException("Invalid investigation type: " + requestDto.getInvestigationType());
        }

        // Save investigation to database
        BenefitsInvestigationEntity entity = BenefitsInvestigationEntity.builder()
                .patientId(patientId)
                .programId(requestDto.getProgramId())
                .investigationType(BenefitsInvestigationEntity.InvestigationType.valueOf(
                        requestDto.getInvestigationType().toUpperCase()))
                .payerName(requestDto.getPayerName())
                .payerPlanId(requestDto.getPayerPlanId())
                .memberId(requestDto.getMemberId())
                .patientState(requestDto.getPatientState())
                .medicationName(requestDto.getMedicationName())
                .coverageStatus(result.getCoverageStatus())
                .coverageType(result.getCoverageType())
                .priorAuthRequired(result.getPriorAuthRequired())
                .deductibleApplies(result.getDeductibleApplies())
                .specialtyPharmacyRequired(result.getSpecialtyPharmacyRequired())
                .notes(result.getNotes())
                .resultPayload(result.getAdditionalData())
                .createdById(user.getId())
                .build();

        BenefitsInvestigationEntity saved = investigationRepository.save(entity);

        // Audit log
        auditService.logEvent(
                "BENEFITS_INVESTIGATION_RUN",
                "BENEFITS_INVESTIGATION",
                saved.getId(),
                "CREATE"
        );

        log.info("Benefits investigation completed and saved with id {}", saved.getId());

        return mapToResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<BenefitsInvestigationResponseDto> getPatientInvestigations(Long patientId) {
        // Verify patient exists
        patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

        List<BenefitsInvestigationEntity> investigations = investigationRepository
                .findByPatientIdOrderByCreatedAtDesc(patientId);

        return investigations.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BenefitsInvestigationResponseDto getLatestInvestigation(Long patientId, String investigationType) {
        BenefitsInvestigationEntity.InvestigationType type =
                BenefitsInvestigationEntity.InvestigationType.valueOf(investigationType.toUpperCase());

        return investigationRepository.findFirstByPatientIdAndInvestigationTypeOrderByCreatedAtDesc(patientId, type)
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new RuntimeException(
                        "No " + investigationType + " investigation found for patient: " + patientId));
    }

    @Transactional(readOnly = true)
    public BenefitsInvestigationResponseDto getInvestigationById(Long id) {
        BenefitsInvestigationEntity investigation = investigationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Investigation not found with id: " + id));

        return mapToResponseDto(investigation);
    }

    private BenefitsInvestigationResponseDto mapToResponseDto(BenefitsInvestigationEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        boolean isExpired = entity.getExpiresAt() != null && entity.getExpiresAt().isBefore(now);

        return BenefitsInvestigationResponseDto.builder()
                .id(entity.getId())
                .patientId(entity.getPatientId())
                .patientReferenceId(entity.getPatient() != null ? entity.getPatient().getReferenceId() : null)
                .patientName(entity.getPatient() != null ?
                        entity.getPatient().getFirstName() + " " + entity.getPatient().getLastName() : null)
                .programId(entity.getProgramId())
                .programName(entity.getProgram() != null ? entity.getProgram().getName() : null)
                .investigationType(entity.getInvestigationType().name())
                .payerName(entity.getPayerName())
                .payerPlanId(entity.getPayerPlanId())
                .memberId(entity.getMemberId())
                .patientState(entity.getPatientState())
                .medicationName(entity.getMedicationName())
                .coverageStatus(entity.getCoverageStatus())
                .coverageType(entity.getCoverageType())
                .priorAuthRequired(entity.getPriorAuthRequired())
                .deductibleApplies(entity.getDeductibleApplies())
                .specialtyPharmacyRequired(entity.getSpecialtyPharmacyRequired())
                .copayAmount(entity.getCopayAmount())
                .coinsurancePercentage(entity.getCoinsurancePercentage())
                .notes(entity.getNotes())
                .resultPayload(entity.getResultPayload())
                .expiresAt(entity.getExpiresAt())
                .createdById(entity.getCreatedById())
                .createdByEmail(entity.getCreatedBy() != null ? entity.getCreatedBy().getEmail() : null)
                .createdAt(entity.getCreatedAt())
                .isExpired(isExpired)
                .build();
    }
}
