package com.sonexus.portal.domain.ports;

/**
 * Port for sending notifications.
 * Implementations: NoOpNotificationAdapter (local), EmailNotificationAdapter (prod)
 */
public interface NotificationPort {

    /**
     * Send email notification
     */
    void sendEmail(String to, String subject, String body);

    /**
     * Send email notification with HTML content
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);

    /**
     * Send notification about enrollment status change
     */
    void notifyEnrollmentStatusChange(String userEmail, String patientName, String newStatus);

    /**
     * Send notification about provider affiliation approval
     */
    void notifyProviderAffiliationApproved(String userEmail, String providerName);
}
