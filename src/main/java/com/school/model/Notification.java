package com.school.model;

import java.time.LocalDateTime;

public class Notification {
    public enum NotificationType { NEW_ASSIGNMENT, NEW_MESSAGE, RESULT_PUBLISHED, ATTENDANCE_ALERT, NEW_ANNOUNCEMENT }
    private long notificationId;
    private long userId;
    private NotificationType notificationType;
    private String title;
    private String message;
    private String referenceTable;
    private Long referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public Notification() {}
    public Notification(long notificationId, long userId, NotificationType notificationType, String title, String message, String referenceTable, Long referenceId, boolean isRead, LocalDateTime createdAt, LocalDateTime readAt) {
        this.notificationId = notificationId; this.userId = userId; this.notificationType = notificationType; this.title = title; this.message = message; this.referenceTable = referenceTable; this.referenceId = referenceId; this.isRead = isRead; this.createdAt = createdAt; this.readAt = readAt;
    }

    public long getNotificationId() { return notificationId; }
    public void setNotificationId(long notificationId) { this.notificationId = notificationId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getReferenceTable() { return referenceTable; }
    public void setReferenceTable(String referenceTable) { this.referenceTable = referenceTable; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}

