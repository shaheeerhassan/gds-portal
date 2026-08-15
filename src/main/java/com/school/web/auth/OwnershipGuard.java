package com.school.web.auth;

import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.UnauthorizedException;
import com.school.model.Parent;
import com.school.service.interfaces.ParentService;
import com.school.service.interfaces.StudentService;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public final class OwnershipGuard {

    private static final String ROLE_PARENT = "PARENT";

    private OwnershipGuard() {
    }

    public static void requireLinkedParent(HttpServletRequest request, ParentService parentService, long studentId) {
        long authUserId = AuthContext.getUserId(request);
        if (authUserId == 0)
            throw new UnauthorizedException("Authentication required.");
        if (!AuthContext.hasRole(request, ROLE_PARENT))
            return;

        Parent parent;
        try {
            parent = parentService.getParentByUserId(authUserId);
        } catch (ResourceNotFoundException e) {
            throw new UnauthorizedException("Parent profile not found for the current user.");
        }

        List<Parent> parents = parentService.getParentsByStudentId(studentId);
        for (Parent linked : parents) {
            if (linked.getParentId() == parent.getParentId())
                return;
        }
        throw new UnauthorizedException("You are not linked to this student.");
    }

    public static void requireSelfOrRolesOrLinkedParent(HttpServletRequest request,
                                                        long ownerUserId, long studentId,
                                                        ParentService parentService, String... allowedRoles) {
        long authUserId = AuthContext.getUserId(request);
        if (authUserId == ownerUserId)
            return;
        if (AuthContext.hasRole(request, ROLE_PARENT)) {
            requireLinkedParent(request, parentService, studentId);
            return;
        }
        RoleGuard.requireRole(request, allowedRoles);
    }

    public static long ownStudentId(HttpServletRequest request, StudentService studentService) {
        long authUserId = AuthContext.getUserId(request);
        if (authUserId == 0)
            throw new UnauthorizedException("Authentication required.");
        try {
            return studentService.getStudentByUserId(authUserId).getStudentId();
        } catch (ResourceNotFoundException e) {
            throw new UnauthorizedException("Student profile not found for the current user.");
        }
    }

    public static void requireOwnStudent(HttpServletRequest request, StudentService studentService, long studentId) {
        long ownStudentId = ownStudentId(request, studentService);
        if (ownStudentId != studentId)
            throw new UnauthorizedException("You can only access your own data.");
    }
}
