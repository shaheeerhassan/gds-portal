package com.school.dao.impl;

import com.school.dao.interfaces.ReportDao;
import com.school.exceptions.DaoException;
import static com.school.config.DBConfig.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDaoImpl implements ReportDao {

    private static final String STUDENT_PERFORMANCE = "SELECT s.student_id, s.first_name, s.last_name, s.registration_number, e.exam_name, e.max_marks, " +
            "sub.subject_name, m.marks_obtained, m.grade, m.remarks " +
            "FROM marks m " +
            "JOIN examinations e ON m.examination_id = e.examination_id " +
            "JOIN students s ON m.student_id = s.student_id " +
            "JOIN subjects sub ON e.subject_id = sub.subject_id " +
            "WHERE m.student_id = ? AND e.academic_year_id = ? " +
            "ORDER BY e.exam_name, sub.subject_name";

    private static final String TEACHER_ATTENDANCE = "SELECT ta.attendance_date, ta.status, ta.check_in_time, ta.check_out_time, " +
            "t.first_name, t.last_name " +
            "FROM teacher_attendances ta JOIN teachers t ON ta.teacher_id = t.teacher_id " +
            "WHERE ta.teacher_id = ? AND YEAR(ta.attendance_date) = ? AND MONTH(ta.attendance_date) = ? " +
            "ORDER BY ta.attendance_date";

    private static final String CLASS_ATTENDANCE = "SELECT sa.attendance_date, s.student_id, s.first_name, s.last_name, sa.status, sa.remarks " +
            "FROM student_attendances sa " +
            "JOIN student_classes sc ON sa.student_class_id = sc.student_class_id " +
            "JOIN students s ON sc.student_id = s.student_id " +
            "WHERE sc.section_id = ? AND YEAR(sa.attendance_date) = ? AND MONTH(sa.attendance_date) = ? " +
            "ORDER BY sa.attendance_date, s.first_name";

    private static final String TEACHER_PERFORMANCE = "SELECT a.title, a.section_id, sub.subject_name, " +
            "COUNT(sbm.submission_id) AS total_submissions, " +
            "AVG(sbm.marks_awarded) AS average_marks, " +
            "SUM(CASE WHEN sbm.status <> 'GRADED' THEN 1 ELSE 0 END) AS pending_submissions " +
            "FROM assignments a " +
            "JOIN subjects sub ON a.subject_id = sub.subject_id " +
            "JOIN sections sec ON a.section_id = sec.section_id " +
            "LEFT JOIN submissions sbm ON a.assignment_id = sbm.assignment_id " +
            "WHERE a.teacher_id = ? AND sec.academic_year_id = ? " +
            "GROUP BY a.assignment_id " +
            "ORDER BY a.title";

    private static final String EXAMINATION_REPORT = "SELECT sub.subject_name, e.exam_name, e.max_marks, e.passing_marks, " +
            "s.student_id, s.first_name, s.last_name, m.marks_obtained, m.grade " +
            "FROM examinations e " +
            "JOIN subjects sub ON e.subject_id = sub.subject_id " +
            "JOIN marks m ON e.examination_id = m.examination_id " +
            "JOIN students s ON m.student_id = s.student_id " +
            "WHERE e.examination_id = ? " +
            "ORDER BY m.marks_obtained DESC";

    private static final String STUDENT_ATTENDANCE_SUMMARY = "SELECT sa.status, COUNT(*) AS count " +
            "FROM student_attendances sa " +
            "JOIN student_classes sc ON sa.student_class_id = sc.student_class_id " +
            "WHERE sc.student_id = ? AND sa.attendance_date BETWEEN " +
            "(SELECT start_date FROM academic_years WHERE academic_year_id = ?) " +
            "AND (SELECT end_date FROM academic_years WHERE academic_year_id = ?) " +
            "GROUP BY sa.status";

    @Override
    public Map<String, Object> getStudentPerformanceReport(long studentId, int academicYearId) {
        Map<String, Object> report = new LinkedHashMap<>();
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(STUDENT_PERFORMANCE)) {

            ps.setLong(1, studentId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                boolean hasRows = false;
                List<Map<String, Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    hasRows = true;
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("student_id", resultSet.getLong("student_id"));
                    row.put("student_name", resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                    row.put("registration_number", resultSet.getString("registration_number"));
                    row.put("exam_name", resultSet.getString("exam_name"));
                    row.put("subject_name", resultSet.getString("subject_name"));
                    row.put("marks_obtained", resultSet.getDouble("marks_obtained"));
                    row.put("max_marks", resultSet.getDouble("max_marks"));
                    row.put("grade", resultSet.getString("grade"));
                    row.put("remarks", resultSet.getString("remarks"));
                    results.add(row);
                }
                if (hasRows) {
                    report.put("student", results.get(0));
                }
                report.put("results", results);
            }
            return report;
        } catch (SQLException e) {
            throw new DaoException("Error generating student performance report", e);
        }
    }

    @Override
    public Map<String, Object> getTeacherAttendanceReport(long teacherId, int month, int year) {
        Map<String, Object> report = new LinkedHashMap<>();
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(TEACHER_ATTENDANCE)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, year);
            ps.setInt(3, month);

            try (ResultSet resultSet = ps.executeQuery()) {
                int present = 0, absent = 0, late = 0, leave = 0;
                List<Map<String, Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("teacher_id", teacherId);
                    row.put("teacher_name", resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                    row.put("attendance_date", resultSet.getDate("attendance_date").toLocalDate());
                    row.put("status", resultSet.getString("status"));
                    row.put("check_in_time", resultSet.getTime("check_in_time"));
                    row.put("check_out_time", resultSet.getTime("check_out_time"));
                    results.add(row);
                    switch (resultSet.getString("status")) {
                        case "PRESENT": present++; break;
                        case "ABSENT": absent++; break;
                        case "LATE": late++; break;
                        case "LEAVE": leave++; break;
                    }
                }
                report.put("results", results);
                report.put("present_days", present);
                report.put("absent_days", absent);
                report.put("late_days", late);
                report.put("leave_days", leave);
                report.put("total_days", present + absent + late + leave);
            }
            return report;
        } catch (SQLException e) {
            throw new DaoException("Error generating teacher attendance report", e);
        }
    }

    @Override
    public List<Map<String, Object>> getClassAttendanceReport(int sectionId, int month, int year) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(CLASS_ATTENDANCE)) {

            ps.setInt(1, sectionId);
            ps.setInt(2, year);
            ps.setInt(3, month);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("attendance_date", resultSet.getDate("attendance_date").toLocalDate());
                    row.put("student_id", resultSet.getLong("student_id"));
                    row.put("student_name", resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                    row.put("status", resultSet.getString("status"));
                    row.put("remarks", resultSet.getString("remarks"));
                    results.add(row);
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DaoException("Error generating class attendance report", e);
        }
    }

    @Override
    public Map<String, Object> getTeacherPerformanceReport(long teacherId, int academicYearId) {
        Map<String, Object> report = new LinkedHashMap<>();
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(TEACHER_PERFORMANCE)) {

            ps.setLong(1, teacherId);
            ps.setInt(2, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("assignment_title", resultSet.getString("title"));
                    row.put("section_id", resultSet.getInt("section_id"));
                    row.put("subject_name", resultSet.getString("subject_name"));
                    row.put("total_submissions", resultSet.getLong("total_submissions"));
                    double averageMarks = resultSet.getDouble("average_marks");
                    row.put("average_marks", resultSet.wasNull() ? null : averageMarks);
                    row.put("pending_submissions", resultSet.getLong("pending_submissions"));
                    results.add(row);
                }
                report.put("results", results);
                report.put("teacher_id", teacherId);
                report.put("academic_year_id", academicYearId);
            }
            return report;
        } catch (SQLException e) {
            throw new DaoException("Error generating teacher performance report", e);
        }
    }

    @Override
    public List<Map<String, Object>> getExaminationReport(long examinationId) {
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(EXAMINATION_REPORT)) {

            ps.setLong(1, examinationId);

            try (ResultSet resultSet = ps.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("subject_name", resultSet.getString("subject_name"));
                    row.put("exam_name", resultSet.getString("exam_name"));
                    row.put("max_marks", resultSet.getDouble("max_marks"));
                    double passingMarks = resultSet.getDouble("passing_marks");
                    row.put("passing_marks", resultSet.wasNull() ? null : passingMarks);
                    row.put("student_id", resultSet.getLong("student_id"));
                    row.put("student_name", resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
                    row.put("marks_obtained", resultSet.getDouble("marks_obtained"));
                    row.put("grade", resultSet.getString("grade"));
                    results.add(row);
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DaoException("Error generating examination report", e);
        }
    }

    @Override
    public Map<String, Object> getStudentAttendanceSummary(long studentId, int academicYearId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        try (Connection cn = getDataSource().getConnection();
             PreparedStatement ps = cn.prepareStatement(STUDENT_ATTENDANCE_SUMMARY)) {

            ps.setLong(1, studentId);
            ps.setInt(2, academicYearId);
            ps.setInt(3, academicYearId);

            try (ResultSet resultSet = ps.executeQuery()) {
                int present = 0, absent = 0, late = 0, leave = 0;
                while (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    switch (resultSet.getString("status")) {
                        case "PRESENT": present = count; break;
                        case "ABSENT": absent = count; break;
                        case "LATE": late = count; break;
                        case "LEAVE": leave = count; break;
                    }
                }
                summary.put("student_id", studentId);
                summary.put("academic_year_id", academicYearId);
                summary.put("present_days", present);
                summary.put("absent_days", absent);
                summary.put("late_days", late);
                summary.put("leave_days", leave);
                summary.put("total_days", present + absent + late + leave);
            }
            return summary;
        } catch (SQLException e) {
            throw new DaoException("Error generating student attendance summary", e);
        }
    }
}