package com.school.model;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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
}

