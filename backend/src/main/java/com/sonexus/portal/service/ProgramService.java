package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.AddServiceRequest;
import com.sonexus.portal.api.dto.ProgramResponse;
import com.sonexus.portal.api.dto.SupportServiceResponse;
import com.sonexus.portal.infrastructure.persistence.entity.ProgramEntity;
import com.sonexus.portal.infrastructure.persistence.entity.SupportServiceEntity;
import com.sonexus.portal.infrastructure.persistence.repository.ProgramRepository;
import com.sonexus.portal.infrastructure.persistence.repository.SupportServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProgramService {

    private final ProgramRepository programRepository;
    private final SupportServiceRepository supportServiceRepository;

    public List<ProgramResponse> getAllActivePrograms() {
        List<ProgramEntity> programs = programRepository.findByActiveTrue();
        return programs.stream()
                .map(this::mapToProgramResponse)
                .collect(Collectors.toList());
    }

    public ProgramResponse getProgramById(Long id) {
        ProgramEntity program = programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found"));
        return mapToProgramResponse(program);
    }

    public List<SupportServiceResponse> getProgramServices(Long programId) {
        List<SupportServiceEntity> services = supportServiceRepository.findByProgramIdAndActiveTrue(programId);
        return services.stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupportServiceResponse addServiceToProgram(Long programId, AddServiceRequest request) {
        ProgramEntity program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        SupportServiceEntity service = SupportServiceEntity.builder()
                .program(program)
                .name(request.getName())
                .description(request.getDescription())
                .serviceType(request.getServiceType())
                .active(true)
                .build();

        SupportServiceEntity saved = supportServiceRepository.save(service);
        log.info("Service added to program: serviceId={}, programId={}, name={}",
                saved.getId(), programId, request.getName());

        return mapToServiceResponse(saved);
    }

    private ProgramResponse mapToProgramResponse(ProgramEntity entity) {
        return ProgramResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.getActive())
                .build();
    }

    private SupportServiceResponse mapToServiceResponse(SupportServiceEntity entity) {
        return SupportServiceResponse.builder()
                .id(entity.getId())
                .programId(entity.getProgram().getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .serviceType(entity.getServiceType())
                .active(entity.getActive())
                .build();
    }
}
