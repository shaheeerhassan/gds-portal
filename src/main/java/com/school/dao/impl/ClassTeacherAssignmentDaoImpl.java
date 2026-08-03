package com.school.dao.impl;

import com.school.dao.interfaces.ClassTeacherAssignmentDao;
import com.school.exception.DaoException;
import com.school.model.ClassTeacherAssignment;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClassTeacherAssignmentDaoImpl implements ClassTeacherAssignmentDao {

    private static final String INSERT = "INSERT INTO class_teacher_assignments (teacher_id, section_id, academic_year_id, assigned_date, is_active) VALUES (?, ?, ?, ?, ?)";
    private static final String DELETE = "UPDATE class_teacher_assignments SET is_active = FALSE, removed_date = ? WHERE section_id = ? AND academic_year_id = ? AND is_active = TRUE";
    private static final String SELECT_CURRENT_BY_SECTION = "SELECT * FROM class_teacher_assignments WHERE section_id = ? AND is_active = TRUE";
    private static final String SELECT_CURRENT_BY_TEACHER = "SELECT * FROM class_teacher_assignments WHERE teacher_id = ? AND academic_year_id = ? AND is_active = TRUE";
    private static final String SELECT_HISTORY_BY_TEACHER = "SELECT * FROM class_teacher_assignments WHERE teacher_id = ? ORDER BY assigned_date DESC";
    private static final String SELECT_HISTORY_BY_SECTION = "SELECT * FROM class_teacher_assignments WHERE section_id = ? ORDER BY assigned_date DESC";
    private static final String COUNT_ASSIGNED = "SELECT COUNT(*) FROM class_teacher_assignments WHERE teacher_id = ? AND academic_year_id = ? AND is_active = TRUE";

    @Override
    public boolean assignClassTeacher(ClassTeacherAssignment assignment) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, assignment.getTeacherId());
            ps.setInt(2, assignment.getSectionId());
            ps.setInt(3, assignment.getAcademicYearId());
            LocalDate assignedDate = assignment.getAssignedDate() != null
                    ? assignment.getAssignedDate() : LocalDate.now();
            ps.setDate(4, Date.valueOf(assignedDate));
            ps.setBoolean(5, true);

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    assignment.setAssignmentId(resultSet.getLong(1));
                    assignment.setAssignedDate(assignedDate);
                    assignment.setActive(true);
                }
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error assigning class teacher", e);
        }
    }

    @Override
    public boolean deleteClassTeacher(int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setInt(2, sectionId);
            ps.setInt(3, academicYearId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting class teacher assignment", e);
        }
    }

    @Override
    public ClassTeacherAssignment getCurrentAssignmentBySection(int sectionId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_CURRENT_BY_SECTION)) {

            ps.setInt(1, sectionId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching class teacher assignment", e);
        }
    }

    @Override
    public ClassTeacherAssignment getCurrentAssignmentForTeacher(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_CURRENT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching class teacher assignment", e);
        }
    }

    @Override
    public boolean isTeacherAssignedAsClassTeacher(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_ASSIGNED)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking class teacher assignment", e);
        }
    }

    @Override
    public List<ClassTeacherAssignment> getAssignmentHistoryForTeacher(long teacherId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_HISTORY_BY_TEACHER)) {

            ps.setLong(1, teacherId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<ClassTeacherAssignment> assignments = new ArrayList<>();
                while (resultSet.next())
                    assignments.add(mapRow(resultSet));
                return assignments;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching assignment history", e);
        }
    }

    @Override
    public List<ClassTeacherAssignment> getAssignmentHistoryForSection(int sectionId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_HISTORY_BY_SECTION)) {

            ps.setInt(1, sectionId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<ClassTeacherAssignment> assignments = new ArrayList<>();
                while (resultSet.next())
                    assignments.add(mapRow(resultSet));
                return assignments;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching assignment history", e);
        }
    }

    private ClassTeacherAssignment mapRow(ResultSet resultSet) throws SQLException {
        ClassTeacherAssignment assignment = new ClassTeacherAssignment();
        assignment.setAssignmentId(resultSet.getLong("assignment_id"));
        assignment.setTeacherId(resultSet.getLong("teacher_id"));
        assignment.setSectionId(resultSet.getInt("section_id"));
        assignment.setAcademicYearId(resultSet.getInt("academic_year_id"));
        Date assignedDate = resultSet.getDate("assigned_date");
        if (assignedDate != null)
            assignment.setAssignedDate(assignedDate.toLocalDate());
        Date removedDate = resultSet.getDate("removed_date");
        if (removedDate != null)
            assignment.setRemovedDate(removedDate.toLocalDate());
        assignment.setActive(resultSet.getBoolean("is_active"));
        return assignment;
    }
}