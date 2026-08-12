package com.school.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.model.Student;
import com.school.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStudentRequest {
    private String email;
    private String username;
    private String password;
    private Student student;

    @JsonIgnore
    public User toUser() {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        return user;
    }
}
