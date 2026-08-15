package com.school.web.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LogoutRequest {
    private String refreshToken;
    private String accessToken;
}
