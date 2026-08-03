package com.school.dao.impl;

import com.school.dao.interfaces.AssignmentDao;
import com.school.exception.DaoException;
import com.school.model.Assignment;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDaoImpl implements AssignmentDao {

    private static final String INSERT = "INSERT INTO assignments (teacher_id, subject_id, section_id, title, description, max_marks, deadline, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM assignments WHERE assignment_id = ?";
    private static final String SELECT_BY_SECTION = "SELECT a.* FROM assignments a JOIN sections s ON a.section_id = s.section_id WHERE a.section_id = ? AND s.academic_year_id = ? ORDER BY a.assignment_id";
    private static final String SELECT_BY_TEACHER = "SELECT a.* FROM assignments a JOIN sections s ON a.section_id = s.section_id WHERE a.teacher_id = ? AND s.academic_year_id = ? ORDER BY a.assignment_id";
    private static final String UPDATE = "UPDATE assignments SET subject_id = ?, section_id = ?, title = ?, description = ?, max_marks = ?, deadline = ?, status = ?, updated_at = ? WHERE assignment_id = ?";
    private static final String DELETE = "DELETE FROM assignments WHERE assignment_id = ?";

    @Override
    public boolean insertAssignment(Assignment assignment) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, assignment.getTeacherId());
            ps.setInt(2, assignment.getSubjectId());
            ps.setInt(3, assignment.getSectionId());
            ps.setString(4, assignment.getTitle());
            ps.setString(5, assignment.getDescription());
            ps.setDouble(6, assignment.getMaxMarks());
            if (assignment.getDeadline() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(assignment.getDeadline()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            ps.setString(8, assignment.getStatus().name());
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(9, Timestamp.valueOf(now));
            ps.setTimestamp(10, Timestamp.valueOf(now));

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    assignment.setAssignmentId(resultSet.getLong(1));
                    assignment.setCreatedAt(now);
                    assignment.setUpdatedAt(now);
                }
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting assignment", e);
        }
    }

    @Override
    public boolean updateAssignment(Assignment assignment) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setInt(1, assignment.getSubjectId());
            ps.setInt(2, assignment.getSectionId());
            ps.setString(3, assignment.getTitle());
            ps.setString(4, assignment.getDescription());
            ps.setDouble(5, assignment.getMaxMarks());
            if (assignment.getDeadline() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(assignment.getDeadline()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.setString(7, assignment.getStatus().name());
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(9, assignment.getAssignmentId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating assignment", e);
        }
    }

    @Override
    public boolean deleteAssignment(long assignmentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setLong(1, assignmentId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting assignment", e);
        }
    }

    @Override
    public Assignment getAssignmentById(long assignmentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, assignmentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching assignment", e);
        }
    }

    @Override
    public List<Assignment> getAssignmentsBySection(int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SECTION)) {

            ps.setInt(1, sectionId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Assignment> assignments = new ArrayList<>();
                while (resultSet.next())
                    assignments.add(mapRow(resultSet));
                return assignments;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching assignments", e);
        }
    }

    @Override
    public List<Assignment> getAssignmentsByTeacher(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Assignment> assignments = new ArrayList<>();
                while (resultSet.next())
                    assignments.add(mapRow(resultSet));
                return assignments;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching assignments", e);
        }
    }

    private Assignment mapRow(ResultSet resultSet) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(resultSet.getLong("assignment_id"));
        assignment.setTeacherId(resultSet.getLong("teacher_id"));
        assignment.setSubjectId(resultSet.getInt("subject_id"));
        assignment.setSectionId(resultSet.getInt("section_id"));
        assignment.setTitle(resultSet.getString("title"));
        assignment.setDescription(resultSet.getString("description"));
        assignment.setMaxMarks(resultSet.getDouble("max_marks"));
        Timestamp deadline = resultSet.getTimestamp("deadline");
        if (deadline != null)
            assignment.setDeadline(deadline.toLocalDateTime());
        String status = resultSet.getString("status");
        if (status != null)
            assignment.setStatus(Assignment.Status.valueOf(status));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null)
            assignment.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null)
            assignment.setUpdatedAt(updatedAt.toLocalDateTime());
        return assignment;
    }
}