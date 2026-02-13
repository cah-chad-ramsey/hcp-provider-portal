package com.sonexus.portal.service;

import com.sonexus.portal.api.dto.NextActionResponse;
import com.sonexus.portal.domain.ports.AuthProvider;
import com.sonexus.portal.infrastructure.persistence.entity.*;
import com.sonexus.portal.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final AuthProvider authProvider;
    private final EnrollmentRepository enrollmentRepository;
    private final BenefitsInvestigationRepository benefitsInvestigationRepository;
    private final PatientRepository patientRepository;
    private final PatientServiceEnrollmentRepository patientServiceEnrollmentRepository;
    private final SecureMessageThreadRepository messageThreadRepository;

    @Transactional(readOnly = true)
    public List<NextActionResponse> getNextActions() {
        Long userId = authProvider.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"))
                .getId();

        List<NextActionResponse> actions = new ArrayList<>();

        // Rule 1: Check for enrollments submitted > 7 days ago without status update
        actions.addAll(checkStaleEnrollments());

        // Rule 2: Check for expired benefits investigations
        actions.addAll(checkExpiredBenefits());

        // Rule 3: Check for patients with no active services
        actions.addAll(checkPatientsWithoutServices());

        // Rule 4: Check for unread messages
        actions.addAll(checkUnreadMessages(userId));

        // Sort by priority and days overdue
        actions.sort((a, b) -> {
            int priorityCompare = getPriorityValue(a.getPriority()) - getPriorityValue(b.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            return Integer.compare(
                    b.getDaysOverdue() != null ? b.getDaysOverdue() : 0,
                    a.getDaysOverdue() != null ? a.getDaysOverdue() : 0
            );
        });

        log.info("Generated {} next actions for user {}", actions.size(), userId);
        return actions;
    }

    private List<NextActionResponse> checkStaleEnrollments() {
        List<NextActionResponse> actions = new ArrayList<>();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<EnrollmentEntity> staleEnrollments = enrollmentRepository.findAll().stream()
                .filter(e -> "SUBMITTED".equals(e.getStatus()) &&
                        e.getSubmittedAt() != null &&
                        e.getSubmittedAt().isBefore(sevenDaysAgo))
                .toList();

        for (EnrollmentEntity enrollment : staleEnrollments) {
            long daysOverdue = ChronoUnit.DAYS.between(enrollment.getSubmittedAt(), LocalDateTime.now());

            actions.add(NextActionResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Follow up on enrollment")
                    .description("Enrollment for " + enrollment.getPatient().getFirstName() + " " +
                            enrollment.getPatient().getLastName() + " has been pending for " + daysOverdue + " days")
                    .actionType("ENROLLMENT")
                    .priority(daysOverdue > 14 ? "HIGH" : "MEDIUM")
                    .resourceId(enrollment.getId())
                    .resourceName(enrollment.getPatient().getFirstName() + " " + enrollment.getPatient().getLastName())
                    .actionUrl("/patients/" + enrollment.getPatient().getId())
                    .icon("schedule")
                    .daysOverdue((int) daysOverdue)
                    .build());
        }

        return actions;
    }

    private List<NextActionResponse> checkExpiredBenefits() {
        List<NextActionResponse> actions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        List<BenefitsInvestigationEntity> expiredBenefits = benefitsInvestigationRepository.findAll().stream()
                .filter(bi -> bi.getExpiresAt() != null && bi.getExpiresAt().isBefore(now))
                .toList();

        for (BenefitsInvestigationEntity bi : expiredBenefits) {
            long daysExpired = ChronoUnit.DAYS.between(bi.getExpiresAt(), now);

            actions.add(NextActionResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Re-run benefits investigation")
                    .description("Benefits investigation for " + bi.getPatient().getFirstName() + " " +
                            bi.getPatient().getLastName() + " expired " + daysExpired + " days ago")
                    .actionType("BENEFITS")
                    .priority(daysExpired > 30 ? "HIGH" : "MEDIUM")
                    .resourceId(bi.getPatient().getId())
                    .resourceName(bi.getPatient().getFirstName() + " " + bi.getPatient().getLastName())
                    .actionUrl("/patients/" + bi.getPatient().getId())
                    .icon("update")
                    .daysOverdue((int) daysExpired)
                    .build());
        }

        return actions;
    }

    private List<NextActionResponse> checkPatientsWithoutServices() {
        List<NextActionResponse> actions = new ArrayList<>();

        List<PatientEntity> allPatients = patientRepository.findAll();

        for (PatientEntity patient : allPatients) {
            List<PatientServiceEnrollmentEntity> services =
                    patientServiceEnrollmentRepository.findByPatientId(patient.getId());

            boolean hasActiveServices = services.stream()
                    .anyMatch(s -> "ACTIVE".equals(s.getStatus()));

            if (!hasActiveServices) {
                actions.add(NextActionResponse.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Enroll in support services")
                        .description(patient.getFirstName() + " " + patient.getLastName() +
                                " is not enrolled in any support services")
                        .actionType("SERVICE")
                        .priority("LOW")
                        .resourceId(patient.getId())
                        .resourceName(patient.getFirstName() + " " + patient.getLastName())
                        .actionUrl("/patients/" + patient.getId())
                        .icon("add_circle")
                        .daysOverdue(null)
                        .build());
            }
        }

        return actions;
    }

    private List<NextActionResponse> checkUnreadMessages(Long userId) {
        List<NextActionResponse> actions = new ArrayList<>();

        // Get threads where user has unread messages
        List<SecureMessageThreadEntity> threadsWithUnread = messageThreadRepository.findAll().stream()
                .filter(thread -> {
                    long unreadCount = thread.getMessages().stream()
                            .filter(m -> m.getReadAt() == null && !m.getSentBy().getId().equals(userId))
                            .count();
                    return unreadCount > 0;
                })
                .toList();

        if (!threadsWithUnread.isEmpty()) {
            actions.add(NextActionResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Review unread messages")
                    .description("You have " + threadsWithUnread.size() + " message thread(s) with unread messages")
                    .actionType("MESSAGE")
                    .priority("MEDIUM")
                    .resourceId(null)
                    .resourceName(null)
                    .actionUrl("/messages")
                    .icon("mail")
                    .daysOverdue(null)
                    .build());
        }

        return actions;
    }

    private int getPriorityValue(String priority) {
        return switch (priority) {
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            case "LOW" -> 3;
            default -> 4;
        };
    }
}
