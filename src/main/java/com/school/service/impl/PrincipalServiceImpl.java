package com.school.service.impl;

import com.school.dao.impl.PrincipalDaoImpl;
import com.school.dao.interfaces.PrincipalDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.Principal;
import com.school.model.User;
import com.school.service.interfaces.PrincipalService;
import com.school.service.interfaces.RoleService;
import com.school.service.interfaces.UserService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class PrincipalServiceImpl implements PrincipalService {

    private final PrincipalDao principalDao;
    private final UserService userService;
    private final RoleService roleService;

    public PrincipalServiceImpl() {
        principalDao = new PrincipalDaoImpl();
        userService = new UserServiceImpl();
        roleService = new RoleServiceImpl();
    }

    @Override
    public Principal createPrincipal(User user, String password, Principal principal) {
        principal.setFirstName(validateName(principal.getFirstName(), "First name"));
        principal.setLastName(validateName(principal.getLastName(), "Last name"));
        principal.setPhone(validatePhone(principal.getPhone()));
        principal.setEmployeeId(validateRequired(principal.getEmployeeId(), "Employee ID"));

        user.setRoleId(roleService.getRoleByName("PRINCIPAL").getRoleId());
        user = userService.createUser(user, password);

        principal.setUserId(user.getUserId());
        principal.setActive(true);

        try {
            if (!principalDao.insertPrincipal(principal)) {
                throw new IllegalStateException("Failed to create principal.");
            }
        } catch (Exception e) {
            userService.deleteUser(user.getUserId());
            throw e;
        }

        return principal;
    }

    @Override
    public Principal getPrincipalById(long principalId) {
        validateId(principalId);
        Principal principal = principalDao.getPrincipalById(principalId);
        if (principal == null)
            throw new ResourceNotFoundException("Principal not found.");
        return principal;
    }

    @Override
    public Principal getPrincipalByUserId(long userId) {
        validateId(userId);
        Principal principal = principalDao.getPrincipalByUserId(userId);
        if (principal == null)
            throw new ResourceNotFoundException("Principal not found.");
        return principal;
    }

    @Override
    public List<Principal> getAllPrincipals() {
        return principalDao.getAllPrincipals();
    }

    @Override
    public void updatePrincipal(Principal principal) {
        validateId(principal.getPrincipalId());

        Principal existing = principalDao.getPrincipalById(principal.getPrincipalId());

        if (principal.getEmployeeId() == null)
            principal.setEmployeeId(existing.getEmployeeId());
        if (principal.getFirstName() == null)
            principal.setFirstName(existing.getFirstName());
        if (principal.getLastName()==null)
            principal.setLastName(existing.getLastName());
        if (principal.getPhone() == null)
            principal.setPhone(existing.getPhone());

        principal.setFirstName(validateName(principal.getFirstName(), "First name"));
        principal.setLastName(validateName(principal.getLastName(), "Last name"));
        principal.setPhone(validatePhone(principal.getPhone()));

        if (!principalDao.updatePrincipal(principal))
            throw new ResourceNotFoundException("Principal not found.");
    }

    @Override
    public void deactivatePrincipal(long principalId) {
        validateId(principalId);
        if (!principalDao.deletePrincipal(principalId))
            throw new ResourceNotFoundException("Principal not found.");
    }
}
