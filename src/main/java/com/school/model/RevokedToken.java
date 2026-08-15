package com.school.model;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RevokedToken {
    private String jti;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
