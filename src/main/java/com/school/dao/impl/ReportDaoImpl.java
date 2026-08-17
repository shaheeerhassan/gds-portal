package com.school.dao.impl;

import com.school.dao.interfaces.ReportDao;
import com.school.exceptions.DaoException;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDaoImpl implements ReportDao {

    private static final String STUDENT_INFO = "SELECT student_id, first_name, last_name, registration_number " +
            "FROM students WHERE student_id = ?";

    private static final String STUDENT_PERFORMANCE = "SELECT e.exam_name, e.max_marks, " +
            "sub.subject_name, m.marks_obtained, m.grade, m.remarks " +
            "FROM marks m " +
            "JOIN examinations e ON m.examination_id = e.examination_id " +
            "JOIN subjects sub ON e.subject_id = sub.subject_id " +
            "WHERE m.student_id = ? AND e.academic_year_id = ? " +
            "ORDER BY e.exam_name, sub.subject_name";

    private static final String TEACHER_INFO =
            "SELECT teacher_id, first_name, last_name FROM teachers WHERE teacher_id = ?";

    private static final String TEACHER_ATTENDANCE =
            "SELECT attendance_date, status " +
                    "FROM teacher_attendance " +
                    "WHERE teacher_id = ? AND attendance_date >= ? AND attendance_date < ? " +
                    "ORDER BY attendance_date";

    private static final String SECTION_INFO =
            "SELECT section_id FROM sections WHERE section_id = ?";

    private static final String CLASS_ATTENDANCE =
            "SELECT sa.attendance_date, s.student_id, s.first_name, s.last_name, sa.status, sa.remarks " +
                    "FROM student_attendance sa " +
                    "JOIN student_classes sc ON sa.student_class_id = sc.student_class_id " +
                    "JOIN students s ON sc.student_id = s.student_id " +
                    "WHERE sc.section_id = ? AND sa.attendance_date >= ? AND sa.attendance_date < ? " +
                    "ORDER BY sa.attendance_date, s.first_name";

    private static final String TEACHER_ID =
            "SELECT teacher_id FROM teachers WHERE teacher_id = ?";

    private static final String TEACHER_PERFORMANCE = "SELECT a.assignment_id, a.title, a.section_id, sub.subject_name, " +
            "COUNT(sbm.submission_id) AS total_submissions, " +
            "AVG(sbm.marks_awarded) AS average_marks, " +
            "SUM(CASE WHEN sbm.status <> 'GRADED' THEN 1 ELSE 0 END) AS pending_submissions " +
            "FROM assignments a " +
            "JOIN subjects sub ON a.subject_id = sub.subject_id " +
            "JOIN sections sec ON a.section_id = sec.section_id " +
            "LEFT JOIN submissions sbm ON a.assignment_id = sbm.assignment_id " +
            "WHERE a.teacher_id = ? AND sec.academic_year_id = ? " +
            "GROUP BY a.assignment_id, a.title, a.section_id, sub.subject_name " +
            "ORDER BY a.title";

    private static final String EXAM_INFO =
            "SELECT examination_id FROM examinations WHERE examination_id = ?";

    private static final String EXAMINATION_REPORT = "SELECT sub.subject_name, e.exam_name, e.max_marks, e.passing_marks, " +
            "s.student_id, s.first_name, s.last_name, m.marks_obtained, m.grade " +
            "FROM examinations e " +
            "JOIN subjects sub ON e.subject_id = sub.subject_id " +
            "JOIN marks m ON e.examination_id = m.examination_id " +
            "JOIN students s ON m.student_id = s.student_id " +
            "WHERE e.examination_id = ? " +
            "ORDER BY m.marks_obtained DESC";

    private static final String STUDENT_INFO_MIN =
            "SELECT student_id FROM students WHERE student_id = ?";

    private static final String ACADEMIC_YEAR_RANGE =
            "SELECT start_date, end_date FROM academic_years WHERE academic_year_id = ?";

    private static final String STUDENT_ATTENDANCE_SUMMARY = "SELECT sa.status, COUNT(*) AS count " +
            "FROM student_attendance sa " +
            "JOIN student_classes sc ON sa.student_class_id = sc.student_class_id " +
            "WHERE sc.student_id = ? AND sa.attendance_date BETWEEN ? AND ? " +
            "GROUP BY sa.status";


    @Override
    public Map<String, Object> getStudentPerformanceReport(long studentId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection()) {

            Map<String, Object> student = fetchStudent(cn, studentId);
            if (student == null) {
                return null; // signals "student not found" to the service layer
            }

            List<Map<String, Object>> results = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(STUDENT_PERFORMANCE)) {
                ps.setLong(1, studentId);
                ps.setInt(2, academicYearId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("exam_name", rs.getString("exam_name"));
                        row.put("subject_name", rs.getString("subject_name"));
                        row.put("marks_obtained", rs.getDouble("marks_obtained"));
                        row.put("max_marks", rs.getDouble("max_marks"));
                        row.put("grade", rs.getString("grade"));
                        row.put("remarks", rs.getString("remarks"));
                        results.add(row);
                    }
                }
            }

            Map<String, Object> report = new LinkedHashMap<>();
            report.put("student", student);
            report.put("results", results);
            return report;

        } catch (SQLException e) {
            throw new DaoException("Error generating student performance report", e);
        }
    }

    private Map<String, Object> fetchStudent(Connection cn, long studentId) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(STUDENT_INFO)) {
            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Map<String, Object> student = new LinkedHashMap<>();
                student.put("student_id", rs.getLong("student_id"));
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                student.put("student_name", ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim());
                student.put("registration_number", rs.getString("registration_number"));
                return student;
            }
        }
    }

    @Override
    public Map<String, Object> getTeacherAttendanceReport(long teacherId, int month, int year) {
        try (Connection cn = getDataSource().getConnection()) {

            Map<String, Object> teacher = fetchTeacher(cn, teacherId);
            if (teacher == null) {
                return null;
            }

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.plusMonths(1);

            List<Map<String, Object>> results = new ArrayList<>();
            int present = 0, absent = 0, late = 0, leave = 0, other = 0;

            try (PreparedStatement ps = cn.prepareStatement(TEACHER_ATTENDANCE)) {
                ps.setLong(1, teacherId);
                ps.setDate(2, java.sql.Date.valueOf(start));
                ps.setDate(3, java.sql.Date.valueOf(end));

                System.out.println("loogoooogogogogogogogo "+teacherId +" "+start+" "+end);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        String status = rs.getString("status");
                        row.put("attendance_date", rs.getDate("attendance_date").toLocalDate());
                        row.put("status", status);
                        results.add(row);

                        switch (status == null ? "" : status) {
                            case "PRESENT": present++; break;
                            case "ABSENT": absent++; break;
                            case "LATE": late++; break;
                            case "LEAVE": leave++; break;
                            default: other++; break;
                        }
                    }
                }
            }

            Map<String, Object> report = new LinkedHashMap<>();
            report.put("teacher", teacher);
            report.put("results", results);
            report.put("present_days", present);
            report.put("absent_days", absent);
            report.put("late_days", late);
            report.put("leave_days", leave);
            if (other > 0) report.put("unrecognized_status_days", other);
            report.put("total_days", results.size());
            return report;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DaoException("Error generating teacher attendance report", e);
        }
    }

    private Map<String, Object> fetchTeacher(Connection cn, long teacherId) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(TEACHER_INFO)) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Map<String, Object> teacher = new LinkedHashMap<>();
                teacher.put("teacher_id", rs.getLong("teacher_id"));
                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                teacher.put("teacher_name", ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim());
                return teacher;
            }
        }
    }

    @Override
    public List<Map<String, Object>> getClassAttendanceReport(int sectionId, int month, int year) {
        try (Connection cn = getDataSource().getConnection()) {

            if (!sectionExists(cn, sectionId)) {
                return null;
            }

            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.plusMonths(1);

            List<Map<String, Object>> results = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(CLASS_ATTENDANCE)) {
                ps.setInt(1, sectionId);
                ps.setDate(2, java.sql.Date.valueOf(start));
                ps.setDate(3, java.sql.Date.valueOf(end));

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("attendance_date", rs.getDate("attendance_date").toLocalDate());
                        row.put("student_id", rs.getLong("student_id"));
                        String first = rs.getString("first_name");
                        String last = rs.getString("last_name");
                        row.put("student_name", ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim());
                        row.put("status", rs.getString("status"));
                        row.put("remarks", rs.getString("remarks"));
                        results.add(row);
                    }
                }
            }
            return results;

        } catch (SQLException e) {
            throw new DaoException("Error generating class attendance report", e);
        }
    }

    private boolean sectionExists(Connection cn, int sectionId) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SECTION_INFO)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public Map<String, Object> getTeacherPerformanceReport(long teacherId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection()) {

            if (!exists(cn, TEACHER_ID, teacherId)) {
                return null;
            }

            List<Map<String, Object>> results = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(TEACHER_PERFORMANCE)) {
                ps.setLong(1, teacherId);
                ps.setInt(2, academicYearId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("assignment_title", rs.getString("title"));
                        row.put("section_id", rs.getInt("section_id"));
                        row.put("subject_name", rs.getString("subject_name"));
                        row.put("total_submissions", rs.getLong("total_submissions"));
                        double averageMarks = rs.getDouble("average_marks");
                        row.put("average_marks", rs.wasNull() ? null : averageMarks);
                        row.put("pending_submissions", rs.getLong("pending_submissions"));
                        results.add(row);
                    }
                }
            }

            Map<String, Object> report = new LinkedHashMap<>();
            report.put("results", results);
            report.put("teacher_id", teacherId);
            report.put("academic_year_id", academicYearId);
            return report;

        } catch (SQLException e) {
            throw new DaoException("Error generating teacher performance report", e);
        }
    }

    // small reusable existence helper
    private boolean exists(Connection cn, String sql, long id) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<Map<String, Object>> getExaminationReport(long examinationId) {
        try (Connection cn = getDataSource().getConnection()) {

            if (!exists(cn, EXAM_INFO, examinationId)) {
                return null; // "examination not found"
            }

            try (PreparedStatement ps = cn.prepareStatement(EXAMINATION_REPORT)) {
                ps.setLong(1, examinationId);

                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String, Object>> results = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("subject_name", rs.getString("subject_name"));
                        row.put("exam_name", rs.getString("exam_name"));
                        row.put("max_marks", rs.getDouble("max_marks"));
                        double passingMarks = rs.getDouble("passing_marks");
                        row.put("passing_marks", rs.wasNull() ? null : passingMarks);
                        row.put("student_id", rs.getLong("student_id"));
                        String first = rs.getString("first_name");
                        String last = rs.getString("last_name");
                        row.put("student_name", ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim());
                        row.put("marks_obtained", rs.getDouble("marks_obtained"));
                        row.put("grade", rs.getString("grade"));
                        results.add(row);
                    }
                    return results;
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Error generating examination report", e);
        }
    }

    @Override
    public Map<String, Object> getStudentAttendanceSummary(long studentId, int academicYearId) {
        try (Connection cn = getDataSource().getConnection()) {

            if (!exists(cn, STUDENT_INFO_MIN, studentId)) {
                return null; // "student not found"
            }

            LocalDate[] range = fetchAcademicYearRange(cn, academicYearId);
            if (range == null) {
                return null; // "academic year not found"
            }

            Map<String, Object> summary = new LinkedHashMap<>();
            try (PreparedStatement ps = cn.prepareStatement(STUDENT_ATTENDANCE_SUMMARY)) {
                ps.setLong(1, studentId);
                ps.setDate(2, java.sql.Date.valueOf(range[0]));
                ps.setDate(3, java.sql.Date.valueOf(range[1]));

                try (ResultSet rs = ps.executeQuery()) {
                    int present = 0, absent = 0, late = 0, leave = 0, other = 0;
                    while (rs.next()) {
                        int count = rs.getInt("count");
                        String status = rs.getString("status");
                        switch (status == null ? "" : status) {
                            case "PRESENT": present = count; break;
                            case "ABSENT": absent = count; break;
                            case "LATE": late = count; break;
                            case "LEAVE": leave = count; break;
                            default: other += count; break;
                        }
                    }
                    summary.put("student_id", studentId);
                    summary.put("academic_year_id", academicYearId);
                    summary.put("present_days", present);
                    summary.put("absent_days", absent);
                    summary.put("late_days", late);
                    summary.put("leave_days", leave);
                    if (other > 0) summary.put("unrecognized_status_days", other);
                    summary.put("total_days", present + absent + late + leave + other);
                }
            }
            return summary;

        } catch (SQLException e) {
            throw new DaoException("Error generating student attendance summary", e);
        }
    }

    private LocalDate[] fetchAcademicYearRange(Connection cn, int academicYearId) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(ACADEMIC_YEAR_RANGE)) {
            ps.setInt(1, academicYearId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new LocalDate[] {
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate()
                };
            }
        }
    }
}