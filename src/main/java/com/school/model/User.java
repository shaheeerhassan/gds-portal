package com.school.model;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    private long userId;
    private int roleId;
    private String username;
    private String email;
    private String passwordHash;
    private String profilePictureUrl;
    private boolean isActive;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}