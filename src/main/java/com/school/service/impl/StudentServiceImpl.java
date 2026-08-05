package com.school.service.impl;

import com.school.dao.impl.StudentDaoImpl;
import com.school.dao.interfaces.StudentDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.UnauthorizedException;
import com.school.exceptions.ValidationException;
import com.school.model.Student;
import com.school.model.User;
import com.school.service.interfaces.RoleService;
import com.school.service.interfaces.StudentService;
import com.school.service.interfaces.UserService;

import java.util.List;

import static com.school.validations.ValidatorUtil.*;

public class StudentServiceImpl implements StudentService {

    private final StudentDao studentDao;
    private final UserService userService;
    private final RoleService roleService;

    public StudentServiceImpl() {
        studentDao = new StudentDaoImpl();
        userService = new UserServiceImpl();
        roleService = new RoleServiceImpl();
    }

    @Override
    public Student createStudent(User user, String password, Student student) {
        validateStudent(student);
        student.setFirstName(validateName(student.getFirstName(), "First name"));
        student.setLastName(validateName(student.getLastName(), "Last name"));

        user.setRoleId(roleService.getRoleByName("STUDENT").getRoleId());
        user = userService.createUser(user, password);

        student.setUserId(user.getUserId());
        student.setActive(true);

        try {
            if (!studentDao.insertStudent(student)) {
                throw new IllegalStateException("Failed to create student.");
            }
        } catch (Exception e) {
            userService.deleteUser(user.getUserId());
            throw e;
        }

        return student;
    }

    @Override
    public Student getStudentByStudentId(long studentId) {
        validateId(studentId);
        Student student = studentDao.getStudentByStudentId(studentId);
        if (student == null)
            throw new ResourceNotFoundException("Student not found.");
        return student;
    }

    @Override
    public Student getStudentByUserId(long userId) {
        validateId(userId);
        Student student = studentDao.getStudentByUserId(userId);
        if (student == null)
            throw new ResourceNotFoundException("Student not found.");
        return student;
    }

    @Override
    public Student getStudentByRegistrationNumber(String registrationNumber) {
        registrationNumber = validateRequired(registrationNumber, "Registration number");
        Student student = studentDao.getStudentByRegistrationNumber(registrationNumber);
        if (student == null)
            throw new ResourceNotFoundException("Student not found.");
        return student;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentDao.getAllStudents();
    }

    @Override
    public List<Student> searchStudentsByName(String name) {
        name = validateRequired(name, "Name");
        return studentDao.getAllStudentsByName(name);
    }

    @Override
    public List<Student> getStudentsByClass(int classId, int academicYearId) {
        validateId(classId);
        validateId(academicYearId);
        return studentDao.getAllStudentsByClass(classId, academicYearId);
    }

    @Override
    public List<Student> getStudentsBySection(int sectionId) {
        validateId(sectionId);
        return studentDao.getAllStudentsBySection(sectionId);
    }

    @Override
    public List<Student> getStudentsByParentId(long parentId) {
        validateId(parentId);
        return studentDao.getAllStudentsByParentId(parentId);
    }

    @Override
    public void updateStudent(Student student) {
        validateId(student.getStudentId());

        Student existing = studentDao.getStudentByStudentId(student.getStudentId());

        if (existing == null)
            throw new ResourceNotFoundException("Student not found.");

        if (student.getRegistrationNumber() == null)
            student.setRegistrationNumber(existing.getRegistrationNumber());

        if (student.getFirstName() == null)
            student.setFirstName(existing.getFirstName());

        if (student.getLastName() == null)
            student.setLastName(existing.getLastName());

        if (student.getDateOfBirth() == null)
            student.setDateOfBirth(existing.getDateOfBirth());

        if (student.getGender() == null)
            student.setGender(existing.getGender());

        if (student.getAdmissionDate() == null)
            student.setAdmissionDate(existing.getAdmissionDate());

        student.setUserId(existing.getUserId());
        student.setActive(existing.isActive());

        validateStudent(student);

        student.setFirstName(
                validateName(student.getFirstName(), "First name")
        );

        student.setLastName(
                validateName(student.getLastName(), "Last name")
        );

        if (!studentDao.updateStudentDetails(student))
            throw new ResourceNotFoundException("Student not found.");
    }

    @Override
    public void deactivateStudent(long studentId) {
        validateId(studentId);
        if (!studentDao.deleteStudent(studentId, false))
            throw new ResourceNotFoundException("Student not found.");
    }

    private void validateStudent(Student student) {
        validateRequired(student.getRegistrationNumber(), "Registration number");
        if (student.getGender() == null)
            throw new ValidationException("Gender is required.");
    }
}
