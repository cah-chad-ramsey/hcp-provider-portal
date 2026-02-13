package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.ProviderAffiliationRequest;
import com.sonexus.portal.api.dto.ProviderAffiliationResponse;
import com.sonexus.portal.api.dto.ProviderResponse;
import com.sonexus.portal.service.ProviderAffiliationService;
import com.sonexus.portal.service.ProviderService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Providers", description = "Provider and affiliation management")
public class ProviderController {

    private final ProviderService providerService;
    private final ProviderAffiliationService affiliationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Search providers", description = "Search for active providers by name")
    public ResponseEntity<Page<ProviderResponse>> searchProviders(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Searching providers: search={}", search);
        Page<ProviderResponse> providers = providerService.searchProviders(search, pageable);
        return ResponseEntity.ok(providers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get provider by ID", description = "Get provider details by ID")
    public ResponseEntity<ProviderResponse> getProvider(@PathVariable Long id) {
        log.info("Getting provider: id={}", id);
        ProviderResponse provider = providerService.getProviderById(id);
        return ResponseEntity.ok(provider);
    }

    @PostMapping("/associate")
    @PreAuthorize("hasRole('OFFICE_STAFF')")
    @Operation(summary = "Request provider affiliation", description = "Office staff can request affiliation with a provider")
    public ResponseEntity<ProviderAffiliationResponse> requestAffiliation(
            @Valid @RequestBody ProviderAffiliationRequest request) {
        log.info("Requesting provider affiliation: providerId={}", request.getProviderId());
        ProviderAffiliationResponse response = affiliationService.requestAffiliation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/affiliations")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT')")
    @Operation(summary = "Get user affiliations", description = "Get current user's provider affiliations")
    public ResponseEntity<List<ProviderAffiliationResponse>> getUserAffiliations() {
        log.info("Getting user affiliations");
        List<ProviderAffiliationResponse> affiliations = affiliationService.getUserAffiliations();
        return ResponseEntity.ok(affiliations);
    }
}
