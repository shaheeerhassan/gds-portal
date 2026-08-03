package com.school.dao.impl;

import com.school.dao.interfaces.ExaminationDao;
import com.school.exception.DaoException;
import com.school.model.Examination;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ExaminationDaoImpl implements ExaminationDao {

    private static final String INSERT = "INSERT INTO examinations (exam_name, subject_id, section_id, academic_year_id, exam_date, start_time, end_time, max_marks, passing_marks, status, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM examinations WHERE examination_id = ?";
    private static final String SELECT_BY_SECTION = "SELECT * FROM examinations WHERE section_id = ? AND academic_year_id = ? ORDER BY examination_id";
    private static final String SELECT_SPECIFIC_BY_SECTION = "SELECT * FROM examinations WHERE section_id = ? AND exam_name = ? AND academic_year_id = ? ORDER BY examination_id";
    private static final String SELECT_BY_TEACHER = "SELECT DISTINCT e.* FROM examinations e JOIN teacher_subjects ts ON e.subject_id = ts.subject_id WHERE ts.teacher_id = ? AND e.academic_year_id = ? ORDER BY e.examination_id";
    private static final String SELECT_SPECIFIC_BY_TEACHER = "SELECT DISTINCT e.* FROM examinations e JOIN teacher_subjects ts ON e.subject_id = ts.subject_id WHERE ts.teacher_id = ? AND e.exam_name = ? AND e.academic_year_id = ? ORDER BY e.examination_id";
    private static final String SELECT_BY_TEACHER_AND_SECTION = "SELECT DISTINCT e.* FROM examinations e JOIN teacher_subjects ts ON e.subject_id = ts.subject_id WHERE ts.teacher_id = ? AND e.section_id = ? AND e.academic_year_id = ? ORDER BY e.examination_id";
    private static final String SELECT_SPECIFIC_BY_TEACHER_AND_SECTION = "SELECT DISTINCT e.* FROM examinations e JOIN teacher_subjects ts ON e.subject_id = ts.subject_id WHERE ts.teacher_id = ? AND e.section_id = ? AND e.exam_name = ? AND e.academic_year_id = ? ORDER BY e.examination_id";
    private static final String UPDATE_STATUS = "UPDATE examinations SET status = ? WHERE examination_id = ?";
    private static final String UPDATE = "UPDATE examinations SET exam_name = ?, subject_id = ?, section_id = ?, academic_year_id = ?, exam_date = ?, start_time = ?, end_time = ?, max_marks = ?, passing_marks = ?, status = ? WHERE examination_id = ?";

    @Override
    public boolean insertExamination(Examination examination) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, examination.getExamName());
            ps.setInt(2, examination.getSubjectId());
            ps.setInt(3, examination.getSectionId());
            ps.setInt(4, examination.getAcademicYearId());
            if (examination.getExamDate() != null) {
                ps.setDate(5, Date.valueOf(examination.getExamDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            if (examination.getStartTime() != null) {
                ps.setTime(6, Time.valueOf(examination.getStartTime()));
            } else {
                ps.setNull(6, Types.TIME);
            }
            if (examination.getEndTime() != null) {
                ps.setTime(7, Time.valueOf(examination.getEndTime()));
            } else {
                ps.setNull(7, Types.TIME);
            }
            ps.setDouble(8, examination.getMaxMarks());
            if (examination.getPassingMarks() != null) {
                ps.setDouble(9, examination.getPassingMarks());
            } else {
                ps.setNull(9, Types.DOUBLE);
            }
            ps.setString(10, examination.getStatus().name());
            ps.setLong(11, examination.getCreatedBy());
            LocalDateTime now = LocalDateTime.now();
            ps.setTimestamp(12, Timestamp.valueOf(now));

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    examination.setExaminationId(resultSet.getLong(1));
                    examination.setCreatedAt(now);
                }
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting examination", e);
        }
    }

    @Override
    public Examination getExaminationById(long examinationId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, examinationId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching examination", e);
        }
    }

    @Override
    public List<Examination> getExaminationsBySection(int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SECTION)) {

            ps.setInt(1, sectionId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Examination> examinations = new ArrayList<>();
                while (resultSet.next())
                    examinations.add(mapRow(resultSet));
                return examinations;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching examinations", e);
        }
    }

    @Override
    public List<Examination> getSpecificExaminationsBySection(int sectionId, String examName, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_SPECIFIC_BY_SECTION)) {

            ps.setInt(1, sectionId);
            ps.setString(2, examName);
            ps.setInt(3, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Examination> examinations = new ArrayList<>();
                while (resultSet.next())
                    examinations.add(mapRow(resultSet));
                return examinations;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching examinations", e);
        }
    }

    @Override
    public boolean updateExaminationStatus(long examinationId, Examination.Status status) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE_STATUS)) {

            ps.setString(1, status.name());
            ps.setLong(2, examinationId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating examination status", e);
        }
    }

    @Override
    public boolean updateExamination(Examination examination) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setString(1, examination.getExamName());
            ps.setInt(2, examination.getSubjectId());
            ps.setInt(3, examination.getSectionId());
            ps.setInt(4, examination.getAcademicYearId());
            if (examination.getExamDate() != null) {
                ps.setDate(5, Date.valueOf(examination.getExamDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            if (examination.getStartTime() != null) {
                ps.setTime(6, Time.valueOf(examination.getStartTime()));
            } else {
                ps.setNull(6, Types.TIME);
            }
            if (examination.getEndTime() != null) {
                ps.setTime(7, Time.valueOf(examination.getEndTime()));
            } else {
                ps.setNull(7, Types.TIME);
            }
            ps.setDouble(8, examination.getMaxMarks());
            if (examination.getPassingMarks() != null) {
                ps.setDouble(9, examination.getPassingMarks());
            } else {
                ps.setNull(9, Types.DOUBLE);
            }
            ps.setString(10, examination.getStatus().name());
            ps.setLong(11, examination.getExaminationId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating examination", e);
        }
    }

    @Override
    public List<Examination> getExaminationsByTeacher(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Examination> examinations = new ArrayList<>();
                while (resultSet.next())
                    examinations.add(mapRow(resultSet));
                return examinations;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching examinations", e);
        }
    }

    @Override
    public List<Examination> getSpecificExaminationsByTeacher(long teacherId, String examName, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_SPECIFIC_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setString(2, examName);
            ps.setInt(3, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Examination> examinations = new ArrayList<>();
                while (resultSet.next())
                    examinations.add(mapRow(resultSet));
                return examinations;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching examinations", e);
        }
    }

    @Override
    public List<Examination> getExaminationsByTeacherAndSection(long teacherId, int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TEACHER_AND_SECTION)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, sectionId);
            ps.setInt(3, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Examination> examinations = new ArrayList<>();
                while (resultSet.next())
                    examinations.add(mapRow(resultSet));
                return examinations;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching examinations", e);
        }
    }

    @Override
    public List<Examination> getSpecificExaminationsByTeacherAndSection(long teacherId, int sectionId, String examName, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_SPECIFIC_BY_TEACHER_AND_SECTION)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, sectionId);
            ps.setString(3, examName);
            ps.setInt(4, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Examination> examinations = new ArrayList<>();
                while (resultSet.next())
                    examinations.add(mapRow(resultSet));
                return examinations;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching examinations", e);
        }
    }

    private Examination mapRow(ResultSet resultSet) throws SQLException {
        Examination examination = new Examination();
        examination.setExaminationId(resultSet.getLong("examination_id"));
        examination.setExamName(resultSet.getString("exam_name"));
        examination.setSubjectId(resultSet.getInt("subject_id"));
        examination.setSectionId(resultSet.getInt("section_id"));
        examination.setAcademicYearId(resultSet.getInt("academic_year_id"));
        Date examDate = resultSet.getDate("exam_date");
        if (examDate != null)
            examination.setExamDate(examDate.toLocalDate());
        Time startTime = resultSet.getTime("start_time");
        if (startTime != null)
            examination.setStartTime(startTime.toLocalTime());
        Time endTime = resultSet.getTime("end_time");
        if (endTime != null)
            examination.setEndTime(endTime.toLocalTime());
        examination.setMaxMarks(resultSet.getDouble("max_marks"));
        double passingMarks = resultSet.getDouble("passing_marks");
        if (!resultSet.wasNull())
            examination.setPassingMarks(passingMarks);
        String status = resultSet.getString("status");
        if (status != null)
            examination.setStatus(Examination.Status.valueOf(status));
        examination.setCreatedBy(resultSet.getLong("created_by"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null)
            examination.setCreatedAt(createdAt.toLocalDateTime());
        return examination;
    }
}