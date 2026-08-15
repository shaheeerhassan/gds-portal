package com.school.dao.impl;

import com.school.dao.interfaces.AnnouncementDao;
import com.school.exceptions.DaoException;
import com.school.model.Announcement;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDaoImpl implements AnnouncementDao {

    private static final String INSERT = "INSERT INTO announcements (title, content, created_by, target_role_id, class_id, section_id, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM announcements WHERE announcement_id = ?";
    private static final String SELECT_ACTIVE = "SELECT * FROM announcements WHERE is_active = TRUE ORDER BY created_at DESC";
    private static final String SELECT_BY_ROLE = "SELECT * FROM announcements WHERE is_active = TRUE AND target_role_id = ? ORDER BY created_at DESC";
    private static final String SELECT_BY_CLASS = "SELECT * FROM announcements WHERE is_active = TRUE AND class_id = ? ORDER BY created_at DESC";
    private static final String SELECT_BY_SECTION = "SELECT * FROM announcements WHERE is_active = TRUE AND section_id = ? ORDER BY created_at DESC";
    private static final String UPDATE = "UPDATE announcements SET title = ?, content = ?, target_role_id = ?, class_id = ?, section_id = ?, updated_at = ? WHERE announcement_id = ?";
    private static final String DISABLE = "UPDATE announcements SET is_active = FALSE, updated_at = ? WHERE announcement_id = ?";
    private static final String SELECT_GLOBAL = "SELECT * FROM announcements WHERE is_active = TRUE AND target_role_id IS NULL AND class_id IS NULL AND section_id IS NULL ORDER BY created_at DESC";

    @Override
    public boolean insertAnnouncement(Announcement announcement) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, announcement.getTitle());
            ps.setString(2, announcement.getContent());
            ps.setLong(3, announcement.getCreatedBy());
            if (announcement.getTargetRoleId() != null) {
                ps.setInt(4, announcement.getTargetRoleId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (announcement.getClassId() != null) {
                ps.setInt(5, announcement.getClassId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            if (announcement.getSectionId() != null) {
                ps.setInt(6, announcement.getSectionId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setBoolean(7, announcement.isActive());
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(8, Timestamp.valueOf(now));
            ps.setTimestamp(9, Timestamp.valueOf(now));

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    announcement.setAnnouncementId(resultSet.getLong(1));
                    announcement.setCreatedAt(now);
                    announcement.setUpdatedAt(now);
                }
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting announcement", e);
        }
    }

    @Override
    public List<Announcement> getAllActiveAnnouncements() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ACTIVE);
             ResultSet resultSet = ps.executeQuery()) {

            List<Announcement> announcements = new ArrayList<>();
            while (resultSet.next())
                announcements.add(mapRowToAnnouncement(resultSet));
            return announcements;
        } catch (SQLException e) {
            throw new DaoException("Error fetching announcements", e);
        }
    }

    @Override
    public boolean updateAnnouncement(Announcement announcement) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, announcement.getTitle());
            ps.setString(2, announcement.getContent());
            if (announcement.getTargetRoleId() != null) {
                ps.setInt(3, announcement.getTargetRoleId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (announcement.getClassId() != null) {
                ps.setInt(4, announcement.getClassId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            if (announcement.getSectionId() != null) {
                ps.setInt(5, announcement.getSectionId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(7, announcement.getAnnouncementId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating announcement", e);
        }
    }

    @Override
    public boolean disableAnnouncement(long announcementId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DISABLE)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, announcementId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error disabling announcement", e);
        }
    }

    @Override
    public Announcement getAnnouncementById(long announcementId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, announcementId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRowToAnnouncement(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching announcement", e);
        }
    }

    @Override
    public List<Announcement> getAnnouncementsByTargetRole(int roleId) {
        return queryBy(SELECT_BY_ROLE, roleId);
    }

    @Override
    public List<Announcement> getAnnouncementsByTargetClass(int classId) {
        return queryBy(SELECT_BY_CLASS, classId);
    }

    @Override
    public List<Announcement> getAnnouncementsByTargetSection(int sectionId) {
        List<Announcement> announcements = new ArrayList<>();
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SECTION)) {
             
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    announcements.add(mapRowToAnnouncement(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching section announcements", e);
        }
        return announcements;
    }

    @Override
    public List<Announcement> getGlobalAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_GLOBAL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                announcements.add(mapRowToAnnouncement(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching global announcements", e);
        }
        return announcements;
    }

    private List<Announcement> queryBy(String sql, int id) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Announcement> announcements = new ArrayList<>();
                while (resultSet.next())
                    announcements.add(mapRowToAnnouncement(resultSet));
                return announcements;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching announcements", e);
        }
    }

    private Announcement mapRowToAnnouncement(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setAnnouncementId(rs.getLong("announcement_id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setContent(rs.getString("content"));
        announcement.setCreatedBy(rs.getLong("created_by"));
        announcement.setTargetRoleId(rs.getInt("target_role_id"));
        if (rs.wasNull()) announcement.setTargetRoleId(null);
        announcement.setClassId(rs.getInt("class_id"));
        if (rs.wasNull()) announcement.setClassId(null);
        announcement.setSectionId(rs.getInt("section_id"));
        if (rs.wasNull()) announcement.setSectionId(null);
        announcement.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) announcement.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) announcement.setUpdatedAt(updated.toLocalDateTime());
        return announcement;
    }
}