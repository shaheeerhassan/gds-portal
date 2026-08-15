package com.school.dao.impl;

import com.school.dao.interfaces.StudentAttendanceDao;
import com.school.exceptions.DaoException;
import com.school.model.StudentAttendance;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceDaoImpl implements StudentAttendanceDao {

    private static final String INSERT = "INSERT INTO student_attendance (student_class_id, attendance_date, status, period_id, marked_by, marked_at, is_locked, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM student_attendance WHERE attendance_id = ?";
    private static final String SELECT_BY_SECTION_AND_DATE = "SELECT sa.* FROM student_attendance sa JOIN student_classes sc ON sa.student_class_id = sc.student_class_id WHERE sc.section_id = ? AND sa.attendance_date = ? ORDER BY sa.attendance_id";
    private static final String SELECT_BY_STUDENT = "SELECT sa.* FROM student_attendance sa JOIN student_classes sc ON sa.student_class_id = sc.student_class_id WHERE sc.student_id = ? AND sa.attendance_date BETWEEN ? AND ? ORDER BY sa.attendance_date";
    private static final String UPDATE_STATUS = "UPDATE student_attendance SET status = ? WHERE attendance_id = ?";
    private static final String LOCK_DATE = "UPDATE student_attendance sa JOIN student_classes sc ON sa.student_class_id = sc.student_class_id SET sa.is_locked = TRUE WHERE sc.section_id = ? AND sa.attendance_date = ?";
    private static final String COUNT_EXISTS = "SELECT COUNT(*) FROM student_attendance WHERE student_class_id = ? AND attendance_date = ? AND (period_id = ? OR (? IS NULL AND period_id IS NULL))";
    private static final String COUNT_LOCKED = "SELECT COUNT(*) FROM student_attendance WHERE student_class_id = ? AND attendance_date = ? AND is_locked = TRUE";
    private static final String COUNT_PRESENT = "SELECT COUNT(DISTINCT student_class_id) FROM student_attendance WHERE attendance_date = ? AND status = 'PRESENT'";

    @Override
    public boolean insertStudentAttendance(List<StudentAttendance> attendanceRecords) {
        Connection cn = null;
        try {
            cn = getDataSource().getConnection();
            cn.setAutoCommit(false);

            try (PreparedStatement ps = cn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                for (StudentAttendance attendance : attendanceRecords) {
                    ps.setLong(1, attendance.getStudentClassId());
                    if (attendance.getAttendanceDate() != null) {
                        ps.setDate(2, Date.valueOf(attendance.getAttendanceDate()));
                    } else {
                        ps.setNull(2, Types.DATE);
                    }
                    ps.setString(3, attendance.getStatus().name());
                    if (attendance.getPeriodId() > 0) {
                        ps.setInt(4, attendance.getPeriodId());
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }
                    ps.setLong(5, attendance.getMarkedBy());
                    ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setBoolean(7, attendance.isLocked());
                    ps.setString(8, attendance.getRemarks());
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
    public boolean updateStudentAttendance(long attendanceId, StudentAttendance.Status status) {
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
    public boolean lockAttendanceForDate(LocalDate date, int sectionId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(LOCK_DATE)) {

            ps.setInt(1, sectionId);
            ps.setDate(2, Date.valueOf(date));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Error locking attendance", e);
        }
    }

    @Override
    public boolean existsAttendance(long studentClassId, LocalDate date, Integer periodId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_EXISTS)) {

            ps.setLong(1, studentClassId);
            ps.setDate(2, Date.valueOf(date));
            if (periodId != null) {
                ps.setInt(3, periodId);
                ps.setInt(4, periodId);
            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking attendance", e);
        }
    }

    @Override
    public boolean isAttendanceLocked(long studentClassId, LocalDate date) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_LOCKED)) {

            ps.setLong(1, studentClassId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DaoException("Error checking attendance lock", e);
        }
    }

    @Override
    public List<StudentAttendance> getStudentAttendanceBySectionAndDate(int sectionId, LocalDate date) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_SECTION_AND_DATE)) {

            ps.setInt(1, sectionId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet resultSet = ps.executeQuery()) {
                List<StudentAttendance> attendances = new ArrayList<>();
                while (resultSet.next())
                    attendances.add(mapRow(resultSet));
                return attendances;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching attendance", e);
        }
    }

    @Override
    public List<StudentAttendance> getStudentAttendanceByStudentId(long studentId, LocalDate startDate, LocalDate endDate) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_STUDENT)) {

            ps.setLong(1, studentId);
            ps.setDate(2, Date.valueOf(startDate));
            ps.setDate(3, Date.valueOf(endDate));

            try (ResultSet resultSet = ps.executeQuery()) {
                List<StudentAttendance> attendances = new ArrayList<>();
                while (resultSet.next())
                    attendances.add(mapRow(resultSet));
                return attendances;
            }
        } catch (SQLException e) {
            throw new DaoException("Error fetching attendance", e);
        }
    }

    @Override
    public StudentAttendance getStudentAttendanceById(long attendanceId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRowToStudentAttendance(rs);
            }
        } catch (SQLException e) {
            throw new DaoException("Error retrieving student attendance by id", e);
        }
        return null;
    }

    @Override
    public int countPresentStudents(LocalDate date) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(COUNT_PRESENT)) {

            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new DaoException("Error counting present students", e);
        }
    }

    private StudentAttendance mapRow(ResultSet resultSet) throws SQLException {
        StudentAttendance attendance = new StudentAttendance();
        attendance.setAttendanceId(resultSet.getLong("attendance_id"));
        attendance.setStudentClassId(resultSet.getLong("student_class_id"));
        Date attendanceDate = resultSet.getDate("attendance_date");
        if (attendanceDate != null)
            attendance.setAttendanceDate(attendanceDate.toLocalDate());
        String status = resultSet.getString("status");
        if (status != null)
            attendance.setStatus(StudentAttendance.Status.valueOf(status));
        attendance.setPeriodId(resultSet.getInt("period_id"));
        attendance.setMarkedBy(resultSet.getLong("marked_by"));
        Timestamp markedAt = resultSet.getTimestamp("marked_at");
        if (markedAt != null)
            attendance.setMarkedAt(markedAt.toLocalDateTime());
        attendance.setLocked(resultSet.getBoolean("is_locked"));
        attendance.setRemarks(resultSet.getString("remarks"));
        return attendance;
    }

    private StudentAttendance mapRowToStudentAttendance(ResultSet rs) throws SQLException {
        StudentAttendance attendance = new StudentAttendance();
        attendance.setAttendanceId(rs.getLong("attendance_id"));
        attendance.setStudentClassId(rs.getLong("student_class_id"));
        Date date = rs.getDate("attendance_date");
        if (date != null) attendance.setAttendanceDate(date.toLocalDate());
        attendance.setStatus(StudentAttendance.Status.valueOf(rs.getString("status")));
        attendance.setPeriodId(rs.getInt("period_id"));
        attendance.setMarkedBy(rs.getLong("marked_by"));
        Timestamp ts = rs.getTimestamp("marked_at");
        if (ts != null) attendance.setMarkedAt(ts.toLocalDateTime());
        attendance.setLocked(rs.getBoolean("is_locked"));
        attendance.setRemarks(rs.getString("remarks"));
        return attendance;
    }
}