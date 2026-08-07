package com.school.dao.interfaces;

import com.school.model.Administrator;

import java.sql.Connection;
import java.util.List;

public interface AdministratorDao {
    boolean insertAdministrator(Administrator admin);
    boolean insertAdministrator(Administrator admin, Connection connection);
    Administrator getAdministratorById(long adminId);

    Administrator getAdminByUserId(long userId);
    List<Administrator> getAllAdmins();
    boolean updateAdmin(Administrator admin);
}
