package com.school.dao.impl;

import com.school.dao.interfaces.ClassDao;
import com.school.exceptions.DaoException;
import com.school.model.Class;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassDaoImpl implements ClassDao {

    private static final String INSERT = "INSERT INTO classes (class_name, numeric_level, description) VALUES (?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM classes WHERE class_id = ?";
    private static final String SELECT_BY_NUMERIC_LEVEL = "SELECT * FROM classes WHERE numeric_level = ?";
    private static final String SELECT_ALL = "SELECT * FROM classes ORDER BY class_id";
    private static final String UPDATE = "UPDATE classes SET class_name = ?, numeric_level = ?, description = ? WHERE class_id = ?";
    private static final String DELETE = "DELETE FROM classes WHERE class_id = ?";

    @Override
    public boolean insertClass(Class c) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getClassName());
            ps.setInt(2, c.getNumericLevel());
            ps.setString(3, c.getDescription());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    c.setClassId(resultSet.getInt(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting class", e);
        }
    }

    @Override
    public Class getClassById(int classId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, classId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching class", e);
        }
    }

    @Override
    public Class getClassByNumericLevel(int numericLevel) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_NUMERIC_LEVEL)) {

            ps.setInt(1, numericLevel);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching class", e);
        }
    }

    @Override
    public List<Class> getAllClasses() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<Class> classes = new ArrayList<>();
            while (resultSet.next())
                classes.add(mapRow(resultSet));
            return classes;
        } catch (SQLException e) {
            throw new DaoException("Error fetching classes", e);
        }
    }

    @Override
    public boolean updateClass(Class c) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, c.getClassName());
            ps.setInt(2, c.getNumericLevel());
            ps.setString(3, c.getDescription());
            ps.setInt(4, c.getClassId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating class", e);
        }
    }

    @Override
    public boolean deleteClass(int classId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setInt(1, classId);

            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new com.school.exceptions.ValidationException("Cannot delete class because it is still referenced by other records.");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign key constraint")) {
                throw new com.school.exceptions.ValidationException("Cannot delete class because it is still referenced by other records.");
            }
            throw new DaoException("Error deleting class", e);
        }
    }

    @Override
    public int getClassCount() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM classes");
             ResultSet resultSet = ps.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Error counting classes", e);
        }
    }

    private Class mapRow(ResultSet resultSet) throws SQLException {
        Class c = new Class();
        c.setClassId(resultSet.getInt("class_id"));
        c.setClassName(resultSet.getString("class_name"));
        c.setNumericLevel(resultSet.getInt("numeric_level"));
        c.setDescription(resultSet.getString("description"));
        return c;
    }
}