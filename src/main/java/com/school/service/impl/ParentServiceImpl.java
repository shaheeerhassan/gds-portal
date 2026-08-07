package com.school.service.impl;

import com.school.dao.impl.ParentDaoImpl;
import com.school.dao.interfaces.ParentDao;
import com.school.exceptions.DaoException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Parent;
import com.school.model.StudentParentLink;
import com.school.model.User;
import com.school.service.interfaces.ParentService;
import com.school.service.interfaces.RoleService;
import com.school.service.interfaces.UserService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.school.config.DBConfig.getDataSource;
import static com.school.validations.ValidatorUtil.*;

public class ParentServiceImpl implements ParentService {

    private final ParentDao parentDao;
    private final UserService userService;
    private final RoleService roleService;

    public ParentServiceImpl() {
        parentDao = new ParentDaoImpl();
        userService = new UserServiceImpl();
        roleService = new RoleServiceImpl();
    }

    @Override
    public Parent createParent(User user, String password, Parent parent) {
        parent.setFirstName(validateName(parent.getFirstName(), "First name"));
        parent.setLastName(validateName(parent.getLastName(), "Last name"));
        parent.setPhone(validatePhone(parent.getPhone()));

        user.setRoleId(roleService.getRoleByName("PARENT").getRoleId());

        try (Connection cn = getDataSource().getConnection()) {
            cn.setAutoCommit(false);
            try {
                user = userService.createUser(user, password, cn);

                parent.setUserId(user.getUserId());
                parent.setActive(true);

                if (!parentDao.insertParent(parent, cn))
                    throw new IllegalStateException("Failed to create parent.");

                cn.commit();
            } catch (Exception e) {
                try { cn.rollback(); } catch (SQLException ignore) {}
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Error creating parent", e);
        }

        return parent;
    }

    @Override
    public Parent getParentById(long parentId) {
        validateId(parentId);
        Parent parent = parentDao.getParentById(parentId);
        if (parent == null)
            throw new ResourceNotFoundException("Parent not found.");
        return parent;
    }

    @Override
    public Parent getParentByUserId(long userId) {
        validateId(userId);
        Parent parent = parentDao.getParentByUserId(userId);
        if (parent == null)
            throw new ResourceNotFoundException("Parent not found.");
        return parent;
    }

    @Override
    public List<Parent> getParentsByStudentId(long studentId) {
        validateId(studentId);
        return parentDao.getParentsByStudentId(studentId);
    }

    @Override
    public void updateParent(Parent parent) {
        validateId(parent.getParentId());

        Parent existing = parentDao.getParentById(parent.getParentId());

        if (existing == null)
            throw new ResourceNotFoundException("Parent not found.");

        if (parent.getFirstName() == null)
            parent.setFirstName(existing.getFirstName());
        if (parent.getLastName() == null)
            parent.setLastName(existing.getLastName());
        if (parent.getPhone() == null)
            parent.setPhone(existing.getPhone());
        if (parent.getOccupation() == null)
            parent.setOccupation(existing.getOccupation());

        parent.setFirstName(validateName(parent.getFirstName(), "First name"));
        parent.setLastName(validateName(parent.getLastName(), "Last name"));
        parent.setPhone(validatePhone(parent.getPhone()));

        if (!parentDao.updateParentDetails(parent))
            throw new ResourceNotFoundException("Parent not found.");
    }

    @Override
    public void deactivateParent(long parentId) {
        validateId(parentId);
        if (!parentDao.deleteParent(parentId))
            throw new ResourceNotFoundException("Parent not found.");
    }

    @Override
    public void linkParentToStudent(long studentId, long parentId,
                                    StudentParentLink.RelationshipType relationshipType, boolean isPrimaryContact) {
        validateId(studentId);
        validateId(parentId);
        if (relationshipType == null)
            throw new ValidationException("Relationship type is required.");

        if (!parentDao.linkParentToStudent(studentId, parentId, relationshipType, isPrimaryContact))
            throw new IllegalStateException("Failed to link parent to student.");
    }

    @Override
    public void unlinkParentFromStudent(long parentId, long studentId) {
        validateId(parentId);
        validateId(studentId);
        if (!parentDao.unlinkParentFromStudent(parentId, studentId))
            throw new ResourceNotFoundException("Parent-student link not found.");
    }
}
