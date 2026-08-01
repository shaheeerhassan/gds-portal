package com.school.model;

public class StudentParentLink {
    public enum RelationshipType { FATHER, MOTHER, GUARDIAN, OTHER }
    private long linkId;
    private long studentId;
    private long parentId;
    private RelationshipType relationshipType;
    private boolean isPrimaryContact;

    public StudentParentLink() {}
    public StudentParentLink(long linkId, long studentId, long parentId, RelationshipType relationshipType, boolean isPrimaryContact) {
        this.linkId = linkId; this.studentId = studentId; this.parentId = parentId; this.relationshipType = relationshipType; this.isPrimaryContact = isPrimaryContact;
    }

    public long getLinkId() { return linkId; }
    public void setLinkId(long linkId) { this.linkId = linkId; }
    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
    public long getParentId() { return parentId; }
    public void setParentId(long parentId) { this.parentId = parentId; }
    public RelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(RelationshipType relationshipType) { this.relationshipType = relationshipType; }
    public boolean isPrimaryContact() { return isPrimaryContact; }
    public void setPrimaryContact(boolean primaryContact) { isPrimaryContact = primaryContact; }
}
