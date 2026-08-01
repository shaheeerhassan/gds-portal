package com.school.model;

public class Parent {
    private long parentId;
    private long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String occupation;
    private boolean isActive;

    public Parent() {}
    public Parent(long parentId, long userId, String firstName, String lastName, String phone, String occupation, boolean isActive) {
        this.parentId = parentId; this.userId = userId; this.firstName = firstName; this.lastName = lastName; this.phone = phone; this.occupation = occupation; this.isActive = isActive;
    }

    public long getParentId() { return parentId; }
    public void setParentId(long parentId) { this.parentId = parentId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
