package com.school.dao.impl;

import com.school.dao.interfaces.AdministratorDao;
import com.school.exceptions.DaoException;
import com.school.model.Administrator;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdministratorDaoImpl implements AdministratorDao {

    private static final String INSERT = "INSERT INTO administrators (user_id, employee_id, first_name, last_name, phone) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM administrators WHERE admin_id = ?";
    private static final String SELECT_BY_USER_ID = "SELECT * FROM administrators WHERE user_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM administrators ORDER BY admin_id";
    private static final String UPDATE = "UPDATE administrators SET employee_id = ?, first_name = ?, last_name = ?, phone = ? WHERE admin_id = ?";

    @Override
    public boolean insertAdministrator(Administrator admin) {
        try (Connection cn = getDataSource().getConnection()) {
            return insertAdministrator(admin, cn);
        } catch (SQLException e) {
            throw new DaoException("Error inserting administrator", e);
        }
    }

    @Override
    public boolean insertAdministrator(Administrator admin, Connection cn) {
        try (PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, admin.getUserId());
            ps.setString(2, admin.getEmployeeId());
            ps.setString(3, admin.getFirstName());
            ps.setString(4, admin.getLastName());
            ps.setString(5, admin.getPhone());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    admin.setAdminId(resultSet.getLong(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting administrator", e);
        }
    }

    @Override
    public Administrator getAdministratorById(long adminId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, adminId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching administrator", e);
        }
    }

    @Override
    public Administrator getAdminByUserId(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_USER_ID)) {

            ps.setLong(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching administrator", e);
        }
    }

    @Override
    public List<Administrator> getAllAdmins() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<Administrator> admins = new ArrayList<>();
            while (resultSet.next())
                admins.add(mapRow(resultSet));
            return admins;
        } catch (SQLException e) {
            throw new DaoException("Error fetching administrators", e);
        }
    }

    @Override
    public boolean updateAdmin(Administrator admin) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, admin.getEmployeeId());
            ps.setString(2, admin.getFirstName());
            ps.setString(3, admin.getLastName());
            ps.setString(4, admin.getPhone());
            ps.setLong(5, admin.getAdminId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating administrator", e);
        }
    }

    private Administrator mapRow(ResultSet resultSet) throws SQLException {
        Administrator admin = new Administrator();
        admin.setAdminId(resultSet.getLong("admin_id"));
        admin.setUserId(resultSet.getLong("user_id"));
        admin.setEmployeeId(resultSet.getString("employee_id"));
        admin.setFirstName(resultSet.getString("first_name"));
        admin.setLastName(resultSet.getString("last_name"));
        admin.setPhone(resultSet.getString("phone"));
        return admin;
    }
}