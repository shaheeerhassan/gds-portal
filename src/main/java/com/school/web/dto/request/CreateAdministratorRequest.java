package com.school.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.model.Administrator;
import com.school.model.User;

public class CreateAdministratorRequest {
    private String email;
    private String username;
    private String password;
    private Administrator administrator;

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

    public Administrator getAdministrator() {
        return administrator;
    }

    public void setAdministrator(Administrator administrator) {
        this.administrator = administrator;
    }
}
