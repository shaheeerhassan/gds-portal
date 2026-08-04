package com.school.service.impl;

import com.school.dao.impl.NotificationDaoImpl;
import com.school.dao.interfaces.NotificationDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Notification;
import com.school.service.interfaces.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class NotificationServiceImpl implements NotificationService {

    private final NotificationDao notificationDao;

    public NotificationServiceImpl() {
        notificationDao = new NotificationDaoImpl();
    }

    @Override
    public Notification createNotification(Notification notification) {
        validateNotification(notification);

        if (!notificationDao.insertNotification(notification))
            throw new IllegalStateException("Failed to create notification.");

        return notification;
    }

    @Override
    public void createNotifications(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty())
            throw new ValidationException("At least one notification is required.");

        for (Notification notification : notifications)
            validateNotification(notification);

        if (!notificationDao.insertNotifications(notifications))
            throw new IllegalStateException("Failed to create notifications.");
    }

    @Override
    public Notification getNotificationById(long notificationId) {
        validateId(notificationId);
        Notification notification = notificationDao.getNotificationById(notificationId);
        if (notification == null)
            throw new ResourceNotFoundException("Notification not found.");
        return notification;
    }

    @Override
    public List<Notification> getNotificationsForUser(long userId, int limit, int offset) {
        validateId(userId);
        if (limit <= 0)
            throw new ValidationException("Limit must be a positive number.");
        if (offset < 0)
            throw new ValidationException("Offset cannot be negative.");
        return notificationDao.getNotificationsForUser(userId, limit, offset);
    }

    @Override
    public List<Notification> getUnreadNotifications(long userId) {
        validateId(userId);
        return notificationDao.getUnreadNotifications(userId);
    }

    @Override
    public int getUnreadCount(long userId) {
        validateId(userId);
        return notificationDao.getUnreadCount(userId);
    }

    @Override
    public void markAsRead(long notificationId, LocalDateTime readAt) {
        validateId(notificationId);
        if (!notificationDao.markAsRead(notificationId, readAt))
            throw new ResourceNotFoundException("Notification not found.");
    }

    @Override
    public void markAllAsRead(long userId) {
        validateId(userId);
        notificationDao.markAllAsRead(userId);
    }

    @Override
    public void deleteNotification(long notificationId) {
        validateId(notificationId);
        if (!notificationDao.deleteNotification(notificationId))
            throw new ResourceNotFoundException("Notification not found.");
    }

    @Override
    public void deleteAllNotifications(long userId) {
        validateId(userId);
        notificationDao.deleteAllNotifications(userId);
    }

    private void validateNotification(Notification notification) {
        validateId(notification.getUserId());
        if (notification.getNotificationType() == null)
            throw new ValidationException("Notification type is required.");
        notification.setTitle(validateRequired(notification.getTitle(), "Title"));
        notification.setMessage(validateRequired(notification.getMessage(), "Message"));
    }
}
