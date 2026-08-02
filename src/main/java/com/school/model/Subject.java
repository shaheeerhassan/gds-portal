package com.school.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Subject {
    private int subjectId;
    private String subjectName;
    private String subjectCode;
    private String description;
}
