package com.school.service.interfaces;

import com.school.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {
    Notification createNotification(Notification notification);
    void createNotifications(List<Notification> notifications);
    void generateNotificationsForAnnouncement(com.school.model.Announcement announcement);
    void broadcastNotification(com.school.model.BroadcastNotificationRequest request);
    Notification getNotificationById(long notificationId);
    List<Notification> getNotificationsForUser(long userId, int limit, int offset);
    List<Notification> getUnreadNotifications(long userId);
    int getUnreadCount(long userId);
    void markAsRead(long notificationId, LocalDateTime readAt);
    void markAllAsRead(long userId);
    void deleteNotification(long notificationId);
    void deleteAllNotifications(long userId);
}
