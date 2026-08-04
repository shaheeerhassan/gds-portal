package com.school.dao.impl;

import com.school.dao.interfaces.ParentDao;
import com.school.exceptions.DaoException;
import com.school.model.Parent;
import com.school.model.StudentParentLink;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParentDaoImpl implements ParentDao {

    private static final String INSERT = "INSERT INTO parents (user_id, first_name, last_name, phone, occupation, is_active) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM parents WHERE parent_id = ?";
    private static final String SELECT_BY_USER_ID = "SELECT * FROM parents WHERE user_id = ?";
    private static final String SELECT_BY_STUDENT = "SELECT p.* FROM parents p JOIN student_parent_links spl ON p.parent_id = spl.parent_id WHERE spl.student_id = ? ORDER BY p.parent_id";
    private static final String UPDATE = "UPDATE parents SET first_name = ?, last_name = ?, phone = ?, occupation = ?, is_active = ? WHERE parent_id = ?";
    private static final String DELETE = "UPDATE parents SET is_active = FALSE WHERE parent_id = ?";

    private static final String LINK_INSERT = "INSERT INTO student_parent_links (student_id, parent_id, relationship_type, is_primary_contact) VALUES (?, ?, ?, ?)";
    private static final String LINK_DELETE = "DELETE FROM student_parent_links WHERE parent_id = ? AND student_id = ?";
    private static final String CLEAR_PRIMARY = "UPDATE student_parent_links SET is_primary_contact = FALSE WHERE student_id = ?";

    @Override
    public boolean insertParent(Parent parent) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, parent.getUserId());
            ps.setString(2, parent.getFirstName());
            ps.setString(3, parent.getLastName());
            ps.setString(4, parent.getPhone());
            ps.setString(5, parent.getOccupation());
            ps.setBoolean(6, parent.isActive());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    parent.setParentId(resultSet.getLong(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting parent", e);
        }
    }

    @Override
    public boolean linkParentToStudent(long studentId, long parentId, StudentParentLink.RelationshipType relationshipType, boolean isPrimaryContact) {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);

            if (isPrimaryContact) {
                try (PreparedStatement ps = cn.prepareStatement(CLEAR_PRIMARY)) {
                    ps.setLong(1, studentId);
                    ps.executeUpdate();
                }
            }

            boolean linked;
            try (PreparedStatement ps = cn.prepareStatement(LINK_INSERT)) {
                ps.setLong(1, studentId);
                ps.setLong(2, parentId);
                ps.setString(3, relationshipType.name());
                ps.setBoolean(4, isPrimaryContact);
                linked = ps.executeUpdate() > 0;
            }

            cn.commit();
            return linked;
        } catch (SQLException e) {
            if (cn != null) {
                try { cn.rollback(); } catch (SQLException ignore) {}
            }
            throw new DaoException("Error linking parent to student", e);
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public boolean unlinkParentFromStudent(long parentId, long studentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(LINK_DELETE)) {

            ps.setLong(1, parentId);
            ps.setLong(2, studentId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error unlinking parent from student", e);
        }
    }

    @Override
    public Parent getParentById(long parentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, parentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching parent", e);
        }
    }

    @Override
    public List<Parent> getParentsByStudentId(long studentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_STUDENT)) {

            ps.setLong(1, studentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Parent> parents = new ArrayList<>();
                while (resultSet.next())
                    parents.add(mapRow(resultSet));
                return parents;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching parents for student", e);
        }
    }

    @Override
    public Parent getParentByUserId(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_USER_ID)) {

            ps.setLong(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching parent", e);
        }
    }

    @Override
    public boolean updateParentDetails(Parent parent) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, parent.getFirstName());
            ps.setString(2, parent.getLastName());
            ps.setString(3, parent.getPhone());
            ps.setString(4, parent.getOccupation());
            ps.setBoolean(5, parent.isActive());
            ps.setLong(6, parent.getParentId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating parent", e);
        }
    }

    @Override
    public boolean deleteParent(long parentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setLong(1, parentId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting parent", e);
        }
    }

    private Parent mapRow(ResultSet resultSet) throws SQLException {
        Parent parent = new Parent();
        parent.setParentId(resultSet.getLong("parent_id"));
        parent.setUserId(resultSet.getLong("user_id"));
        parent.setFirstName(resultSet.getString("first_name"));
        parent.setLastName(resultSet.getString("last_name"));
        parent.setPhone(resultSet.getString("phone"));
        parent.setOccupation(resultSet.getString("occupation"));
        parent.setActive(resultSet.getBoolean("is_active"));
        return parent;
    }
}