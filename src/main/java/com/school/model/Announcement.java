package com.school.model;

import java.time.LocalDateTime;

public class Announcement {
    private long announcementId;
    private String title;
    private String content;
    private long createdBy;
    private Integer targetRoleId; // Integer wrapper for nullable IDs
    private Integer classId;
    private Integer sectionId;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Announcement() {}
    public Announcement(long announcementId, String title, String content, long createdBy, Integer targetRoleId, Integer classId, Integer sectionId, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.announcementId = announcementId; this.title = title; this.content = content; this.createdBy = createdBy; this.targetRoleId = targetRoleId; this.classId = classId; this.sectionId = sectionId; this.isActive = isActive; this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public long getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(long announcementId) { this.announcementId = announcementId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }
    public Integer getTargetRoleId() { return targetRoleId; }
    public void setTargetRoleId(Integer targetRoleId) { this.targetRoleId = targetRoleId; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public Integer getSectionId() { return sectionId; }
    public void setSectionId(Integer sectionId) { this.sectionId = sectionId; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
