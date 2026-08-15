package com.school.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.model.Parent;
import com.school.model.User;

public class CreateParentRequest {
    private String email;
    private String username;
    private String password;
    private Parent parent;

    @JsonIgnore
    public User toUser() {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        return user;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}
