package com.school.web.controller;

import com.school.exceptions.ValidationException;
import com.school.model.Notification;
import com.school.service.impl.NotificationServiceImpl;
import com.school.service.interfaces.NotificationService;
import com.school.web.auth.AuthContext;
import com.school.web.auth.RoleGuard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet(urlPatterns = "/api/notifications/*")
public class NotificationController extends BaseServlet {

    private static final String ROLE_ADMIN = "ADMINISTRATOR";
    private static final String ROLE_PRINCIPAL = "PRINCIPAL";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_PARENT = "PARENT";

    private final NotificationService notificationService;

    public NotificationController() {
        this.notificationService = new NotificationServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if (path.startsWith("/me/unread-count")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
            writeJson(resp, notificationService.getUnreadCount(AuthContext.getUserId(req)));
            return;
        }
        if (path.startsWith("/me/unread")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
            writeJson(resp, notificationService.getUnreadNotifications(AuthContext.getUserId(req)));
            return;
        }
        if (path.startsWith("/me")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
            long userId = AuthContext.getUserId(req);
            int limit = parseIntOrDefault(req.getParameter("limit"), 20);
            int offset = parseIntOrDefault(req.getParameter("offset"), 0);
            writeJson(resp, notificationService.getNotificationsForUser(userId, limit, offset));
            return;
        }
        if (path.startsWith("/id/")) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
            long notificationId = parseLong(path.substring("/id/".length()));
            Notification notification = notificationService.getNotificationById(notificationId);
            RoleGuard.requireUserOrRole(req, notification.getUserId(), ROLE_ADMIN);
            writeJson(resp, notification);
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = pathInfo(req);

        if ("/broadcast".equals(path)) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            com.school.model.BroadcastNotificationRequest request = readBody(req, com.school.model.BroadcastNotificationRequest.class);
            if (request == null || request.getNotification() == null)
                throw new ValidationException("Broadcast notification request is required.");
            if (request.getNotification().getCreatedAt() == null)
                request.getNotification().setCreatedAt(LocalDateTime.now());

            notificationService.broadcastNotification(request);
            writeStatusMessage(resp, "Broadcast notifications created.");
            return;
        }
        if ("/bulk".equals(path)) {
            // FIX: Added ROLE_TEACHER
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            BulkNotificationsRequest request = readBody(req, BulkNotificationsRequest.class);
            if (request == null || request.getNotifications() == null || request.getNotifications().isEmpty())
                throw new ValidationException("notifications is required.");
            List<Notification> notifications = request.getNotifications();
            for (Notification notification : notifications) {
                if (notification.getCreatedAt() == null)
                    notification.setCreatedAt(LocalDateTime.now());
            }
            notificationService.createNotifications(notifications);
            writeStatusMessage(resp, "Notifications created.");
            return;
        }
        if ("/".equals(path) || "".equals(path)) {
            RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER);
            Notification notification = readBody(req, Notification.class);
            if (notification == null)
                throw new ValidationException("Request body is required.");
            if (notification.getCreatedAt() == null)
                notification.setCreatedAt(LocalDateTime.now());
            writeJson(resp, notificationService.createNotification(notification), "Notification created.");
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
        String path = pathInfo(req);

        if (path.startsWith("/read-all")) {
            notificationService.markAllAsRead(AuthContext.getUserId(req));
            writeStatusMessage(resp, "All notifications marked as read.");
            return;
        }
        if (path.startsWith("/read/")) {
            long notificationId = parseLong(path.substring("/read/".length()));
            Notification notification = notificationService.getNotificationById(notificationId);
            RoleGuard.requireUserOrRole(req, notification.getUserId(), ROLE_ADMIN);
            notificationService.markAsRead(notificationId, LocalDateTime.now());
            writeStatusMessage(resp, "Notification marked as read.");
            return;
        }
        throw new ValidationException("Unsupported path: " + path);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RoleGuard.requireRole(req, ROLE_ADMIN, ROLE_PRINCIPAL, ROLE_TEACHER, ROLE_STUDENT, ROLE_PARENT);
        String path = pathInfo(req);

        if (path.startsWith("/me/all")) {
            notificationService.deleteAllNotifications(AuthContext.getUserId(req));
            writeStatusMessage(resp, "All notifications deleted.");
            return;
        }
        long notificationId = parseLong(path.substring(1));
        Notification notification = notificationService.getNotificationById(notificationId);
        RoleGuard.requireUserOrRole(req, notification.getUserId(), ROLE_ADMIN);
        notificationService.deleteNotification(notificationId);
        writeStatusMessage(resp, "Notification deleted.");
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank())
            return defaultValue;
        return parseInt(value);
    }

    public static class BulkNotificationsRequest {
        private List<Notification> notifications;

        public List<Notification> getNotifications() {
            return notifications;
        }

        public void setNotifications(List<Notification> notifications) {
            this.notifications = notifications;
        }
    }
}