package com.school.dao.impl;

import com.school.dao.interfaces.MarkDao;
import com.school.exceptions.DaoException;
import com.school.model.Mark;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MarkDaoImpl implements MarkDao {

    private static final String INSERT = "INSERT INTO marks (examination_id, student_id, marks_obtained, grade, remarks, entered_by, entered_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM marks WHERE mark_id = ?";
    private static final String SELECT_BY_EXAMINATION = "SELECT * FROM marks WHERE examination_id = ? ORDER BY mark_id";
    private static final String SELECT_BY_STUDENT_AND_EXAMINATION = "SELECT * FROM marks WHERE student_id = ? AND examination_id = ?";
    private static final String SELECT_BY_STUDENT_AND_YEAR = "SELECT m.* FROM marks m JOIN examinations e ON m.examination_id = e.examination_id WHERE m.student_id = ? AND e.academic_year_id = ? ORDER BY m.mark_id";
    private static final String SELECT_BY_STUDENT_AND_EXAM_TYPE = "SELECT m.* FROM marks m JOIN examinations e ON m.examination_id = e.examination_id WHERE m.student_id = ? AND e.exam_name = ? AND e.academic_year_id = ? ORDER BY m.mark_id";
    private static final String UPDATE = "UPDATE marks SET marks_obtained = ?, grade = ?, remarks = ?, entered_by = ?, entered_at = ? WHERE mark_id = ?";
    private static final String DELETE = "DELETE FROM marks WHERE mark_id = ?";

    @Override
    public boolean insertMarks(List<Mark> marks) {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                for (Mark mark : marks) {
                    ps.setLong(1, mark.getExaminationId());
                    ps.setLong(2, mark.getStudentId());
                    ps.setDouble(3, mark.getMarksObtained());
                    ps.setString(4, mark.getGrade());
                    ps.setString(5, mark.getRemarks());
                    ps.setLong(6, mark.getEnteredBy());
                    ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                    ps.addBatch();
                }

                ps.executeBatch();

                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    int index = 0;
                    while (resultSet.next() && index < marks.size()) {
                        marks.get(index).setMarkId(resultSet.getLong(1));
                        index++;
                    }
                }
            }

            cn.commit();
            return true;
        } catch (SQLException e) {
            if (cn != null) {
                try { cn.rollback(); } catch (SQLException ignore) {}
            }
            throw new DaoException("Error inserting marks", e);
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public boolean updateMark(Mark mark) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setDouble(1, mark.getMarksObtained());
            ps.setString(2, mark.getGrade());
            ps.setString(3, mark.getRemarks());
            ps.setLong(4, mark.getEnteredBy());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(6, mark.getMarkId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating mark", e);
        }
    }

    @Override
    public boolean deleteMark(long markId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setLong(1, markId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting mark", e);
        }
    }

    @Override
    public List<Mark> getMarksByExamination(long examinationId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_EXAMINATION)) {

            ps.setLong(1, examinationId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Mark> marks = new ArrayList<>();
                while (resultSet.next())
                    marks.add(mapRow(resultSet));
                return marks;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching marks", e);
        }
    }

    @Override
    public List<Mark> getStudentMarksForYear(long studentId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_STUDENT_AND_YEAR)) {

            ps.setLong(1, studentId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Mark> marks = new ArrayList<>();
                while (resultSet.next())
                    marks.add(mapRow(resultSet));
                return marks;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching marks", e);
        }
    }

    @Override
    public List<Mark> getStudentMarksByExamType(long studentId, String examName, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_STUDENT_AND_EXAM_TYPE)) {

            ps.setLong(1, studentId);
            ps.setString(2, examName);
            ps.setInt(3, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Mark> marks = new ArrayList<>();
                while (resultSet.next())
                    marks.add(mapRow(resultSet));
                return marks;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching marks", e);
        }
    }

    @Override
    public Mark getMarkById(long markId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, markId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching mark", e);
        }
    }

    @Override
    public Mark getMarkByStudentAndExamination(long studentId, long examinationId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_STUDENT_AND_EXAMINATION)) {

            ps.setLong(1, studentId);
            ps.setLong(2, examinationId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching mark", e);
        }
    }

    private Mark mapRow(ResultSet resultSet) throws SQLException {
        Mark mark = new Mark();
        mark.setMarkId(resultSet.getLong("mark_id"));
        mark.setExaminationId(resultSet.getLong("examination_id"));
        mark.setStudentId(resultSet.getLong("student_id"));
        mark.setMarksObtained(resultSet.getDouble("marks_obtained"));
        mark.setGrade(resultSet.getString("grade"));
        mark.setRemarks(resultSet.getString("remarks"));
        mark.setEnteredBy(resultSet.getLong("entered_by"));
        Timestamp enteredAt = resultSet.getTimestamp("entered_at");
        if (enteredAt != null)
            mark.setEnteredAt(enteredAt.toLocalDateTime());
        return mark;
    }
}