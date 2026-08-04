package com.school.dao.impl;

import com.school.dao.interfaces.TeacherSubjectDao;
import com.school.exceptions.DaoException;
import com.school.model.Subject;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TeacherSubjectDaoImpl implements TeacherSubjectDao {

    private static final String INSERT = "INSERT INTO teacher_subjects (teacher_id, subject_id, section_id, academic_year_id, assigned_at) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_TEACHER = "SELECT DISTINCT s.* FROM subjects s JOIN teacher_subjects ts ON s.subject_id = ts.subject_id WHERE ts.teacher_id = ? AND ts.academic_year_id = ? ORDER BY s.subject_id";
    private static final String DELETE = "DELETE FROM teacher_subjects WHERE teacher_subject_id = ?";

    @Override
    public boolean assignTeacherSubject(long teacherId, int subjectId, int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, subjectId);
            ps.setInt(3, sectionId);
            ps.setInt(4, academicYearId);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error assigning teacher subject", e);
        }
    }

    @Override
    public List<Subject> getTeacherSubjects(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Subject> subjects = new ArrayList<>();
                while (resultSet.next())
                    subjects.add(mapSubject(resultSet));
                return subjects;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching teacher subjects", e);
        }
    }

    @Override
    public boolean unassignTeacherSubject(long teacherSubjectId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setLong(1, teacherSubjectId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error unassigning teacher subject", e);
        }
    }

    private Subject mapSubject(ResultSet resultSet) throws SQLException {
        Subject subject = new Subject();
        subject.setSubjectId(resultSet.getInt("subject_id"));
        subject.setSubjectName(resultSet.getString("subject_name"));
        subject.setSubjectCode(resultSet.getString("subject_code"));
        subject.setDescription(resultSet.getString("description"));
        return subject;
    }
}