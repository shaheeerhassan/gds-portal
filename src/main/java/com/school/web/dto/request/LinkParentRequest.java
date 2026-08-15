package com.school.web.dto.request;

import com.school.model.StudentParentLink;

public class LinkParentRequest {
    private long studentId;
    private long parentId;
    private StudentParentLink.RelationshipType relationshipType;
    private boolean primaryContact;

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public StudentParentLink.RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(StudentParentLink.RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(boolean primaryContact) {
        this.primaryContact = primaryContact;
    }
}
