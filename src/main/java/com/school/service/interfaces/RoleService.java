package com.school.service.interfaces;

import com.school.model.Role;

import java.util.List;

public interface RoleService {
    Role getRoleById(int roleId);
    Role getRoleByName(String roleName);
    List<Role> getAllRoles();
}
