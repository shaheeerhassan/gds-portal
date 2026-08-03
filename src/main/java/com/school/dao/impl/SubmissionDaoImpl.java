package com.school.dao.impl;

import com.school.dao.interfaces.SubmissionDao;
import com.school.exception.DaoException;
import com.school.model.Submission;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDaoImpl implements SubmissionDao {

    private static final String INSERT = "INSERT INTO submissions (assignment_id, student_id, submitted_at, file_url, status, marks_awarded, feedback, graded_by, graded_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM submissions WHERE submission_id = ?";
    private static final String SELECT_BY_ASSIGNMENT = "SELECT * FROM submissions WHERE assignment_id = ? ORDER BY submission_id";
    private static final String SELECT_BY_STUDENT = "SELECT * FROM submissions WHERE student_id = ? ORDER BY submission_id";
    private static final String SELECT_BY_STATUS = "SELECT * FROM submissions WHERE status = ? ORDER BY submission_id";
    private static final String SELECT_BY_ASSIGNMENT_AND_STUDENT = "SELECT * FROM submissions WHERE assignment_id = ? AND student_id = ?";
    private static final String UPDATE = "UPDATE submissions SET file_url = ?, status = ? WHERE submission_id = ?";
    private static final String MARK_LATE = "UPDATE submissions SET status = ? WHERE submission_id = ?";
    private static final String GRADE = "UPDATE submissions SET marks_awarded = ?, feedback = ?, status = ?, graded_at = ? WHERE submission_id = ?";
    private static final String DELETE = "DELETE FROM submissions WHERE submission_id = ?";
    private static final String COUNT_BY_ASSIGNMENT = "SELECT COUNT(*) FROM submissions WHERE assignment_id = ?";
    private static final String COUNT_EXISTS = "SELECT COUNT(*) FROM submissions WHERE assignment_id = ? AND student_id = ?";

    @Override
    public boolean insertSubmission(Submission submission) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, submission.getAssignmentId());
            ps.setLong(2, submission.getStudentId());
            LocalDateTime submittedAt = submission.getSubmittedAt() != null
                    ? submission.getSubmittedAt() : LocalDateTime.now();
            ps.setTimestamp(3, Timestamp.valueOf(submittedAt));
            ps.setString(4, submission.getFileUrl());
            ps.setString(5, submission.getStatus().name());
            if (submission.getMarksAwarded() != null) {
                ps.setDouble(6, submission.getMarksAwarded());
            } else {
                ps.setNull(6, Types.DOUBLE);
            }
            ps.setString(7, submission.getFeedback());
            if (submission.getGradedBy() != null) {
                ps.setLong(8, submission.getGradedBy());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            if (submission.getGradedAt() != null) {
                ps.setTimestamp(9, Timestamp.valueOf(submission.getGradedAt()));
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    submission.setSubmissionId(resultSet.getLong(1));
                    submission.setSubmittedAt(submittedAt);
                }
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting submission", e);
        }
    }

    @Override
    public boolean updateSubmission(Submission submission) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, submission.getFileUrl());
            ps.setString(2, submission.getStatus().name());
            ps.setLong(3, submission.getSubmissionId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating submission", e);
        }
    }

    @Override
    public boolean markSubmissionLate(long submissionId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(MARK_LATE)) {

            ps.setString(1, Submission.Status.LATE.name());
            ps.setLong(2, submissionId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error marking submission late", e);
        }
    }

    @Override
    public boolean gradeSubmission(long submissionId, double marksObtained, String feedback) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(GRADE)) {

            ps.setDouble(1, marksObtained);
            ps.setString(2, feedback);
            ps.setString(3, Submission.Status.GRADED.name());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(5, submissionId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error grading submission", e);
        }
    }

    @Override
    public boolean deleteSubmission(long submissionId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setLong(1, submissionId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting submission", e);
        }
    }

    @Override
    public boolean submissionExists(long assignmentId, long studentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_EXISTS)) {

            ps.setLong(1, assignmentId);
            ps.setLong(2, studentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking submission", e);
        }
    }

    @Override
    public int getSubmissionCount(long assignmentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_BY_ASSIGNMENT)) {

            ps.setLong(1, assignmentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Error counting submissions", e);
        }
    }

    @Override
    public List<Submission> getSubmissionsByAssignment(long assignmentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ASSIGNMENT)) {

            ps.setLong(1, assignmentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Submission> submissions = new ArrayList<>();
                while (resultSet.next())
                    submissions.add(mapRow(resultSet));
                return submissions;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching submissions", e);
        }
    }

    @Override
    public List<Submission> getSubmissionsByStudent(long studentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_STUDENT)) {

            ps.setLong(1, studentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Submission> submissions = new ArrayList<>();
                while (resultSet.next())
                    submissions.add(mapRow(resultSet));
                return submissions;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching submissions", e);
        }
    }

    @Override
    public List<Submission> getSubmissionsByStatus(Submission.Status status) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_STATUS)) {

            ps.setString(1, status.name());

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Submission> submissions = new ArrayList<>();
                while (resultSet.next())
                    submissions.add(mapRow(resultSet));
                return submissions;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching submissions", e);
        }
    }

    @Override
    public Submission getSubmission(long assignmentId, long studentId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ASSIGNMENT_AND_STUDENT)) {

            ps.setLong(1, assignmentId);
            ps.setLong(2, studentId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching submission", e);
        }
    }

    @Override
    public Submission getSubmissionById(long submissionId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, submissionId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching submission", e);
        }
    }

    private Submission mapRow(ResultSet resultSet) throws SQLException {
        Submission submission = new Submission();
        submission.setSubmissionId(resultSet.getLong("submission_id"));
        submission.setAssignmentId(resultSet.getLong("assignment_id"));
        submission.setStudentId(resultSet.getLong("student_id"));
        Timestamp submittedAt = resultSet.getTimestamp("submitted_at");
        if (submittedAt != null)
            submission.setSubmittedAt(submittedAt.toLocalDateTime());
        submission.setFileUrl(resultSet.getString("file_url"));
        String status = resultSet.getString("status");
        if (status != null)
            submission.setStatus(Submission.Status.valueOf(status));
        double marksAwarded = resultSet.getDouble("marks_awarded");
        if (!resultSet.wasNull())
            submission.setMarksAwarded(marksAwarded);
        submission.setFeedback(resultSet.getString("feedback"));
        long gradedBy = resultSet.getLong("graded_by");
        if (!resultSet.wasNull())
            submission.setGradedBy(gradedBy);
        Timestamp gradedAt = resultSet.getTimestamp("graded_at");
        if (gradedAt != null)
            submission.setGradedAt(gradedAt.toLocalDateTime());
        return submission;
    }
}