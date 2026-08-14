package com.school.dao.impl;

import com.school.dao.interfaces.TeacherAttendanceDao;
import com.school.exceptions.DaoException;
import com.school.model.TeacherAttendance;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TeacherAttendanceDaoImpl implements TeacherAttendanceDao {

    private static final String INSERT = "INSERT INTO teacher_attendance (teacher_id, attendance_date, status, marked_at) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM teacher_attendance WHERE attendance_id = ?";
    private static final String SELECT_BY_DATE = "SELECT * FROM teacher_attendance WHERE attendance_date = ? ORDER BY attendance_id";
    private static final String SELECT_BY_TEACHER = "SELECT * FROM teacher_attendance WHERE teacher_id = ? AND attendance_date BETWEEN ? AND ? ORDER BY attendance_date";
    private static final String UPDATE_STATUS = "UPDATE teacher_attendance SET status = ? WHERE attendance_id = ?";
    private static final String COUNT_EXISTS = "SELECT COUNT(*) FROM teacher_attendance WHERE teacher_id = ? AND attendance_date = ?";

    @Override
    public boolean insertTeacherAttendance(List<TeacherAttendance> attendanceRecords) {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                for (TeacherAttendance attendance : attendanceRecords) {
                    ps.setLong(1, attendance.getTeacherId());
                    if (attendance.getAttendanceDate() != null) {
                        ps.setDate(2, Date.valueOf(attendance.getAttendanceDate()));
                    } else {
                        ps.setNull(2, Types.DATE);
                    }
                    ps.setString(3, attendance.getStatus().name());
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    ps.addBatch();
                }

                ps.executeBatch();

                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    int index = 0;
                    while (resultSet.next() && index < attendanceRecords.size()) {
                        attendanceRecords.get(index).setAttendanceId(resultSet.getLong(1));
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
            throw new DaoException("Error inserting attendance records", e);
        } finally {
            if (cn != null) {
                try { cn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public boolean updateTeacherAttendance(long attendanceId, TeacherAttendance.Status status) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(UPDATE_STATUS)) {

            ps.setString(1, status.name());
            ps.setLong(2, attendanceId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating attendance", e);
        }
    }


    @Override
    public boolean existsAttendance(long teacherId, LocalDate date) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_EXISTS)) {

            ps.setLong(1, teacherId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking teacher attendance", e);
        }
    }

    @Override
    public List<TeacherAttendance> getTeacherAttendanceByDate(LocalDate date) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_DATE)) {

            ps.setDate(1, Date.valueOf(date));

            try (ResultSet resultSet = ps.executeQuery()) {
                List<TeacherAttendance> attendances = new ArrayList<>();
                while (resultSet.next())
                    attendances.add(mapRow(resultSet));
                return attendances;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching attendance", e);
        }
    }

    @Override
    public List<TeacherAttendance> getTeacherAttendanceByTeacherId(long teacherId, LocalDate startDate, LocalDate endDate) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));

            try (ResultSet resultSet = ps.executeQuery()) {
                List<TeacherAttendance> attendances = new ArrayList<>();
                while (resultSet.next())
                    attendances.add(mapRow(resultSet));
                return attendances;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching attendance", e);
        }
    }

    @Override
    public TeacherAttendance getTeacherAttendanceById(long attendanceId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, attendanceId);

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error fetching attendance", e);
        }
    }

    private TeacherAttendance mapRow(ResultSet resultSet) throws SQLException {
        TeacherAttendance attendance = new TeacherAttendance();
        attendance.setAttendanceId(resultSet.getLong("attendance_id"));
        attendance.setTeacherId(resultSet.getLong("teacher_id"));
        Date attendanceDate = resultSet.getDate("attendance_date");
        if (attendanceDate != null)
            attendance.setAttendanceDate(attendanceDate.toLocalDate());
        String status = resultSet.getString("status");
        if (status != null)
            attendance.setStatus(TeacherAttendance.Status.valueOf(status));
        Timestamp markedAt = resultSet.getTimestamp("marked_at");
        if (markedAt != null)
            attendance.setMarkedAt(markedAt.toLocalDateTime());
        return attendance;
    }
}