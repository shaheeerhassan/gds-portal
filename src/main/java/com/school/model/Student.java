package com.school.model;

import lombok.*;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Student {
    public enum Gender { MALE, FEMALE, OTHER }
    private long studentId;
    private long userId;
    private String registrationNumber;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private LocalDate admissionDate;
    private boolean isActive;
}
