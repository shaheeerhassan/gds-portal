package com.school.service.interfaces;

import com.school.model.Parent;
import com.school.model.StudentParentLink;
import com.school.model.User;

import java.util.List;

public interface ParentService {
    Parent createParent(User user, String password, Parent parent);
    Parent getParentById(long parentId);
    Parent getParentByUserId(long userId);
    List<Parent> getParentsByStudentId(long studentId);
    void updateParent(Parent parent);
    void deactivateParent(long parentId);

    com.school.web.dto.response.PaginatedResponse<com.school.web.dto.response.ParentDirectoryDTO> getParentsDirectory(String query, int page, int size);

    void linkParentToStudent(long studentId, long parentId,
                             StudentParentLink.RelationshipType relationshipType, boolean isPrimaryContact);
    void unlinkParentFromStudent(long parentId, long studentId);
}
