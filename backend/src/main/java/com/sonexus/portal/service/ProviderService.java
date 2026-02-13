package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.ProviderResponse;
import com.sonexus.portal.infrastructure.persistence.entity.ProviderEntity;
import com.sonexus.portal.infrastructure.persistence.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProviderService {

    private final ProviderRepository providerRepository;

    public Page<ProviderResponse> searchProviders(String searchTerm, Pageable pageable) {
        Page<ProviderEntity> providers;

        if (searchTerm == null || searchTerm.isBlank()) {
            providers = providerRepository.findByActiveTrue(pageable);
        } else {
            // Search by name (could be expanded to search by NPI, specialty, etc.)
            providers = providerRepository.findByActiveTrueAndNameContainingIgnoreCase(
                searchTerm.trim(), pageable
            );
        }

        return providers.map(this::mapToResponse);
    }

    public ProviderResponse getProviderById(Long providerId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        return mapToResponse(provider);
    }

    private ProviderResponse mapToResponse(ProviderEntity entity) {
        return ProviderResponse.builder()
                .id(entity.getId())
                .npi(entity.getNpi())
                .name(entity.getName())
                .specialty(entity.getSpecialty())
                .addressLine1(entity.getAddressLine1())
                .addressLine2(entity.getAddressLine2())
                .city(entity.getCity())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .phone(entity.getPhone())
                .fax(entity.getFax())
                .email(entity.getEmail())
                .active(entity.getActive())
                .build();
    }
}
