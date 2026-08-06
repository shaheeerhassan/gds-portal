package com.school.model;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RefreshToken {
    private long tokenId;
    private long userId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private boolean isUsed;
    private LocalDateTime createdAt;
}
