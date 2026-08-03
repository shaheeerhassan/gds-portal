package com.school.dao.impl;

import com.school.dao.interfaces.StudentDao;
import com.school.exception.DaoException;
import com.school.model.Student;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDaoImpl implements StudentDao {

    private static final String INSERT = "INSERT INTO students (user_id, registration_number, first_name, last_name, date_of_birth, gender, admission_date, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM students WHERE student_id = ?";
    private static final String SELECT_BY_USER_ID = "SELECT * FROM students WHERE user_id = ?";
    private static final String SELECT_BY_REGISTRATION = "SELECT * FROM students WHERE registration_number = ?";
    private static final String SELECT_ALL = "SELECT * FROM students ORDER BY student_id";
    private static final String SELECT_BY_NAME = "SELECT * FROM students WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY student_id";
    private static final String SELECT_BY_CLASS = "SELECT DISTINCT s.* FROM students s JOIN student_classes sc ON s.student_id = sc.student_id WHERE sc.class_id = ? AND sc.academic_year_id = ? AND sc.is_active = TRUE ORDER BY sc.roll_number, s.student_id";
    private static final String SELECT_BY_SECTION = "SELECT DISTINCT s.* FROM students s JOIN student_classes sc ON s.student_id = sc.student_id WHERE sc.section_id = ? AND sc.is_active = TRUE ORDER BY sc.roll_number, s.student_id";
    private static final String UPDATE = "UPDATE students SET registration_number = ?, first_name = ?, last_name = ?, date_of_birth = ?, gender = ?, admission_date = ?, is_active = ? WHERE student_id = ?";
    private static final String UPDATE_STATUS = "UPDATE students SET is_active = ? WHERE student_id = ?";

    @Override
    public boolean insertStudent(Student student) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, student.getUserId());
            ps.setString(2, student.getRegistrationNumber());
            ps.setString(3, student.getFirstName());
            ps.setString(4, student.getLastName());
            if (student.getDateOfBirth() != null) {
                ps.setDate(5, Date.valueOf(student.getDateOfBirth()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, student.getGender().name());
            if (student.getAdmissionDate() != null) {
                ps.setDate(7, Date.valueOf(student.getAdmissionDate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setBoolean(8, student.isActive());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    student.setStudentId(resultSet.getLong(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting student", e);
        }
    }

    @Override
    public List<Student> getAllStudents() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<Student> students = new ArrayList<>();
            while (resultSet.next())
                students.add(mapRow(resultSet));
            return students;
        } catch (SQLException e) {
            throw new DaoException("Error fetching students", e);
        }
    }

    @Override
    public List<Student> getAllStudentsByName(String name) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_NAME)) {

            String pattern = "%" + name + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Student> students = new ArrayList<>();
                while (resultSet.next())
                    students.add(mapRow(resultSet));
                return students;
            }
        } catch (SQLException e) {
            throw new DaoException("Error searching students", e);
        }
    }

    @Override
    public List<Student> getAllStudentsByClass(int classId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_CLASS)) {

            ps.setInt(1, classId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Student> students = new ArrayList<>();
                while (resultSet.next())
                    students.add(mapRow(resultSet));
                return students;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching students by class", e);
        }
    }

    @Override
    public List<Student> getAllStudentsBySection(int sectionId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SECTION)) {

            ps.setInt(1, sectionId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Student> students = new ArrayList<>();
                while (resultSet.next())
                    students.add(mapRow(resultSet));
                return students;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching students by section", e);
        }
    }

    @Override
    public Student getStudentByUserId(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_USER_ID)) {

            ps.setLong(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching student", e);
        }
    }

    @Override
    public Student getStudentByStudentId(long studentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, studentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching student", e);
        }
    }

    @Override
    public Student getStudentByRegistrationNumber(String registrationNumber) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_REGISTRATION)) {

            ps.setString(1, registrationNumber);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching student", e);
        }
    }

    @Override
    public boolean updateStudentDetails(Student student) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, student.getRegistrationNumber());
            ps.setString(2, student.getFirstName());
            ps.setString(3, student.getLastName());
            if (student.getDateOfBirth() != null) {
                ps.setDate(4, Date.valueOf(student.getDateOfBirth()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, student.getGender().name());
            if (student.getAdmissionDate() != null) {
                ps.setDate(6, Date.valueOf(student.getAdmissionDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setBoolean(7, student.isActive());
            ps.setLong(8, student.getStudentId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating student", e);
        }
    }

    @Override
    public boolean deleteStudent(long studentId, boolean isActive) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE_STATUS)) {

            ps.setBoolean(1, isActive);
            ps.setLong(2, studentId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting student", e);
        }
    }

    private Student mapRow(ResultSet resultSet) throws SQLException {
        Student student = new Student();
        student.setStudentId(resultSet.getLong("student_id"));
        student.setUserId(resultSet.getLong("user_id"));
        student.setRegistrationNumber(resultSet.getString("registration_number"));
        student.setFirstName(resultSet.getString("first_name"));
        student.setLastName(resultSet.getString("last_name"));
        Date dateOfBirth = resultSet.getDate("date_of_birth");
        if (dateOfBirth != null)
            student.setDateOfBirth(dateOfBirth.toLocalDate());
        String gender = resultSet.getString("gender");
        if (gender != null)
            student.setGender(Student.Gender.valueOf(gender));
        Date admissionDate = resultSet.getDate("admission_date");
        if (admissionDate != null)
            student.setAdmissionDate(admissionDate.toLocalDate());
        student.setActive(resultSet.getBoolean("is_active"));
        return student;
    }
}