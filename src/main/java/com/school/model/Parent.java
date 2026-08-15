package com.school.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Parent {
    private long parentId;
    private long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String occupation;
    @JsonAlias("isActive")
    private boolean isActive;
}
