package com.school.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Role {
    private int roleId;
    private String roleName;
    private String description;
}