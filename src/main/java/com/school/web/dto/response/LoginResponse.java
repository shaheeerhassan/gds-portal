package com.school.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.school.model.User;
import lombok.*;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginResponse {
    private String token;
    private long expiresIn;
    private String refreshToken;
    private User user;
    private Map<String, Object> profile;
}
