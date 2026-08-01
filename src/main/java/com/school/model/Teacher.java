package com.school.model;

import java.time.LocalDate;

public class Teacher {
    public enum Gender { MALE, FEMALE, OTHER }
    private long teacherId;
    private long userId;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String phone;
    private Gender gender;
    private LocalDate dateOfBirth;
    private LocalDate hireDate;
    private String qualification;
    private boolean isActive;

    public Teacher() {}
    public Teacher(long teacherId, long userId, String employeeId, String firstName, String lastName, String phone, Gender gender, LocalDate dateOfBirth, LocalDate hireDate, String qualification, boolean isActive) {
        this.teacherId = teacherId; this.userId = userId; this.employeeId = employeeId; this.firstName = firstName; this.lastName = lastName; this.phone = phone; this.gender = gender; this.dateOfBirth = dateOfBirth; this.hireDate = hireDate; this.qualification = qualification; this.isActive = isActive;
    }

    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }
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
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}

