package com.school.dao.impl;

import com.school.model.Administrator;

import java.util.List;

public interface AdministratorDao {
    boolean insertAdministrator(Administrator admin);
    Administrator getAdministratorById(long adminId);

    Administrator getAdminByUserId(long userId);
    List<Administrator> getAllAdmins();
    boolean updateAdmin(Administrator admin);
}
