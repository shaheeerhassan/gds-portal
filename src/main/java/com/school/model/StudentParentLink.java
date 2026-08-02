package com.school.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentParentLink {
    public enum RelationshipType { FATHER, MOTHER, GUARDIAN, OTHER }
    private long linkId;
    private long studentId;
    private long parentId;
    private RelationshipType relationshipType;
    private boolean isPrimaryContact;
}
