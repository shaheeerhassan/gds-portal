package com.school.dao.impl;

import com.school.dao.interfaces.StudentClassDao;
import com.school.exceptions.DaoException;
import com.school.model.StudentClass;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentClassDaoImpl implements StudentClassDao {

    private static final String INSERT = "INSERT INTO student_classes (student_id, class_id, section_id, academic_year_id, roll_number, enrollment_date, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String COUNT_ENROLLED = "SELECT COUNT(*) FROM student_classes WHERE student_id = ? AND academic_year_id = ? AND is_active = TRUE";
    private static final String SELECT_CURRENT_BY_STUDENT = "SELECT * FROM student_classes WHERE student_id = ? AND is_active = TRUE";
    private static final String SELECT_HISTORY_BY_STUDENT = "SELECT * FROM student_classes WHERE student_id = ? ORDER BY enrollment_date DESC";
    private static final String SELECT_BY_SECTION = "SELECT * FROM student_classes WHERE section_id = ? AND academic_year_id = ? AND is_active = TRUE ORDER BY roll_number";
    private static final String END_ENROLLMENT = "UPDATE student_classes SET is_active = FALSE WHERE student_id = ? AND academic_year_id = ? AND is_active = TRUE";
    private static final String UPDATE_ROLL_NUMBER = "UPDATE student_classes SET roll_number = ? WHERE student_id = ? AND academic_year_id = ? AND is_active = TRUE";
    private static final String SELECT_SECTION_CLASS_ID = "SELECT class_id FROM sections WHERE section_id = ?";
    private static final String END_ENROLLMENT_SOURCE = "UPDATE student_classes SET is_active = FALSE WHERE section_id = ? AND academic_year_id = ? AND is_active = TRUE";
    private static final String DELETE_OLD_ENROLLMENT = "DELETE FROM student_classes WHERE section_id = ? AND academic_year_id = ? AND is_active = TRUE";
    private static final String DELETE_STUDENT_CLASS = "DELETE FROM student_classes WHERE student_id = ? AND academic_year_id = ?";

    @Override
    public boolean enrollStudent(StudentClass studentClass) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, studentClass.getStudentId());
            ps.setInt(2, studentClass.getClassId());
            ps.setInt(3, studentClass.getSectionId());
            ps.setInt(4, studentClass.getAcademicYearId());
            ps.setString(5, studentClass.getRollNumber());
            if (studentClass.getEnrollmentDate() != null) {
                ps.setDate(6, Date.valueOf(studentClass.getEnrollmentDate()));
            } else {
                studentClass.setEnrollmentDate(LocalDate.now());
                ps.setDate(6, Date.valueOf(studentClass.getEnrollmentDate()));
            }
            ps.setBoolean(7, studentClass.isActive());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    studentClass.setStudentClassId(resultSet.getLong(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error enrolling student", e);
        }
    }

    @Override
    public boolean isStudentEnrolled(long studentId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_ENROLLED)) {

            ps.setLong(1, studentId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking student enrollment", e);
        }
    }

    @Override
    public StudentClass getCurrentEnrollment(long studentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_CURRENT_BY_STUDENT)) {

            ps.setLong(1, studentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching enrollment", e);
        }
    }

    @Override
    public List<StudentClass> getEnrollmentHistory(long studentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_HISTORY_BY_STUDENT)) {

            ps.setLong(1, studentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<StudentClass> enrollments = new ArrayList<>();
                while (resultSet.next())
                    enrollments.add(mapRow(resultSet));
                return enrollments;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching enrollment history", e);
        }
    }

    @Override
    public List<StudentClass> getStudentsBySection(int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SECTION)) {

            ps.setInt(1, sectionId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<StudentClass> enrollments = new ArrayList<>();
                while (resultSet.next())
                    enrollments.add(mapRow(resultSet));
                return enrollments;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching students by section", e);
        }
    }

    @Override
    public boolean endEnrollment(long studentId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(END_ENROLLMENT)) {

            ps.setLong(1, studentId);
            ps.setInt(2, academicYearId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error ending enrollment", e);
        }
    }

    @Override
    public boolean transferStudent(StudentClass studentClass, int newSectionId) {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);

            boolean ended;
            try (PreparedStatement ps = cn.prepareStatement(DELETE_STUDENT_CLASS)) {
                ps.setLong(1, studentClass.getStudentId());
                ps.setInt(2, studentClass.getAcademicYearId());
                ps.executeUpdate();
            }

            int newClassId;
            try (PreparedStatement ps = cn.prepareStatement(SELECT_SECTION_CLASS_ID)) {
                ps.setInt(1, newSectionId);
                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet.next()) {
                        newClassId = resultSet.getInt("class_id");
                    } else {
                        cn.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, studentClass.getStudentId());
                ps.setInt(2, newClassId);
                ps.setInt(3, newSectionId);
                ps.setInt(4, studentClass.getAcademicYearId());
                ps.setString(5, studentClass.getRollNumber());
                ps.setDate(6, Date.valueOf(LocalDate.now()));
                ps.setBoolean(7, true);
                ended = ps.executeUpdate() > 0;
            }

            cn.commit();
            return ended;
        } catch (SQLException e) {
            if (cn != null) {
                try { cn.rollback(); } catch (SQLException ignore) {}
            }
            throw new DaoException("Error transferring student", e);
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public boolean updateRollNumber(long studentId, int academicYearId, String newRollNumber) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE_ROLL_NUMBER)) {

            ps.setString(1, newRollNumber);
            ps.setLong(2, studentId);
            ps.setInt(3, academicYearId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating roll number", e);
        }
    }

    @Override
    public int promoteSection(int sourceSectionId, int sourceAcademicYearId, int targetSectionId, int targetAcademicYearId) {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);

            int promoted;
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO student_classes (student_id, class_id, section_id, academic_year_id, roll_number, enrollment_date, is_active) " +
                    "SELECT sc.student_id, s2.class_id, ?, ?, sc.roll_number, ?, TRUE " +
                    "FROM student_classes sc JOIN sections s2 ON s2.section_id = ? " +
                    "WHERE sc.section_id = ? AND sc.academic_year_id = ? AND sc.is_active = TRUE")) {
                ps.setInt(1, targetSectionId);
                ps.setInt(2, targetAcademicYearId);
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.setInt(4, targetSectionId);
                ps.setInt(5, sourceSectionId);
                ps.setInt(6, sourceAcademicYearId);
                promoted = ps.executeUpdate();
            }

            if (promoted == 0) {
                cn.rollback();
                return 0;
            }

            try (PreparedStatement ps = cn.prepareStatement(END_ENROLLMENT_SOURCE)) {
                ps.setInt(1, sourceSectionId);
                ps.setInt(2, sourceAcademicYearId);
                ps.executeUpdate();
            }

            cn.commit();
            return promoted;
        } catch (SQLException e) {
            if (cn != null) {
                try { cn.rollback(); } catch (SQLException ignore) {}
            }
            throw new DaoException("Error promoting section", e);
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    private StudentClass mapRow(ResultSet resultSet) throws SQLException {
        StudentClass studentClass = new StudentClass();
        studentClass.setStudentClassId(resultSet.getLong("student_class_id"));
        studentClass.setStudentId(resultSet.getLong("student_id"));
        studentClass.setClassId(resultSet.getInt("class_id"));
        studentClass.setSectionId(resultSet.getInt("section_id"));
        studentClass.setAcademicYearId(resultSet.getInt("academic_year_id"));
        studentClass.setRollNumber(resultSet.getString("roll_number"));
        Date enrollmentDate = resultSet.getDate("enrollment_date");
        if (enrollmentDate != null)
            studentClass.setEnrollmentDate(enrollmentDate.toLocalDate());
        studentClass.setActive(resultSet.getBoolean("is_active"));
        return studentClass;
    }
}