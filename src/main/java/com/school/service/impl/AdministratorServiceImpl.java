package com.school.service.impl;

import com.school.dao.impl.AdministratorDaoImpl;
import com.school.dao.interfaces.AdministratorDao;
import com.school.exceptions.DaoException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.Administrator;
import com.school.model.User;
import com.school.service.interfaces.AdministratorService;
import com.school.service.interfaces.RoleService;
import com.school.service.interfaces.UserService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.school.config.DBConfig.getDataSource;
import static com.school.validations.ValidatorUtil.*;

public class AdministratorServiceImpl implements AdministratorService {

    private final AdministratorDao administratorDao;
    private final UserService userService;
    private final RoleService roleService;

    public AdministratorServiceImpl() {
        administratorDao = new AdministratorDaoImpl();
        userService = new UserServiceImpl();
        roleService = new RoleServiceImpl();
    }

    @Override
    public Administrator createAdministrator(User user, String password, Administrator administrator) {
        administrator.setEmployeeId(validateRequired(administrator.getEmployeeId(), "Employee ID"));
        administrator.setFirstName(validateName(administrator.getFirstName(), "First name"));
        administrator.setLastName(validateName(administrator.getLastName(), "Last name"));
        administrator.setPhone(validatePhone(administrator.getPhone()));

        user.setRoleId(roleService.getRoleByName("ADMINISTRATOR").getRoleId());

        try (Connection cn = getDataSource().getConnection()) {
            cn.setAutoCommit(false);
            try {
                user = userService.createUser(user, password, cn);

                administrator.setUserId(user.getUserId());

                if (!administratorDao.insertAdministrator(administrator, cn))
                    throw new IllegalStateException("Failed to create administrator.");

                cn.commit();
            } catch (Exception e) {
                try { cn.rollback(); } catch (SQLException ignore) {}
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Error creating administrator", e);
        }

        return administrator;
    }

    @Override
    public Administrator getAdministratorById(long adminId) {
        validateId(adminId);
        Administrator administrator = administratorDao.getAdministratorById(adminId);
        if (administrator == null)
            throw new ResourceNotFoundException("Administrator not found.");
        return administrator;
    }

    @Override
    public Administrator getAdminByUserId(long userId) {
        validateId(userId);
        Administrator administrator = administratorDao.getAdminByUserId(userId);
        if (administrator == null)
            throw new ResourceNotFoundException("Administrator not found.");
        return administrator;
    }

    @Override
    public List<Administrator> getAllAdmins() {
        return administratorDao.getAllAdmins();
    }

    @Override
    public void updateAdministrator(Administrator administrator) {
        validateId(administrator.getAdminId());

        Administrator existing = administratorDao.getAdministratorById(administrator.getAdminId());

        if (existing == null)
            throw new ResourceNotFoundException("Administrator not found.");

        if (administrator.getUserId() == 0)
            administrator.setUserId(existing.getUserId());
        if (administrator.getEmployeeId() == null)
            administrator.setEmployeeId(existing.getEmployeeId());
        if (administrator.getFirstName() == null)
            administrator.setFirstName(existing.getFirstName());
        if (administrator.getLastName()==null)
            administrator.setLastName(existing.getLastName());
        if (administrator.getPhone() == null)
            administrator.setPhone(existing.getPhone());
        
        validateRequired(administrator.getEmployeeId(), "Employee ID");
        administrator.setFirstName(validateName(administrator.getFirstName(), "First name"));
        administrator.setLastName(validateName(administrator.getLastName(), "Last name"));
        administrator.setPhone(validatePhone(administrator.getPhone()));

        if (!administratorDao.updateAdmin(administrator))
            throw new ResourceNotFoundException("Administrator not found.");
    }
}
