package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.NextActionResponse;
import com.sonexus.portal.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard operations")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/next-actions")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get next actions", description = "Get recommended actions for the current user")
    public ResponseEntity<List<NextActionResponse>> getNextActions() {
        log.info("Getting next actions for dashboard");
        List<NextActionResponse> actions = dashboardService.getNextActions();
        return ResponseEntity.ok(actions);
    }
}
