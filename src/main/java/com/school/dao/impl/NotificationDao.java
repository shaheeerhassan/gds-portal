package com.school.dao.impl;

import com.school.model.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationDao {
    boolean insertNotification(Notification notification);
    boolean deleteNotification(long notificationId);
    boolean deleteAllNotifications(long userId);
    boolean insertNotifications(List<Notification> notifications);

    List<Notification> getNotificationsForUser(long userId, int limit, int offset);
    List<Notification> getUnreadNotifications(long userId);

    int getUnreadCount(long userId);

    Notification getNotificationById(long notificationId);

    boolean markAsRead(long notificationId, LocalDateTime readAt);
    boolean markAllAsRead(long userId);
}