package com.school.dao.impl;

import com.school.dao.interfaces.TeacherSubjectDao;
import com.school.exceptions.DaoException;
import com.school.dto.TeacherSubjectDTO;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TeacherSubjectDaoImpl implements TeacherSubjectDao {

    private static final String INSERT = "INSERT INTO teacher_subjects (teacher_id, subject_id, section_id, academic_year_id, assigned_at) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_TEACHER = 
        "SELECT ts.teacher_subject_id, ts.teacher_id, ts.subject_id, s.subject_name, ts.section_id, sec.section_name, c.class_id, c.class_name, ts.academic_year_id " +
        "FROM teacher_subjects ts " +
        "JOIN subjects s ON ts.subject_id = s.subject_id " +
        "JOIN sections sec ON ts.section_id = sec.section_id " +
        "JOIN classes c ON sec.class_id = c.class_id " +
        "WHERE ts.teacher_id = ? AND ts.academic_year_id = ? " +
        "ORDER BY s.subject_name, c.numeric_level, sec.section_name";
    private static final String DELETE = "DELETE FROM teacher_subjects WHERE teacher_subject_id = ?";
    private static final String COUNT_ASSIGNED = "SELECT COUNT(*) FROM teacher_subjects WHERE teacher_id = ? AND subject_id = ? AND section_id = ? AND academic_year_id = ?";
    private static final String COUNT_BY_SECTION_SUBJECT = "SELECT COUNT(*) FROM teacher_subjects WHERE subject_id = ? AND section_id = ? AND academic_year_id = ?";
    private static final String COUNT_OTHER_SUBJECTS = "SELECT COUNT(*) FROM teacher_subjects WHERE teacher_id = ? AND section_id = ? AND academic_year_id = ? AND teacher_subject_id != ?";
    private static final String SELECT_BY_ID = 
        "SELECT ts.teacher_subject_id, ts.teacher_id, ts.subject_id, s.subject_name, ts.section_id, sec.section_name, c.class_id, c.class_name, ts.academic_year_id " +
        "FROM teacher_subjects ts " +
        "JOIN subjects s ON ts.subject_id = s.subject_id " +
        "JOIN sections sec ON ts.section_id = sec.section_id " +
        "JOIN classes c ON sec.class_id = c.class_id " +
        "WHERE ts.teacher_subject_id = ?";

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
    public boolean isAssigned(long teacherId, int subjectId, int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_ASSIGNED)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, subjectId);
            ps.setInt(3, sectionId);
            ps.setInt(4, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking teacher subject assignment", e);
        }
    }

    @Override
    public boolean isSubjectAssignedInSection(int subjectId, int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_BY_SECTION_SUBJECT)) {

            ps.setInt(1, subjectId);
            ps.setInt(2, sectionId);
            ps.setInt(3, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking if subject is assigned in section", e);
        }
    }

    @Override
    public boolean hasOtherSubjectsInSection(long teacherId, int sectionId, int academicYearId, long excludeTeacherSubjectId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_OTHER_SUBJECTS)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, sectionId);
            ps.setInt(3, academicYearId);
            ps.setLong(4, excludeTeacherSubjectId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking other subjects in section", e);
        }
    }

    @Override
    public List<TeacherSubjectDTO> getTeacherSubjects(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<TeacherSubjectDTO> assignments = new ArrayList<>();
                while (resultSet.next()) {
                    TeacherSubjectDTO dto = new TeacherSubjectDTO();
                    dto.setTeacherSubjectId(resultSet.getLong("teacher_subject_id"));
                    dto.setTeacherId(resultSet.getLong("teacher_id"));
                    dto.setSubjectId(resultSet.getInt("subject_id"));
                    dto.setSubjectName(resultSet.getString("subject_name"));
                    dto.setClassId(resultSet.getInt("class_id"));
                    dto.setClassName(resultSet.getString("class_name"));
                    dto.setSectionId(resultSet.getInt("section_id"));
                    dto.setSectionName(resultSet.getString("section_name"));
                    dto.setAcademicYearId(resultSet.getInt("academic_year_id"));
                    assignments.add(dto);
                }
                return assignments;
            }
        } catch (SQLException e) {
            throw new DaoException("Error getting teacher subjects", e);
        }
    }

    @Override
    public TeacherSubjectDTO getTeacherSubjectById(long teacherSubjectId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, teacherSubjectId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    TeacherSubjectDTO dto = new TeacherSubjectDTO();
                    dto.setTeacherSubjectId(resultSet.getLong("teacher_subject_id"));
                    dto.setTeacherId(resultSet.getLong("teacher_id"));
                    dto.setSubjectId(resultSet.getInt("subject_id"));
                    dto.setSubjectName(resultSet.getString("subject_name"));
                    dto.setClassId(resultSet.getInt("class_id"));
                    dto.setClassName(resultSet.getString("class_name"));
                    dto.setSectionId(resultSet.getInt("section_id"));
                    dto.setSectionName(resultSet.getString("section_name"));
                    dto.setAcademicYearId(resultSet.getInt("academic_year_id"));
                    return dto;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error getting teacher subject by id", e);
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
}