package com.school.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Principal {
    private long principalId;
    private long userId;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String phone;
}
