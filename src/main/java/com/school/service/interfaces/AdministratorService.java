package com.school.service.interfaces;

import com.school.model.Administrator;
import com.school.model.User;

import java.util.List;

public interface AdministratorService {
    Administrator createAdministrator(User user, String password, Administrator administrator);
    Administrator getAdministratorById(long adminId);
    Administrator getAdminByUserId(long userId);
    List<Administrator> getAllAdmins();
    void updateAdministrator(Administrator administrator);
}
