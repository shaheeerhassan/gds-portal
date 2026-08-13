package com.school.dao.impl;

import com.school.dao.interfaces.StudentDao;
import com.school.exceptions.DaoException;
import com.school.model.Student;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.school.web.dto.response.StudentDirectoryDTO;

public class StudentDaoImpl implements StudentDao {

    private static final String INSERT = "INSERT INTO students (user_id, registration_number, first_name, last_name, date_of_birth, gender, admission_date, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM students WHERE student_id = ?";
    private static final String SELECT_BY_USER_ID = "SELECT * FROM students WHERE user_id = ?";
    private static final String SELECT_BY_REGISTRATION = "SELECT * FROM students WHERE registration_number = ?";
    private static final String SELECT_ALL = "SELECT * FROM students ORDER BY student_id";
    private static final String SELECT_BY_NAME = "SELECT * FROM students WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY student_id";
    private static final String SELECT_BY_CLASS = "SELECT DISTINCT s.* FROM students s JOIN student_classes sc ON s.student_id = sc.student_id WHERE sc.class_id = ? AND sc.academic_year_id = ? AND sc.is_active = TRUE ORDER BY s.student_id";
    private static final String SELECT_BY_SECTION = "SELECT DISTINCT s.* FROM students s JOIN student_classes sc ON s.student_id = sc.student_id WHERE sc.section_id = ? AND sc.academic_year_id = (SELECT academic_year_id FROM sections WHERE section_id = ?) AND sc.is_active = TRUE ORDER BY s.student_id";
    private static final String SELECT_BY_PARENT = "SELECT DISTINCT s.* FROM students s JOIN student_parent_links spl ON s.student_id = spl.student_id WHERE spl.parent_id = ? ORDER BY s.student_id";
    private static final String UPDATE = "UPDATE students SET registration_number = ?, first_name = ?, last_name = ?, date_of_birth = ?, gender = ?, admission_date = ? WHERE student_id = ?";
    private static final String UPDATE_STATUS = "UPDATE students SET is_active = ? WHERE student_id = ?";
    private static final String COUNT_ACTIVE = "SELECT COUNT(*) FROM students WHERE is_active = TRUE";

    @Override
    public boolean insertStudent(Student student) {
        try (Connection cn = getDataSource().getConnection()) {
            return insertStudent(student, cn);
        } catch (SQLException e) {
            throw new DaoException("Error inserting student", e);
        }
    }

    @Override
    public boolean insertStudent(Student student, Connection cn) {
        try (PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, student.getUserId());
            ps.setString(2, student.getRegistrationNumber());
            ps.setString(3, student.getFirstName());
            ps.setString(4, student.getLastName());
            if (student.getDateOfBirth() != null) {
                ps.setDate(5, Date.valueOf(student.getDateOfBirth()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, student.getGender() != null ? student.getGender().name() : null);
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
            ps.setInt(2, sectionId);

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
    public List<Student> getAllStudentsByParentId(long parentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_PARENT)) {

            ps.setLong(1, parentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Student> students = new ArrayList<>();
                while (resultSet.next())
                    students.add(mapRow(resultSet));
                return students;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching students by parent", e);
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
            ps.setString(5, student.getGender() != null ? student.getGender().name() : null);
            if (student.getAdmissionDate() != null) {
                ps.setDate(6, Date.valueOf(student.getAdmissionDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setLong(7, student.getStudentId());

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
            throw new DaoException("Error updating student status", e);
        }
    }

    @Override
    public int countActiveStudents() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_ACTIVE);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Error counting active students", e);
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

    @Override
    public List<StudentDirectoryDTO> getStudentDirectory(String query, Integer academicYearId, Integer classId, Integer sectionId, Boolean isEnrolled, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
            "SELECT s.student_id, s.registration_number, s.first_name, s.last_name, s.is_active, " +
            "sc.academic_year_id, ay.name as academic_year_name, sc.class_id, c.name as class_name, " +
            "sc.section_id, sec.name as section_name, sc.roll_number " +
            "FROM students s " +
            "LEFT JOIN student_classes sc ON s.student_id = sc.student_id AND sc.academic_year_id = ? AND sc.is_active = TRUE " +
            "LEFT JOIN academic_years ay ON sc.academic_year_id = ay.academic_year_id " +
            "LEFT JOIN classes c ON sc.class_id = c.class_id " +
            "LEFT JOIN sections sec ON sc.section_id = sec.section_id " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        params.add(academicYearId != null ? academicYearId : 0);

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (s.first_name LIKE ? OR s.last_name LIKE ? OR CONCAT(s.first_name, ' ', s.last_name) LIKE ? OR s.registration_number LIKE ?) ");
            String likeQuery = "%" + query.trim() + "%";
            params.add(likeQuery);
            params.add(likeQuery);
            params.add(likeQuery);
            params.add(likeQuery);
        }

        if (classId != null) {
            sql.append("AND sc.class_id = ? ");
            params.add(classId);
        }

        if (sectionId != null) {
            sql.append("AND sc.section_id = ? ");
            params.add(sectionId);
        }

        if (isEnrolled != null) {
            if (isEnrolled) {
                sql.append("AND sc.student_id IS NOT NULL ");
            } else {
                sql.append("AND sc.student_id IS NULL ");
            }
        }

        sql.append("ORDER BY s.first_name, s.last_name LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
             
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<StudentDirectoryDTO> result = new ArrayList<>();
                while (rs.next()) {
                    StudentDirectoryDTO dto = new StudentDirectoryDTO();
                    dto.setStudentId(rs.getLong("student_id"));
                    dto.setRegistrationNumber(rs.getString("registration_number"));
                    dto.setFirstName(rs.getString("first_name"));
                    dto.setLastName(rs.getString("last_name"));
                    dto.setActive(rs.getBoolean("is_active"));

                    long accYear = rs.getLong("academic_year_id");
                    if (!rs.wasNull()) {
                        dto.setAcademicYearId((int) accYear);
                        dto.setAcademicYearName(rs.getString("academic_year_name"));
                        dto.setClassId(rs.getInt("class_id"));
                        dto.setClassName(rs.getString("class_name"));
                        dto.setSectionId(rs.getInt("section_id"));
                        dto.setSectionName(rs.getString("section_name"));
                        dto.setRollNumber(rs.getString("roll_number"));
                    }
                    result.add(dto);
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching student directory", e);
        }
    }

    @Override
    public int countStudentDirectory(String query, Integer academicYearId, Integer classId, Integer sectionId, Boolean isEnrolled) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(s.student_id) " +
            "FROM students s " +
            "LEFT JOIN student_classes sc ON s.student_id = sc.student_id AND sc.academic_year_id = ? AND sc.is_active = TRUE " +
            "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        params.add(academicYearId != null ? academicYearId : 0);

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (s.first_name LIKE ? OR s.last_name LIKE ? OR CONCAT(s.first_name, ' ', s.last_name) LIKE ? OR s.registration_number LIKE ?) ");
            String likeQuery = "%" + query.trim() + "%";
            params.add(likeQuery);
            params.add(likeQuery);
            params.add(likeQuery);
            params.add(likeQuery);
        }

        if (classId != null) {
            sql.append("AND sc.class_id = ? ");
            params.add(classId);
        }

        if (sectionId != null) {
            sql.append("AND sc.section_id = ? ");
            params.add(sectionId);
        }

        if (isEnrolled != null) {
            if (isEnrolled) {
                sql.append("AND sc.student_id IS NOT NULL ");
            } else {
                sql.append("AND sc.student_id IS NULL ");
            }
        }

        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
             
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Error counting student directory", e);
        }
        return 0;
    }
}