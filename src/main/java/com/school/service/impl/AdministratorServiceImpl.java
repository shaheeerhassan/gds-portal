package com.school.service.impl;

import com.school.dao.impl.AdministratorDaoImpl;
import com.school.dao.interfaces.AdministratorDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.model.Administrator;
import com.school.model.User;
import com.school.service.interfaces.AdministratorService;
import com.school.service.interfaces.RoleService;
import com.school.service.interfaces.UserService;

import java.util.List;

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
        user = userService.createUser(user, password);

        administrator.setUserId(user.getUserId());

        if (!administratorDao.insertAdministrator(administrator))
            throw new IllegalStateException("Failed to create administrator.");

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
        validateId(administrator.getAdminId(), administrator.getUserId());
        validateRequired(administrator.getEmployeeId(), "Employee ID");
        administrator.setFirstName(validateName(administrator.getFirstName(), "First name"));
        administrator.setLastName(validateName(administrator.getLastName(), "Last name"));
        administrator.setPhone(validatePhone(administrator.getPhone()));

        if (!administratorDao.updateAdmin(administrator))
            throw new ResourceNotFoundException("Administrator not found.");
    }
}
