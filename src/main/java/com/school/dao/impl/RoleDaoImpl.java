package com.school.dao.impl;

import com.school.dao.interfaces.RoleDao;
import com.school.exceptions.DaoException;
import com.school.model.Role;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDaoImpl implements RoleDao {

    private static final String SELECT_BY_ID = "SELECT * FROM roles WHERE role_id = ?";
    private static final String SELECT_BY_NAME = "SELECT * FROM roles WHERE role_name = ?";
    private static final String SELECT_ALL = "SELECT * FROM roles ORDER BY role_id";

    @Override
    public Role getRoleById(int roleId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, roleId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching role", e);
        }
    }

    @Override
    public Role getRoleByName(String roleName) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_NAME)) {

            ps.setString(1, roleName);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching role", e);
        }
    }

    @Override
    public List<Role> getAllRoles() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<Role> roles = new ArrayList<>();
            while (resultSet.next())
                roles.add(mapRow(resultSet));
            return roles;
        } catch (SQLException e) {
            throw new DaoException("Error fetching roles", e);
        }
    }

    private Role mapRow(ResultSet resultSet) throws SQLException {
        Role role = new Role();
        role.setRoleId(resultSet.getInt("role_id"));
        role.setRoleName(resultSet.getString("role_name"));
        role.setDescription(resultSet.getString("description"));
        return role;
    }
}