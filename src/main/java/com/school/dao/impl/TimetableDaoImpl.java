package com.school.dao.impl;

import com.school.dao.interfaces.TimetableDao;
import com.school.exceptions.DaoException;
import com.school.model.Timetable;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimetableDaoImpl implements TimetableDao {

    private static final String INSERT = "INSERT INTO timetables (section_id, subject_id, teacher_id, period_id, day_of_week, academic_year_id) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_SECTION_AND_YEAR = "SELECT * FROM timetables WHERE section_id = ? AND academic_year_id = ? ORDER BY day_of_week, period_id";
    private static final String SELECT_BY_TEACHER = "SELECT * FROM timetables WHERE teacher_id = ? AND academic_year_id = ? ORDER BY day_of_week, period_id";
    private static final String SELECT_BY_SECTION_AND_DAY = "SELECT * FROM timetables WHERE section_id = ? AND day_of_week = ? AND academic_year_id = ? ORDER BY period_id";
    private static final String UPDATE = "UPDATE timetables SET section_id = ?, subject_id = ?, teacher_id = ?, period_id = ?, day_of_week = ?, academic_year_id = ? WHERE timetable_id = ?";
    private static final String DELETE = "DELETE FROM timetables WHERE timetable_id = ?";

    @Override
    public boolean insertTimeTable(Timetable timetable) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, timetable.getSectionId());
            ps.setInt(2, timetable.getSubjectId());
            ps.setLong(3, timetable.getTeacherId());
            ps.setInt(4, timetable.getPeriodId());
            ps.setString(5, timetable.getDayOfWeek().name());
            ps.setInt(6, timetable.getAcademicYearId());

            int success = ps.executeUpdate();

            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next())
                    timetable.setTimetableId(resultSet.getLong(1));
            }

            return success == 1;
        } catch (SQLException e) {
            throw new DaoException("Error inserting timetable", e);
        }
    }

    @Override
    public boolean updateTimeTable(Timetable timetable) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE)) {

            ps.setInt(1, timetable.getSectionId());
            ps.setInt(2, timetable.getSubjectId());
            ps.setLong(3, timetable.getTeacherId());
            ps.setInt(4, timetable.getPeriodId());
            ps.setString(5, timetable.getDayOfWeek().name());
            ps.setInt(6, timetable.getAcademicYearId());
            ps.setLong(7, timetable.getTimetableId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating timetable", e);
        }
    }

    @Override
    public boolean deleteTimetable(long timetableId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(DELETE)) {

            ps.setLong(1, timetableId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting timetable", e);
        }
    }

    @Override
    public List<Timetable> getTimetableBySectionAndYear(int sectionId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SECTION_AND_YEAR)) {

            ps.setInt(1, sectionId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Timetable> timetables = new ArrayList<>();
                while (resultSet.next())
                    timetables.add(mapRow(resultSet));
                return timetables;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching timetable", e);
        }
    }

    @Override
    public List<Timetable> getTimetableByTeacher(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Timetable> timetables = new ArrayList<>();
                while (resultSet.next())
                    timetables.add(mapRow(resultSet));
                return timetables;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching timetable", e);
        }
    }

    @Override
    public List<Timetable> getTimetableByDay(int sectionId, Timetable.DayOfWeek day, int academicYearId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SECTION_AND_DAY)) {

            ps.setInt(1, sectionId);
            ps.setString(2, day.name());
            ps.setInt(3, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Timetable> timetables = new ArrayList<>();
                while (resultSet.next())
                    timetables.add(mapRow(resultSet));
                return timetables;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching timetable", e);
        }
    }

    private Timetable mapRow(ResultSet resultSet) throws SQLException {
        Timetable timetable = new Timetable();
        timetable.setTimetableId(resultSet.getLong("timetable_id"));
        timetable.setSectionId(resultSet.getInt("section_id"));
        timetable.setSubjectId(resultSet.getInt("subject_id"));
        timetable.setTeacherId(resultSet.getLong("teacher_id"));
        timetable.setPeriodId(resultSet.getInt("period_id"));
        String dayOfWeek = resultSet.getString("day_of_week");
        if (dayOfWeek != null)
            timetable.setDayOfWeek(Timetable.DayOfWeek.valueOf(dayOfWeek));
        timetable.setAcademicYearId(resultSet.getInt("academic_year_id"));
        return timetable;
    }
}