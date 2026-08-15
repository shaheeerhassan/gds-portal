package com.school.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.model.User;
import lombok.*;

@Getter
@Setter
public class CreateUserRequest {
    private String email;
    private String username;
    private String password;
    private int roleId;

    @JsonIgnore
    public User toUser() {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setRoleId(roleId);
        return user;
    }
}
