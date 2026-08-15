package com.school.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Announcement {
    private long announcementId;
    private String title;
    private String content;
    private long createdBy;
    private Integer targetRoleId; // Integer wrapper for nullable IDs
    private Integer classId;
    private Integer sectionId;
    @JsonAlias("isActive")
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
