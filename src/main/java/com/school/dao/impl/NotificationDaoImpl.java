package com.school.dao.impl;

import com.school.dao.interfaces.NotificationDao;
import com.school.exception.DaoException;
import com.school.model.Notification;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDaoImpl implements NotificationDao {

    private static final String INSERT = "INSERT INTO notifications (user_id, notification_type, title, message, reference_table, reference_id, is_read, created_at, read_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM notifications WHERE notification_id = ?";
    private static final String SELECT_FOR_USER = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
    private static final String SELECT_UNREAD = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
    private static final String COUNT_UNREAD = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
    private static final String DELETE_BY_ID = "DELETE FROM notifications WHERE notification_id = ?";
    private static final String DELETE_ALL = "DELETE FROM notifications WHERE user_id = ?";
    private static final String MARK_AS_READ = "UPDATE notifications SET is_read = TRUE, read_at = ? WHERE notification_id = ?";
    private static final String MARK_ALL_AS_READ = "UPDATE notifications SET is_read = TRUE, read_at = ? WHERE user_id = ? AND is_read = FALSE";

    @Override
    public boolean insertNotification(Notification notification) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, notification.getUserId());
            ps.setString(2, notification.getNotificationType().name());
            ps.setString(3, notification.getTitle());
            ps.setString(4, notification.getMessage());
            ps.setString(5, notification.getReferenceTable());
            if (notification.getReferenceId() != null) {
                ps.setLong(6, notification.getReferenceId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            ps.setBoolean(7, notification.isRead());
            notification.setCreatedAt(LocalDateTime.now());
            ps.setTimestamp(8, Timestamp.valueOf(notification.getCreatedAt()));
            if (notification.getReadAt() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(notification.getReadAt()));
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    notification.setNotificationId(resultSet.getLong(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting notification", e);
        }
    }

    @Override
    public boolean insertNotifications(List<Notification> notifications) {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                for (Notification notification : notifications) {
                    ps.setLong(1, notification.getUserId());
                    ps.setString(2, notification.getNotificationType().name());
                    ps.setString(3, notification.getTitle());
                    ps.setString(4, notification.getMessage());
                    ps.setString(5, notification.getReferenceTable());
                    if (notification.getReferenceId() != null) {
                        ps.setLong(6, notification.getReferenceId());
                    } else {
                        ps.setNull(6, Types.BIGINT);
                    }
                    ps.setBoolean(7, notification.isRead());
                    notification.setCreatedAt(LocalDateTime.now());
                    ps.setTimestamp(8, Timestamp.valueOf(notification.getCreatedAt()));
                    if (notification.getReadAt() != null) {
                        ps.setTimestamp(9, Timestamp.valueOf(notification.getReadAt()));
                    } else {
                        ps.setNull(9, Types.TIMESTAMP);
                    }
                    ps.addBatch();
                }

                ps.executeBatch();

                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    int index = 0;
                    while (resultSet.next() && index < notifications.size()) {
                        notifications.get(index).setNotificationId(resultSet.getLong(1));
                        index++;
                    }
                }
            }

            cn.commit();
            return true;
        } catch (SQLException e) {
            if (cn != null) {
                try { cn.rollback(); } catch (SQLException ignore) {}
            }
            throw new DaoException("Error inserting notifications", e);
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public boolean deleteNotification(long notificationId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE_BY_ID)) {

            ps.setLong(1, notificationId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting notification", e);
        }
    }

    @Override
    public boolean deleteAllNotifications(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE_ALL)) {

            ps.setLong(1, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting notifications", e);
        }
    }

    @Override
    public List<Notification> getNotificationsForUser(long userId, int limit, int offset) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_FOR_USER)) {

            ps.setLong(1, userId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Notification> notifications = new ArrayList<>();
                while (resultSet.next())
                    notifications.add(mapRow(resultSet));
                return notifications;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching notifications", e);
        }
    }

    @Override
    public List<Notification> getUnreadNotifications(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_UNREAD)) {

            ps.setLong(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Notification> notifications = new ArrayList<>();
                while (resultSet.next())
                    notifications.add(mapRow(resultSet));
                return notifications;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching notifications", e);
        }
    }

    @Override
    public int getUnreadCount(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_UNREAD)) {

            ps.setLong(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Error counting notifications", e);
        }
    }

    @Override
    public Notification getNotificationById(long notificationId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, notificationId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching notification", e);
        }
    }

    @Override
    public boolean markAsRead(long notificationId, LocalDateTime readAt) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(MARK_AS_READ)) {

            ps.setTimestamp(1, Timestamp.valueOf(readAt));
            ps.setLong(2, notificationId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error marking notification read", e);
        }
    }

    @Override
    public boolean markAllAsRead(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(MARK_ALL_AS_READ)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error marking notifications read", e);
        }
    }

    private Notification mapRow(ResultSet resultSet) throws SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(resultSet.getLong("notification_id"));
        notification.setUserId(resultSet.getLong("user_id"));
        String notificationType = resultSet.getString("notification_type");
        if (notificationType != null)
            notification.setNotificationType(Notification.NotificationType.valueOf(notificationType));
        notification.setTitle(resultSet.getString("title"));
        notification.setMessage(resultSet.getString("message"));
        notification.setReferenceTable(resultSet.getString("reference_table"));
        long referenceId = resultSet.getLong("reference_id");
        if (!resultSet.wasNull())
            notification.setReferenceId(referenceId);
        notification.setRead(resultSet.getBoolean("is_read"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null)
            notification.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp readAt = resultSet.getTimestamp("read_at");
        if (readAt != null)
            notification.setReadAt(readAt.toLocalDateTime());
        return notification;
    }
}