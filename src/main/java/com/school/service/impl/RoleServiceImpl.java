package com.school.service.impl;

import com.school.dao.impl.RoleDaoImpl;
import com.school.dao.interfaces.RoleDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.Role;
import com.school.service.interfaces.RoleService;

import java.util.List;

import static com.school.validations.ValidatorUtil.validateId;

public class RoleServiceImpl implements RoleService {

    private final RoleDao roleDao;

    public RoleServiceImpl() {
        roleDao = new RoleDaoImpl();
    }

    @Override
    public Role getRoleById(int roleId) {
        validateId(roleId);
        Role role = roleDao.getRoleById(roleId);
        if (role == null)
            throw new ResourceNotFoundException("Role not found.");
        return role;
    }

    @Override
    public Role getRoleByName(String roleName) {
        Role role = roleDao.getRoleByName(roleName);
        if (role == null)
            throw new ResourceNotFoundException("Role not found.");
        return role;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleDao.getAllRoles();
    }
}
