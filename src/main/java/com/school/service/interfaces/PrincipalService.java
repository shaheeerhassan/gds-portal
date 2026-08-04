package com.school.service.interfaces;

import com.school.model.Principal;
import com.school.model.User;

import java.util.List;

public interface PrincipalService {
    Principal createPrincipal(User user, String password, Principal principal);
    Principal getPrincipalById(long principalId);
    Principal getPrincipalByUserId(long userId);
    List<Principal> getAllPrincipals();
    void updatePrincipal(Principal principal);
    void deactivatePrincipal(long principalId);
}
