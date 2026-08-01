package com.school.dao.impl;

import com.school.model.Notification;
import java.util.List;

public interface NotificationDao {
    long insertNotification(Notification notification);
    List<Notification> getNotificationsForUser(long userId, int limit, int offset);
    List<Notification> getUnreadNotifications(long userId);
    boolean markAsRead(long notificationId);
    boolean markAllAsRead(long userId);
}