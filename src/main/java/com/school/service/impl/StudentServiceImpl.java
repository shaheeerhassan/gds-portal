package com.school.service.impl;

import com.school.dao.impl.StudentDaoImpl;
import com.school.dao.interfaces.StudentDao;
import com.school.exceptions.DaoException;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.UnauthorizedException;
import com.school.exceptions.ValidationException;
import com.school.model.Student;
import com.school.model.User;
import com.school.service.interfaces.RoleService;
import com.school.service.interfaces.StudentService;
import com.school.service.interfaces.UserService;
import com.school.web.dto.response.PaginatedResponse;
import com.school.web.dto.response.StudentDirectoryDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.school.config.DBConfig.getDataSource;
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
        if (student.getAdmissionDate() == null)
            throw new ValidationException("Admission date is required.");

        user.setRoleId(roleService.getRoleByName("STUDENT").getRoleId());

        try (Connection cn = getDataSource().getConnection()) {
            cn.setAutoCommit(false);
            try {
                user = userService.createUser(user, password, cn);

                student.setUserId(user.getUserId());
                student.setActive(true);

                if (!studentDao.insertStudent(student, cn))
                    throw new IllegalStateException("Failed to create student.");

                cn.commit();
            } catch (Exception e) {
                try { cn.rollback(); } catch (SQLException ignore) {}
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("Error creating student", e);
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
        if (studentDao.getStudentByStudentId(studentId) == null)
            throw new ResourceNotFoundException("Student not found.");
        studentDao.deleteStudent(studentId, false);
        User user = userService.getUserById(studentDao.getStudentByStudentId(studentId).getUserId());
        userService.updateUserStatus(user.getUserId(), false);
    }

    @Override
    public int getActiveStudentCount() {
        return studentDao.countActiveStudents();
    }

    private void validateStudent(Student student) {
        validateRequired(student.getRegistrationNumber(), "Registration number");
        if (student.getGender() == null)
            throw new ValidationException("Gender is required.");
    }

    @Override
    public PaginatedResponse<StudentDirectoryDTO> getStudentDirectory(String query, Integer academicYearId, Integer classId, Integer sectionId, Boolean isEnrolled, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        int offset = page * size;
        List<StudentDirectoryDTO> content = studentDao.getStudentDirectory(query, academicYearId, classId, sectionId, isEnrolled, offset, size);
        int totalElements = studentDao.countStudentDirectory(query, academicYearId, classId, sectionId, isEnrolled);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;

        return new PaginatedResponse<>(content, totalElements, totalPages, page, size, hasNext);
    }
}
