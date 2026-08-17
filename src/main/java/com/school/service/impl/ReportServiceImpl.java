package com.school.service.impl;

import com.school.dao.impl.ReportDaoImpl;
import com.school.dao.interfaces.ReportDao;
import com.school.exceptions.ResourceNotFoundException;
import com.school.exceptions.ValidationException;
import com.school.model.Subject;
import com.school.service.interfaces.ReportService;

import java.util.*;

public class ReportServiceImpl implements ReportService {

    private static final int MIN_YEAR = 1900;
    private static final int MAX_YEAR = 2100;

    private final ReportDao reportDao;

    public ReportServiceImpl() {
        this.reportDao = new ReportDaoImpl();
    }

    @Override
    public Map<String, Object> getStudentPerformanceReport(long studentId, int academicYearId) {
        validatePositive(studentId, "studentId");
        validatePositive(academicYearId, "academicYearId");

        Map<String, Object> report = reportDao.getStudentPerformanceReport(studentId, academicYearId);
        if (report == null) {
            throw new ResourceNotFoundException("Student not found: " + studentId);
        }

        List<Map<String, Object>> results = asList(report.get("results"));
        Set<Object> subjects = new HashSet<>();

        double totalObtained = 0;
        double totalMax = 0;
        for (Map<String, Object> row : results) {
            double obtained = toDouble(row.get("marks_obtained"));
            double max = toDouble(row.get("max_marks"));
            totalObtained += obtained;
            totalMax += max;
            row.put("percentage", max > 0 ? round(obtained / max * 100) : 0.0);
            subjects.add(row.get("subject_name"));
        }

        report.put("total_exams", results.size());
        report.put("total_marks_obtained", round(totalObtained));
        report.put("total_max_marks", round(totalMax));
        report.put("total_subjects", subjects.size());
        report.put("overall_percentage", totalMax > 0 ? round(totalObtained / totalMax * 100) : 0.0);
        return report;
    }

    @Override
    public Map<String, Object> getTeacherAttendanceReport(long teacherId, int month, int year) {
        validatePositive(teacherId, "teacherId");
        validateMonth(month);
        validateYear(year);

        Map<String, Object> report = reportDao.getTeacherAttendanceReport(teacherId, month, year);

        int total = toInt(report.get("total_days"));
        int present = toInt(report.get("present_days"));
        int absent = toInt(report.get("absent_days"));
        int late = toInt(report.get("late_days"));
        int leave = toInt(report.get("leave_days"));
        if (total > 0) {
            report.put("attendance_rate", round(present * 100.0 / total));
            report.put("absence_rate", round((absent + late + leave) * 100.0 / total));
        }
        return report;
    }

    @Override
    public Map<String, Object> getClassAttendanceReport(int sectionId, int month, int year) {
        validatePositive(sectionId, "sectionId");
        validateMonth(month);
        validateYear(year);

        List<Map<String, Object>> rows = reportDao.getClassAttendanceReport(sectionId, month, year);
        if (rows == null) {
            throw new ResourceNotFoundException("Section not found: " + sectionId);
        }

        int present = 0, absent = 0, late = 0, leave = 0;
        for (Map<String, Object> row : rows) {
            switch (String.valueOf(row.get("status"))) {
                case "PRESENT": present++; break;
                case "ABSENT": absent++; break;
                case "LATE": late++; break;
                case "LEAVE": leave++; break;
                default: break;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("present_records", present);
        summary.put("absent_records", absent);
        summary.put("late_records", late);
        summary.put("leave_records", leave);
        summary.put("total_records", rows.size());
        if (!rows.isEmpty()) {
            summary.put("attendance_rate", round(present * 100.0 / rows.size()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("section_id", sectionId);
        result.put("month", month);
        result.put("year", year);
        result.put("results", rows);
        result.put("summary", summary);
        return result;
    }

    @Override
    public Map<String, Object> getTeacherPerformanceReport(long teacherId, int academicYearId) {
        validatePositive(teacherId, "teacherId");
        validatePositive(academicYearId, "academicYearId");

        Map<String, Object> report = reportDao.getTeacherPerformanceReport(teacherId, academicYearId);
        if (report == null) {
            throw new ResourceNotFoundException("Teacher not found: " + teacherId);
        }

        List<Map<String, Object>> results = asList(report.get("results"));

        long totalAssignments = results.size();
        long totalSubmissions = 0;
        long pendingSubmissions = 0;
        double averageSum = 0; // sum of per-assignment averages (see note on weighting above)
        int gradedAssignments = 0;
        for (Map<String, Object> row : results) {
            totalSubmissions += toLong(row.get("total_submissions"));
            pendingSubmissions += toLong(row.get("pending_submissions"));
            if (row.get("average_marks") != null) {
                averageSum += toDouble(row.get("average_marks"));
                gradedAssignments++;
            }
        }

        report.put("total_assignments", totalAssignments);
        report.put("total_submissions", totalSubmissions);
        report.put("pending_submissions_total", pendingSubmissions);
        report.put("graded_assignments", gradedAssignments);
        if (gradedAssignments > 0) {
            report.put("overall_average_marks", round(averageSum / gradedAssignments));
        }
        return report;
    }

    @Override
    public Map<String, Object> getExaminationReport(long examinationId) {
        validatePositive(examinationId, "examinationId");

        List<Map<String, Object>> rows = reportDao.getExaminationReport(examinationId);

        if (rows == null) {
            throw new ResourceNotFoundException("Exam not found");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("examination_id", examinationId);

        int total = rows.size();
        double sum = 0;
        Double highest = null;
        Double lowest = null;
        int passed = 0;
        Double passingMarks = null;
        for (Map<String, Object> row : rows) {
            double obtained = toDouble(row.get("marks_obtained"));
            double max = toDouble(row.get("max_marks"));
            sum += obtained;
            if (highest == null || obtained > highest)
                highest = obtained;
            if (lowest == null || obtained < lowest)
                lowest = obtained;
            if (row.get("passing_marks") != null) {
                double pass = toDouble(row.get("passing_marks"));
                if (passingMarks == null)
                    passingMarks = pass;
                if (obtained >= pass)
                    passed++;
            }
            if (max > 0)
                row.put("percentage", round(obtained / max * 100));
        }

        result.put("results", rows);
        result.put("total_students", total);
        if (total > 0) {
            result.put("average_marks", round(sum / total));
            result.put("highest_marks", round(highest));
            result.put("lowest_marks", round(lowest));
        }
        if (passingMarks != null) {
            result.put("passing_marks", passingMarks);
            result.put("passed_students", passed);
            if (total > 0)
                result.put("pass_percentage", round(passed * 100.0 / total));
        }
        return result;
    }

    @Override
    public Map<String, Object> getStudentAttendanceSummary(long studentId, int academicYearId) {
        validatePositive(studentId, "studentId");
        validatePositive(academicYearId, "academicYearId");

        Map<String, Object> summary = reportDao.getStudentAttendanceSummary(studentId, academicYearId);
        if (summary == null) {
            throw new ResourceNotFoundException("Student or academic year not found");
        }
        return summary;
    }

    private void validatePositive(long value, String name) {
        if (value <= 0)
            throw new ValidationException(name + " must be a positive number.");
    }

    private void validatePositive(int value, String name) {
        if (value <= 0)
            throw new ValidationException(name + " must be a positive number.");
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12)
            throw new ValidationException("month must be between 1 and 12.");
    }

    private void validateYear(int year) {
        if (year < MIN_YEAR || year > MAX_YEAR)
            throw new ValidationException("year must be between " + MIN_YEAR + " and " + MAX_YEAR + ".");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object value) {
        if (value instanceof List)
            return (List<Map<String, Object>>) value;
        return new ArrayList<>();
    }

    private double toDouble(Object value) {
        if (value == null)
            return 0;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long toLong(Object value) {
        if (value == null)
            return 0;
        if (value instanceof Number)
            return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int toInt(Object value) {
        if (value == null)
            return 0;
        if (value instanceof Number)
            return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
