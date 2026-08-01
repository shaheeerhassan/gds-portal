package com.school.model;

public class Administrator {
    private long adminId;
    private long userId;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String phone;

    public Administrator() {}
    public Administrator(long adminId, long userId, String employeeId, String firstName, String lastName, String phone) {
        this.adminId = adminId; this.userId = userId; this.employeeId = employeeId; this.firstName = firstName; this.lastName = lastName; this.phone = phone;
    }

    public long getAdminId() { return adminId; }
    public void setAdminId(long adminId) { this.adminId = adminId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}