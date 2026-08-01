package com.school.model;

import java.time.LocalDate;

public class Student {
    public enum Gender { MALE, FEMALE, OTHER }
    private long studentId;
    private long userId;
    private String registrationNumber;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private LocalDate admissionDate;
    private boolean isActive;

    public Student() {}
    public Student(long studentId, long userId, String registrationNumber, String firstName, String lastName, LocalDate dateOfBirth, Gender gender, LocalDate admissionDate, boolean isActive) {
        this.studentId = studentId; this.userId = userId; this.registrationNumber = registrationNumber; this.firstName = firstName; this.lastName = lastName; this.dateOfBirth = dateOfBirth; this.gender = gender; this.admissionDate = admissionDate; this.isActive = isActive;
    }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(LocalDate admissionDate) { this.admissionDate = admissionDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
