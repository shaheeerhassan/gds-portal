package com.school.dao.interfaces;

import com.school.model.Principal;

import java.sql.Connection;
import java.util.List;

public interface PrincipalDao {
    boolean insertPrincipal(Principal principal);
    boolean insertPrincipal(Principal principal, Connection connection);
    Principal getPrincipalById(long principalId);

    Principal getPrincipalByUserId(long userId);
    List<Principal> getAllPrincipals();
    boolean updatePrincipal(Principal principal);
    boolean deletePrincipal(long principalId); // Soft delete via is_active
}

