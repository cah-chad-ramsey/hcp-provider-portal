package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.ProviderAffiliationRequest;
import com.sonexus.portal.api.dto.ProviderAffiliationResponse;
import com.sonexus.portal.api.dto.ProviderResponse;
import com.sonexus.portal.api.dto.VerifyAffiliationRequest;
import com.sonexus.portal.domain.ports.AuthProvider;
import com.sonexus.portal.infrastructure.persistence.entity.ProviderAffiliationEntity;
import com.sonexus.portal.infrastructure.persistence.entity.ProviderAffiliationEntity.AffiliationStatus;
import com.sonexus.portal.infrastructure.persistence.entity.ProviderEntity;
import com.sonexus.portal.infrastructure.persistence.entity.UserEntity;
import com.sonexus.portal.infrastructure.persistence.repository.ProviderAffiliationRepository;
import com.sonexus.portal.infrastructure.persistence.repository.ProviderRepository;
import com.sonexus.portal.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderAffiliationService {

    private final ProviderAffiliationRepository affiliationRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final AuthProvider authProvider;

    @Transactional
    public ProviderAffiliationResponse requestAffiliation(ProviderAffiliationRequest request) {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProviderEntity provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check if affiliation already exists
        if (affiliationRepository.findByUserIdAndProviderId(userId, request.getProviderId()).isPresent()) {
            throw new RuntimeException("Affiliation request already exists for this provider");
        }

        ProviderAffiliationEntity affiliation = ProviderAffiliationEntity.builder()
                .user(user)
                .provider(provider)
                .status(AffiliationStatus.PENDING)
                .build();

        ProviderAffiliationEntity saved = affiliationRepository.save(affiliation);
        log.info("Provider affiliation requested: user={}, provider={}", userId, request.getProviderId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProviderAffiliationResponse> getUserAffiliations() {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        List<ProviderAffiliationEntity> affiliations = affiliationRepository.findByUserId(userId);
        return affiliations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProviderAffiliationResponse> getPendingAffiliations() {
        List<ProviderAffiliationEntity> affiliations =
                affiliationRepository.findByStatus(AffiliationStatus.PENDING);
        return affiliations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProviderAffiliationResponse verifyAffiliation(Long affiliationId, VerifyAffiliationRequest request) {
        Long adminUserId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        UserEntity adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProviderAffiliationEntity affiliation = affiliationRepository.findById(affiliationId)
                .orElseThrow(() -> new RuntimeException("Affiliation not found"));

        if (affiliation.getStatus() != AffiliationStatus.PENDING) {
            throw new RuntimeException("Affiliation has already been processed");
        }

        affiliation.setStatus(request.getApproved() ? AffiliationStatus.APPROVED : AffiliationStatus.REJECTED);
        affiliation.setVerifiedAt(LocalDateTime.now());
        affiliation.setVerifiedBy(adminUser);
        affiliation.setVerificationReason(request.getReason());

        ProviderAffiliationEntity updated = affiliationRepository.save(affiliation);
        log.info("Provider affiliation verified: id={}, approved={}, adminUser={}",
                affiliationId, request.getApproved(), adminUserId);

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public boolean hasApprovedAffiliation(Long userId) {
        return !affiliationRepository.findByUserIdAndStatus(userId, AffiliationStatus.APPROVED).isEmpty();
    }

    private ProviderAffiliationResponse mapToResponse(ProviderAffiliationEntity entity) {
        ProviderResponse providerResponse = ProviderResponse.builder()
                .id(entity.getProvider().getId())
                .npi(entity.getProvider().getNpi())
                .name(entity.getProvider().getName())
                .specialty(entity.getProvider().getSpecialty())
                .addressLine1(entity.getProvider().getAddressLine1())
                .addressLine2(entity.getProvider().getAddressLine2())
                .city(entity.getProvider().getCity())
                .state(entity.getProvider().getState())
                .zipCode(entity.getProvider().getZipCode())
                .phone(entity.getProvider().getPhone())
                .fax(entity.getProvider().getFax())
                .email(entity.getProvider().getEmail())
                .active(entity.getProvider().getActive())
                .build();

        String userName = entity.getUser().getFirstName() + " " + entity.getUser().getLastName();
        String verifiedByEmail = entity.getVerifiedBy() != null ? entity.getVerifiedBy().getEmail() : null;

        return ProviderAffiliationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .userEmail(entity.getUser().getEmail())
                .userName(userName)
                .provider(providerResponse)
                .status(entity.getStatus().name())
                .requestedAt(entity.getRequestedAt())
                .verifiedAt(entity.getVerifiedAt())
                .verifiedByEmail(verifiedByEmail)
                .verificationReason(entity.getVerificationReason())
                .build();
    }
}
