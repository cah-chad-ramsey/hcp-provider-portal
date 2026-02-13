package com.sonexus.portal.api.controller;

import com.sonexus.portal.api.dto.ProgramResponse;
import com.sonexus.portal.api.dto.SupportServiceResponse;
import com.sonexus.portal.service.ProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Programs", description = "Program and support services management")
public class ProgramController {

    private final ProgramService programService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get all programs", description = "Get all active programs")
    public ResponseEntity<List<ProgramResponse>> getAllPrograms() {
        log.info("Getting all active programs");
        List<ProgramResponse> programs = programService.getAllActivePrograms();
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get program by ID", description = "Get program details by ID")
    public ResponseEntity<ProgramResponse> getProgram(@PathVariable Long id) {
        log.info("Getting program: id={}", id);
        ProgramResponse program = programService.getProgramById(id);
        return ResponseEntity.ok(program);
    }

    @GetMapping("/{id}/services")
    @PreAuthorize("hasAnyRole('OFFICE_STAFF', 'SUPPORT_AGENT', 'ADMIN')")
    @Operation(summary = "Get program services", description = "Get all services for a program")
    public ResponseEntity<List<SupportServiceResponse>> getProgramServices(@PathVariable Long id) {
        log.info("Getting services for program: id={}", id);
        List<SupportServiceResponse> services = programService.getProgramServices(id);
        return ResponseEntity.ok(services);
    }
}
