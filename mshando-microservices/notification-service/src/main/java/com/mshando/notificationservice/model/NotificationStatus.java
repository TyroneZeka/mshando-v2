package com.mshando.notificationservice.model;

/**
 * Enumeration for notification status.
 * 
 * @author Mshando Team
 * @version 1.0.0
 */
public enum NotificationStatus {
    PENDING,
    SCHEDULED,
    SENT,
    DELIVERED,
    FAILED,
    BOUNCED,
    CANCELLED
}
