package com.school.dao.interfaces;

import com.school.model.Parent;
import com.school.model.StudentParentLink;
import java.sql.Connection;
import java.util.List;

public interface ParentDao {
    boolean insertParent(Parent parent);
    boolean insertParent(Parent parent, Connection connection);

    boolean linkParentToStudent(long studentId, long parentId, StudentParentLink.RelationshipType relationshipType, boolean isPrimaryContact);
    boolean unlinkParentFromStudent(long parentId, long studentId);

    Parent getParentById(long parentId);
    List<Parent> getParentsByStudentId(long studentId);
    Parent getParentByUserId(long userId);

    boolean updateParentDetails(Parent parent);
    boolean deleteParent(long parentId);
}