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
    public void generateNotificationsForAnnouncement(com.school.model.Announcement announcement) {
        java.util.List<Long> targetUserIds = new java.util.ArrayList<>();
        com.school.dao.interfaces.UserDao userDao = new com.school.dao.impl.UserDaoImpl();
        com.school.dao.interfaces.StudentDao studentDao = new com.school.dao.impl.StudentDaoImpl();
        com.school.dao.interfaces.AcademicYearDao academicYearDao = new com.school.dao.impl.AcademicYearDaoImpl();

        if (announcement.getTargetRoleId() != null) {
            java.util.List<com.school.model.User> users = userDao.getUsersByRole(announcement.getTargetRoleId());
            for (com.school.model.User u : users) {
                targetUserIds.add(u.getUserId());
            }
        } else if (announcement.getSectionId() != null) {
            java.util.List<com.school.model.Student> students = studentDao.getAllStudentsBySection(announcement.getSectionId());
            for (com.school.model.Student s : students) {
                targetUserIds.add(s.getUserId());
            }
        } else if (announcement.getClassId() != null) {
            com.school.model.AcademicYear activeYear = academicYearDao.getCurrentAcademicYear();
            if (activeYear != null) {
                java.util.List<com.school.model.Student> students = studentDao.getAllStudentsByClass(announcement.getClassId(), activeYear.getAcademicYearId());
                for (com.school.model.Student s : students) {
                    targetUserIds.add(s.getUserId());
                }
            }
        } else {
            java.util.List<com.school.model.User> users = userDao.getAllUsers();
            for (com.school.model.User u : users) {
                targetUserIds.add(u.getUserId());
            }
        }

        if (targetUserIds.isEmpty()) return;

        java.util.List<Notification> notifications = new java.util.ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Long userId : targetUserIds) {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setNotificationType(Notification.NotificationType.NEW_ANNOUNCEMENT);
            n.setTitle("New Announcement: " + announcement.getTitle());
            n.setMessage(announcement.getContent());
            n.setReferenceTable("announcements");
            n.setReferenceId(announcement.getAnnouncementId());
            n.setRead(false);
            n.setCreatedAt(now);
            notifications.add(n);
        }
        
        // Use batch insert logic for better performance since there can be many notifications
        if (!notificationDao.insertNotifications(notifications))
            throw new IllegalStateException("Failed to create notifications for announcement.");
    }

    @Override
    public void broadcastNotification(com.school.model.BroadcastNotificationRequest request) {
        if (request == null || request.getNotification() == null) {
            throw new ValidationException("Broadcast request and notification cannot be null.");
        }
        
        java.util.Set<Long> targetUserIds = new java.util.HashSet<>();
        com.school.dao.interfaces.UserDao userDao = new com.school.dao.impl.UserDaoImpl();

        if (request.isGlobal()) {
            java.util.List<com.school.model.User> allUsers = userDao.getAllUsers();
            for (com.school.model.User u : allUsers) {
                targetUserIds.add(u.getUserId());
            }
        } else if (request.getTargetRoleIds() != null && !request.getTargetRoleIds().isEmpty()) {
            for (Integer roleId : request.getTargetRoleIds()) {
                java.util.List<com.school.model.User> users = userDao.getUsersByRole(roleId);
                for (com.school.model.User u : users) {
                    targetUserIds.add(u.getUserId());
                }
            }
        } else {
            throw new ValidationException("Broadcast must target global or at least one role.");
        }

        if (targetUserIds.isEmpty()) return;

        java.util.List<Notification> notifications = new java.util.ArrayList<>();
        Notification template = request.getNotification();
        LocalDateTime now = LocalDateTime.now();

        for (Long userId : targetUserIds) {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setNotificationType(template.getNotificationType());
            n.setTitle(template.getTitle());
            n.setMessage(template.getMessage());
            n.setReferenceTable(template.getReferenceTable());
            n.setReferenceId(template.getReferenceId());
            n.setRead(false);
            n.setCreatedAt(now);
            notifications.add(n);
        }

        if (!notificationDao.insertNotifications(notifications))
            throw new IllegalStateException("Failed to broadcast notifications.");
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
        if (!notificationDao.markAsRead(notificationId, readAt != null ? readAt : LocalDateTime.now()))
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
