package com.school.model;

import lombok.*;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Teacher {
    public enum Gender { MALE, FEMALE, OTHER }
    private long teacherId;
    private long userId;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String phone;
    private Gender gender;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private String qualification;
    private boolean isActive;
}

