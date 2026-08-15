package com.school.service.impl;

import com.school.dao.impl.PrincipalDaoImpl;
import com.school.dao.interfaces.PrincipalDao;
import com.school.exceptions.DaoException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.Principal;
import com.school.model.User;
import com.school.service.interfaces.PrincipalService;
import com.school.service.interfaces.RoleService;
import com.school.service.interfaces.UserService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.school.config.DBConfig.getDataSource;
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

        try (Connection cn = getDataSource().getConnection()) {
            cn.setAutoCommit(false);
            try {
                user = userService.createUser(user, password, cn);

                principal.setUserId(user.getUserId());
                principal.setActive(true);

                if (!principalDao.insertPrincipal(principal, cn))
                    throw new IllegalStateException("Failed to create principal.");

                cn.commit();
            } catch (Exception e) {
                try { cn.rollback(); } catch (SQLException ignore) {}
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Error creating principal", e);
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

        if (existing == null)
            throw new ResourceNotFoundException("Principal not found.");

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
