package com.school.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Administrator {
    private long adminId;
    private long userId;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String phone;
}