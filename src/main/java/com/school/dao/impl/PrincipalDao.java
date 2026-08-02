package com.school.dao.impl;

import com.school.model.Principal;

import java.util.List;

public interface PrincipalDao {
    boolean insertPrincipal(Principal principal);
    Principal getPrincipalById(long principalId);

    Principal getPrincipalByUserId(long userId);
    List<Principal> getAllPrincipals();
    boolean updatePrincipal(Principal principal);
}

