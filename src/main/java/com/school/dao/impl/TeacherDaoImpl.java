package com.school.dao.impl;

import com.school.dao.interfaces.TeacherDao;
import com.school.exceptions.DaoException;
import com.school.model.Section;
import com.school.model.Teacher;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TeacherDaoImpl implements TeacherDao {

    private static final String INSERT = "INSERT INTO teachers (user_id, employee_id, first_name, last_name, phone, gender, date_of_birth, hire_date, qualification, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM teachers WHERE teacher_id = ?";
    private static final String SELECT_BY_USER_ID = "SELECT * FROM teachers WHERE user_id = ?";
    private static final String SELECT_BY_EMPLOYEE_ID = "SELECT * FROM teachers WHERE employee_id = ?";
    private static final String SELECT_BY_EMAIL = "SELECT t.* FROM teachers t JOIN users u ON t.user_id = u.user_id WHERE u.email = ?";
    private static final String SELECT_ALL = "SELECT * FROM teachers ORDER BY teacher_id";
    private static final String SELECT_BY_NAME_EMPID = "SELECT * FROM teachers WHERE first_name LIKE ? OR last_name LIKE ? OR employee_id LIKE ? ORDER BY teacher_id";
    private static final String SELECT_BY_SUBJECT = "SELECT DISTINCT t.* FROM teachers t JOIN teacher_subjects ts ON t.teacher_id = ts.teacher_id WHERE ts.subject_id = ? ORDER BY t.teacher_id";
    private static final String SELECT_BY_SECTION = "SELECT DISTINCT t.* FROM teachers t JOIN teacher_classes tc ON t.teacher_id = tc.teacher_id WHERE tc.section_id = ? AND tc.academic_year_id = ? ORDER BY t.teacher_id";
    private static final String SELECT_CLASS_TEACHERS = "SELECT t.*, s.* FROM teachers t JOIN class_teacher_assignments cta ON t.teacher_id = cta.teacher_id JOIN sections s ON cta.section_id = s.section_id WHERE cta.is_active = TRUE";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM teachers";
    private static final String UPDATE = "UPDATE teachers SET employee_id = ?, first_name = ?, last_name = ?, phone = ?, gender = ?, date_of_birth = ?, hire_date = ?, qualification = ? WHERE teacher_id = ?";
    private static final String DELETE = "UPDATE teachers SET is_active = FALSE WHERE teacher_id = ?";

    @Override
    public boolean insertTeacher(Teacher teacher) {
        try (Connection cn = getDataSource().getConnection()) {
            return insertTeacher(teacher, cn);
        } catch (SQLException e) {
            throw new DaoException("Error inserting teacher", e);
        }
    }

    @Override
    public boolean insertTeacher(Teacher teacher, Connection cn) {
        try (PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, teacher.getUserId());
            ps.setString(2, teacher.getEmployeeId());
            ps.setString(3, teacher.getFirstName());
            ps.setString(4, teacher.getLastName());
            ps.setString(5, teacher.getPhone());
            ps.setString(6, teacher.getGender() != null ? teacher.getGender().name() : null);
            if (teacher.getDateOfBirth() != null) {
                ps.setDate(7, Date.valueOf(teacher.getDateOfBirth()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            if (teacher.getHireDate() != null) {
                ps.setDate(8, Date.valueOf(teacher.getHireDate()));
            } else {
                ps.setNull(8, Types.DATE);
            }
            ps.setString(9, teacher.getQualification());
            ps.setBoolean(10, teacher.isActive());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    teacher.setTeacherId(resultSet.getLong(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting teacher", e);
        }
    }

    @Override
    public List<Teacher> getAllTeachers() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            List<Teacher> teachers = new ArrayList<>();
            while (resultSet.next())
                teachers.add(mapTeacher(resultSet));
            return teachers;
        } catch (SQLException e) {
            throw new DaoException("Error fetching teachers", e);
        }
    }

    @Override
    public List<Teacher> searchTeachersByNameEmpId(String name) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_NAME_EMPID)) {

            String pattern = "%" + name + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Teacher> teachers = new ArrayList<>();
                while (resultSet.next())
                    teachers.add(mapTeacher(resultSet));
                return teachers;
            }
        } catch (SQLException e) {
            throw new DaoException("Error searching teachers", e);
        }
    }

    @Override
    public List<Teacher> getAllTeachersBySubject(int subjectId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SUBJECT)) {

            ps.setInt(1, subjectId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Teacher> teachers = new ArrayList<>();
                while (resultSet.next())
                    teachers.add(mapTeacher(resultSet));
                return teachers;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching teachers by subject", e);
        }
    }

    @Override
    public List<Teacher> getTeachersBySection(int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SECTION)) {

            ps.setInt(1, sectionId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Teacher> teachers = new ArrayList<>();
                while (resultSet.next())
                    teachers.add(mapTeacher(resultSet));
                return teachers;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching teachers by section", e);
        }
    }

    @Override
    public Map<Long, List<Section>> getAllClassTeachers() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_CLASS_TEACHERS);
             ResultSet resultSet = ps.executeQuery()) {

            Map<Long, List<Section>> result = new LinkedHashMap<>();
            while (resultSet.next()) {
                Teacher teacher = mapTeacher(resultSet);
                result.computeIfAbsent(teacher.getTeacherId(), k -> new ArrayList<>())
                      .add(mapSection(resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new DaoException("Error fetching class teachers", e);
        }
    }

    @Override
    public Teacher getTeacherByUserId(long userId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_USER_ID)) {

            ps.setLong(1, userId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapTeacher(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching teacher", e);
        }
    }

    @Override
    public Teacher getTeacherById(long teacherId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, teacherId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapTeacher(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching teacher", e);
        }
    }

    @Override
    public Teacher getTeacherByEmployeeId(String employeeId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_EMPLOYEE_ID)) {

            ps.setString(1, employeeId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapTeacher(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching teacher", e);
        }
    }

    @Override
    public Teacher getTeacherByEmail(String email) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_EMAIL)) {

            ps.setString(1, email);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapTeacher(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching teacher", e);
        }
    }

    @Override
    public int getTeacherCount() {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_ALL);
             ResultSet resultSet = ps.executeQuery()) {

            if (resultSet.next())
                return resultSet.getInt(1);
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Error counting teachers", e);
        }
    }

    @Override
    public boolean updateTeacherDetails(Teacher teacher) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, teacher.getEmployeeId());
            ps.setString(2, teacher.getFirstName());
            ps.setString(3, teacher.getLastName());
            ps.setString(4, teacher.getPhone());
            ps.setString(5, teacher.getGender() != null ? teacher.getGender().name() : null);
            if (teacher.getDateOfBirth() != null) {
                ps.setDate(6, Date.valueOf(teacher.getDateOfBirth()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            if (teacher.getHireDate() != null) {
                ps.setDate(7, Date.valueOf(teacher.getHireDate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, teacher.getQualification());
            ps.setLong(9, teacher.getTeacherId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating teacher", e);
        }
    }

    @Override
    public boolean deleteTeacher(long teacherId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setLong(1, teacherId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting teacher", e);
        }
    }

    private Teacher mapTeacher(ResultSet resultSet) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setTeacherId(resultSet.getLong("teacher_id"));
        teacher.setUserId(resultSet.getLong("user_id"));
        teacher.setEmployeeId(resultSet.getString("employee_id"));
        teacher.setFirstName(resultSet.getString("first_name"));
        teacher.setLastName(resultSet.getString("last_name"));
        teacher.setPhone(resultSet.getString("phone"));
        String gender = resultSet.getString("gender");
        if (gender != null)
            teacher.setGender(Teacher.Gender.valueOf(gender));
        Date dateOfBirth = resultSet.getDate("date_of_birth");
        if (dateOfBirth != null)
            teacher.setDateOfBirth(dateOfBirth.toLocalDate());
        Date hireDate = resultSet.getDate("hire_date");
        if (hireDate != null)
            teacher.setHireDate(hireDate.toLocalDate());
        teacher.setQualification(resultSet.getString("qualification"));
        teacher.setActive(resultSet.getBoolean("is_active"));
        return teacher;
    }

    private Section mapSection(ResultSet resultSet) throws SQLException {
        Section section = new Section();
        section.setSectionId(resultSet.getInt("section_id"));
        section.setClassId(resultSet.getInt("class_id"));
        section.setAcademicYearId(resultSet.getInt("academic_year_id"));
        section.setSectionName(resultSet.getString("section_name"));
        int capacity = resultSet.getInt("capacity");
        if (!resultSet.wasNull())
            section.setCapacity(capacity);
        section.setRoomNumber(resultSet.getString("room_number"));
        return section;
    }
}