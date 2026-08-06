package com.school.model;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias("isActive")
    private boolean isActive;
}
