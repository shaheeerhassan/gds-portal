package com.school.dao.impl;

import com.school.model.Role;
import java.util.List;

public interface RoleDao {
    Role getRoleById(int roleId);
    Role getRoleByName(String roleName);
    List<Role> getAllRoles();
}