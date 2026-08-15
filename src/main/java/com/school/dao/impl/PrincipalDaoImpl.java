package com.school.dao.impl;

import com.school.dao.interfaces.PrincipalDao;
import com.school.exceptions.DaoException;
import com.school.model.Principal;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrincipalDaoImpl implements PrincipalDao {

    private static final String INSERT = "INSERT INTO principals (user_id, employee_id, first_name, last_name, phone, is_active) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM principals WHERE principal_id = ?";
    private static final String SELECT_BY_USER_ID = "SELECT * FROM principals WHERE user_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM principals WHERE is_active = TRUE ORDER BY principal_id";
    private static final String UPDATE = "UPDATE principals SET employee_id = ?, first_name = ?, last_name = ?, phone = ? WHERE principal_id = ?";
    private static final String DELETE = "UPDATE principals SET is_active = FALSE WHERE principal_id = ?";

    @Override
    public boolean insertPrincipal(Principal principal) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, principal.getUserId());
            ps.setString(2, principal.getEmployeeId());
            ps.setString(3, principal.getFirstName());
            ps.setString(4, principal.getLastName());
            ps.setString(5, principal.getPhone());
            ps.setBoolean(6, principal.isActive());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    principal.setPrincipalId(resultSet.getLong(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting principal", e);
        }
    }

    @Override
    public Principal getPrincipalById(long principalId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, principalId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching principal", e);
        }
    }

    @Override
    public Principal getPrincipalByUserId(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_USER_ID)) {

            ps.setLong(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching principal", e);
        }
    }

    @Override
    public List<Principal> getAllPrincipals() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<Principal> principals = new ArrayList<>();
            while (resultSet.next())
                principals.add(mapRow(resultSet));
            return principals;
        } catch (SQLException e) {
            throw new DaoException("Error fetching principals", e);
        }
    }

    @Override
    public boolean updatePrincipal(Principal principal) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, principal.getEmployeeId());
            ps.setString(2, principal.getFirstName());
            ps.setString(3, principal.getLastName());
            ps.setString(4, principal.getPhone());
            ps.setLong(5, principal.getPrincipalId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating principal", e);
        }
    }

    @Override
    public boolean deletePrincipal(long principalId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setLong(1, principalId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting principal", e);
        }
    }

    private Principal mapRow(ResultSet resultSet) throws SQLException {
        Principal principal = new Principal();
        principal.setPrincipalId(resultSet.getLong("principal_id"));
        principal.setUserId(resultSet.getLong("user_id"));
        principal.setEmployeeId(resultSet.getString("employee_id"));
        principal.setFirstName(resultSet.getString("first_name"));
        principal.setLastName(resultSet.getString("last_name"));
        principal.setPhone(resultSet.getString("phone"));
        principal.setActive(resultSet.getBoolean("is_active"));
        return principal;
    }
}